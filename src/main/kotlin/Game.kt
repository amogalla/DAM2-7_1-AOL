import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import org.openrndr.math.Vector2
import kotlin.math.atan2
import kotlin.random.Random

enum class GameState {
    STOPPED, RUNNING
}

fun Vector2.angle(): Double {
    val rawAngle = atan2(y = this.y, x = this.x)
    return (rawAngle / Math.PI) * 180
}

class Game {
    var prevTime = 0L
    val ship = ShipData()
    var finalBossEliminado: Boolean = false
    var contadorFinalBossVisible: Boolean = false

    var targetLocation by mutableStateOf<DpOffset>(DpOffset.Zero)

    var gameObjects = mutableStateListOf<GameObject>()
    var gameState by mutableStateOf(GameState.RUNNING)
    var gameStatus by mutableStateOf("Let's play!")

    var vidasFinalBoss:Int = 11

    fun startGame() {
        finalBossEliminado = false
        vidasFinalBoss = 11
        gameObjects.clear()
        ship.position = Vector2(width.value / 2.0, height.value / 2.0)
        ship.movementVector = Vector2.ZERO
        gameObjects.add(ship)

        //Añadimos un asteroide
        gameObjects.add(AsteroidData().apply {
            position = Vector2(100.0, 100.0); angle = Random.nextDouble() * 360.0; speed = 2.0
        })

        //Añadimos un alien de Los Simpson's
        gameObjects.add(SimpsonAlienData().apply {
            position = Vector2(100.0, 100.0); angle = Random.nextDouble() * 360.0; speed = 2.0
        })

        gameState = GameState.RUNNING
        gameStatus = "Good luck!"
    }

    fun update(time: Long) {
        val delta = time - prevTime
        val floatDelta = (delta / 1E8).toFloat()
        prevTime = time

        if (gameState == GameState.STOPPED) return

        val cursorVector = Vector2(targetLocation.x.value.toDouble(), targetLocation.y.value.toDouble())
        val shipToCursor = cursorVector - ship.position
        val angle = atan2(y = shipToCursor.y, x = shipToCursor.x)

        val bullets = gameObjects.filterIsInstance<BulletData>()

        ship.visualAngle = shipToCursor.angle()
        ship.movementVector = ship.movementVector + (shipToCursor.normalized * floatDelta.toDouble())

        for (gameObject in gameObjects) {
            gameObject.update(floatDelta, this)
        }

        // Limit number of bullets at the same time
        if (bullets.count() > 3) {
            gameObjects.remove(bullets.first())
        }

        val asteroids = gameObjects.filterIsInstance<AsteroidData>()
        val aliens = gameObjects.filterIsInstance<SimpsonAlienData>()
        val finalBoss = gameObjects.filterIsInstance<FinalBossData>()

        val listaEnemigosTotales:MutableList<EnemyData> = asteroids.toMutableList()
        listaEnemigosTotales.addAll(aliens.toMutableList())
        listaEnemigosTotales.addAll(finalBoss.toMutableList())

        // Bullet <-> Enemy interaction
        (listaEnemigosTotales).forEach { enemy ->
            val least = bullets.firstOrNull { it.overlapsWith(enemy) } ?: return@forEach
            if (enemy.position.distanceTo(least.position) < enemy.size) {

                if(enemy !is FinalBossData) {
                    gameObjects.remove(enemy)
                    gameObjects.remove(least)
                }

                if (enemy.size < 50.0) return@forEach
                // it's still pretty big, let's spawn some smaller ones
                repeat(2) {

                    if (enemy is AsteroidData) {
                        gameObjects.add(AsteroidData(
                            enemy.speed * 2,
                            Random.nextDouble() * 360.0,
                            enemy.position
                        ).apply {
                            size = enemy.size / 2
                        })
                    }

                    if (enemy is SimpsonAlienData) {
                        gameObjects.add(SimpsonAlienData(
                            enemy.speed * 2,
                            Random.nextDouble() * 360.0,
                            enemy.position
                        ).apply {
                            size = enemy.size / 2
                        })
                    }

                    if (enemy is FinalBossData) {
                        // Bullet <-> FinalBoss interaction
                        if (enemy.position.distanceTo(least.position) < enemy.size) {
                            gameObjects.remove(least)
                            vidasFinalBoss--
                            if (vidasFinalBoss <= 0) {
                                finalBossEliminado = true
                                gameObjects.remove(enemy)
                                winGame()
                            }
                        }
                    }
                }
            }
        }

        // Enemy <-> Ship interaction
        if (listaEnemigosTotales.any { asteroid -> ship.overlapsWith(asteroid) }) {
            endGame()
        }

        // Win LEVEL 1 condition
        if (listaEnemigosTotales.isEmpty() && !finalBossEliminado) {
            showFinalBoss()
        }
    }

    fun showFinalBoss(){
        //Añadimos un finalBoss
        gameObjects.add(FinalBossData().apply {
            position = Vector2(100.0, 100.0); angle = Random.nextDouble() * 360.0; speed = 2.0
        })

        contadorFinalBossVisible = true
}


    fun endGame() {
        gameObjects.remove(ship)
        gameState = GameState.STOPPED
        gameStatus = "Better luck next time!"
    }

    fun winGame() {
        gameState = GameState.STOPPED
        gameStatus = "Congratulations!"
    }

    var width by mutableStateOf(0.dp)
    var height by mutableStateOf(0.dp)
}