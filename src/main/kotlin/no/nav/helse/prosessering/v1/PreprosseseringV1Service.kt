package no.nav.helse.prosessering.v1

import no.nav.helse.dokument.K9MellomlagringGateway.Dokument
import no.nav.helse.dokument.K9MellomlagringGateway.DokumentEier
import no.nav.helse.dokument.K9MellomlagringService
import no.nav.helse.dokument.Søknadsformat
import no.nav.helse.felles.AktørId
import no.nav.helse.felles.CorrelationId
import no.nav.helse.felles.Metadata
import no.nav.helse.felles.SøknadId
import no.nav.helse.prosessering.v1.søknad.MeldingV1
import no.nav.helse.prosessering.v1.søknad.PreprossesertMeldingV1
import org.slf4j.LoggerFactory
import java.net.URI

internal class PreprosseseringV1Service(
    private val pdfV1Generator: PdfV1Generator,
    private val k9MellomlagringService: K9MellomlagringService
) {

    private companion object {
        private val logger = LoggerFactory.getLogger(PreprosseseringV1Service::class.java)
    }

    internal suspend fun preprosesser(
        melding: MeldingV1,
        metadata: Metadata
    ): PreprossesertMeldingV1 {
        val søknadId = SøknadId(melding.søknadId)
        logger.info("Preprosesserer $søknadId")

        val correlationId = CorrelationId(metadata.correlationId)
        val dokumentEier = DokumentEier(melding.søker.fødselsnummer)

        logger.info("Genererer Oppsummerings-PDF av søknaden.")
        val oppsummeringPdf = pdfV1Generator.generateSoknadOppsummeringPdf(melding)

        logger.info("Mellomlagrer Oppsummerings-PDF.")
        val oppsummeringPdfDokumentId = k9MellomlagringService.lagreDokument(
            dokument = Dokument(
                eier = dokumentEier,
                content = oppsummeringPdf,
                contentType = "application/pdf",
                title = "Søknad ekstra omsorgsdager – ikke tilsyn"
            ),
            correlationId = correlationId
        ).dokumentId()

        logger.info("Mellomlagrer Oppsummerings-JSON")
        val søknadJsonDokumentId = k9MellomlagringService.lagreDokument(
            dokument = Dokument(
                eier = dokumentEier,
                content = Søknadsformat.somJson(melding.k9Format),
                contentType = "application/json",
                title = "Søknad ekstra omsorgsdager – ikke tilsyn som JSON"
            ),
            correlationId = correlationId
        ).dokumentId()

        val komplettDokumentId = mutableListOf(
            listOf(
                oppsummeringPdfDokumentId,
                søknadJsonDokumentId
            )
        )

        logger.info("Totalt ${komplettDokumentId.size} dokumentbolker med totalt ${komplettDokumentId.flatten().size} dokumenter.")

        val preprossesertMeldingV1 = PreprossesertMeldingV1(
            melding = melding,
            dokumentId = komplettDokumentId.toList(),
            søkerAktørId = AktørId(melding.søker.aktørId)
        )
        preprossesertMeldingV1.reportMetrics()
        return preprossesertMeldingV1
    }
}

fun URI.dokumentId(): String = this.toString().substringAfterLast("/")