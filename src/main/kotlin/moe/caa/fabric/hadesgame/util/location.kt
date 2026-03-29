package moe.caa.fabric.hadesgame.util

import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.future.asDeferred
import moe.caa.fabric.hadesgame.GameCore
import net.minecraft.block.Blocks
import net.minecraft.block.FluidBlock
import net.minecraft.entity.Entity
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.server.world.ChunkTicketType
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.ChunkPos
import net.minecraft.world.Heightmap
import kotlin.math.max

fun Entity.getLocation() = Location(
    this.world as ServerWorld,
    this.x, this.y, this.z,
    this.yaw, this.pitch
)

fun Entity.teleport(location: Location) = teleport(
    location.world,
    location.x,
    location.y,
    location.z,
    emptySet(),
    location.yaw,
    location.pitch,
    true
)


data class Location(
    var world: ServerWorld,
    var x: Double,
    var y: Double,
    var z: Double,
    var yaw: Float = 0.0.toFloat(),
    var pitch: Float = 0.0.toFloat(),
)

val randomLocationChunkTicketType: ChunkTicketType = Registry.register(
    Registries.TICKET_TYPE,
    "hadesgame:random_location",
    ChunkTicketType(300, false, ChunkTicketType.Use.LOADING)
)

suspend fun ServerWorld.randomLobbySpawnLocation(): Location {
    while (true) {
        val posX = (-20000000 + Math.random() * 40000000).toInt()
        val posZ = (-20000000 + Math.random() * 40000000).toInt()

        val chunkPos = ChunkPos(posX shr 4, posZ shr 4)
        chunkManager.addTicket(randomLocationChunkTicketType, chunkPos, 0)

        try {
            val worldChunk = GameCore.coroutineScope.async {
                repeat(10) {
                    val chunk = chunkManager.chunkLoadingManager
                        .getCurrentChunkHolder(chunkPos.toLong())
                        ?.accessibleFuture?.asDeferred()?.await()?.orElse(null)
                    if (chunk != null) return@async chunk
                    delay(50)
                }
                return@async null
            }.await()

            if (worldChunk == null) {
                continue
            }

            val posY = getTopY(Heightmap.Type.WORLD_SURFACE, posX, posZ)
            val block = getBlockState(BlockPos(posX, posY - 1, posZ))

            if (block.block is FluidBlock) {
                continue
            }
            if (block.block == Blocks.AIR) {
                continue
            }

            var platformMaxY = posY
            for (x in posX - 10..posX + 10) {
                for (z in posZ - 10..posZ + 10) {
                    platformMaxY = max(platformMaxY, getTopY(Heightmap.Type.WORLD_SURFACE, x, z))
                }
            }

            if (platformMaxY + 40 > height) {
                continue
            }

            return Location(this, posX + 0.5, posY + 20.0, posZ + 0.5)
        } finally {
            chunkManager.removeTicket(randomLocationChunkTicketType, chunkPos, 0)
        }
    }
}
