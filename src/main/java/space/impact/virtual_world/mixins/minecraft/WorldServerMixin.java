package space.impact.virtual_world.mixins.minecraft;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.profiler.Profiler;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.storage.ISaveHandler;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import space.impact.virtual_world.api.*;
import space.impact.virtual_world.api.fluids.RegionFluid;
import space.impact.virtual_world.api.ores.RegionOre;

import java.util.HashSet;
import java.util.Set;

import static space.impact.virtual_world.api.OreGenerator.SHIFT_REGION_FROM_CHUNK;

@Mixin(WorldServer.class)
@SuppressWarnings("AddedMixinMembersNamePattern")
public abstract class WorldServerMixin extends World implements IResourceWorld {
	
	public WorldServerMixin(ISaveHandler p_i45368_1_, String p_i45368_2_, WorldProvider p_i45368_3_, WorldSettings p_i45368_4_, Profiler p_i45368_5_) {
		super(p_i45368_1_, p_i45368_2_, p_i45368_3_, p_i45368_4_, p_i45368_5_);
	}
	
	@Unique
	private final Set<ResourceRegion> virtualWorldResources$regions = new HashSet<>();
	
	@Unique
	@NotNull
	@Override
	public Set<ResourceRegion> getResourceRegions() {
		return virtualWorldResources$regions;
	}
	
	@Unique
	@Override
	public void generateResourceRegion(@NotNull Chunk chunk) {
		int dim = provider.dimensionId;
		
		RegionOre regionOre = new RegionOre(chunk.xPosition >> SHIFT_REGION_FROM_CHUNK, chunk.zPosition >> SHIFT_REGION_FROM_CHUNK, dim);
		OreGenerator.generate(regionOre, this);
		
		RegionFluid regionFluid = new RegionFluid(chunk.xPosition >> SHIFT_REGION_FROM_CHUNK, chunk.zPosition >> SHIFT_REGION_FROM_CHUNK, dim);
		FluidGenerator.createFluidRegion(chunk, this);
		
		virtualWorldResources$regions.add(new ResourceRegion(regionOre, regionFluid));
	}
	
	@Override
	public ResourceRegion getResourceRegionFromChunk(@NotNull Chunk chunk) {
		ResourceRegion region = VirtualAPI.findRegionResourceFromChunk(virtualWorldResources$regions, chunk);
		
		if (region == null) generateResourceRegion(chunk);
		
		return VirtualAPI.findRegionResourceFromChunk(virtualWorldResources$regions, chunk);
	}
	
	@Override
	public void writeToNBT(@NotNull NBTTagCompound nbt) {
		NBTTagList regions = new NBTTagList();
		for (ResourceRegion regionRes : virtualWorldResources$regions) {
			NBTTagCompound region = new NBTTagCompound();
			regionRes.writeToNBT(region);
			regions.appendTag(region);
		}
		nbt.setTag("REGIONS", regions);
	}
	
	@Override
	public void readFromNBT(@NotNull NBTTagCompound nbt) {
		NBTTagList regions = (NBTTagList) nbt.getTag("REGIONS");
		for (int i = 0; i < regions.tagCount(); i++) {
			NBTTagCompound region = regions.getCompoundTagAt(i);
			ResourceRegion regionRes = ResourceRegion.readFromNBT(region);
			virtualWorldResources$regions.add(regionRes);
		}
	}
}
