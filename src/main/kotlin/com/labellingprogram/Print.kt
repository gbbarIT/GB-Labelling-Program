package com.labellingprogram

import javafx.application.Platform
import javafx.collections.ObservableSet
import javafx.embed.swing.SwingFXUtils
import javafx.print.Printer
import javafx.scene.SnapshotParameters
import javafx.scene.control.Alert
import javafx.scene.control.ChoiceDialog
import javafx.scene.image.WritableImage
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import javafx.scene.transform.Scale
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory
import java.awt.Desktop
import java.awt.image.BufferedImage
import java.io.File
import java.util.*

object Print {
    fun scanForPrinters(): ObservableSet<Printer>? {
        return Printer.getAllPrinters()
    }

    fun promptUserToSelectPrinter(printers: ObservableSet<Printer>?): Printer? {
        val printerNames = printers?.map { it.name } ?: emptyList()

        val dialog = ChoiceDialog(printerNames.firstOrNull(), printerNames)
        dialog.title = "Printer Selection"
        dialog.headerText = "Select a printer"
        dialog.contentText = "Available printers:"

        val result = dialog.showAndWait()
        return result.flatMap { selectedPrinterName ->
            printers?.find { it.name == selectedPrinterName }?.let { Optional.of(it) } ?: Optional.empty()
        }.orElse(null)
    }

    fun printPaneAsPDF(pane: Pane, widthInches: Double, heightInches: Double, dpi: Int) {
        Platform.runLater {
            val tempFile = File.createTempFile("label_", ".pdf")
            savePaneAsPDF(pane, tempFile, widthInches, heightInches, dpi)
            openPDFInDefaultViewer(
                tempFile
            )
        }
    }

    fun saveMultiplePanes(
        pane: Pane,
        document: PDDocument,
        widthInches: Double,
        heightInches: Double,
        dpi: Int
    ) {

        val imageWidth = (widthInches * dpi).toInt()
        val imageHeight = (heightInches * dpi).toInt()

        val scaleX = imageWidth / pane.width
        val scaleY = imageHeight / pane.height

        val snapshotParams = SnapshotParameters()
        snapshotParams.transform = Scale(scaleX, scaleY)
        snapshotParams.fill = Color.TRANSPARENT

        val writableImage = WritableImage(imageWidth, imageHeight)
        val snapshot = pane.snapshot(snapshotParams, writableImage)
        var bufferedImage = SwingFXUtils.fromFXImage(snapshot, null)

        val widthInPoints = widthInches * 72
        val heightInPoints = heightInches * 72

        val pageSize = PDRectangle(widthInPoints.toFloat(), heightInPoints.toFloat())
        val page = PDPage(pageSize)
        document.addPage(page)

        val pdImage = LosslessFactory.createFromImage(document, bufferedImage)
        PDPageContentStream(document, page).use { contentStream ->
            contentStream.drawImage(pdImage, 0f, 0f, widthInPoints.toFloat(), heightInPoints.toFloat())
        }
    }

    fun savePaneAsPDF(pane: Pane, outputFile: File, widthInches: Double, heightInches: Double, dpi: Int) {

        val imageWidth = (widthInches * dpi).toInt()
        val imageHeight = (heightInches * dpi).toInt()

        val scaleX = imageWidth / pane.width
        val scaleY = imageHeight / pane.height

        val snapshotParams = SnapshotParameters()
        snapshotParams.transform = Scale(scaleX, scaleY)
        snapshotParams.fill = Color.TRANSPARENT

        val writableImage = WritableImage(imageWidth, imageHeight)

        val snapshot = pane.snapshot(snapshotParams, writableImage)
        var bufferedImage = SwingFXUtils.fromFXImage(snapshot, null)


      //  bufferedImage = convertToGrayscale(bufferedImage)

        val widthInPoints = widthInches * 72
        val heightInPoints = heightInches * 72

        PDDocument().use { document ->
            val pageSize = PDRectangle(widthInPoints.toFloat(), heightInPoints.toFloat())
            val page = PDPage(pageSize)
            document.addPage(page)
            val pdImage = LosslessFactory.createFromImage(document, bufferedImage)
            PDPageContentStream(document, page).use { contentStream ->
                contentStream.drawImage(pdImage, 0f, 0f, widthInPoints.toFloat(), heightInPoints.toFloat())
            }
            document.save(outputFile)
        }
    }

    private fun convertToGrayscale(bufferedImage: BufferedImage): BufferedImage {
        val width = bufferedImage.width
        val height = bufferedImage.height
        val grayscaleImage = BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY)
        val graphics = grayscaleImage.createGraphics()
        graphics.drawImage(bufferedImage, 0, 0, null)
        graphics.dispose()
        return grayscaleImage
    }

    fun openPDFInDefaultViewer(pdfFile: File) {
        Platform.runLater {
            try {
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(pdfFile)
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Desktop is not supported on this system.")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to open PDF: ${e.message}")
            }
        }
    }

    private fun showAlert(alertType: Alert.AlertType, title: String, content: String) {
        val alert = Alert(alertType)
        alert.title = title
        alert.headerText = null
        alert.contentText = content
        alert.showAndWait()
    }

    fun addImageToPDFDocument(
        image: WritableImage,
        document: PDDocument,
        widthInches: Double,
        heightInches: Double,
        dpi: Int
    ) {
        val bufferedImage = SwingFXUtils.fromFXImage(image, null)

        val widthInPoints = widthInches * 72
        val heightInPoints = heightInches * 72

        val pageSize = PDRectangle(widthInPoints.toFloat(), heightInPoints.toFloat())
        val page = PDPage(pageSize)
        document.addPage(page)

        val pdImage = LosslessFactory.createFromImage(document, bufferedImage)
        PDPageContentStream(document, page).use { contentStream ->
            contentStream.drawImage(pdImage, 0f, 0f, widthInPoints.toFloat(), heightInPoints.toFloat())
        }
    }

}
