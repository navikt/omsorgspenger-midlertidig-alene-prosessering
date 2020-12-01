package no.nav.helse.prosessering.v1

import io.prometheus.client.Counter
import no.nav.helse.prosessering.v1.søknad.PreprossesertMeldingV1
import org.slf4j.LoggerFactory

private val annenForelderSituasjonCounter = Counter.build()
    .name("annen_forelder_situasjon_counter")
    .help("Teller for annenForelder sin situasjon")
    .labelNames("situasjon")
    .register()

private val generelCounter = Counter.build()
    .name("generel_counter")
    .help("Generel counter")
    .labelNames("spm", "svar")
    .register()

internal fun PreprossesertMeldingV1.reportMetrics(){
    val logger = LoggerFactory.getLogger("no.nav.helse.prosessering.v1.Metrics")

    annenForelderSituasjonCounter.labels(annenForelder.situasjon.name).inc()

    //Sjekker hvor mange som sender inn hvor de sier "Nei" til at perioden er over 6 mnd
    if(annenForelder.periodeOver6Måneder != null && !annenForelder.periodeOver6Måneder){
        generelCounter.labels("erPeriodenOver6MndSpørsmål", "Nei")
        logger.info("Metrikk: `erPeriodenOver6MndSpørsmål`, `Nei` økes med en")
    }

    //Sjekker om hvor mange som sender inn perioder som er under 6 mnd
    if(annenForelder.periodeFraOgMed != null && annenForelder.periodeTilOgMed != null){
        if(!annenForelder.periodeFraOgMed.plusMonths(6).isBefore(annenForelder.periodeTilOgMed)){
            generelCounter.labels("erPeriodenOver6Mnd", "Nei")
            logger.info("Metrikk: `erPeriodenOver6Mnd`, `Nei` økes med en")
        }
    }
}