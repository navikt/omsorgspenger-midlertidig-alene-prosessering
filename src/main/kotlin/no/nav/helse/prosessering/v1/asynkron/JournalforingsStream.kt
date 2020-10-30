package no.nav.helse.prosessering.v1.asynkron

import no.nav.helse.CorrelationId
import no.nav.helse.joark.JoarkGateway
import no.nav.helse.joark.Navn
import no.nav.helse.kafka.KafkaConfig
import no.nav.helse.kafka.ManagedKafkaStreams
import no.nav.helse.kafka.ManagedStreamHealthy
import no.nav.helse.kafka.ManagedStreamReady
import no.nav.helse.prosessering.v1.statusLoggFormat
import no.nav.helse.prosessering.v1.søknad.PreprossesertMeldingV1
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.Topology
import org.apache.kafka.streams.kstream.Consumed
import org.apache.kafka.streams.kstream.Produced
import org.slf4j.LoggerFactory

internal class JournalforingsStream(
    joarkGateway: JoarkGateway,
    kafkaConfig: KafkaConfig
) {

    private val stream = ManagedKafkaStreams(
        name = NAME,
        properties = kafkaConfig.stream(NAME),
        topology = topology(joarkGateway),
        unreadyAfterStreamStoppedIn = kafkaConfig.unreadyAfterStreamStoppedIn
    )

    internal val ready = ManagedStreamReady(stream)
    internal val healthy = ManagedStreamHealthy(stream)

    private companion object {
        private const val NAME = "JournalforingV1"
        private val logger = LoggerFactory.getLogger("no.nav.$NAME.topology")

        private fun topology(joarkGateway: JoarkGateway): Topology {
            val builder = StreamsBuilder()
            val fraPreprossesert: Topic<TopicEntry<PreprossesertMeldingV1>> = Topics.PREPROSSESERT
            val tilCleanup: Topic<TopicEntry<Cleanup>> = Topics.CLEANUP

            val mapValues = builder
                .stream<String, TopicEntry<PreprossesertMeldingV1>>(
                    fraPreprossesert.name,
                    Consumed.with(fraPreprossesert.keySerde, fraPreprossesert.valueSerde)
                )
                .filter { _, entry -> 1 == entry.metadata.version }
                .mapValues { soknadId, entry ->
                    process(NAME, soknadId, entry) {
                        logger.info(statusLoggFormat("Journalfører søknad med ID = {}", soknadId))

                        val dokumenter = entry.data.dokumentUrls
                        logger.trace("Journalfører dokumenter: {}", dokumenter)

                        val journalPostId = joarkGateway.journalfør(
                            mottatt = entry.data.mottatt,
                            norskIdent = entry.data.søker.fødselsnummer,
                            correlationId = CorrelationId(entry.metadata.correlationId),
                            dokumenter = dokumenter,
                            navn = Navn(
                                fornavn = entry.data.søker.fornavn,
                                mellomnavn = entry.data.søker.mellomnavn,
                                etternavn = entry.data.søker.etternavn
                            )
                        )

                        logger.trace("Dokumenter journalført med ID = ${journalPostId.journalpostId}.")
                        val journalfort = Journalfort(
                            journalpostId = journalPostId.journalpostId,
                            søknad = entry.data
                        )
                        Cleanup(
                            metadata = entry.metadata,
                            melding = entry.data,
                            journalførtMelding = journalfort
                        )
                    }
                }
            mapValues
                .to(tilCleanup.name, Produced.with(tilCleanup.keySerde, tilCleanup.valueSerde))
            return builder.build()
        }
    }

    internal fun stop() = stream.stop(becauseOfError = false)
}