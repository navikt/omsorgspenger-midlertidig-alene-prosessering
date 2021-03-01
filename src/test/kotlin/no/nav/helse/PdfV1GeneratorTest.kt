package no.nav.helse

import no.nav.helse.prosessering.v1.PdfV1Generator
import no.nav.helse.prosessering.v1.søknad.*
import java.io.File
import java.time.LocalDate
import java.time.ZonedDateTime
import kotlin.test.Test

class PdfV1GeneratorTest {

    private companion object {
        private val generator = PdfV1Generator()
        private val fødselsdato = LocalDate.now()
    }

    private fun fullGyldigMelding(soknadsId: String): MeldingV1 {
        return MeldingV1(
            språk = "nb",
            søknadId = soknadsId,
            mottatt = ZonedDateTime.now(),
            søker = Søker(
                aktørId = "123456",
                fornavn = "Ærling",
                mellomnavn = "Øverbø",
                etternavn = "Ånsnes",
                fødselsnummer = "29099012345",
                fødselsdato = fødselsdato
            ),
            id = "123456789",
            barn = listOf(
                Barn(
                    navn = "OLE TEST NAVNESEN",
                    identitetsnummer = "12334",
                    aktørId = null
                ), Barn(
                    navn = "Dole",
                    identitetsnummer = "98444",
                    aktørId = null
                ), Barn(
                    navn = "Doffen",
                    identitetsnummer = "123123123",
                    aktørId = null
                )
            ),
            arbeidssituasjon = listOf(
                Arbeidssituasjon.FRILANSER,
                Arbeidssituasjon.SELVSTENDIG_NÆRINGSDRIVENDE,
                Arbeidssituasjon.ANNEN
            ),
            annenForelder = AnnenForelder(
                navn = "Berit",
                fnr = "02119970078",
                situasjon = Situasjon.SYKDOM,
                situasjonBeskrivelse = "Beskrivelse blabla",
                periodeOver6Måneder = false,
                periodeFraOgMed = LocalDate.parse("2020-12-01"),
                periodeTilOgMed = LocalDate.parse("2021-05-31")
            ),
            antallBarn = 2,
            fødselsårBarn = listOf(2005, 2019),
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
            harForståttRettigheterOgPlikter = true,
            harBekreftetOpplysninger = true
        )
    }

    private fun genererOppsummeringsPdfer(writeBytes: Boolean) {

        var id = "1-full-søknad"
        var pdf = generator.generateSoknadOppsummeringPdf(
            melding = fullGyldigMelding(soknadsId = id)
        )
        if (writeBytes) File(pdfPath(soknadId = id)).writeBytes(pdf)

        id = "2-søknad-tomt-medlemskap"
        pdf = generator.generateSoknadOppsummeringPdf(
            melding = fullGyldigMelding(soknadsId = id).copy(
                medlemskap = Medlemskap(
                    harBoddIUtlandetSiste12Mnd = false,
                    skalBoIUtlandetNeste12Mnd = false
                )
            )
        )
        if (writeBytes) File(pdfPath(soknadId = id)).writeBytes(pdf)

        id = "3-søknad-medlemskap-som-null" //TODO 26.02.2021 - Fjernes når frontend er oppdatert
        pdf = generator.generateSoknadOppsummeringPdf(
            melding = fullGyldigMelding(soknadsId = id).copy(
                medlemskap = null
            )
        )
        if (writeBytes) File(pdfPath(soknadId = id)).writeBytes(pdf)

        id = "4-søknad-alle-gamle-felter-som-null" //TODO 26.02.2021 - Fjernes når frontend er oppdatert
        pdf = generator.generateSoknadOppsummeringPdf(
            melding = fullGyldigMelding(soknadsId = id).copy(
                medlemskap = null,
                arbeidssituasjon = null,
                antallBarn = null,
                fødselsårBarn = null
            )
        )
        if (writeBytes) File(pdfPath(soknadId = id)).writeBytes(pdf)
    }

    private fun pdfPath(soknadId: String) = "${System.getProperty("user.dir")}/generated-pdf-$soknadId.pdf"

    @Test
    fun `generering av oppsummerings-PDF fungerer`() {
        genererOppsummeringsPdfer(false)
    }

    @Test
    //@Ignore
    fun `opprett lesbar oppsummerings-PDF`() {
        genererOppsummeringsPdfer(true)
    }
}