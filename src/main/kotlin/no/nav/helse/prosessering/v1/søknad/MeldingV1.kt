package no.nav.helse.prosessering.v1.søknad

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

data class MeldingV1(
    val søknadId: String,
    val mottatt: ZonedDateTime,
    val språk: String? = "nb",
    val søker: Søker,
    val id: String,
    val arbeidssituasjon: List<Arbeidssituasjon>,
    val annenForelder: AnnenForelder,
    val antallBarn: Int,
    val alderAvAlleBarn: List<Int>,
    val medlemskap: Medlemskap,
    val utenlandsoppholdIPerioden: UtenlandsoppholdIPerioden?,
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

enum class Arbeidssituasjon(){
    SELVSTENDIG_NÆRINGSDRIVENDE,
    ARBEIDSTAKER,
    FRILANSER,
    ANNEN
}

data class AnnenForelder(
    val navn: String,
    val fnr: String,
    val situasjon: Situasjon,
    val situasjonBeskrivelse: String,
    val periodeOver6Måneder: Boolean,
    @JsonFormat(pattern = "yyyy-MM-dd") val periodeFraOgMed: LocalDate,
    @JsonFormat(pattern = "yyyy-MM-dd") val periodeTilOgMed: LocalDate
) {
    fun somMapTilPdf(): Map<String, Any?> {
        return mapOf(
            "navn" to navn,
            "fnr" to fnr,
            "situasjon" to situasjon.utskriftvennlig,
            "beskrivelse" to situasjonBeskrivelse,
            "periodeOver6Måneder" to periodeOver6Måneder,
            "periodeFraOgMed" to periodeFraOgMed,
            "periodeTilOgMed" to periodeTilOgMed
        )
    }
}

enum class Situasjon(val utskriftvennlig: String){
    INNLAGT_I_HELSEINSTITUSJON("Innlagt i helseinstitusjon"),
    UTØVER_VERNEPLIKT("Utøver verneplikt"),
    FENGSEL("Fengsel"),
    SYKDOM("Sykdom"),
    ANNET("Annet")
}

data class Medlemskap(
    val harBoddIUtlandetSiste12Mnd: Boolean,
    val utenlandsoppholdSiste12Mnd: List<Utenlandsopphold> = listOf(),
    val skalBoIUtlandetNeste12Mnd: Boolean,
    val utenlandsoppholdNeste12Mnd: List<Utenlandsopphold> = listOf()
) {
    fun somMapTilPdf(): Map<String, Any?>{
        return mapOf(
            "harBoddIUtlandetSiste12Mnd" to harBoddIUtlandetSiste12Mnd,
            "utenlandsoppholdSiste12Mnd" to utenlandsoppholdSiste12Mnd.somMapTilPdf(),
            "skalBoIUtlandetNeste12Mnd" to skalBoIUtlandetNeste12Mnd,
            "utenlandsoppholdNeste12Mnd" to utenlandsoppholdNeste12Mnd.somMapTilPdf()
            )
    }
}

data class Utenlandsopphold(
    @JsonFormat(pattern = "yyyy-MM-dd") val fraOgMed: LocalDate,
    @JsonFormat(pattern = "yyyy-MM-dd") val tilOgMed: LocalDate,
    val landkode: String,
    val landnavn: String
){
    fun somMapTilPdf(): Map<String, Any?>{
        val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy").withZone(ZoneId.of("Europe/Oslo"))
        return mapOf(
            "landnavn" to landnavn
        )
    }
}


data class UtenlandsoppholdIPerioden(
    val skalOppholdeSegIUtlandetIPerioden: Boolean,
    val opphold: List<Utenlandsopphold> = listOf()
){
    fun somMapTilPdf(): Map<String, Any?>{
        return mapOf(
            "opphold" to opphold.somMapTilPdf()
        )
    }
}

private fun List<Utenlandsopphold>.somMapTilPdf(): List<Map<String, Any?>> {
    return map {
        it.somMapTilPdf()
    }
}