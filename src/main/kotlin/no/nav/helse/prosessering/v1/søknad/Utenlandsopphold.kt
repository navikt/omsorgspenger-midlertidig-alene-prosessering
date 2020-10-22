package no.nav.helse.prosessering.v1.søknad

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class Utenlandsopphold(
    @JsonFormat(pattern = "yyyy-MM-dd") val fraOgMed: LocalDate,
    @JsonFormat(pattern = "yyyy-MM-dd") val tilOgMed: LocalDate,
    val landkode: String,
    val landnavn: String
){
    fun somMapTilPdf(): Map<String, Any?>{
        val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy").withZone(ZoneId.of("Europe/Oslo"))
        return mapOf(
            "landnavn" to landnavn,
            "landkode" to landkode,
            "fraOgMed" to dateFormatter.format(fraOgMed),
            "tilOgMed" to dateFormatter.format(tilOgMed)
        )
    }
}

internal fun List<Utenlandsopphold>.somMapTilPdf(): List<Map<String, Any?>> {
    return map {
        it.somMapTilPdf()
    }
}