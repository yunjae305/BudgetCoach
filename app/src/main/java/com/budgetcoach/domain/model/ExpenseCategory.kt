package com.budgetcoach.domain.model

enum class ExpenseCategory(val displayName: String, val emoji: String) {
    FOOD("식비", "🍽️"),
    TRANSPORT("교통", "🚗"),
    SHOPPING("쇼핑", "🛍️"),
    CULTURE("문화/여가", "🎬"),
    MEDICAL("의료", "🏥"),
    EDUCATION("교육", "📚"),
    HOUSING("주거", "🏠"),
    COMMUNICATION("통신", "📱"),
    OTHER("기타", "📦");

    companion object {
        fun fromName(name: String): ExpenseCategory =
            entries.find { it.name == name } ?: OTHER
    }
}
