package space.gtimpact.virtual_world.api.services.storage

class StripedLocks(
    size: Int = 64,
) {
    private val locks = Array(size) { Any() }

    fun lockFor(key: Any): Any {
        return locks[(key.hashCode() and Int.MAX_VALUE) % locks.size]
    }
}
