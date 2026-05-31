package space.gtimpact.virtual_world.api.saving

import com.google.gson.GsonBuilder
import net.minecraft.client.Minecraft
import space.gtimpact.virtual_world.api.game.scanner.ScanMapKey
import space.gtimpact.virtual_world.api.game.scanner.ScannerClientStateManager
import space.gtimpact.virtual_world.api.game.scanner.fluids.FluidScanMapEntry
import space.gtimpact.virtual_world.api.game.scanner.fluids.FluidScanPacketDto
import space.gtimpact.virtual_world.api.game.scanner.fluids.toData
import space.gtimpact.virtual_world.api.game.scanner.fluids.toDto
import space.gtimpact.virtual_world.api.game.scanner.ores.OreScanMapEntry
import space.gtimpact.virtual_world.api.game.scanner.ores.OreScanPacketDto
import space.gtimpact.virtual_world.api.game.scanner.ores.toData
import space.gtimpact.virtual_world.api.game.scanner.ores.toDto
import space.gtimpact.virtual_world.config.Config
import java.io.File

object ClientDataSaver {

    const val DIR_ORES_L0 = "ores_0"
    const val DIR_ORES_L1 = "ores_1"
    const val DIR_FLUIDS = "fluids"

    private fun getStorageDirectory(): File {
        val folderMC = Minecraft.getMinecraft().mcDataDir
        val fileClient = File(folderMC, "virtualworld/data")
        return fileClient
    }

    private fun saveJson(dimFolder: File, data: String, dirName: String) {
        runCatching {
            val fileTarget = File(dimFolder, "$dirName.json")
            if (!fileTarget.canRead()) fileTarget.createNewFile()
            fileTarget.writeText(data)
        }
    }

    private inline fun <reified T> readJson(file: File, dirName: String): T? {
        return runCatching {
            val fileTarget = File(file, "$dirName.json")
            if (!fileTarget.canRead()) fileTarget.createNewFile()
            gson.fromJson(fileTarget.readText(), T::class.java)
        }.getOrNull()
    }

    fun save(worldId: String) {
        try {
            val worldCacheDirectory = File(getStorageDirectory(), worldId)

            ScannerClientStateManager.scanOreMap.forEach { (key, value) ->

                val dimFolder = File(worldCacheDirectory, "DIM${key.dimension}")
                    .apply { mkdirs() }

                val dto = value.toData().toDto()

                when (value.layer) {
                    0 -> saveJson(dimFolder, gson.toJson(dto), DIR_ORES_L0)
                    1 -> saveJson(dimFolder, gson.toJson(dto), DIR_ORES_L1)
                    else -> Unit
                }
            }

            ScannerClientStateManager.scanFluidMap.forEach { (key, value) ->
                val dimFolder = File(worldCacheDirectory, "DIM${key.dimension}")
                    .apply { mkdirs() }

                val dto = value.toData().toDto()

                saveJson(dimFolder, gson.toJson(dto), DIR_FLUIDS)
            }

        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    fun load(worldId: String) {

        val worldCacheDirectory = File(getStorageDirectory(), worldId)

        ScannerClientStateManager.scanOreMap.clear()
        ScannerClientStateManager.scanFluidMap.clear()

        worldCacheDirectory.listFiles()?.forEach { file ->
            val dim = file.name.replace("DIM", "").toIntOrNull() ?: return@forEach

            readJson<OreScanPacketDto>(file, DIR_ORES_L0)?.also { data ->
                val key = ScanMapKey(dimension = dim, layer = 0)
                ScannerClientStateManager.scanOreMap[key] = OreScanMapEntry.fromClientScanState(data.toData())
            }

            readJson<OreScanPacketDto>(file, DIR_ORES_L1)?.also { data ->
                val key = ScanMapKey(dimension = dim, layer = 1)
                ScannerClientStateManager.scanOreMap[key] = OreScanMapEntry.fromClientScanState(data.toData())
            }

            readJson<FluidScanPacketDto>(file, DIR_FLUIDS)?.also { data ->
                val key = ScanMapKey(dimension = dim, layer = 0)
                ScannerClientStateManager.scanFluidMap[key] = FluidScanMapEntry.fromClientScanState(data.toData())
            }
        }
    }

    private val gson = GsonBuilder()
        .apply { if (Config.enableDebug) setPrettyPrinting() }
        .create()
}
