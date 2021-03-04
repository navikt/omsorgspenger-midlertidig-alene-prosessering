package no.nav.helse

import no.nav.helse.prosessering.v1.søknad.*
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.*

object SøknadUtils {

    fun gyldigSøknad(
        søkerFødselsnummer: String = "02119970078",
        søknadId: String = UUID.randomUUID().toString(),
        mottatt: ZonedDateTime = ZonedDateTime.now()
    ) = MeldingV1(
        språk = "nb",
        søknadId = søknadId,
        mottatt = mottatt,
        søker = Søker(
            aktørId = "123456",
            fødselsnummer = søkerFødselsnummer,
            fødselsdato = LocalDate.now().minusDays(1000),
            etternavn = "Nordmann",
            mellomnavn = "Mellomnavn",
            fornavn = "Ola"
        ),
        id = "01ARZ3NDEKTSV4RRFFQ69G5FAV",
        annenForelder = AnnenForelder(
            navn = "Berit",
            fnr = "02119970078",
            situasjon = Situasjon.FENGSEL,
            situasjonBeskrivelse = "Sitter i fengsel..",
            periodeOver6Måneder = false,
            periodeFraOgMed = LocalDate.parse("2020-01-01"),
            periodeTilOgMed = LocalDate.parse("2020-10-01")
        ),
        barn = listOf(
          Barn(
              navn = "Ole Dole",
              identitetsnummer = "29076523302",
              aktørId = null
          )
        ),
        harBekreftetOpplysninger = true,
        harForståttRettigheterOgPlikter = true
    )
}
