package space.gtimpact.virtual_world.config

import net.minecraftforge.common.config.Configuration
import java.io.File

object Config {

    //Category
    private const val GENERAL = "general"

    //Values
    var countWaterForLPDrill = 10
    var countWaterForMPDrill = 100
    var countWaterForHPDrill = 1000

    var enableDebug = false
    var hasTestResources = false

    var scannerDischargeAmount = 100_000

    // newGen

    var maxCachedRegions: Int = 256
    var emptyWeight: Double = 2.0
    var balanceAreaVeins: Int = 128

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

            maxCachedRegions = cfg.getInt(
                "maxCachedRegions",
                GENERAL,
                maxCachedRegions,
                16,
                1024,
                "Maximum number of generated regions kept in memory cache."
            )

            emptyWeight = cfg.getFloat(
                "emptyWeight",
                GENERAL,
                emptyWeight.toFloat(),
                0.0f,
                50.0f,
                "Virtual empty entry weight used by balanced resource generation. Higher value increases chance that no ore/fluid vein is generated."
            ).toDouble()

            balanceAreaVeins = cfg.getInt(
                "balanceAreaVeins",
                GENERAL,
                balanceAreaVeins,
                8,
                1024,
                "Size of one balanced generation area in vein cells per axis. Larger values improve weight distribution accuracy but make balance repeat over a larger area"
            )
        }
    }
}
