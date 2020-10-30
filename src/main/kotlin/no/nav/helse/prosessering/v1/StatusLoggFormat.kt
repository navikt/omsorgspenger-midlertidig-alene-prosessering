package no.nav.helse.prosessering.v1

internal fun statusLoggFormat(melding: String, id: String): String {
    return String.format("%1$-38s %2$36s", melding, id)
}