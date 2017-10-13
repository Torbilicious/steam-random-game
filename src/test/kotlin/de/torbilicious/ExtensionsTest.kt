package de.torbilicious

import org.junit.Assert
import org.junit.Test
import java.util.*


class ExtensionsTests {

    @Test
    fun `Empty list should return null`() {
        val emptyList = emptyList<String>()

        Assert.assertTrue(emptyList.random() == null)
    }

    @Test
    fun `Singleton list should return only element`() {
        val onlyElement = "I am the only element!"
        val collection = Collections.singletonList(onlyElement)

        Assert.assertTrue(collection.random() == onlyElement)
    }

    @Test
    fun `List with two elements returns one element`() {
        val elementOne = "I am the first element!"
        val elementTwo = "I am the secend element!"
        val collection = listOf(elementOne, elementTwo)

        val random = collection.random()
        Assert.assertTrue(random == elementOne || random == elementTwo)
    }

    @Test
    fun `List with many elemnts works`() {
        val list = mutableListOf<Int>()

        (-10000..10000).forEach {
            list.add(it)
        }

        val random = list.random()
        Assert.assertTrue(random != null)
        Assert.assertTrue(list.contains(list.random()))
    }
}