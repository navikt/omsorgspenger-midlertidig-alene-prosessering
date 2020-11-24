package no.nav.helse.prosessering.v1

import io.prometheus.client.Counter
import no.nav.helse.prosessering.v1.søknad.PreprossesertMeldingV1

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
    annenForelderSituasjonCounter.labels(annenForelder.situasjon.name).inc()

    if(annenForelder.periodeOver6Måneder != null && !annenForelder.periodeOver6Måneder){
        generelCounter.labels("erPeriodenOver6MndSpørsmål", "Nei")
    }
}