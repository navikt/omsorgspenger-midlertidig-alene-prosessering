package no.nav.helse

import no.nav.helse.dokument.Søknadsformat
import no.nav.helse.prosessering.v1.søknad.*
import org.skyscreamer.jsonassert.JSONAssert
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*
import kotlin.test.Test

class SøknadsformatTest {

    @Test
    fun `Søknaden journalføres som JSON uten vedlegg`() {
        val søknadId = UUID.randomUUID().toString()
        val json = Søknadsformat.somJson(melding(søknadId))

        val forventetSøknad =
            //language=json
            """
            {
              "søknadId": $søknadId,
              "mottatt": "2018-01-02T03:04:05.000000006Z",
              "språk": "nb",
              "søker": {
                "fødselsnummer": "02119970078",
                "fornavn": "Ola",
                "mellomnavn": "Mellomnavn",
                "etternavn": "Nordmann",
                "fødselsdato": "2018-01-24",
                "aktørId": "123456"
              },
              "id": "123456789",
              "arbeidssituasjon": [
                "FRILANSER",
                "SELVSTENDIG_NÆRINGSDRIVENDE"
              ],
              "annenForelder": {
                "navn": "Berit",
                "fnr": "02119970078",
                "situasjon": "FENGSEL",
                "situasjonBeskrivelse": "Sitter i fengsel..",
                "periodeOver6Måneder": false,
                "periodeFraOgMed": "2020-01-01",
                "periodeTilOgMed": "2020-10-01"
              },
              "antallBarn": 2,
              "alderAvAlleBarn": [
                5,
                3
              ],
              "medlemskap": {
                "harBoddIUtlandetSiste12Mnd": true,
                "utenlandsoppholdSiste12Mnd": [
                  {
                    "fraOgMed": "2020-01-01",
                    "tilOgMed": "2020-01-10",
                    "landkode": "DE",
                    "landnavn": "Tyskland"
                  },
                  {
                    "fraOgMed": "2020-01-01",
                    "tilOgMed": "2020-01-10",
                    "landkode": "SWE",
                    "landnavn": "Sverige"
                  }
                ],
                "skalBoIUtlandetNeste12Mnd": true,
                "utenlandsoppholdNeste12Mnd": [
                  {
                    "fraOgMed": "2020-10-01",
                    "tilOgMed": "2020-10-10",
                    "landkode": "BR",
                    "landnavn": "Brasil"
                  }
                ]
              },
              "harForståttRettigheterOgPlikter": true,
              "harBekreftetOpplysninger": true
            }
            """.trimIndent()

        JSONAssert.assertEquals(forventetSøknad, String(json), true)
    }

    private fun melding(soknadId: String): MeldingV1 = MeldingV1(
        søknadId = soknadId,
        mottatt = ZonedDateTime.of(2018, 1, 2, 3, 4, 5, 6, ZoneId.of("UTC")),
        søker = Søker(
            aktørId = "123456",
            fødselsnummer = "02119970078",
            etternavn = "Nordmann",
            mellomnavn = "Mellomnavn",
            fornavn = "Ola",
            fødselsdato = LocalDate.parse("2018-01-24")
        ),
        id = "123456789",
        arbeidssituasjon = listOf(Arbeidssituasjon.FRILANSER, Arbeidssituasjon.SELVSTENDIG_NÆRINGSDRIVENDE),
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
        alderAvAlleBarn = listOf(5, 3),
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
