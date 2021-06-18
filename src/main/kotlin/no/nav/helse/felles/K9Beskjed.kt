package no.nav.helse.felles

import no.nav.helse.prosessering.v1.asynkron.Cleanup
import java.util.*

data class K9Beskjed(
    val metadata: Metadata,
    val grupperingsId: String,
    val tekst: String,
    val link: String?,
    val dagerSynlig: Long,
    val søkerFødselsnummer: String,
    val eventId: String,
    val ytelse: String
)
const val DAGER_SYNLIG : Long= 7
const val TEKST = "Vi har mottatt omsorgspengesøknad fra deg om å bli regnet som alene om omsorgen for barn."
const val YTELSE = "OMSORGSPENGER_MIDLERTIDIG_ALENE"

fun Cleanup.tilK9Beskjed() = K9Beskjed(
    metadata = this.metadata,
    grupperingsId = this.melding.søknadId,
    tekst = TEKST,
    søkerFødselsnummer = this.melding.søker.fødselsnummer,
    dagerSynlig = DAGER_SYNLIG,
    link = null,
    eventId = UUID.randomUUID().toString(),
    ytelse = YTELSE
)