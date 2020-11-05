package no.nav.helse

import com.github.tomakehurst.wiremock.WireMockServer
import com.typesafe.config.ConfigFactory
import io.ktor.config.*
import io.ktor.http.*
import io.ktor.server.engine.*
import io.ktor.server.testing.*
import io.ktor.util.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.time.delay
import no.nav.common.KafkaEnvironment
import no.nav.helse.dusseldorf.testsupport.wiremock.WireMockBuilder
import org.json.JSONObject
import org.junit.AfterClass
import org.skyscreamer.jsonassert.JSONAssert
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Duration
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
        val søknad = SøknadUtils.gyldigSøknad()

        kafkaTestProducer.leggTilMottak(søknad)
        journalføringsKonsumer
            .hentJournalførtSøknad(søknad.søknadId)
            .validerJournalførtSøknad(innsendtSøknad = JSONObject(søknad))
    }

    @Test
    fun `En feilprosessert søknad vil bli prosessert etter at tjenesten restartes`() {
        val søknad = SøknadUtils.gyldigSøknad()

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
        val søknad = SøknadUtils.gyldigSøknad(søkerFødselsnummer = dNummerA)

        kafkaTestProducer.leggTilMottak(søknad)
        journalføringsKonsumer
            .hentJournalførtSøknad(søknad.søknadId)
            .validerJournalførtSøknad(innsendtSøknad = JSONObject(søknad))
    }

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

        journalførtSøknad.remove("dokumentUrls")
        journalførtSøknad.remove("mottatt")
        innsendtSøknad.remove("mottatt")

        JSONAssert.assertEquals(innsendtSøknad.toString(), journalførtSøknad.toString(), true)
    }

}