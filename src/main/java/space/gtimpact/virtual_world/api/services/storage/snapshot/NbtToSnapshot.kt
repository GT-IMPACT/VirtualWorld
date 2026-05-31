package space.gtimpact.virtual_world.api.services.storage.snapshot

import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTTagList
import space.gtimpact.virtual_world.api.resources.fluids.FluidVeinResourceSnapshot
import space.gtimpact.virtual_world.api.resources.ores.OreVeinResourceSnapshot
import space.gtimpact.virtual_world.api.resources.ores.OreVeinResourceSnapshot.OreChunkDepositSnapshot
import space.gtimpact.virtual_world.api.resources.ores.OreVeinResourceSnapshot.OreLayerSnapshot
import space.gtimpact.virtual_world.api.services.storage.cache.RegionSnapshotKey

fun NBTTagCompound.toSnapshot(): RegionSnapshot? {
    return RegionSnapshot(
        schemaVersion = this.getInteger("schemaVersion"),
        key = run {
            val tag = this.getCompoundTag("key") ?: return null
            RegionSnapshotKey(
                worldSeed = tag.getLong("worldSeed"),
                dimensionId = tag.getInteger("dimensionId"),
                regionX = tag.getInteger("regionX"),
                regionZ = tag.getInteger("regionZ"),
            )
        },
        region = run {
            val tag = this.getCompoundTag("region") ?: return null

            RegionSnapshotPayload(
                originX = tag.getInteger("originX"),
                originZ = tag.getInteger("originZ"),
                resources = run {
                    val resourcesTag = tag.getTag("resources") as NBTTagList
                    val resourcesSize = tag.getInteger("resourcesSize")

                    val resources = mutableListOf<VirtualResourceSnapshot>()

                    for (i in 0 until resourcesSize) {
                        val tag = resourcesTag.getCompoundTagAt(i)

                        if (tag.hasKey("fluid")) {
                            resources += FluidVeinResourceSnapshot(
                                resX = tag.getInteger("x"),
                                resZ = tag.getInteger("z"),
                                originX = tag.getInteger("originX"),
                                originZ = tag.getInteger("originZ"),
                                amount = tag.getInteger("amount"),
                                fluid = run {
                                    val tag = tag.getCompoundTag("fluid")
                                    ResourceSnapshot(
                                        stableId = tag.getInteger("stableId"),
                                        displayName = tag.getString("displayName"),
                                        color = tag.getInteger("color"),
                                    )
                                }
                            )
                        }

                        if (tag.hasKey("layers")) {
                            resources += OreVeinResourceSnapshot(
                                resX = tag.getInteger("x"),
                                resZ = tag.getInteger("z"),
                                originX = tag.getInteger("originX"),
                                originZ = tag.getInteger("originZ"),
                                layers = run {
                                    val layersTag = tag.getTag("layers") as NBTTagList
                                    val resourcesSize = tag.getInteger("layersSize")
                                    val layers = mutableListOf<OreLayerSnapshot>()
                                    for (n in 0 until resourcesSize) {
                                        val tag = layersTag.getCompoundTagAt(n)
                                        layers += OreLayerSnapshot(
                                            layerIndex = tag.getInteger("layerIndex"),
                                            ore = run {
                                                val tag = tag.getCompoundTag("ore")
                                                ResourceSnapshot(
                                                    stableId = tag.getInteger("stableId"),
                                                    displayName = tag.getString("displayName"),
                                                    color = tag.getInteger("color"),
                                                )
                                            },
                                            chunks = run {
                                                val chunksTag = tag.getTag("chunks") as NBTTagList
                                                val chunksSize = tag.getInteger("chunksSize")
                                                val chunks = mutableListOf<OreChunkDepositSnapshot>()

                                                for (index in 0 until chunksSize) {
                                                    val tag = chunksTag.getCompoundTagAt(index)
                                                    chunks += OreChunkDepositSnapshot(
                                                        localChunkX = tag.getInteger("localChunkX"),
                                                        localChunkZ = tag.getInteger("localChunkZ"),
                                                        chunkX = tag.getInteger("chunkX"),
                                                        chunkZ = tag.getInteger("chunkZ"),
                                                        amount = tag.getInteger("amount"),
                                                    )
                                                }
                                                chunks
                                            }
                                        )
                                    }
                                    layers
                                }
                            )
                        }
                    }
                    resources
                }
            )
        },
    )
}