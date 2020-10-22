package no.nav.helse.prosessering.v1.søknad

data class Medlemskap(
    val harBoddIUtlandetSiste12Mnd: Boolean,
    val utenlandsoppholdSiste12Mnd: List<Utenlandsopphold> = listOf(),
    val skalBoIUtlandetNeste12Mnd: Boolean,
    val utenlandsoppholdNeste12Mnd: List<Utenlandsopphold> = listOf()
) {
    fun somMapTilPdf(): Map<String, Any?>{
        return mapOf(
            "harBoddIUtlandetSiste12Mnd" to harBoddIUtlandetSiste12Mnd,
            "utenlandsoppholdSiste12Mnd" to utenlandsoppholdSiste12Mnd.somMapTilPdf(),
            "skalBoIUtlandetNeste12Mnd" to skalBoIUtlandetNeste12Mnd,
            "utenlandsoppholdNeste12Mnd" to utenlandsoppholdNeste12Mnd.somMapTilPdf()
        )
    }
}