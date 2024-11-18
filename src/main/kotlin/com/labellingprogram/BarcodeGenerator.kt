package com.labellingprogram

import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.WriterException
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.common.BitMatrix
import com.google.zxing.oned.EAN13Writer
import javafx.embed.swing.SwingFXUtils
import javafx.scene.image.Image
import java.awt.Color
import java.awt.Font
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.io.IOException
import java.util.Hashtable

class BarcodeGenerator {

    @Throws(WriterException::class, IOException::class)
    fun generateEAN13Barcode(barcodeText: String?): Image? {
        // Check if the barcodeText is null or empty
        if (barcodeText.isNullOrEmpty()) {
            return null // Do not generate anything if no barcode text is provided
        }

        // Ensure the length is exactly 12 or 13 digits
        if (barcodeText.length != 12 && barcodeText.length != 13) {
            throw IllegalArgumentException("EAN-13 barcode must have exactly 12 or 13 digits")
        }

        val barcodeWriter = EAN13Writer()
        val hints = Hashtable<EncodeHintType, Any>()
        hints[EncodeHintType.MARGIN] = 0

        // Create the BitMatrix for the barcode
        val bitMatrix: BitMatrix = barcodeWriter.encode(barcodeText, BarcodeFormat.EAN_13, 4000, 2000, hints)
        val barcodeImage = MatrixToImageWriter.toBufferedImage(bitMatrix)

        // Create a combined image with extra space for the barcode number text
        val totalHeight = barcodeImage.height + 300
        val totalWidth = barcodeImage.width + 600
        val combinedImage = BufferedImage(totalWidth, totalHeight, BufferedImage.TYPE_INT_ARGB)

        val graphics: Graphics2D = combinedImage.createGraphics()
        graphics.color = Color.WHITE
        graphics.fillRect(0, 0, combinedImage.width, combinedImage.height) // Background

        // Draw the barcode image
        val xOffset = (totalWidth - barcodeImage.width)
        graphics.drawImage(barcodeImage, xOffset, 0, null)

        // Draw extended bars for EAN-13 format
        graphics.color = Color.BLACK
        val extendedBarHeight = 0
        val extendedLines = listOf(0, 2, 46, 48, 92, 94)
        for (i in extendedLines) {
            val xPosition = xOffset + i * (barcodeImage.width / 3)
            graphics.drawLine(
                xPosition,
                barcodeImage.height,
                xPosition,
                barcodeImage.height + extendedBarHeight
            )
        }

        // Draw the barcode text under the barcode image
        graphics.font = Font("Monospaced", Font.BOLD, 350)
        val fontMetrics = graphics.fontMetrics
        val charWidth = fontMetrics.stringWidth("0") + 105
        for ((index, char) in barcodeText.withIndex()) {
            graphics.drawString(char.toString(), xOffset + index * charWidth, barcodeImage.height + 250)
        }

        graphics.dispose()

        return SwingFXUtils.toFXImage(combinedImage, null)
    }
}
