package space.gtimpact.virtual_world.api.core

object WorldGrid {
    /**
     * Minecraft-like chunk:
     * 16 x 16 blocks
     */
    const val CHUNK_BITS = 4
    const val CHUNK_SIZE = 1 shl CHUNK_BITS // 16

    /**
     * Ore vein area:
     * 4 x 4 chunks
     * 64 x 64 blocks
     */
    const val VEIN_CHUNKS = 4
    const val VEIN_BITS = CHUNK_BITS + 2
    const val VEIN_SIZE = 1 shl VEIN_BITS // 64

    /**
     * Generation region:
     * 8 x 8 veins
     * 512 x 512 blocks
     */
    const val REGION_VEINS = 8
    const val REGION_BITS = VEIN_BITS + 3
    const val REGION_SIZE = 1 shl REGION_BITS // 512

    fun worldToChunkCoord(value: Int): Int {
        return value shr CHUNK_BITS
    }

    fun worldToVeinCoord(value: Int): Int {
        return value shr VEIN_BITS
    }

    fun worldToRegionCoord(value: Int): Int {
        return value shr REGION_BITS
    }

    fun chunkToWorldCoord(value: Int): Int {
        return value shl CHUNK_BITS
    }

    fun veinToWorldCoord(value: Int): Int {
        return value shl VEIN_BITS
    }

    fun regionToWorldCoord(value: Int): Int {
        return value shl REGION_BITS
    }
}
