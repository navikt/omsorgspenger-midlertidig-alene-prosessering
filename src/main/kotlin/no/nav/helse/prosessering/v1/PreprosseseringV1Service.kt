package no.nav.helse.prosessering.v1

import no.nav.helse.CorrelationId
import no.nav.helse.dokument.DokumentService
import no.nav.helse.prosessering.AktørId
import no.nav.helse.prosessering.Metadata
import no.nav.helse.prosessering.SøknadId
import org.slf4j.LoggerFactory
import java.net.URI
import kotlin.math.log

internal class PreprosseseringV1Service(
    private val pdfV1Generator: PdfV1Generator,
    private val dokumentService: DokumentService
) {

    private companion object {
        private val logger = LoggerFactory.getLogger(PreprosseseringV1Service::class.java)
    }

    internal suspend fun preprosseser(
        melding: MeldingV1,
        metadata: Metadata
    ): PreprossesertMeldingV1 {
        val søknadId = SøknadId(melding.søknadId)
        logger.trace("Preprosseserer $søknadId")

        val correlationId = CorrelationId(metadata.correlationId)
        val søkerAktørId = AktørId(melding.søker.aktørId)

        logger.trace("Søkerens AktørID = $søkerAktørId")

        logger.trace("Genererer Oppsummerings-PDF av søknaden.")
        val soknadOppsummeringPdf = pdfV1Generator.generateSoknadOppsummeringPdf(melding)
        logger.trace("Generering av Oppsummerings-PDF OK.")


        logger.trace("Mellomlagrer Oppsummerings-PDF.")
        val soknadOppsummeringPdfUrl = dokumentService.lagreSoknadsOppsummeringPdf(
            pdf = soknadOppsummeringPdf,
            correlationId = correlationId,
            aktørId = søkerAktørId,
            dokumentbeskrivelse = "Søknad om omsorgspenger"
        )

        logger.trace("Mellomlagring av Oppsummerings-PDF OK")

        logger.trace("Mellomlagrer Oppsummerings-JSON")

        val soknadJsonUrl = dokumentService.lagreSoknadsMelding(
            melding = melding,
            aktørId = søkerAktørId,
            correlationId = correlationId
        )
        logger.trace("Mellomlagrer Oppsummerings-JSON OK.")

        val komplettDokumentUrls = mutableListOf(
            listOf(
                soknadOppsummeringPdfUrl,
                soknadJsonUrl
            )
        )

        logger.trace("Totalt ${komplettDokumentUrls.size} dokumentbolker.")

        val preprossesertMeldingV1 = PreprossesertMeldingV1(
            melding = melding,
            dokumentUrls = komplettDokumentUrls.toList(),
            søkerAktørId = søkerAktørId
        )
        //melding.reportMetrics() TODO Metrikker
        //preprossesertMeldingV1.reportMetrics() TODO Metrikker
        return preprossesertMeldingV1
    }

}
