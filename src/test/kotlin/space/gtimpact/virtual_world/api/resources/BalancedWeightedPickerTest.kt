package space.gtimpact.virtual_world.api.resources

import space.gtimpact.virtual_world.api.core.ResourcePos
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class BalancedWeightedPickerTest {

    /**
     * Проверяет, что один и тот же seed, измерение, позиция и channel
     * всегда возвращают одинаковый результат.
     */
    @Test
    fun returnsSameResultForSameInput() {
        val items = listOf(
            TestWeightedItem(id = 1, weight = 10.0),
            TestWeightedItem(id = 2, weight = 20.0),
            TestWeightedItem(id = 3, weight = 30.0),
        )
        val picker = createPicker()
        val pos = ResourcePos(x = 37, z = -15)

        val first = picker.pick(
            worldSeed = 12345L,
            dimensionId = 0,
            pos = pos,
            balanceAreaVeins = 128,
            emptyWeight = 5.0,
            channel = 99L,
            items = items,
        )
        val second = picker.pick(
            worldSeed = 12345L,
            dimensionId = 0,
            pos = pos,
            balanceAreaVeins = 128,
            emptyWeight = 5.0,
            channel = 99L,
            items = items,
        )

        assertEquals(first, second)
    }

    /**
     * Проверяет, что результат не зависит от порядка элементов во входном списке,
     * потому что picker использует стабильную сортировку по id.
     */
    @Test
    fun doesNotDependOnItemOrder() {
        val items = listOf(
            TestWeightedItem(id = 1, weight = 10.0),
            TestWeightedItem(id = 2, weight = 20.0),
            TestWeightedItem(id = 3, weight = 30.0),
            TestWeightedItem(id = 4, weight = 40.0),
        )
        val shuffledItems = listOf(items[2], items[0], items[3], items[1])
        val picker = createPicker()

        for (x in -16 until 16) {
            for (z in -16 until 16) {
                val pos = ResourcePos(x = x, z = z)
                val fromOrdered = picker.pick(
                    worldSeed = 456L,
                    dimensionId = -1,
                    pos = pos,
                    balanceAreaVeins = 16,
                    emptyWeight = 7.0,
                    channel = 42L,
                    items = items,
                )
                val fromShuffled = picker.pick(
                    worldSeed = 456L,
                    dimensionId = -1,
                    pos = pos,
                    balanceAreaVeins = 16,
                    emptyWeight = 7.0,
                    channel = 42L,
                    items = shuffledItems,
                )

                assertEquals(fromOrdered, fromShuffled)
            }
        }
    }

    /**
     * Проверяет, что внутри полной balance-area распределение точно соответствует
     * весам, когда размер области делится на сумму весов без остатка.
     */
    @Test
    fun matchesWeightsInsideBalanceArea() {
        val items = listOf(
            TestWeightedItem(id = 10, weight = 1.0),
            TestWeightedItem(id = 20, weight = 3.0),
        )

        val counts = countArea(
            items = items,
            balanceAreaVeins = 8,
            emptyWeight = 0.0,
        )

        assertEquals(16, counts[10])
        assertEquals(48, counts[20])
        assertEquals(null, counts[EMPTY_RESULT])
    }

    /**
     * Проверяет, что пустота участвует в распределении как отдельный weighted-элемент
     * и возвращается из picker как null.
     */
    @Test
    fun usesEmptyWeightAsNullResult() {
        val items = listOf(
            TestWeightedItem(id = 1, weight = 1.0),
            TestWeightedItem(id = 2, weight = 1.0),
        )

        val counts = countArea(
            items = items,
            balanceAreaVeins = 8,
            emptyWeight = 2.0,
        )

        assertEquals(16, counts[1])
        assertEquals(16, counts[2])
        assertEquals(32, counts[EMPTY_RESULT])
    }

    /**
     * Проверяет, что элементы с нулевым, отрицательным, NaN и бесконечным весом
     * не попадают в итоговое распределение.
     */
    @Test
    fun ignoresInvalidWeights() {
        val items = listOf(
            TestWeightedItem(id = 1, weight = 0.0),
            TestWeightedItem(id = 2, weight = -1.0),
            TestWeightedItem(id = 3, weight = Double.NaN),
            TestWeightedItem(id = 4, weight = Double.POSITIVE_INFINITY),
            TestWeightedItem(id = 5, weight = 1.0),
        )

        val counts = countArea(
            items = items,
            balanceAreaVeins = 8,
            emptyWeight = Double.NEGATIVE_INFINITY,
        )

        assertEquals(mapOf(5 to 64), counts)
    }

    /**
     * Проверяет, что при отсутствии валидных элементов и положительном весе пустоты
     * picker возвращает null.
     */
    @Test
    fun returnsNullWhenOnlyEmptyWeightIsAvailable() {
        val picker = createPicker()

        val result = picker.pick(
            worldSeed = 1L,
            dimensionId = 0,
            pos = ResourcePos(x = 0, z = 0),
            balanceAreaVeins = 128,
            emptyWeight = 1.0,
            channel = 1L,
            items = emptyList(),
        )

        assertNull(result)
    }

    /**
     * Проверяет, что отрицательные координаты корректно раскладываются на
     * balance-area и локальную позицию внутри нее.
     */
    @Test
    fun supportsNegativeCoordinates() {
        val items = listOf(
            TestWeightedItem(id = 1, weight = 1.0),
            TestWeightedItem(id = 2, weight = 1.0),
        )
        val picker = createPicker()

        val result = picker.pick(
            worldSeed = 987L,
            dimensionId = 0,
            pos = ResourcePos(x = -129, z = -1),
            balanceAreaVeins = 128,
            emptyWeight = 0.0,
            channel = 5L,
            items = items,
        )

        assertNotNull(result)
        assertTrue(result.id == 1 || result.id == 2)
    }

    /**
     * Проверяет, что невалидный размер balance-area отклоняется сразу,
     * а не приводит к некорректному распределению.
     */
    @Test
    fun rejectsInvalidBalanceAreaSize() {
        val picker = createPicker()

        assertFailsWith<IllegalArgumentException> {
            picker.pick(
                worldSeed = 1L,
                dimensionId = 0,
                pos = ResourcePos(x = 0, z = 0),
                balanceAreaVeins = 0,
                emptyWeight = 0.0,
                channel = 1L,
                items = listOf(TestWeightedItem(id = 1, weight = 1.0)),
            )
        }
    }

    /**
     * Проверяет, что результат для каждой позиции не зависит от порядка обхода
     * позиций внутри balance-area.
     */
    @Test
    fun doesNotDependOnTraversalOrder() {
        val items = listOf(
            TestWeightedItem(id = 1, weight = 2.0),
            TestWeightedItem(id = 2, weight = 3.0),
            TestWeightedItem(id = 3, weight = 5.0),
        )
        val positions = buildList {
            for (x in 0 until 16) {
                for (z in 0 until 16) {
                    this += ResourcePos(x = x, z = z)
                }
            }
        }
        val picker = createPicker()

        val forwardResults = positions.associateWith { pos ->
            picker.pick(
                worldSeed = 555L,
                dimensionId = 0,
                pos = pos,
                balanceAreaVeins = 16,
                emptyWeight = 1.0,
                channel = 8L,
                items = items,
            )
        }
        val backwardResults = positions.asReversed().associateWith { pos ->
            picker.pick(
                worldSeed = 555L,
                dimensionId = 0,
                pos = pos,
                balanceAreaVeins = 16,
                emptyWeight = 1.0,
                channel = 8L,
                items = items,
            )
        }

        assertEquals(forwardResults, backwardResults)
    }

    private fun countArea(
        items: List<TestWeightedItem>,
        balanceAreaVeins: Int,
        emptyWeight: Double,
    ): Map<Int, Int> {
        val picker = createPicker()
        val counts = mutableMapOf<Int, Int>()

        for (x in 0 until balanceAreaVeins) {
            for (z in 0 until balanceAreaVeins) {
                val item = picker.pick(
                    worldSeed = 100L,
                    dimensionId = 0,
                    pos = ResourcePos(x = x, z = z),
                    balanceAreaVeins = balanceAreaVeins,
                    emptyWeight = emptyWeight,
                    channel = 1L,
                    items = items,
                )
                val key = item?.id ?: EMPTY_RESULT
                counts[key] = counts.getOrDefault(key, 0) + 1
            }
        }

        return counts
    }

    private fun createPicker(): BalancedWeightedPicker<TestWeightedItem> {
        return BalancedWeightedPicker(
            idOf = { item -> item.id },
            weightOf = { item -> item.weight },
        )
    }

    private data class TestWeightedItem(
        val id: Int,
        val weight: Double,
    )

    private companion object {
        const val EMPTY_RESULT = Int.MIN_VALUE
    }
}
