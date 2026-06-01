package space.gtimpact.virtual_world.api.resources.ores

import space.gtimpact.virtual_world.api.core.ChunkPos

/**
 * Количество руды в конкретном чанке внутри рудной жилы.
 *
 * Рудная жила занимает область 4x4 чанка, то есть 64x64 блока.
 * Внутри этой жилы каждый из 16 чанков имеет собственное количество руды.
 *
 * Этот объект не описывает конкретные блоки руды внутри чанка.
 * Он хранит только агрегированное количество ресурса, доступное для добычи
 * в данном чанке и конкретном слое жилы.
 *
 * Пример:
 * - жила находится в VeinPos(10, 5)
 * - внутри неё есть 16 чанков
 * - для localChunkX = 2 и localChunkZ = 1 может быть amount = 24
 *
 * Это значит, что в соответствующем чанке жилы доступно 24 единицы руды.
 *
 * @property localChunkX локальная X-позиция чанка внутри жилы. Диапазон: 0..3.
 * @property localChunkZ локальная Z-позиция чанка внутри жилы. Диапазон: 0..3.
 * @property chunkPos мировая позиция чанка в общей сетке чанков.
 * @property amount количество руды в этом чанке. Не должно быть отрицательным.
 */
data class OreChunkDeposit(
    val localChunkX: Int,
    val localChunkZ: Int,
    val chunkPos: ChunkPos,
    val amount: Int,
)
