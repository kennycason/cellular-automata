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
 * Created by kenny on 12/4/16
 *
 * Any live cell with fewer than two live neighbours dies, as if caused by under-population.
 * Any live cell with two or three live neighbours lives on to the next generation.
 * Any live cell with more than three live neighbours dies, as if by over-population.
 * Any dead cell with exactly three live neighbours becomes a live cell, as if by reproduction.
 */

fun main(args: Array<String>) {
    JonConwaysGameOfLife().run()
}

class JonConwaysGameOfLife() {
    val random = Random()
    val screenWidth = 1640
    val screenHeight = 1480
    val cellDim = 4
    val width = screenWidth / cellDim
    val height = screenHeight / cellDim
    val saveImage = false
    val probabilityOfLife = 0.55

    var canvas: BufferedImage = BufferedImage(screenWidth, screenHeight, BufferedImage.TYPE_INT_ARGB)
    var graphics = canvas.createGraphics()

    var fg = array2d(width, height, { false })
    var bg = array2d(width, height, { false })

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
                draw()
                g.drawImage(canvas, 0, 0, screenWidth, screenHeight, this)

                if (saveImage) {
                    ImageIO.write(canvas, "png", File("/tmp/game_of_life_${cellDim}x${cellDim}_${i}.png"))
                }
                i++
                val tmp = fg
                fg = bg
                bg = tmp
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
                val count = countNeighbors(x, y)

                if (fg[x][y]) { // is alive
                    if (count < 2) { // die due to under population
                        bg[x][y] = false
                    } else if (count > 3) { // die due to over population
                        bg[x][y] = false
                    } else { // live to next generation
                        bg[x][y] = true
                    }
                } else { // is dead
                    if (count == 3) { // come to life, reproduction
                        bg[x][y] = true
                    } else { // remain dead
                        bg[x][y] = false
                    }
                }
            }
        }
    }

    private fun countNeighbors(x: Int, y: Int): Int {
        var i = 0
        // vertical/horizontal
        if (x > 0) {
            if (fg[x - 1][y]) { i++ }
        } else {
            if (fg[width - 1][y]) { i++ }
        }

        if (x < width - 1) {
            if (fg[x + 1][y]) { i++ }
        } else {
            if (fg[0][y]) { i++ }
        }

        if (y > 0) {
            if (fg[x][y - 1]) { i++ }
        } else {
            if (fg[x][height - 1]) { i++ }
        }

        if (y < height - 1) {
            if (fg[x][y + 1]) { i++ }
        } else {
            if (fg[x][0]) { i++ }
        }

        // diagonals
        if (x > 0 && y > 0) {
            if (fg[x - 1][y - 1]) { i++ }
        } else {
            if (fg[width - 1][height - 1]) { i++ }
        }

        if (x < width - 1 && y > 0) {
            if (fg[x + 1][y - 1]) { i++ }
        } else {
            if (fg[0][height - 1]) { i++ }
        }

        if (x > 0 && y < height - 1) {
            if (fg[x - 1][y + 1]) { i++ }
        } else {
            if (fg[width - 1][0]) { i++ }
        }

        if (x < width - 1 && y < height - 1) {
            if (fg[x + 1][y + 1]) { i++ }
        } else {
            if (fg[0][0]) { i++ }
        }
        return i
    }

    private fun draw() {
        (0.. width - 1).forEach { x ->
            (0.. height - 1).forEach { y ->
                if (cellDim == 1) {
                    canvas.setRGB(x * cellDim, y * cellDim, if (fg[x][y]) { Color.WHITE.rgb } else { Color.BLACK.rgb })
                } else {
                    drawRectangle(x * cellDim, y * cellDim, if (fg[x][y]) { Color.WHITE.rgb } else { Color.BLACK.rgb })
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
                fg[x][y] = random.nextDouble() > probabilityOfLife
            }
        }
    }

}
