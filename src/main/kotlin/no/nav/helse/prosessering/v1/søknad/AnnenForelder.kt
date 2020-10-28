package no.nav.helse.prosessering.v1.søknad

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate

data class AnnenForelder(
    val navn: String,
    val fnr: String,
    val situasjon: Situasjon,
    val situasjonBeskrivelse: String?,
    val periodeOver6Måneder: Boolean?,
    @JsonFormat(pattern = "yyyy-MM-dd") val periodeFraOgMed: LocalDate?,
    @JsonFormat(pattern = "yyyy-MM-dd") val periodeTilOgMed: LocalDate?
) {
    fun somMapTilPdf(): Map<String, Any?> {
        return mapOf(
            "navn" to navn,
            "fnr" to fnr,
            "situasjon" to situasjon.utskriftvennlig,
            "beskrivelse" to situasjonBeskrivelse,
            "periodeOver6Måneder" to periodeOver6Måneder,
            "periodeFraOgMed" to periodeFraOgMed,
            "periodeTilOgMed" to periodeTilOgMed
        )
    }
}

enum class Situasjon(val utskriftvennlig: String){
    INNLAGT_I_HELSEINSTITUSJON("Innlagt i helseinstitusjon"),
    UTØVER_VERNEPLIKT("Utøver verneplikt"),
    FENGSEL("Fengsel"),
    SYKDOM("Sykdom"),
    ANNET("Annet")
}