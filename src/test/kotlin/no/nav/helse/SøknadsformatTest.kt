package no.nav.helse

import no.nav.helse.dokument.Søknadsformat
import no.nav.helse.prosessering.v1.MeldingV1
import no.nav.helse.prosessering.v1.Søker
import org.skyscreamer.jsonassert.JSONAssert
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*
import kotlin.test.Test

class SøknadsformatTest {

    @Test
    fun `Søknaden journalføres som JSON uten vedlegg`() {
        val søknadId = UUID.randomUUID().toString()
        val json = Søknadsformat.somJson(melding(søknadId))
        println(String(json))
        JSONAssert.assertEquals(
            """{
                  "søknadId": "$søknadId",
                  "mottatt": "2018-01-02T03:04:05.000000006Z",
                  "språk": "nb",
                  "søker": {
                    "fødselsnummer": "1212",
                    "fornavn": "Ola",
                    "mellomnavn": "Mellomnavn",
                    "etternavn": "Nordmann",
                    "fødselsdato": null,
                    "aktørId": "123456"
                  },
                  "harBekreftetOpplysninger": true,
                  "harForståttRettigheterOgPlikter": true
                }

        """.trimIndent(), String(json), true
        )

    }

    private fun melding(soknadId: String): MeldingV1 = MeldingV1(
        søknadId = soknadId,
        mottatt = ZonedDateTime.of(2018, 1, 2, 3, 4, 5, 6, ZoneId.of("UTC")),
        søker = Søker(
            aktørId = "123456",
            fødselsnummer = "1212",
            etternavn = "Nordmann",
            mellomnavn = "Mellomnavn",
            fornavn = "Ola",
            fødselsdato = null
        ),
        harBekreftetOpplysninger = true,
        harForståttRettigheterOgPlikter = true
    )
}
