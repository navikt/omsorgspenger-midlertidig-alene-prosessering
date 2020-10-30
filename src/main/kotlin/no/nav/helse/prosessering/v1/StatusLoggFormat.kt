package no.nav.helse.prosessering.v1

//Brukes når man logger status i flyten. Formaterer slik at loggen er mer lesbar
internal fun statusLoggFormat(melding: String, id: String): String {
    return String.format("%1$-39s %2$36s", melding, id)
}