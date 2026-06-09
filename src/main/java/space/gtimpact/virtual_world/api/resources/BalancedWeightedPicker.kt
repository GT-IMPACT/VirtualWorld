package space.gtimpact.virtual_world.api.resources

import space.gtimpact.virtual_world.api.core.ResourcePos

class BalancedWeightedPicker<T : Any>(
    private val idOf: (T) -> Int,
    private val weightOf: (T) -> Double,
) {

    fun pick(
        worldSeed: Long,
        dimensionId: Int,
        pos: ResourcePos,
        balanceAreaVeins: Int,
        emptyWeight: Double,
        channel: Long,
        items: Collection<T>,
    ): T? {
        require(balanceAreaVeins > 0) { "balanceAreaVeins must be greater than 0" }

        val slotCount = balanceAreaVeins.toLong() * balanceAreaVeins.toLong()
        require(slotCount <= MAX_SLOT_COUNT) { "balanceAreaVeins is too large" }

        val entries = buildEntries(
            items = items,
            emptyWeight = emptyWeight,
        )

        if (entries.isEmpty()) {
            return null
        }

        assignSlots(
            entries = entries,
            slotCount = slotCount,
        )

        val balanceAreaX = floorDiv(pos.x, balanceAreaVeins)
        val balanceAreaZ = floorDiv(pos.z, balanceAreaVeins)
        val localX = floorMod(pos.x, balanceAreaVeins)
        val localZ = floorMod(pos.z, balanceAreaVeins)
        val localSlot = localX.toLong() * balanceAreaVeins.toLong() + localZ.toLong()

        val seed = balanceSeed(
            worldSeed = worldSeed,
            dimensionId = dimensionId,
            balanceAreaX = balanceAreaX,
            balanceAreaZ = balanceAreaZ,
            channel = channel,
        )
        val pickedSlot = permuteSlot(
            slot = localSlot,
            slotCount = slotCount,
            seed = seed,
        )

        var cursor = 0L
        for (entry in entries) {
            cursor += entry.slots
            if (pickedSlot < cursor) {
                return entry.item
            }
        }

        return entries.lastOrNull()?.item
    }

    private fun buildEntries(
        items: Collection<T>,
        emptyWeight: Double,
    ): List<Entry<T>> {
        val entries = items
            .asSequence()
            .mapNotNull { item ->
                val weight = weightOf(item)
                if (!weight.isUsableWeight()) {
                    null
                } else {
                    Entry(
                        item = item,
                        stableOrder = idOf(item).toLong(),
                        weight = weight,
                    )
                }
            }
            .sortedBy { entry -> entry.stableOrder }
            .toMutableList()

        if (emptyWeight.isUsableWeight()) {
            entries += Entry(
                item = null,
                stableOrder = EMPTY_ORDER,
                weight = emptyWeight,
            )
        }

        return entries
    }

    private fun assignSlots(
        entries: List<Entry<T>>,
        slotCount: Long,
    ) {
        var totalWeight = 0.0
        for (entry in entries) {
            totalWeight += entry.weight
        }

        var assignedSlots = 0L
        for (entry in entries) {
            val exactSlots = entry.weight / totalWeight * slotCount.toDouble()
            val wholeSlots = exactSlots.toLong()

            entry.slots = wholeSlots
            entry.remainder = exactSlots - wholeSlots.toDouble()
            assignedSlots += wholeSlots
        }

        var remainingSlots = slotCount - assignedSlots
        if (remainingSlots <= 0L) {
            return
        }

        val remainderOrder = entries.sortedWith(
            compareByDescending<Entry<T>> { entry -> entry.remainder }
                .thenBy { entry -> entry.stableOrder }
        )

        var index = 0
        while (remainingSlots > 0L) {
            remainderOrder[index].slots += 1L
            remainingSlots -= 1L
            index += 1
            if (index == remainderOrder.size) {
                index = 0
            }
        }

        ensureMinimumSlots(
            entries = entries,
            slotCount = slotCount,
        )
    }

    private fun ensureMinimumSlots(
        entries: List<Entry<T>>,
        slotCount: Long,
    ) {
        if (slotCount < entries.size.toLong()) {
            return
        }

        val emptyEntries = entries.filter { entry -> entry.slots == 0L }
        for (emptyEntry in emptyEntries) {
            val donor = findMinimumSlotDonor(entries) ?: return
            donor.slots -= 1L
            emptyEntry.slots = 1L
        }
    }

    private fun findMinimumSlotDonor(entries: List<Entry<T>>): Entry<T>? {
        var donor: Entry<T>? = null

        for (entry in entries) {
            if (entry.slots <= 1L) {
                continue
            }

            val currentDonor = donor
            if (currentDonor == null || entry.slots > currentDonor.slots) {
                donor = entry
            } else if (entry.slots == currentDonor.slots && entry.stableOrder > currentDonor.stableOrder) {
                donor = entry
            }
        }

        return donor
    }

    private fun balanceSeed(
        worldSeed: Long,
        dimensionId: Int,
        balanceAreaX: Int,
        balanceAreaZ: Int,
        channel: Long,
    ): Long {
        var hash = worldSeed
        hash = hash xor (dimensionId.toLong() * DIMENSION_CONST)
        hash = hash xor (balanceAreaX.toLong() * X_CONST)
        hash = hash xor (balanceAreaZ.toLong() * Z_CONST)
        hash = hash xor (channel * CHANNEL_CONST)
        return mix64(hash)
    }

    private fun permuteSlot(
        slot: Long,
        slotCount: Long,
        seed: Long,
    ): Long {
        if (slotCount == 1L) {
            return 0L
        }

        val domain = nextPowerOfTwo(slotCount)
        val mask = domain - 1L
        val multiplierA = mix64(seed xor MULTIPLIER_A_CONST) or 1L
        val multiplierB = mix64(seed xor MULTIPLIER_B_CONST) or 1L
        val offsetA = mix64(seed xor OFFSET_A_CONST) and mask
        val offsetB = mix64(seed xor OFFSET_B_CONST) and mask

        var value = slot
        do {
            value = value xor (value ushr 7)
            value = (value * multiplierA + offsetA) and mask
            value = value xor (value ushr 5)
            value = (value * multiplierB + offsetB) and mask
            value = value xor (value ushr 3)
        } while (value >= slotCount)

        return value
    }

    private fun nextPowerOfTwo(value: Long): Long {
        var power = 1L
        while (power < value) {
            power = power shl 1
        }

        return power
    }

    private fun floorDiv(
        value: Int,
        divisor: Int,
    ): Int {
        var result = value / divisor
        if ((value xor divisor) < 0 && result * divisor != value) {
            result -= 1
        }

        return result
    }

    private fun floorMod(
        value: Int,
        divisor: Int,
    ): Int {
        return value - floorDiv(value, divisor) * divisor
    }

    private fun Double.isUsableWeight(): Boolean {
        return this > 0.0 && !isNaN() && !isInfinite()
    }

    private fun mix64(input: Long): Long {
        var z = input
        z = (z xor (z ushr 30)) * -4658895280553007687L
        z = (z xor (z ushr 27)) * -7723592293110705685L
        z = z xor (z ushr 31)
        return z
    }

    private data class Entry<T : Any>(
        val item: T?,
        val stableOrder: Long,
        val weight: Double,
        var slots: Long = 0L,
        var remainder: Double = 0.0,
    )

    private companion object {
        const val EMPTY_ORDER = Long.MAX_VALUE
        const val MAX_SLOT_COUNT = 1L shl 62

        const val X_CONST = -7046029254386353131L
        const val Z_CONST = -4658895280553007687L
        const val CHANNEL_CONST = -7723592293110705685L
        const val DIMENSION_CONST = 0x632BE59BD9B4E019L

        const val MULTIPLIER_A_CONST = -3372029247567499371L
        const val MULTIPLIER_B_CONST = -5797282940391623497L
        const val OFFSET_A_CONST = 0x165667B19E3779F9L
        const val OFFSET_B_CONST = 0x27D4EB2F165667C5L
    }
}
