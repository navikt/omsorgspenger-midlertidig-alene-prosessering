package no.nav.helse.dokument

import no.nav.helse.felles.CorrelationId
import no.nav.helse.prosessering.v1.søknad.MeldingV1
import no.nav.k9.søknad.JsonUtils
import no.nav.k9.søknad.Søknad
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URI

private val logger: Logger = LoggerFactory.getLogger("nav.DokumentService")

class DokumentService(
    private val dokumentGateway: DokumentGateway
) {
    private suspend fun lagreDokument(
        dokument: DokumentGateway.Dokument,
        correlationId: CorrelationId
    ) : URI {
        return dokumentGateway.lagreDokmenter(
            dokumenter = setOf(dokument),
            correlationId = correlationId
        ).first()
    }

    internal suspend fun lagreSoknadsOppsummeringPdf(
        pdf : ByteArray,
        dokumentEier: DokumentGateway.DokumentEier,
        correlationId: CorrelationId,
        dokumentbeskrivelse: String
    ) : URI {
        return lagreDokument(
            dokument = DokumentGateway.Dokument(
                eier = dokumentEier,
                content = pdf,
                contentType = "application/pdf",
                title = dokumentbeskrivelse
            ),
            correlationId = correlationId
        )
    }

    internal suspend fun lagreSoknadsMelding(
        k9Format: Søknad,
        dokumentEier: DokumentGateway.DokumentEier,
        correlationId: CorrelationId
    ) : URI {
        logger.info("SKAL IKKE VISES I PRID: Format som journalføres mot joark: {}", JsonUtils.toString(k9Format)) //TODO 05.03.2021 - FJERNES FØR PRODSETTING
        return lagreDokument(
            dokument = DokumentGateway.Dokument(
                eier = dokumentEier,
                content = Søknadsformat.somJson(k9Format),
                contentType = "application/json",
                title = "Omsorgsdager - Søknad om å bli regnet som alene som JSON"
            ),
            correlationId = correlationId
        )
    }

    internal suspend fun lagreSoknadsMelding(
        melding: MeldingV1,
        dokumentEier: DokumentGateway.DokumentEier,
        correlationId: CorrelationId
    ) : URI {
        return lagreDokument(
            dokument = DokumentGateway.Dokument(
                eier = dokumentEier,
                content = Søknadsformat.somJson(melding),
                contentType = "application/json",
                title = "Omsorgsdager - Søknad om å bli regnet som alene som JSON"
            ),
            correlationId = correlationId
        )
    }

    internal suspend fun slettDokumeter(
        urlBolks: List<List<URI>>,
        dokumentEier: DokumentGateway.DokumentEier,
        correlationId : CorrelationId
    ) {
        val urls = mutableListOf<URI>()
        urlBolks.forEach { urls.addAll(it) }
        logger.trace("Sletter ${urls.size} dokumenter")
        dokumentGateway.slettDokmenter(
            urls = urls,
            dokumentEier = dokumentEier,
            correlationId = correlationId
        )

    }

}

