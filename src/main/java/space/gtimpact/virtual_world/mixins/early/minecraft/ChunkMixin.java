package space.gtimpact.virtual_world.mixins.early.minecraft;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import space.gtimpact.virtual_world.common.world.IModifiableChunk;
import space.gtimpact.virtual_world.common.world.IWorldNbt;

@Mixin(Chunk.class)
public abstract class ChunkMixin implements IModifiableChunk {

    @Shadow(remap = false)
    public World worldObj;

    @Shadow(remap = false)
    public abstract ChunkCoordIntPair getChunkCoordIntPair();

    @Unique
    private static String NBT_KEY = "VWChunkNBT";
    @Unique
    private static String NBT_KEY_CUSTOM = "VWChunkNBT_Custom";

    @Unique
    private NBTTagCompound virtualWorld$chunkNbt = null;

    @Unique
    private boolean isModified = false;

    @Override
    public void writeToNBT(@NotNull NBTTagCompound nbt) {
        if (virtualWorld$chunkNbt != null) {
            nbt.setTag(NBT_KEY, virtualWorld$chunkNbt);
            isModified = false;
        }
    }

    @Override
    public void readFromNBT(@NotNull NBTTagCompound nbt) {
        virtualWorld$chunkNbt = nbt.getCompoundTag(NBT_KEY);

        if (virtualWorld$chunkNbt == null)
            virtualWorld$chunkNbt = new NBTTagCompound();
    }

    @Override
    public NBTTagCompound getNbt(@NotNull String name) {

        if (virtualWorld$chunkNbt == null)
            virtualWorld$chunkNbt = new NBTTagCompound();

        NBTTagCompound srcTag = virtualWorld$chunkNbt.getCompoundTag(NBT_KEY_CUSTOM);
        if (srcTag == null) return null;

        return srcTag.hasKey(name) ? srcTag.getCompoundTag(name) : null;
    }

    @Override
    public void setNbt(@NotNull NBTTagCompound nbt, @NotNull String name) {

        isModified = true;

        if (virtualWorld$chunkNbt == null)
            virtualWorld$chunkNbt = new NBTTagCompound();

        NBTTagCompound srcTag = virtualWorld$chunkNbt.getCompoundTag(NBT_KEY_CUSTOM);

        srcTag.setTag(name, nbt);

        virtualWorld$chunkNbt.setTag(NBT_KEY_CUSTOM, srcTag);

        if (worldObj instanceof IWorldNbt) {
            ((IWorldNbt) worldObj).addChunk(this);
        }
    }

    @Override
    public boolean isModified() {
        return isModified;
    }

    @NotNull
    @Override
    public ChunkCoordIntPair getCoords() {
        return getChunkCoordIntPair();
    }
}
