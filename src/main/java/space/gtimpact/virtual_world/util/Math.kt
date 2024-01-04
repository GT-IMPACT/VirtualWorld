package space.gtimpact.virtual_world.util

object Math {
    fun repeatOffset(start: Int, end: Int, step: Int, action: (Int) -> Unit) {
        var offset = start
        while (offset <= end) {
            action(offset)
            offset += step
        }
    }
}
