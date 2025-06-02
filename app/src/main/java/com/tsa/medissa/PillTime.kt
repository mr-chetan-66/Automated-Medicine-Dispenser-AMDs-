package com.tsa.medissa

data class PillTime(
    val hour: Int = 0,
    val minute: Int = 0,
    val medicationName: String = "",
    val compartmentNumber: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun getFormattedTime(): String {
        return String.format("%02d:%02d", hour, minute)
    }
}