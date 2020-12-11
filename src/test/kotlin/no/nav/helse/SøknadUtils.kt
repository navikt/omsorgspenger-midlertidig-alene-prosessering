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
        arbeidssituasjon = listOf(Arbeidssituasjon.FRILANSER),
        annenForelder = AnnenForelder(
            navn = "Berit",
            fnr = "02119970078",
            situasjon = Situasjon.FENGSEL,
            situasjonBeskrivelse = "Sitter i fengsel..",
            periodeOver6Måneder = false,
            periodeFraOgMed = LocalDate.parse("2020-01-01"),
            periodeTilOgMed = LocalDate.parse("2020-10-01")
        ),
        antallBarn = 2,
        fødselsårBarn = listOf(2005, 2013),
        medlemskap = Medlemskap(
            harBoddIUtlandetSiste12Mnd = true,
            utenlandsoppholdSiste12Mnd = listOf(
                Utenlandsopphold(
                    fraOgMed = LocalDate.parse("2020-01-01"),
                    tilOgMed = LocalDate.parse("2020-01-10"),
                    landnavn = "Tyskland",
                    landkode = "DE"
                ),
                Utenlandsopphold(
                    fraOgMed = LocalDate.parse("2020-01-01"),
                    tilOgMed = LocalDate.parse("2020-01-10"),
                    landnavn = "Sverige",
                    landkode = "SWE"
                )
            ),
            skalBoIUtlandetNeste12Mnd = true,
            utenlandsoppholdNeste12Mnd = listOf(
                Utenlandsopphold(
                    fraOgMed = LocalDate.parse("2020-10-01"),
                    tilOgMed = LocalDate.parse("2020-10-10"),
                    landnavn = "Brasil",
                    landkode = "BR"
                )
            )
        ),
        harBekreftetOpplysninger = true,
        harForståttRettigheterOgPlikter = true
    )
}
