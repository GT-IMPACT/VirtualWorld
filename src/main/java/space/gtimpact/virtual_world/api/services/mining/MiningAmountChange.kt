package space.gtimpact.virtual_world.api.services.mining

data class MiningAmountChange(
    val changedAmount: Int,
    val totalChangedAmount: Int,
    val remainingAmount: Int
)
