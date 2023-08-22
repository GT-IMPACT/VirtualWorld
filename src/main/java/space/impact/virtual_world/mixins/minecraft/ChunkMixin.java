package space.impact.virtual_world.mixins.minecraft;

import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import space.impact.virtual_world.api.IChunkResourceFluid;
import space.impact.virtual_world.api.IChunkResourceOre;

@Mixin(Chunk.class)
@SuppressWarnings("AddedMixinMembersNamePattern")
public abstract class ChunkMixin implements IChunkResourceOre, IChunkResourceFluid {
	
	@Unique
	private int virtualWorldResources$oreVeinId = 0;
	@Unique
	private int virtualWorldResources$fluidVeinId = 0;
	@Unique
	private int virtualWorldResources$oreAmount = 0;
	@Unique
	private int virtualWorldResources$fluidAmount = 0;
	
	@Override
	@Unique
	public int getAmountOreResource() {
		return virtualWorldResources$oreAmount;
	}
	
	@Override
	@Unique
	public void setAmountOreResource(int amount) {
		virtualWorldResources$oreAmount = amount;
	}
	
	@Override
	public int getIdOreVein() {
		return virtualWorldResources$oreVeinId;
	}
	
	@Override
	public void setIdOreVein(int id) {
		virtualWorldResources$oreVeinId = id;
	}
	
	@Override
	@Unique
	public int getAmountFluidResource() {
		return virtualWorldResources$fluidAmount;
	}
	
	@Override
	@Unique
	public void setAmountFluidResource(int amount) {
		virtualWorldResources$fluidAmount = amount;
	}
	
	@Override
	public int getIdFluidVein() {
		return virtualWorldResources$fluidVeinId;
	}
	
	@Override
	public void setIdFluidVein(int id) {
		virtualWorldResources$fluidVeinId = id;
	}
}
