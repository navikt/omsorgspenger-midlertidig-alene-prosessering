package no.nav.helse.prosessering.v1.søknad

import com.fasterxml.jackson.annotation.JsonFormat
import no.nav.helse.felles.AktørId
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
    val arbeidssituasjon: List<Arbeidssituasjon>? = null, //TODO 26.02.2021 - Fjernes når frontend og api er prodsatt
    val annenForelder: AnnenForelder,
    val antallBarn: Int? = null, //TODO 26.02.2021 - Fjernes når frontend og api er prodsatt
    val fødselsårBarn: List<Int>? = null, //TODO 26.02.2021 - Fjernes når frontend og api er prodsatt
    val medlemskap: Medlemskap? = null, //TODO 26.02.2021 - Fjernes når frontend og api er prodsatt
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
        fødselsårBarn = melding.fødselsårBarn,
        medlemskap = melding.medlemskap,
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