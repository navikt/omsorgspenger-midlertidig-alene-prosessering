package no.nav.helse

import com.github.tomakehurst.wiremock.WireMockServer
import com.typesafe.config.ConfigFactory
import io.ktor.config.ApplicationConfig
import io.ktor.config.HoconApplicationConfig
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.engine.stop
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.createTestEnvironment
import io.ktor.server.testing.handleRequest
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.time.delay
import no.nav.common.KafkaEnvironment
import no.nav.helse.dusseldorf.testsupport.wiremock.WireMockBuilder
import no.nav.helse.prosessering.v1.*
import org.json.JSONObject
import org.junit.AfterClass
import org.junit.Ignore
import org.skyscreamer.jsonassert.JSONAssert
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Duration
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull


@KtorExperimentalAPI
class OmsorgspengesoknadProsesseringTest {

    @KtorExperimentalAPI
    private companion object {

        private val logger: Logger = LoggerFactory.getLogger(OmsorgspengesoknadProsesseringTest::class.java)

        private val wireMockServer: WireMockServer = WireMockBuilder()
            .withAzureSupport()
            .navnOppslagConfig()
            .build()
            .stubK9MellomlagringHealth()
            .stubOmsorgspengerJoarkHealth()
            .stubJournalfor()
            .stubLagreDokument()
            .stubSlettDokument()

        private val kafkaEnvironment = KafkaWrapper.bootstrap()
        private val kafkaTestProducer = kafkaEnvironment.meldingsProducer()

        private val journalføringsKonsumer = kafkaEnvironment.journalføringsKonsumer()
        private val cleanupKonsumer = kafkaEnvironment.cleanupKonsumer()
        private val preprossesertKonsumer = kafkaEnvironment.preprossesertKonsumer()

        // Se https://github.com/navikt/dusseldorf-ktor#f%C3%B8dselsnummer
        private val gyldigFodselsnummerA = "02119970078"
        private val dNummerA = "55125314561"

        private var engine = newEngine(kafkaEnvironment).apply {
            start(wait = true)
        }

        private fun getConfig(kafkaEnvironment: KafkaEnvironment?): ApplicationConfig {
            val fileConfig = ConfigFactory.load()
            val testConfig = ConfigFactory.parseMap(
                TestConfiguration.asMap(
                    wireMockServer = wireMockServer,
                    kafkaEnvironment = kafkaEnvironment
                )
            )
            val mergedConfig = testConfig.withFallback(fileConfig)
            return HoconApplicationConfig(mergedConfig)
        }

        private fun newEngine(kafkaEnvironment: KafkaEnvironment?) = TestApplicationEngine(createTestEnvironment {
            config = getConfig(kafkaEnvironment)
        })

        private fun stopEngine() = engine.stop(5, 60, TimeUnit.SECONDS)

        internal fun restartEngine() {
            stopEngine()
            engine = newEngine(kafkaEnvironment)
            engine.start(wait = true)
        }

        @AfterClass
        @JvmStatic
        fun tearDown() {
            logger.info("Tearing down")
            wireMockServer.stop()
            journalføringsKonsumer.close()
            kafkaTestProducer.close()
            stopEngine()
            kafkaEnvironment.tearDown()
            logger.info("Tear down complete")
        }
    }

    @Test
    fun `test isready, isalive, health og metrics`() {
        with(engine) {
            handleRequest(HttpMethod.Get, "/isready") {}.apply {
                assertEquals(HttpStatusCode.OK, response.status())
                handleRequest(HttpMethod.Get, "/isalive") {}.apply {
                    assertEquals(HttpStatusCode.OK, response.status())
                    handleRequest(HttpMethod.Get, "/metrics") {}.apply {
                        assertEquals(HttpStatusCode.OK, response.status())
                        handleRequest(HttpMethod.Get, "/health") {}.apply {
                            assertEquals(HttpStatusCode.OK, response.status())
                        }
                    }
                }
            }
        }
    }

    @Test
    fun `Gylding søknad blir prosessert av journalføringskonsumer`() {
        val søknad = gyldigSøknad(
            fødselsnummerSoker = gyldigFodselsnummerA
        )

        kafkaTestProducer.leggTilMottak(søknad)
        journalføringsKonsumer
            .hentJournalførtSøknad(søknad.søknadId)
            .validerJournalførtSøknad(innsendtSøknad = JSONObject(søknad))
    }

    @Test
    @Ignore //TODO FJERN ignore når journalføring fungerer.
    fun `En feilprosessert søknad vil bli prosessert etter at tjenesten restartes`() {
        val søknad = gyldigSøknad(
            fødselsnummerSoker = gyldigFodselsnummerA
        )

        wireMockServer.stubJournalfor(500) // Simulerer feil ved journalføring

        kafkaTestProducer.leggTilMottak(søknad)
        ventPaaAtRetryMekanismeIStreamProsessering()
        readyGir200HealthGir503()

        wireMockServer.stubJournalfor(201) // Simulerer journalføring fungerer igjen
        restartEngine()
        journalføringsKonsumer
            .hentJournalførtSøknad(søknad.søknadId)
            .validerJournalførtSøknad(innsendtSøknad = JSONObject(søknad))

    }

    @Test
    fun `Sende søknad hvor søker har D-nummer`() {
        val søknad = gyldigSøknad(
            fødselsnummerSoker = dNummerA
        )

        kafkaTestProducer.leggTilMottak(søknad)
        journalføringsKonsumer
            .hentJournalførtSøknad(søknad.søknadId)
            .validerJournalførtSøknad(innsendtSøknad = JSONObject(søknad))
    }

    private fun gyldigSøknad(
        fødselsnummerSoker: String,
        sprak: String? = "nb"
    ): MeldingV1 = MeldingV1(
        språk = sprak,
        søknadId = UUID.randomUUID().toString(),
        mottatt = ZonedDateTime.now(),
        søker = Søker(
            aktørId = "123456",
            fødselsnummer = fødselsnummerSoker,
            fødselsdato = LocalDate.now().minusDays(1000),
            etternavn = "Nordmann",
            mellomnavn = "Mellomnavn",
            fornavn = "Ola"
        ),
        harBekreftetOpplysninger = true,
        harForståttRettigheterOgPlikter = true
    )

    private fun ventPaaAtRetryMekanismeIStreamProsessering() = runBlocking { delay(Duration.ofSeconds(30)) }

    private fun readyGir200HealthGir503() {
        with(engine) {
            handleRequest(HttpMethod.Get, "/isready") {}.apply {
                assertEquals(HttpStatusCode.OK, response.status())
                handleRequest(HttpMethod.Get, "/health") {}.apply {
                    assertEquals(HttpStatusCode.ServiceUnavailable, response.status())
                }
            }
        }
    }

    private infix fun String.validerJournalførtSøknad(innsendtSøknad: JSONObject) {
        val rawJson = JSONObject(this)

        val metadata = rawJson.getJSONObject("metadata")
        assertNotNull(metadata)
        assertNotNull(metadata.getString("correlationId"))
        assertNotNull(metadata.getString("requestId"))

        val journalførtSøknad = rawJson.getJSONObject("data").getJSONObject("søknad")
        assertNotNull(journalførtSøknad)
        assertEquals(innsendtSøknad.getString("søknadId"), journalførtSøknad.getString("søknadId"))

        val søkerJournalført = journalførtSøknad.getJSONObject("søker")
        val søkerInnsendt = innsendtSøknad.getJSONObject("søker")
        JSONAssert.assertEquals(søkerJournalført, søkerInnsendt, true)
    }
}