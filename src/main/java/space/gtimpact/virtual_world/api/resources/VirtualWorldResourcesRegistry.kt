package space.gtimpact.virtual_world.api.resources

import space.gtimpact.virtual_world.api.resources.fluids.FluidVein
import space.gtimpact.virtual_world.api.resources.ores.OreVein
import java.util.concurrent.ConcurrentHashMap

class VirtualWorldResourcesRegistry {

    internal val oreVeinsMap = ConcurrentHashMap<Int, OreVein>()
    internal val fluidVeinsMap = ConcurrentHashMap<Int, FluidVein>()

    fun registerOreVein(oreVein: OreVein) {
        oreVeinsMap[oreVein.id] = oreVein
    }

    fun registerFluidVein(fluidVein: FluidVein) {
        fluidVeinsMap[fluidVein.id] = fluidVein
    }

    fun getOreVein(id: Int): OreVein? {
        return oreVeinsMap[id]
    }

    fun getFluidVein(id: Int): FluidVein? {
        return fluidVeinsMap[id]
    }
}
