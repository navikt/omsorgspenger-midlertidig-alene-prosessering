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
    val arbeidssituasjon: List<Arbeidssituasjon>,
    val annenForelder: AnnenForelder,
    val antallBarn: Int,
    val fødselsårBarn: List<Int>,
    val medlemskap: Medlemskap,
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

enum class Arbeidssituasjon(val utskriftvennlig: String){
    SELVSTENDIG_NÆRINGSDRIVENDE("Selvstendig næringsdrivende"),
    ARBEIDSTAKER("Arbeidstaker"),
    FRILANSER("Frilanser"),
    ANNEN("Annen")
}

internal fun List<Arbeidssituasjon>.somMapTilPdfArbeidssituasjon(): List<Map<String, Any?>> {
    return map {
        mapOf<String, Any?>(
            "utskriftvennlig" to it.utskriftvennlig
        )
    }
}

internal fun List<Int>.somMapTilPdfAlder(): List<Map<String, Any?>> {
    return map {
        mapOf<String, Any?>(
            "alder" to it
        )
    }
}