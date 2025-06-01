package ru.netology.nmedia

// Функция для форматирования чисел в краткую запись
// ----------------
fun formatNumberShort(count: Int): String {
    return when {
        count < 1_000 -> count.toString()
        count < 10_000 -> {
            val hundreds = (count % 1_000) / 100
            if (hundreds == 0) "${count / 1_000}K"
            else "${count / 1_000}.${hundreds}K"
        }
        count < 1_000_000 -> "${count / 1_000}K"
        else -> {
            val hundredThousands = (count % 1_000_000) / 100_000
            if (hundredThousands == 0) "${count / 1_000_000}M"
            else "${count / 1_000_000}.${hundredThousands}M"
        }
    }
}
// ----------------