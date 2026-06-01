package space.gtimpact.virtual_world.config

import net.minecraftforge.common.config.Configuration
import java.io.File

object Config {

    //Category
    private const val GENERAL = "general"

    //Values
    var MAX_SIZE_REGISTERED_VIRTUAL_ORES = 200
    var MAX_SIZE_REGISTERED_VIRTUAL_FLUIDS = 200
    var IS_DISABLED_VIRTUAL_ORES = false
    var IS_DISABLED_VIRTUAL_FLUIDS = false

    var countWaterForLPDrill = 10
    var countWaterForMPDrill = 100
    var countWaterForHPDrill = 1000

    var enableDebug = false
    var hasTestResources = false

    var scannerDischargeAmount = 100_000

    // newGen

    var maxCachedRegions: Int = 256

    private inline fun onPostCreate(configFile: File?, crossinline action: (Configuration) -> Unit) {
        Configuration(configFile).let { config ->
            config.load()
            action(config)
            if (config.hasChanged()) {
                config.save()
            }
        }
    }

    fun createConfig(configFile: File?) {
        val config = File(File(configFile, "IMPACT"), "VirtualWorld.cfg")
        onPostCreate(config) { cfg ->
            MAX_SIZE_REGISTERED_VIRTUAL_ORES = cfg.getInt(
                "maxSizeRegisteredVirtualOres",
                GENERAL,
                MAX_SIZE_REGISTERED_VIRTUAL_ORES,
                200,
                1000,
                "Max size Registered Virtual Ores"
            )
            MAX_SIZE_REGISTERED_VIRTUAL_FLUIDS = cfg.getInt(
                "maxSizeRegisteredVirtualFluids",
                GENERAL,
                MAX_SIZE_REGISTERED_VIRTUAL_FLUIDS,
                200,
                1000,
                "Max size Registered Virtual Fluids"
            )
            IS_DISABLED_VIRTUAL_ORES = cfg.getBoolean(
                "isDisabledVirtualOres",
                GENERAL,
                IS_DISABLED_VIRTUAL_ORES,
                "Disabled Virtual Ores"
            )
            IS_DISABLED_VIRTUAL_FLUIDS = cfg.getBoolean(
                "isDisabledVirtualFluids",
                GENERAL,
                IS_DISABLED_VIRTUAL_FLUIDS,
                "Disabled Virtual Fluids"
            )

            countWaterForLPDrill = cfg.getInt(
                "countWaterForLPDrill",
                GENERAL,
                countWaterForLPDrill,
                0,
                100,
                "Count Water For LP Drilling Fluids"
            )

            countWaterForMPDrill = cfg.getInt(
                "countWaterForMPDrill",
                GENERAL,
                countWaterForMPDrill,
                0,
                1000,
                "Count Water For MP Drilling Fluids"
            )

            countWaterForHPDrill = cfg.getInt(
                "countWaterForHPDrill",
                GENERAL,
                countWaterForHPDrill,
                0,
                10000,
                "Count Water For HP Drilling Fluids"
            )

            enableDebug = cfg.getBoolean(
                "enableDebug",
                GENERAL,
                enableDebug,
                "Enabled Debug Mode"
            )
            hasTestResources = cfg.getBoolean(
                "hasTestResources",
                GENERAL,
                hasTestResources,
                "Enabled Test Resources"
            )

            scannerDischargeAmount = cfg.getInt(
                "scannerDischargeAmount",
                GENERAL,
                scannerDischargeAmount,
                0,
                1_000_000,
                "Discharge amount scanner per operation"
            )
        }
    }
}
