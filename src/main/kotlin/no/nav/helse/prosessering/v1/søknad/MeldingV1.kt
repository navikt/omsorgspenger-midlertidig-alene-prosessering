package no.nav.helse.prosessering.v1.søknad

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate
import java.time.ZonedDateTime

data class MeldingV1(
    val søknadId: String,
    val mottatt: ZonedDateTime,
    val språk: String? = "nb",
    val søker: Søker,
    val id: String,
    val arbeidssituasjon: List<Arbeidssituasjon>? = null, //TODO 26.02.2021 - Fjernes når frontend og api er prodsatt
    val annenForelder: AnnenForelder,
    val antallBarn: Int? = null, //TODO 26.02.2021 - Fjernes når frontend og api er prodsatt
    val fødselsårBarn: List<Int>? = null, //TODO 26.02.2021 - Fjernes når frontend og api er prodsatt
    val medlemskap: Medlemskap? = null, //TODO 26.02.2021 - Fjernes når frontend og api er prodsatt
    val barn: List<Barn>? = null, //TODO 26.02.2021 - Fjerne null og optional når frontend og api er prodsatt
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

enum class Arbeidssituasjon(val utskriftvennlig: String){ //TODO 26.02.2021 - Fjernes når frontend og api er prodsatt
    SELVSTENDIG_NÆRINGSDRIVENDE("Selvstendig næringsdrivende"),
    ARBEIDSTAKER("Arbeidstaker"),
    FRILANSER("Frilanser"),
    ANNEN("Annen")
}

internal fun List<Arbeidssituasjon>.somMapTilPdfArbeidssituasjon(): List<Map<String, Any?>> { //TODO 26.02.2021 - Fjernes når frontend og api er prodsatt
    return map {
        mapOf<String, Any?>(
            "utskriftvennlig" to it.utskriftvennlig
        )
    }
}

internal fun List<Int>.somMapTilPdfFødselsår(): List<Map<String, Any?>> { //TODO 26.02.2021 - Fjernes når frontend og api er prodsatt
    return map {
        mapOf<String, Any?>(
            "alder" to it
        )
    }
}

internal fun List<Barn>.somMapTilPdf(): List<Map<String, Any?>> {
    return map {
        mapOf<String, Any?>(
            "navn" to it.navn.capitalizeName(),
            "identitetsnummer" to it.identitetsnummer
        )
    }
}

fun String.capitalizeName(): String = split(" ").joinToString(" ") { it.toLowerCase().capitalize() }
