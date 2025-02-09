package space.gtimpact.virtual_world.mixins.early.minecraft;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.profiler.Profiler;
import net.minecraft.world.*;
import net.minecraft.world.storage.ISaveHandler;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import space.gtimpact.virtual_world.common.world.IModifiableChunk;
import space.gtimpact.virtual_world.common.world.IWorldNbt;
import space.gtimpact.virtual_world.extras.NBT;
import space.gtimpact.virtual_world.util.WorldNBT;

import java.util.HashMap;

@SuppressWarnings("ALL")
@Mixin(WorldServer.class)
public abstract class WorldServerMixin extends World implements IWorldNbt {

    @Unique
    private HashMap<ChunkCoordIntPair, NBTTagCompound> virtualWorld$nbtChunks = new HashMap<>();

    public WorldServerMixin(ISaveHandler p_i45368_1_, String p_i45368_2_, WorldProvider p_i45368_3_, WorldSettings p_i45368_4_, Profiler p_i45368_5_) {
        super(p_i45368_1_, p_i45368_2_, p_i45368_3_, p_i45368_4_, p_i45368_5_);
    }

    public WorldServerMixin(ISaveHandler p_i45369_1_, String p_i45369_2_, WorldSettings p_i45369_3_, WorldProvider p_i45369_4_, Profiler p_i45369_5_) {
        super(p_i45369_1_, p_i45369_2_, p_i45369_3_, p_i45369_4_, p_i45369_5_);
    }

    @Override
    public void addChunk(@NotNull IModifiableChunk ch, @NotNull NBTTagCompound nbt, @NotNull String tagName) {
        NBTTagCompound chunk = virtualWorld$nbtChunks.get(ch.getCoords());

        if (chunk == null) {
            chunk = new NBTTagCompound();
        }

        NBTTagCompound chunkData = chunk.getCompoundTag(NBT.CHUNK_DATA);

        if (chunkData == null || chunkData.hasNoTags())
            chunkData = new NBTTagCompound();

        chunkData.setTag(tagName, nbt);

        chunk.setTag(NBT.CHUNK_DATA, chunkData);

        virtualWorld$nbtChunks.put(ch.getCoords(), chunk);
    }

    @NotNull
    @Override
    public NBTTagCompound getChunkNbt(@NotNull IModifiableChunk ch) {
        NBTTagCompound chunk = virtualWorld$nbtChunks.get(ch.getCoords());

        if (chunk == null) {
            chunk = new NBTTagCompound();
        }

        NBTTagCompound chunkData = chunk.getCompoundTag(NBT.CHUNK_DATA);

        if (chunkData == null || chunkData.hasNoTags())
            chunkData = new NBTTagCompound();

        return chunkData;
    }

    @Override
    public void readFromNBT(@NotNull NBTTagCompound nbt) {
        WorldNBT.readFromNBTWorld(virtualWorld$nbtChunks, nbt, this);
    }

    @Override
    public void writeToNBT(@NotNull NBTTagCompound nbt) {
        WorldNBT.writeToNBTWorld(virtualWorld$nbtChunks, nbt, this);
    }
}
