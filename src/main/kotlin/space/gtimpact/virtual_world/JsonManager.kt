package space.gtimpact.virtual_world

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import net.minecraftforge.common.DimensionManager
import space.gtimpact.virtual_world.api.TypeFluidVein
import space.gtimpact.virtual_world.api.VirtualAPI
import space.gtimpact.virtual_world.api.VirtualAPI.GENERATED_REGIONS_VIRTUAL_FLUIDS
import space.gtimpact.virtual_world.api.VirtualAPI.GENERATED_REGIONS_VIRTUAL_ORES
import space.gtimpact.virtual_world.api.fluids.RegionFluid
import space.gtimpact.virtual_world.api.ores.RegionOre
import space.gtimpact.virtual_world.extras.TypeFluidVeinSerialized
import java.io.File
import java.io.FileWriter
import java.util.*

object JsonManager {

    private var WORLD_DIRECTORY: File? = null
    private const val ROOT_FOLDER = "IMPACT"
    private const val VIRTUAL_ORE_FOLDER = "VirtualOres"
    private const val VIRTUAL_FLUID_FOLDER = "VirtualFluids"

    private lateinit var rootDirectory: File
    private lateinit var oresDirectory: File
    private lateinit var fluidsDirectory: File

    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(TypeFluidVein::class.java, TypeFluidVeinSerialized())
        .create()

    private fun initData() {
        space.gtimpact.virtual_world.JsonManager.WORLD_DIRECTORY = DimensionManager.getCurrentSaveRootDirectory()
        if (space.gtimpact.virtual_world.JsonManager.WORLD_DIRECTORY == null) {
            println(IllegalStateException("[IMPACT|VirtualOres] ERROR NOT SET WORLD DIRECTORY"))
        }
        space.gtimpact.virtual_world.JsonManager.rootDirectory = File(space.gtimpact.virtual_world.JsonManager.WORLD_DIRECTORY, space.gtimpact.virtual_world.JsonManager.ROOT_FOLDER)
        space.gtimpact.virtual_world.JsonManager.oresDirectory = File(space.gtimpact.virtual_world.JsonManager.rootDirectory, space.gtimpact.virtual_world.JsonManager.VIRTUAL_ORE_FOLDER)
        space.gtimpact.virtual_world.JsonManager.fluidsDirectory = File(space.gtimpact.virtual_world.JsonManager.rootDirectory, space.gtimpact.virtual_world.JsonManager.VIRTUAL_FLUID_FOLDER)

        if (!space.gtimpact.virtual_world.JsonManager.rootDirectory.isDirectory && !space.gtimpact.virtual_world.JsonManager.rootDirectory.mkdirs()) {
            println(IllegalStateException("[IMPACT|VirtualOres] Failed to create ${space.gtimpact.virtual_world.JsonManager.rootDirectory.absolutePath}"))
        }
        if (!space.gtimpact.virtual_world.JsonManager.oresDirectory.isDirectory && !space.gtimpact.virtual_world.JsonManager.oresDirectory.mkdirs()) {
            println(IllegalStateException("[IMPACT|VirtualOres] Failed to create ${space.gtimpact.virtual_world.JsonManager.oresDirectory.absolutePath}"))
        }
        if (!space.gtimpact.virtual_world.JsonManager.fluidsDirectory.isDirectory && !space.gtimpact.virtual_world.JsonManager.fluidsDirectory.mkdirs()) {
            println(IllegalStateException("[IMPACT|VirtualOres] Failed to create ${space.gtimpact.virtual_world.JsonManager.fluidsDirectory.absolutePath}"))
        }
    }

    private fun clearData() {
        GENERATED_REGIONS_VIRTUAL_ORES.clear()
        GENERATED_REGIONS_VIRTUAL_FLUIDS.clear()
    }

    fun save() {
        runBlocking(Dispatchers.IO) {
            space.gtimpact.virtual_world.JsonManager.saveOres()
            space.gtimpact.virtual_world.JsonManager.saveFluids()
        }

        space.gtimpact.virtual_world.JsonManager.clearData()
        space.gtimpact.virtual_world.JsonManager.WORLD_DIRECTORY = null
    }

    fun load() {
        space.gtimpact.virtual_world.JsonManager.initData()
        space.gtimpact.virtual_world.JsonManager.clearData()

        runBlocking {
            space.gtimpact.virtual_world.JsonManager.loadOres()
            space.gtimpact.virtual_world.JsonManager.loadFluids()
        }
    }

    private fun loadOres() {
        if (!space.gtimpact.virtual_world.JsonManager.oresDirectory.isDirectory) return
        space.gtimpact.virtual_world.JsonManager.oresDirectory.listFiles()?.forEach { folderDim ->
            if (folderDim.isDirectory) {
                var currentDim = 0
                val dimRegions = HashMap<Int, RegionOre>()
                folderDim.listFiles()?.forEach { fileRegion ->
                    fileRegion.bufferedReader().use {
                        val reg = space.gtimpact.virtual_world.JsonManager.gson.fromJson(it, RegionOre::class.java)
                        val hash = Objects.hash(reg.xRegion, reg.zRegion, reg.dim)
                        currentDim = reg.dim
                        dimRegions[hash] = reg
                    }
                }
                GENERATED_REGIONS_VIRTUAL_ORES[currentDim] = dimRegions
            }
        }
        VirtualAPI.resizeOreVeins()
    }

    private fun saveOres() {
        if (GENERATED_REGIONS_VIRTUAL_ORES.isEmpty()) return
        for ((dim, regions) in GENERATED_REGIONS_VIRTUAL_ORES) {
            val folderDim = File(space.gtimpact.virtual_world.JsonManager.oresDirectory, "DIM$dim")
            if (folderDim.mkdirs()) {
                for ((_, region) in regions) {
                    val fileRegion = File(folderDim, "r.${region.xRegion}.${region.zRegion}.json")
                    FileWriter(fileRegion).buffered().use {
                        space.gtimpact.virtual_world.JsonManager.gson.toJson(region, it)
                    }
                }
            }
        }
    }

    private fun loadFluids() {
        if (!space.gtimpact.virtual_world.JsonManager.fluidsDirectory.isDirectory) return
        space.gtimpact.virtual_world.JsonManager.fluidsDirectory.listFiles()?.forEach { folderDim ->
            if (folderDim.isDirectory) {
                var currentDim = 0
                val dimRegions = HashMap<Int, RegionFluid>()
                folderDim.listFiles()?.forEach { fileRegion ->
                    fileRegion.bufferedReader().use {
                        val reg = space.gtimpact.virtual_world.JsonManager.gson.fromJson(it, RegionFluid::class.java)
                        val hash = Objects.hash(reg.xRegion, reg.zRegion, reg.dim)
                        currentDim = reg.dim
                        dimRegions[hash] = reg
                    }
                }
                GENERATED_REGIONS_VIRTUAL_FLUIDS[currentDim] = dimRegions
            }
        }
        VirtualAPI.resizeFluidVeins()
    }

    private fun saveFluids() {
        if (GENERATED_REGIONS_VIRTUAL_FLUIDS.isEmpty()) return
        for ((dim, regions) in GENERATED_REGIONS_VIRTUAL_FLUIDS) {
            val folderDim = File(space.gtimpact.virtual_world.JsonManager.fluidsDirectory, "DIM$dim")
            if (folderDim.mkdirs()) {
                for ((_, region) in regions) {
                    val fileRegion = File(folderDim, "r.${region.xRegion}.${region.zRegion}.json")
                    FileWriter(fileRegion).buffered().use {
                        space.gtimpact.virtual_world.JsonManager.gson.toJson(region, it)
                    }
                }
            }
        }
    }
}