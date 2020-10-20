package no.nav.helse.prosessering.v1

import com.fasterxml.jackson.annotation.JsonAlias
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
    @JsonAlias("selvstendigNæringsdrivende") SELVSTENDIG_NÆRINGSDRIVENDE,
    @JsonAlias("arbeidstaker") ARBEIDSTAKER,
    @JsonAlias("frilanser") FRILANSER,
    @JsonAlias("annen") ANNEN
}

data class AnnenForelder(
    val navn: String,
    val fnr: String,
    val situasjon: Situasjon,
    val situasjonBeskrivelse: String,
    val periodeOver6Måneder: Boolean? = null, //Settes til null for å unngå default false
    @JsonFormat(pattern = "yyyy-MM-dd") val periodeFraOgMed: LocalDate,
    @JsonFormat(pattern = "yyyy-MM-dd") val periodeTilOgMed: LocalDate
)

enum class Situasjon(){
    @JsonAlias("innlagtIHelseinstitusjon") INNLAGT_I_HELSEINSTITUSJON,
    @JsonAlias("utøverVerneplikt") UTØVER_VERNEPLIKT,
    @JsonAlias("fengsel") FENGSEL,
    @JsonAlias("sykdom") SYKDOM,
    @JsonAlias("annet") ANNET
}

data class Medlemskap(
    val harBoddIUtlandetSiste12Mnd: Boolean? = null, //Settes til null for å unngå default false
    val utenlandsoppholdSiste12Mnd: List<Utenlandsopphold> = listOf(),
    val skalBoIUtlandetNeste12Mnd: Boolean? = null, //Settes til null for å unngå default false
    val utenlandsoppholdNeste12Mnd: List<Utenlandsopphold> = listOf()
)

data class Utenlandsopphold(
    @JsonFormat(pattern = "yyyy-MM-dd") val fraOgMed: LocalDate,
    @JsonFormat(pattern = "yyyy-MM-dd") val tilOgMed: LocalDate,
    val landkode: String,
    val landnavn: String
)

data class UtenlandsoppholdIPerioden(
    val skalOppholdeSegIUtlandetIPerioden: Boolean? = null, //Settes til null for å unngå default false
    val opphold: List<Utenlandsopphold> = listOf() //TODO Sender frontend null eller tom liste?
)