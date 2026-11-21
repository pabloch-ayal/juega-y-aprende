package com.rasecalay.juegayaprende

data class ColorItem(
    val name: String,
    val hexColor: String,
    val emoji: String,
    val darkText: Boolean = false
)

data class NumberItem(
    val number: Int,
    val word: String,
    val hexColor: String,
    val itemsEmoji: String
)

object GameData {
    val COLORS = listOf(
        ColorItem("Rojo", "#FF3B30", "🍎"),
        ColorItem("Azul", "#007AFF", "🐳"),
        ColorItem("Amarillo", "#FFCC00", "🐥", darkText = true),
        ColorItem("Verde", "#34C759", "🐸"),
        ColorItem("Naranja", "#FF9500", "🍊"),
        ColorItem("Morado", "#AF52DE", "🍇"),
        ColorItem("Rosado", "#FF69B4", "🦩"),
        ColorItem("Celeste", "#5AC8FA", "☁️", darkText = true),
        ColorItem("Marrón", "#8B5A2B", "🐻"),
        ColorItem("Negro", "#2C2C2E", "🐈‍⬛"),
        ColorItem("Blanco", "#F8F9FA", "🐇", darkText = true),
        ColorItem("Gris", "#8E8E93", "🐘")
    )

    val NUMBERS = listOf(
        NumberItem(1, "Uno", "#FF3B30", "🍎"),
        NumberItem(2, "Dos", "#007AFF", "⭐⭐"),
        NumberItem(3, "Tres", "#34C759", "🐥🐥🐥"),
        NumberItem(4, "Cuatro", "#FF9500", "🚗🚗🚗🚗"),
        NumberItem(5, "Cinco", "#AF52DE", "🎈🎈🎈🎈🎈"),
        NumberItem(6, "Seis", "#FF2D55", "🍓🍓🍓🍓🍓🍓"),
        NumberItem(7, "Siete", "#00C7BE", "🐟🐟🐟🐟🐟🐟🐟"),
        NumberItem(8, "Ocho", "#5856D6", "🦋🦋🦋🦋🦋🦋🦋🦋"),
        NumberItem(9, "Nueve", "#E67E22", "🌼🌼🌼🌼🌼🌼🌼🌼🌼"),
        NumberItem(10, "Diez", "#2ECC71", "🍬🍬🍬🍬🍬🍬🍬🍬🍬🍬")
    )

    fun getPraise(childName: String): String {
        val phrases = listOf(
            "¡Muy bien $childName! ¡Lo hiciste excelente!",
            "¡Genial $childName! ¡Eres súper inteligente!",
            "¡Bravo $childName! ¡Acertaste!",
            "¡Fantástico $childName! ¡Lo lograste!",
            "¡Eso es $childName! ¡Muy bien hecho!"
        )
        return phrases.random()
    }
}
