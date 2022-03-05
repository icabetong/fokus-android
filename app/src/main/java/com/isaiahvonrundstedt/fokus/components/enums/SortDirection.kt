package com.isaiahvonrundstedt.fokus.components.enums

enum class SortDirection {
    ASCENDING, DESCENDING;

    companion object {
        fun parse(s: String?): SortDirection {
            return when (s) {
                ASCENDING.toString() -> ASCENDING
                DESCENDING.toString() -> DESCENDING
                else -> throw IllegalStateException("Sort Direction must be ascending or descending")
            }
        }
    }
}