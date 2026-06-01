package space.gtimpact.virtual_world.api.game.scanner.ores

data class OreScanPacketDto(
    val dimensionId: Int,
    val layerIndex: Int,
    val veins: List<OreVeinPacketDto>,
) {
    data class OreVeinPacketDto(
        val veinX: Int,
        val veinZ: Int,

        val resourceId: Short,

        val totalGenerated: Int?,
        val totalMined: Int?,
        val totalRemaining: Int?,

        val chunks: List<OreChunkPacketDto>,
    )

    data class OreChunkPacketDto(
        val chunkX: Int,
        val chunkZ: Int,
        val generatedAmount: Int,
        val minedAmount: Int,
        val remainingAmount: Int,
    )
}
