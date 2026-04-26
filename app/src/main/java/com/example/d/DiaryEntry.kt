package com.example.d

import java.io.File

data class DiaryEntry(
    val file: File,
    val timestamp: Long,
    val title: String,
    val preview: String
)
