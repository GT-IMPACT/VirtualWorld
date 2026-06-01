package space.gtimpact.virtual_world.api.core

import kotlin.random.Random

object StableRandom {

    fun fromSeedAndResourceLayer(
        worldSeed: Long,
        dimensionId: Int,
        pos: ResourcePos,
        layerIndex: Int
    ): Random {
        var hash = worldSeed

        hash = hash xor (dimensionId.toLong() * DIMENSION_CONST)
        hash = hash xor (pos.x.toLong() * X_CONST)
        hash = hash xor (pos.z.toLong() * Z_CONST)
        hash = hash xor (layerIndex.toLong() * LAYER_CONST)

        return Random(mix64(hash))
    }

    fun fromSeedAndRegion(
        worldSeed: Long,
        regionPos: RegionPos
    ): Random {
        var hash = worldSeed

        hash = hash xor (regionPos.x.toLong() * X_CONST)
        hash = hash xor (regionPos.z.toLong() * Z_CONST)

        return Random(mix64(hash))
    }

    fun fromSeedAndFluidResource(
        worldSeed: Long,
        dimensionId: Int,
        pos: ResourcePos,
    ): Random {
        var hash = worldSeed

        hash = hash xor FLUID_CONST
        hash = hash xor (dimensionId.toLong() * DIMENSION_CONST)
        hash = hash xor (pos.x.toLong() * X_CONST)
        hash = hash xor (pos.z.toLong() * Z_CONST)

        return Random(mix64(hash))
    }

    private fun mix64(input: Long): Long {
        var z = input

        z = (z xor (z ushr 30)) * -4658895280553007687L
        z = (z xor (z ushr 27)) * -7723592293110705685L
        z = z xor (z ushr 31)

        return z
    }

    private const val X_CONST = -7046029254386353131L
    private const val Z_CONST = -4658895280553007687L
    private const val LAYER_CONST = -7723592293110705685L
    private const val FLUID_CONST = -3372029247567499371L
    private const val DIMENSION_CONST = 0x632BE59BD9B4E019L
}
