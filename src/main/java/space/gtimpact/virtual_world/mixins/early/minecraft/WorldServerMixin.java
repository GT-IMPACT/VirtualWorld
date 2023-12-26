package space.gtimpact.virtual_world.mixins.early.minecraft;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.profiler.Profiler;
import net.minecraft.world.*;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.storage.ISaveHandler;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import space.gtimpact.virtual_world.common.world.IChunkNbt;
import space.gtimpact.virtual_world.common.world.IModifiableChunk;
import space.gtimpact.virtual_world.common.world.IWorldNbt;

import java.util.HashSet;
import java.util.Set;

@Mixin(WorldServer.class)
public abstract class WorldServerMixin extends World implements IWorldNbt {

    @Unique
    private Set<ChunkCoordIntPair> virtualWorld$nbtChunks = new HashSet<>();

    public WorldServerMixin(ISaveHandler p_i45368_1_, String p_i45368_2_, WorldProvider p_i45368_3_, WorldSettings p_i45368_4_, Profiler p_i45368_5_) {
        super(p_i45368_1_, p_i45368_2_, p_i45368_3_, p_i45368_4_, p_i45368_5_);
    }

    public WorldServerMixin(ISaveHandler p_i45369_1_, String p_i45369_2_, WorldSettings p_i45369_3_, WorldProvider p_i45369_4_, Profiler p_i45369_5_) {
        super(p_i45369_1_, p_i45369_2_, p_i45369_3_, p_i45369_4_, p_i45369_5_);
    }

    @Override
    public void addChunk(@NotNull IChunkNbt ch) {
        virtualWorld$nbtChunks.add(ch.getCoords());
    }

    @Override
    public void readFromNBT(@NotNull NBTTagCompound nbt) {
        virtualWorld$nbtChunks.clear();
        NBTTagList chunks = (NBTTagList) nbt.getTag("chunks");
        for (int i = 0; i < chunks.tagCount(); i++) {
            NBTTagCompound chunk = chunks.getCompoundTagAt(i);
            if (chunk != null) {
                Chunk ch = getChunkFromChunkCoords(
                        chunk.getInteger("x"),
                        chunk.getInteger("z")
                );
                if (ch instanceof IChunkNbt) {
                    try {
                        ((IChunkNbt) ch).readFromNBT(chunk);
                    } catch (Exception ignored) {}
                }
                virtualWorld$nbtChunks.add(ch.getChunkCoordIntPair());
            }
        }
    }

    @Override
    public void writeToNBT(@NotNull NBTTagCompound nbt) {
        NBTTagList chunks = new NBTTagList();
        for (ChunkCoordIntPair pair : virtualWorld$nbtChunks) {
            NBTTagCompound chunkNbt = new NBTTagCompound();
            Chunk ch = getChunkFromChunkCoords(pair.chunkXPos, pair.chunkZPos);
            if (ch != null) {
                if (ch instanceof IModifiableChunk) {
                   try {
                       if (((IModifiableChunk) ch).isModified()) {
                           chunkNbt.setInteger("x", pair.chunkXPos);
                           chunkNbt.setInteger("z", pair.chunkZPos);
                           ((IModifiableChunk) ch).writeToNBT(chunkNbt);
                           chunks.appendTag(chunkNbt);
                       }
                   } catch (Exception ignored) {}
                }
            }
        }
        nbt.setTag("chunks", chunks);
    }
}
