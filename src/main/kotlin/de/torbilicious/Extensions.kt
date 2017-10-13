package de.torbilicious

fun <T> Iterable<T>.random(): T? {
    return if (count() == 0) {
        null
    } else {
        val n = (Math.random() * count()).toInt()
        this.drop(n-1).take(1).single()
    }
}
