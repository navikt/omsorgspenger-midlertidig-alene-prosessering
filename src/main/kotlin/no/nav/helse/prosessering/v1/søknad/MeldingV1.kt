package no.nav.helse.prosessering.v1.søknad

import com.fasterxml.jackson.annotation.JsonFormat
import no.nav.k9.søknad.Søknad
import java.time.LocalDate
import java.time.ZonedDateTime

data class MeldingV1(
    val søknadId: String,
    val mottatt: ZonedDateTime,
    val språk: String? = "nb",
    val søker: Søker,
    val id: String,
    val annenForelder: AnnenForelder,
    val barn: List<Barn>,
    val k9Format: Søknad? = null, //TODO 05.03.2021 - Settes til påbudt når det er prodsatt i API
    val harForståttRettigheterOgPlikter: Boolean,
    val harBekreftetOpplysninger: Boolean
)

data class Søker(
    val fødselsnummer: String,
    val fornavn: String,
    val mellomnavn: String?,
    val etternavn: String,
    @JsonFormat(pattern = "yyyy-MM-dd") val fødselsdato: LocalDate?,
    val aktørId: String
) {
    override fun toString(): String {
        return "Soker(fornavn='$fornavn', mellomnavn=$mellomnavn, etternavn='$etternavn', fødselsdato=$fødselsdato, aktørId='$aktørId')"
    }
}

data class Barn (
    val navn: String,
    val aktørId: String?,
    var identitetsnummer: String?,
)

internal fun List<Barn>.somMapTilPdf(): List<Map<String, Any?>> {
    return map {
        mapOf<String, Any?>(
            "navn" to it.navn.capitalizeName(),
            "identitetsnummer" to it.identitetsnummer
        )
    }
}

fun String.capitalizeName(): String = split(" ").joinToString(" ") { it.toLowerCase().capitalize() }