package com.kennycason.cellular.automata

import java.awt.Color
import java.awt.Graphics
import java.awt.image.BufferedImage
import java.io.File
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import javax.imageio.ImageIO
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.WindowConstants

/**
 * Created by kenny on 12/4/16.
 *
 * Rock, Scissors, Paper
 */

fun main(args: Array<String>) {
    RockScissorsPaper().run()
}

class RockScissorsPaper() {
    val random = Random()
    val screenWidth = 640
    val screenHeight = 480
    val cellDim = 2
    val width = screenWidth / cellDim
    val height = screenHeight / cellDim
    val saveImage = false
    val printConvergenceStats = true

    var canvas: BufferedImage = BufferedImage(screenWidth, screenHeight, BufferedImage.TYPE_INT_ARGB)
    var graphics = canvas.createGraphics()

    var fg = array2d(width, height, { Pokemon(hp = 10, type = Type.NONE) })

    val palette = mapOf(
            Pair(Type.NONE, Color(0x000000).rgb),
            Pair(Type.ROCK, Color(0x086FFF).rgb),
            Pair(Type.SCISSORS, Color(0xFF6608).rgb),
            Pair(Type.PAPER, Color(0x1f962b).rgb)
    )
    val population = mapOf(
            Pair(Type.NONE, AtomicInteger()),
            Pair(Type.ROCK, AtomicInteger()),
            Pair(Type.SCISSORS, AtomicInteger()),
            Pair(Type.PAPER, AtomicInteger())
    )

    fun run() {
        randomize()

        val frame = JFrame()
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)
        frame.setSize(screenWidth, screenHeight)
        frame.setVisible(true)

        var i = 0
        val panel = object: JPanel() {
            override fun paintComponent(g: Graphics) {
                super.paintComponent(g)
                step()
                countPopulation(i)
                draw()
                g.drawImage(canvas, 0, 0, screenWidth, screenHeight, this)

                if (saveImage) {
                    ImageIO.write(canvas, "png", File("rock_scissors_paper_${cellDim}x${cellDim}_${i}.png"))
                }
                i++
            }
        }
        frame.add(panel)
        panel.revalidate()

        while (true) {
            panel.repaint()
        }
    }

    private fun step() {
        (0.. width - 1).forEach { x ->
            (0.. height - 1).forEach { y ->
                // vertical/horizontal
                if (x > 0) {
                    battle(fg[x - 1][y], fg[x][y])
                }
                if (x < width - 1) {
                     battle(fg[x + 1][y], fg[x][y])
                }
                if (y > 0) {
                    battle(fg[x][y - 1], fg[x][y])
                }
                if (y < height - 1) {
                    battle(fg[x][y + 1], fg[x][y])
                }
                // diagonals
                if (x > 0 && y > 0) {
                    battle(fg[x - 1][y - 1], fg[x][y])
                }
                if (x < width - 1 && y > 0) {
                    battle(fg[x + 1][y - 1], fg[x][y])
                }
                if (x > 0 && y < height - 1) {
                    battle(fg[x - 1][y + 1], fg[x][y])
                }
                if (x < width - 1 && y < height - 1) {
                    battle(fg[x + 1][y + 1], fg[x][y])
                }
            }
        }
    }

    private fun battle(defender: Pokemon, attacker: Pokemon) {
        if (defender.type == Type.NONE && attacker.type == Type.NONE) { return }

        if (defender.type == Type.SCISSORS && attacker.type == Type.ROCK) { defender.hp-- }
        if (defender.type == Type.ROCK && attacker.type == Type.PAPER) { defender.hp--  }
        if (defender.type == Type.PAPER && attacker.type == Type.SCISSORS) { defender.hp--  }

        if (defender.hp <= 0 || defender.type == Type.NONE) {
            defender.hp = 10
            defender.type = attacker.type
        }
    }

    private fun countPopulation(i: Int) {
        population[Type.SCISSORS]!!.set(0)
        population[Type.ROCK]!!.set(0)
        population[Type.PAPER]!!.set(0)

        (0.. width - 1).forEach { x ->
            (0..height - 1).forEach { y ->
                population[fg[x][y]!!.type]!!.incrementAndGet()
            }
        }

        if (printConvergenceStats) {
            val total = width * height
            println("$i, " +
                    "${population[Type.SCISSORS]!!.get().toFloat() / total}, " +
                    "${population[Type.ROCK]!!.get().toFloat() / total}, " +
                    "${population[Type.PAPER]!!.get().toFloat() / total}")
        }
    }

    private fun draw() {
        (0.. width - 1).forEach { x ->
            (0.. height - 1).forEach { y ->
                if (cellDim == 1) {
                    canvas.setRGB(x * cellDim, y * cellDim, palette[fg[x][y].type]!!)
                } else {
                    drawRectangle(x * cellDim, y * cellDim, palette[fg[x][y].type]!!)
                }
            }
        }
    }

    private fun  drawRectangle(startX: Int, startY: Int, rgb: Int) {
        (0.. cellDim - 1).forEach { x ->
            (0.. cellDim - 1).forEach { y ->
                canvas.setRGB(startX + x, startY + y, rgb)
            }
        }
    }

    private fun randomize() {
        (0.. width - 1).forEach { x ->
            (0.. height - 1).forEach { y ->
                fg[x][y].type = generateType()
            }
        }
    }

    private fun generateType() = Type.values()[random.nextInt(Type.values().size)]

    data class Pokemon(var hp: Int = 20,
                       var type: Type = Type.NONE)

    enum class Type {
        SCISSORS,
        ROCK,
        PAPER,
        NONE
    }
}
