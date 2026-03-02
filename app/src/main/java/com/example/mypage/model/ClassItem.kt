package com.example.mypage.model

data class ClassItem(
    val section: String,
    val day: String,
    val startTime: String,
    val endTime: String,
    val subject: String,
    val room: String?
)
fun List<ClassItem>.filterBySection(section: String): List<ClassItem> {
    return this.filter { it.section == section }
}