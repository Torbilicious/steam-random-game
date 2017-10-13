package de.torbilicious

fun <T> Iterable<T>.random(): T? {
    return when {
        count() == 0 -> null
        else -> {
            val n = (Math.random() * count()).toInt()
            this.drop(kotlin.comparisons.maxOf(n-1, 0))
                    .take(1)
                    .single()
        }
    }
}
