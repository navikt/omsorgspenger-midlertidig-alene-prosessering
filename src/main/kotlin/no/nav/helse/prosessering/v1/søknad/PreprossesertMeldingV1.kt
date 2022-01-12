package no.nav.helse.prosessering.v1.søknad

import com.fasterxml.jackson.annotation.JsonFormat
import no.nav.helse.felles.AktørId
import no.nav.k9.søknad.Søknad
import java.time.LocalDate
import java.time.ZonedDateTime

data class PreprossesertMeldingV1(
    val søknadId: String,
    val mottatt: ZonedDateTime,
    val språk: String?,
    val dokumentId: List<List<String>>,
    val søker: PreprossesertSøker,
    val id: String,
    val annenForelder: AnnenForelder,
    val barn: List<Barn>,
    val k9Format: Søknad,
    val harForståttRettigheterOgPlikter: Boolean,
    val harBekreftetOpplysninger: Boolean
) {
    internal constructor(
        melding: MeldingV1,
        dokumentId: List<List<String>>,
        søkerAktørId: AktørId
    ) : this(
        språk = melding.språk,
        søknadId = melding.søknadId,
        mottatt = melding.mottatt,
        dokumentId = dokumentId,
        søker = PreprossesertSøker(melding.søker, søkerAktørId),
        id = melding.id,
        annenForelder = melding.annenForelder,
        barn = melding.barn,
        k9Format = melding.k9Format,
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