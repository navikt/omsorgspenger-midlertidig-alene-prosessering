package no.nav.helse.prosessering.v1

import com.fasterxml.jackson.annotation.JsonFormat
import no.nav.helse.prosessering.AktørId
import java.net.URI
import java.time.LocalDate
import java.time.ZonedDateTime

data class PreprossesertMeldingV1(
    val søknadId: String,
    val mottatt: ZonedDateTime,
    val språk: String?,
    val dokumentUrls: List<List<URI>>,
    val søker: PreprossesertSøker,
    val id: String,
    val arbeidssituasjon: List<Arbeidssituasjon>,
    val annenForelder: AnnenForelder,
    val antallBarn: Int,
    val alderAvAlleBarn: List<Int>,
    val medlemskap: Medlemskap,
    val utenlandsoppholdIPerioden: UtenlandsoppholdIPerioden?,
    val harForståttRettigheterOgPlikter: Boolean,
    val harBekreftetOpplysninger: Boolean
) {
    internal constructor(
        melding: MeldingV1,
        dokumentUrls: List<List<URI>>,
        søkerAktørId: AktørId
    ) : this(
        språk = melding.språk,
        søknadId = melding.søknadId,
        mottatt = melding.mottatt,
        dokumentUrls = dokumentUrls,
        søker = PreprossesertSøker(melding.søker, søkerAktørId),
        id = melding.id,
        arbeidssituasjon = melding.arbeidssituasjon,
        annenForelder = melding.annenForelder,
        antallBarn = melding.antallBarn,
        alderAvAlleBarn = melding.alderAvAlleBarn,
        medlemskap = melding.medlemskap,
        utenlandsoppholdIPerioden = melding.utenlandsoppholdIPerioden,
        harForståttRettigheterOgPlikter = melding.harForståttRettigheterOgPlikter,
        harBekreftetOpplysninger = melding.harBekreftetOpplysninger
    )
}

data class PreprossesertSøker(
    val fødselsnummer: String,
    val fornavn: String,
    val mellomnavn: String?,
    val etternavn: String,
    val aktørId: String,
    @JsonFormat(pattern = "yyyy-MM-dd") val fødselsdato: LocalDate?
    ) {
    internal constructor(søker: Søker, aktørId: AktørId) : this(
        fødselsnummer = søker.fødselsnummer,
        fornavn = søker.fornavn,
        mellomnavn = søker.mellomnavn,
        etternavn = søker.etternavn,
        aktørId = aktørId.id,
        fødselsdato = søker.fødselsdato
    )
}