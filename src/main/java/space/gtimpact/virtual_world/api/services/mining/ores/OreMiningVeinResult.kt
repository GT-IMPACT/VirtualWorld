package space.gtimpact.virtual_world.api.services.mining.ores

data class OreMiningVeinResult(
    val results: List<OreMiningResult>,
    val requestedAmount: Int,
    val minedAmount: Int,
    val remainingAmount: Int,
)
