package space.gtimpact.virtual_world.mixins.early.minecraft;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import space.gtimpact.virtual_world.common.world.IModifiableChunk;
import space.gtimpact.virtual_world.util.WorldNBT;

@SuppressWarnings("ALL")
@Mixin(Chunk.class)
public abstract class ChunkMixin implements IModifiableChunk {

    @Shadow
    public World worldObj;

    @Shadow
    @Final
    public int xPosition;

    @Shadow
    @Final
    public int zPosition;

    @Override
    public NBTTagCompound getNbt(@NotNull String name) {
        return WorldNBT.getChunkNBT(this, name, worldObj);
    }

    @Override
    public void setNbt(@NotNull NBTTagCompound nbt, @NotNull String name) {
        WorldNBT.setChunkNBT(this, nbt, name, worldObj);
    }

    @NotNull
    @Override
    public ChunkCoordIntPair getCoords() {
        return new ChunkCoordIntPair(xPosition, zPosition);
    }
}
