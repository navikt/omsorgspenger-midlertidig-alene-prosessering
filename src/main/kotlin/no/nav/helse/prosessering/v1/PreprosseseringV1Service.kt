package no.nav.helse.prosessering.v1

import no.nav.helse.dokument.DokumentGateway
import no.nav.helse.dokument.DokumentService
import no.nav.helse.felles.AktørId
import no.nav.helse.felles.CorrelationId
import no.nav.helse.felles.Metadata
import no.nav.helse.felles.SøknadId
import no.nav.helse.prosessering.v1.søknad.MeldingV1
import no.nav.helse.prosessering.v1.søknad.PreprossesertMeldingV1
import org.slf4j.LoggerFactory

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
        val dokumentEier = DokumentGateway.DokumentEier(melding.søker.fødselsnummer)

        logger.trace("Genererer Oppsummerings-PDF av søknaden.")
        val søknadOppsummeringPdf = pdfV1Generator.generateSoknadOppsummeringPdf(melding)
        logger.trace("Generering av Oppsummerings-PDF OK.")

        logger.trace("Mellomlagrer Oppsummerings-PDF.")

        val soknadOppsummeringPdfUrl = dokumentService.lagreSoknadsOppsummeringPdf(
            pdf = søknadOppsummeringPdf,
            correlationId = correlationId,
            dokumentEier = dokumentEier,
            dokumentbeskrivelse = "Omsorgsdager - Søknad om å bli regnet som alene"
        )

        logger.trace("Mellomlagring av Oppsummerings-PDF OK")

        logger.trace("Mellomlagrer Oppsummerings-JSON")

        val søknadJsonUrl = dokumentService.lagreSoknadsMelding(
            k9Format = melding.k9Format,
            dokumentEier = dokumentEier,
            correlationId = correlationId
        )

        logger.trace("Mellomlagrer Oppsummerings-JSON OK.")
        val komplettDokumentUrls = mutableListOf(
            listOf(
                soknadOppsummeringPdfUrl,
                søknadJsonUrl
            )
        )

        logger.trace("Totalt ${komplettDokumentUrls.size} dokumentbolker.")

        val preprossesertMeldingV1 = PreprossesertMeldingV1(
            melding = melding,
            dokumentUrls = komplettDokumentUrls.toList(),
            søkerAktørId = AktørId(melding.søker.aktørId)
        )
        preprossesertMeldingV1.reportMetrics()
        return preprossesertMeldingV1
    }

}
