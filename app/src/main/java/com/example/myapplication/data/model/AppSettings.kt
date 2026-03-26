package com.example.myapplication.data.model

// Enum untuk pilihan tema
enum class AppTheme(val label: String) {
    LIGHT("Terang"),
    DARK("Gelap"),
    SYSTEM("Ikuti Sistem")
}

// Enum untuk pilihan bahasa
enum class AppLanguage(val label: String, val code: String) {
    INDONESIAN("Bahasa Indonesia", "id"),
    ENGLISH("English", "en"),
    CHINESE("中文", "zh");
    
    companion object {
        fun fromCode(code: String): AppLanguage {
            return entries.find { it.code == code } ?: INDONESIAN
        }
    }
}

// Data class untuk menyimpan pengaturan aplikasi
data class AppSettings(
    val theme: AppTheme = AppTheme.SYSTEM,
    val language: AppLanguage = AppLanguage.INDONESIAN
)