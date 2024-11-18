package com.labellingprogram

import javafx.application.Platform
import javafx.concurrent.Task
import javafx.fxml.FXML
import javafx.geometry.Side
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.stage.FileChooser
import org.apache.pdfbox.pdmodel.PDDocument
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import java.util.concurrent.CountDownLatch
import javax.swing.JOptionPane

class MainController {




    @FXML
    lateinit var customTab: Tab
    lateinit var unmarkAllBtn: Button
    lateinit var clearListBtn: Button
    lateinit var selectAllHarvesterRadio: RadioButton
    lateinit var selectAllChainsawRadio: RadioButton
    lateinit var tabSideNav: TabPane
    lateinit var excelTab: TabPane

    lateinit var chSleeveCustomerPartNumber: TextField
    lateinit var chSleeveNotes: TextArea
    lateinit var chSleeveProductModel: TextField
    lateinit var chSleevePartNumber: TextField
    lateinit var chSleeveCm: TextField
    lateinit var chSleeveXref: TextField
    lateinit var chSleeveDL: TextField
    lateinit var chSleevePitch: TextField
    lateinit var chSleeveMm: TextField
    lateinit var chSleeveGauge: TextField
    lateinit var chSleeveMountType: TextField
    lateinit var chSleeveLotNr: TextField
    lateinit var chSleeveInch: TextField
    lateinit var chSleeveFits: TextField
    lateinit var chSleeveCustomerDetails: TextArea
    lateinit var chSleeveBarcodeImageView: ImageView
    lateinit var chBottomBoxProductModel: TextField
    lateinit var chSleeveImageView: ImageView

    lateinit var chBoxProductModel1: TextField
    lateinit var chBoxPartNumber: TextField
    lateinit var chBoxGW: TextField
    lateinit var chBoxCm: TextField
    lateinit var chBoxXref: TextField
    lateinit var chBoxNW: TextField
    lateinit var chBoxDL: TextField
    lateinit var chBoxPitch: TextField
    lateinit var chBoxMm: TextField
    lateinit var chBoxGauge: TextField
    lateinit var chBoxMountType: TextField
    lateinit var chBoxLotNr: TextField
    lateinit var chBoxInch: TextField
    lateinit var chBlockchainFits: TextField
    lateinit var chBoxCustomerDetails: TextArea
    lateinit var chBoxCustomerPartNumber: TextField
    lateinit var chBoxProductModel2: TextField
    lateinit var chBoxLot: TextField
    lateinit var chBoxBarcodeImageView: ImageView
    lateinit var chBoxImageView: ImageView
    lateinit var chainBoxPane: Pane
    lateinit var chainSleevePane: Pane
    lateinit var labelTabPane: TabPane
    lateinit var sleeveBodyCustomerDetail: TextArea
    lateinit var sleeveTopProductModel: TextField
    lateinit var sleeveTopPartNumber: TextField
    lateinit var sleeveTopChainPitch: TextField
    lateinit var sleeveTopMountGaugeMm: TextField
    lateinit var sleeveTopMountGauge: TextField
    lateinit var sleeveTopBarDimCmTextField: TextField
    lateinit var sleeveTopBarDimInTextField: TextField
    lateinit var sleeveMountWidth: TextField
    lateinit var sleeveTopMountType: TextField
    lateinit var sleeveFits: TextField
    lateinit var sleeveXref: TextField
    lateinit var sleeveCustomerPartNumber: TextField
    lateinit var sleeveLotNrTextField: TextField
    lateinit var sleeveBarcodeImage: ImageView
    lateinit var sleeveImageView: ImageView
    lateinit var sleevePane: Pane
    lateinit var printBtn: Button
    lateinit var saveBtn: Button
    lateinit var importBtn: Button
    lateinit var bottomPartNumber: TextField
    lateinit var topChainPitch: TextField
    lateinit var topMountGauge: TextField
    lateinit var topMountGaugeMm: TextField
    lateinit var topBarDimCmTextField: TextField
    lateinit var topBarDimInTextField: TextField
    lateinit var bodyLotNr: TextField
    lateinit var barcodeImageView: ImageView
    lateinit var bottomLotNr1TextField: TextField
    lateinit var bottomGweightTextField: TextField
    lateinit var bottomNweightTextField: TextField
    lateinit var bodyProductModel: TextField
    lateinit var bodyCustomerPartNumber: TextField
    lateinit var bodyXref: TextField
    lateinit var bodyFits: TextField
    lateinit var topMountType: TextField
    lateinit var topMountWidth: TextField
    lateinit var topProductModel: TextField
    lateinit var bodyCustomerDetail: TextArea
    lateinit var topPartNumber: TextField
    lateinit var imageImageView: ImageView
    lateinit var stickerPane: Pane

    private var allExcelData = mutableListOf<Pair<String, Map<String, List<String>>>>()


    lateinit var partsTab: Tab
    lateinit var chainBoxTab: Tab
    lateinit var chainSleeveTab: Tab
    lateinit var boxTab: Tab
    lateinit var sleeveTab: Tab

    private var selectedSheetIndex = 0
    private var selectedRowIndex = 0

    data class ListItem(val label: String, var isSelected: Boolean = false)
    data class SelectedItem(val label: Label, val checkBox: CheckBox, val sheetIndex: Int, val rowIndex: Int)

    private val selectedItems = mutableListOf<SelectedItem>()

    lateinit var addCustomFieldBtn: Button
    private lateinit var saveCustomBtn: Button
    private lateinit var exportCustomBtn: Button
    private lateinit var customListView: ListView<HBox>

    private val customFields = ArrayList<CustomField>()

    data class CustomField(val key: String, var value: String)

    fun initialize() {

        if (!::customListView.isInitialized) {
            setupCustomTab()
        }

        populateCustomFields()

        importBtn.setOnAction {
            excelParser {
                println("File imported and data processed")
            }
        }

        printBtn.setOnAction {
            printLabel()
        }

        saveBtn.setOnMouseClicked {
            saveSelectedItemsAsBulkPDF()
        }

        val importedFilePaths = getImportedFilePaths()
        if (importedFilePaths.isNotEmpty()) {
            val parser = ExcelParser()

            val loadingDialog = Alert(Alert.AlertType.INFORMATION)
            loadingDialog.title = "Loading Labels"
            loadingDialog.headerText = "Please wait, loading previous labels..."
            val loadingLabel = Label("Loaded 0 items")
            loadingDialog.dialogPane.content = VBox(loadingLabel)
            loadingDialog.show()

            val loadTask = object : Task<Void>() {
                override fun call(): Void? {
                    importedFilePaths.forEachIndexed { index, filePath ->
                        val file = File(filePath)


                        if (file.exists()) {
                            val parsedData = parser.parseExcelFileMultipleSheets(file)
                            Platform.runLater {
                                allExcelData.addAll(parsedData)
                                createNewTabForFile(file.name, parsedData)

                                updateMessage("Loaded ${index + 1} of ${importedFilePaths.size} files")
                            }
                        } else {
                            removeImportedFilePath(filePath)
                        }
                    }
                    return null
                }
            }

            loadingLabel.textProperty().bind(loadTask.messageProperty())

            loadTask.setOnSucceeded {
                loadingDialog.close()
            }

            Thread(loadTask).start()
        }
    }

    private val configFilePath = "config.properties"

    private fun saveLastAccessedFolder(folderPath: String) {
        val properties = Properties()
        if (Files.exists(Paths.get(configFilePath))) {
            properties.load(Files.newInputStream(Paths.get(configFilePath)))
        }
        properties["lastAccessedFolder"] = folderPath
        properties.store(Files.newOutputStream(Paths.get(configFilePath)), null)
    }

    private fun getLastAccessedFolder(): String {
        val properties = Properties()
        if (Files.exists(Paths.get(configFilePath))) {
            properties.load(Files.newInputStream(Paths.get(configFilePath)))
            return properties.getProperty("lastAccessedFolder", "")
        }
        return ""
    }

    private val importedFilesKey = "importedFiles"

    private fun saveImportedFilePaths(filePaths: List<String>) {
        val properties = Properties()
        if (Files.exists(Paths.get(configFilePath))) {
            properties.load(Files.newInputStream(Paths.get(configFilePath)))
        }
        properties[importedFilesKey] = filePaths.joinToString(",")
        properties.store(Files.newOutputStream(Paths.get(configFilePath)), null)
    }

    private fun getImportedFilePaths(): List<String> {
        val properties = Properties()
        if (Files.exists(Paths.get(configFilePath))) {
            properties.load(Files.newInputStream(Paths.get(configFilePath)))
            val filePaths = properties.getProperty(importedFilesKey, "")
            if (filePaths.isNotEmpty()) {
                return filePaths.split(",").map { it.trim() }
            }
        }
        return emptyList()
    }

    private fun excelParser(onLoaded: () -> Unit) {
        val fileChooser = FileChooser()

        val lastAccessedFolder = getLastAccessedFolder()
        if (lastAccessedFolder.isNotEmpty()) {
            fileChooser.initialDirectory = File(lastAccessedFolder)
        }

        fileChooser.extensionFilters.add(FileChooser.ExtensionFilter("Excel Files", "*.xlsx"))
        val selectedFiles = fileChooser.showOpenMultipleDialog(null)

        if (selectedFiles != null && selectedFiles.isNotEmpty()) {
            saveLastAccessedFolder(selectedFiles.first().parentFile.absolutePath)

            val importedFilePaths = getImportedFilePaths()
            val selectedFilePaths = selectedFiles.map { it.absolutePath }
            val duplicateFiles = selectedFilePaths.filter { it in importedFilePaths }

            if (duplicateFiles.isNotEmpty()) {
                val duplicateFilesString = duplicateFiles.joinToString("\n")
                val alert = Alert(Alert.AlertType.CONFIRMATION)
                alert.title = "Duplicate Files"
                alert.headerText = "The following files are already imported:\n$duplicateFilesString"
                alert.contentText = "Would you like to proceed with adding these files anyway?"

                val result = alert.showAndWait()
                if (result.get() != ButtonType.OK) {
                    return
                }

            }

            saveImportedFilePaths(selectedFilePaths)

            val parser = ExcelParser()

            val loadingDialog = Alert(Alert.AlertType.INFORMATION)
            loadingDialog.title = "Loading Labels"
            loadingDialog.headerText = "Please wait, loading and merging labels..."
            val loadingLabel = Label("Loaded 0 items")
            loadingDialog.dialogPane.content = VBox(loadingLabel)
            loadingDialog.show()

            val latch = CountDownLatch(selectedFiles.size)

            val loadTask = object : Task<Void>() {
                override fun call(): Void? {
                    selectedFiles.forEachIndexed { index, file ->
                        val parsedData = parser.parseExcelFileMultipleSheets(file)
                        Platform.runLater {
                            allExcelData.addAll(parsedData)
                            createNewTabForFile(file.name, parsedData)  // Create a new tab for each file
                            updateMessage("Loaded ${index + 1} of ${selectedFiles.size} files")
                        }
                        latch.countDown()
                    }

                    return null
                }
            }

            loadingLabel.textProperty().bind(loadTask.messageProperty())

            loadTask.setOnSucceeded {
                latch.await()
                loadingDialog.close()
                onLoaded()
            }

            Thread(loadTask).start()
        }
    }


    private fun removeImportedFilePath(fileName: String) {
        val properties = Properties()
        if (Files.exists(Paths.get(configFilePath))) {
            properties.load(Files.newInputStream(Paths.get(configFilePath)))

            val filePaths = properties.getProperty(importedFilesKey, "").split(",").map { it.trim() }.toMutableList()
            val filePathToRemove = filePaths.find { it.endsWith(fileName) }
            filePaths.remove(filePathToRemove)

            properties[importedFilesKey] = filePaths.joinToString(",")
            properties.store(Files.newOutputStream(Paths.get(configFilePath)), null)
        }
    }


    private fun createNewTabForFile(fileName: String, data: List<Pair<String, Map<String, List<String>>>>) {
        val newTab = Tab(fileName)
        val innerTabPane = TabPane()
        innerTabPane.side = Side.RIGHT

        innerTabPane.tabs.forEach { tab ->
            tab.isClosable = false
        }

        displayAllListView(data, innerTabPane)

        newTab.content = innerTabPane
        excelTab.tabs.add(newTab)
    }


    private fun displayAllListView(data: List<Pair<String, Map<String, List<String>>>>, tabPane: TabPane) {
        tabPane.tabs.clear()

        data.forEachIndexed { sheetIndex, (sheetName, sheetData) ->
            val numRows = sheetData.values.firstOrNull()?.size ?: 0
            val labelTypesRaw = sheetData["Label Type"] ?: List(numRows) { "NOSES & SPROCKETS" }
            val labelTypes = labelTypesRaw.map { if (it.isBlank()) "NOSES & SPROCKETS" else it }
            val distinctLabelTypes = labelTypes.distinct()

            distinctLabelTypes.forEach { labelType ->
                val tab = Tab(labelType)
                tab.isClosable = false
                val vbox = VBox(10.0)

                val headerCheckBox = CheckBox("Select/Deselect All")
                headerCheckBox.style = "-fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 10px;"
                val listView = ListView<HBox>()
                VBox.setVgrow(listView, Priority.ALWAYS)

                vbox.children.addAll(headerCheckBox, listView)
                tab.content = vbox

                tabPane.tabs.add(tab)

                val filteredIndexes = labelTypes.indices.filter { labelTypes[it] == labelType }
                val rowCheckBoxes = mutableListOf<CheckBox>()

                filteredIndexes.forEach { rowIndex ->
                    val checkBox = CheckBox()
                    addRowToListView(sheetIndex, sheetData, rowIndex, checkBox, listView)
                    rowCheckBoxes.add(checkBox)
                }

                headerCheckBox.selectedProperty().addListener { _, _, isSelected ->
                    rowCheckBoxes.forEach { checkBox ->
                        checkBox.isSelected = isSelected
                        unmarkAllBtn.setOnAction {
                            checkBox.isSelected = false
                            toggleAllCheckboxes(false)
                            headerCheckBox.isSelected = false
                        }
                    }
                }
                listView.selectionModel.selectedItemProperty().addListener { _, _, selectedItem ->
                    (selectedItem?.children?.getOrNull(1) as? Label)?.let { selectedLabel ->
                        val selectedItemObj = selectedItems.find { it.label == selectedLabel } ?: return@addListener
                        selectedSheetIndex = selectedItemObj.sheetIndex
                        selectedRowIndex = selectedItemObj.rowIndex
                        populateTextFieldsWithSelectedItem(selectedRowIndex, data[selectedSheetIndex].second)
                    }
                }

                selectAllHarvesterRadio.selectedProperty().addListener { _, _, isSelected ->
                    tabPane.tabs.forEach { tab ->
                        if (tab.text.equals("Harvester Bar", ignoreCase = true)) { // Case-insensitive check
                            val vbox = tab.content as? VBox
                            val listView = vbox?.children?.filterIsInstance<ListView<HBox>>()?.firstOrNull()
                            val rowCheckBoxes = listView?.items?.mapNotNull { item ->
                                item.children.filterIsInstance<CheckBox>().firstOrNull()
                            } ?: listOf()
                            rowCheckBoxes.forEach { checkBox ->
                                checkBox.isSelected = isSelected
                            }
                        }
                    }
                }

                selectAllChainsawRadio.selectedProperty().addListener { _, _, isSelected ->
                    tabPane.tabs.forEach { tab ->
                        if (tab.text.equals("Chainsaw Bar", ignoreCase = true)) { // Case-insensitive check
                            val vbox = tab.content as? VBox
                            val listView = vbox?.children?.filterIsInstance<ListView<HBox>>()?.firstOrNull()
                            val rowCheckBoxes = listView?.items?.mapNotNull { item ->
                                item.children.filterIsInstance<CheckBox>().firstOrNull()
                            } ?: listOf()
                            rowCheckBoxes.forEach { checkBox ->
                                checkBox.isSelected = isSelected
                            }
                        }
                    }
                }

                clearListBtn.setOnAction {
                    val alert = Alert(Alert.AlertType.CONFIRMATION)
                    alert.title = "Confirm Clear Import List"
                    alert.headerText = "Are you sure you want to remove all imported documents from the list?"

                    val result = alert.showAndWait()
                    if (result.get() == ButtonType.OK) {
                        excelTab.tabs.filter { it.text != "Custom" }.forEach { tab ->
                            excelTab.tabs.remove(tab)
                        }
                        allExcelData.clear()
                        selectedItems.clear()
                        selectedSheetIndex = -1
                        selectedRowIndex = -1
                    }
                }
            }
        }
    }

    private fun addRowToListView(
        sheetIndex: Int,
        sheetData: Map<String, List<String>>,
        rowIndex: Int,
        checkBox: CheckBox,
        listView: ListView<HBox>
    ) {
        val customer = sheetData["Customer"]?.getOrNull(rowIndex) ?: ""
        val partNo = sheetData["Part Number"]?.getOrNull(rowIndex) ?: ""
        val productQty = sheetData["Order Quantity"]?.getOrNull(rowIndex) ?: ""

        val listItemText = Label("Customer: $customer\nPart Number: $partNo\nLabel Quantity: $productQty").apply {
            style = "-fx-font-size: 14px; -fx-padding: 5px; -fx-font-weight: bold;"
        }

        val hBox = HBox(10.0, checkBox, listItemText)
        listView.items.add(hBox)

        val selectedItem = SelectedItem(listItemText, checkBox, sheetIndex, rowIndex)
        selectedItems.add(selectedItem)
    }


    private fun toggleAllCheckboxes(selectAll: Boolean) {
        selectedItems.forEach { (_, checkBox) ->
            checkBox.isSelected = selectAll
        }
    }


    private fun populateTextFieldsWithSelectedItem(rowIndex: Int, selectedSheetData: Map<String, List<String>>) {

        val productModel = selectedSheetData["Product Model"]?.getOrNull(rowIndex) ?: ""
        val partNumber = selectedSheetData["Part Number"]?.getOrNull(rowIndex) ?: ""
        val pitch = selectedSheetData["Pitch"]?.getOrNull(rowIndex) ?: ""
        val mountWidth = selectedSheetData["Mount Width"]?.getOrNull(rowIndex) ?: ""
        val mount = selectedSheetData["Mount"]?.getOrNull(rowIndex) ?: ""
        val cm = selectedSheetData["cm"]?.getOrNull(rowIndex) ?: ""
        val inch = selectedSheetData["Inch"]?.getOrNull(rowIndex) ?: ""
        val gauge = selectedSheetData["Gauge"]?.getOrNull(rowIndex) ?: ""
        val mm = selectedSheetData["mm"]?.getOrNull(rowIndex) ?: ""
        val xref = selectedSheetData["Xref"]?.getOrNull(rowIndex) ?: ""
        val fits = selectedSheetData["Fits"]?.getOrNull(rowIndex) ?: ""
        val lotNumber = selectedSheetData["Lot Number"]?.getOrNull(rowIndex) ?: ""
        val stickerDetail = selectedSheetData["Sticker Detai"]?.getOrNull(rowIndex) ?: ""
        val customerPartNumber = selectedSheetData["Customer Part Number"]?.getOrNull(rowIndex) ?: ""
        var dl = selectedSheetData["DL"]?.getOrNull(rowIndex) ?: ""
        val netWeight = selectedSheetData["Net Weight"]?.getOrNull(rowIndex) ?: ""
        val grossWeight = selectedSheetData["Gross Weight"]?.getOrNull(rowIndex) ?: ""

        val barcodeSleeve = selectedSheetData["Barcode"]?.getOrNull(rowIndex) ?: ""
        val barcodePackage = selectedSheetData["Barcode Package"]?.getOrNull(rowIndex) ?: ""

        val barcodeGenerator = BarcodeGenerator()

        var barcodeBoxImage: Image? = null
        var barcodeSleeveImage: Image? = null

        if (barcodePackage.isNotBlank()) {
            try {
                barcodeBoxImage = barcodeGenerator.generateEAN13Barcode(barcodePackage)
            } catch (e: Exception) {
                println("Error generating box barcode: ${e.message}")
            }
        }


        if (dl.endsWith(".0")) {
            dl = dl.dropLast(2)
        }

        if (barcodeSleeve.isNotBlank()) {
            try {
                barcodeSleeveImage = barcodeGenerator.generateEAN13Barcode(barcodeSleeve)
            } catch (e: Exception) {
                println("Error generating sleeve barcode: ${e.message}")
            }
        }

        barcodeImageView.image = barcodeBoxImage


        topProductModel.text = productModel
        topPartNumber.text = partNumber
        topChainPitch.text = pitch
        topMountWidth.text = mountWidth
        topMountType.text = mount
        topBarDimCmTextField.text = cm
        topBarDimInTextField.text = inch
        topMountGauge.text = gauge
        topMountGaugeMm.text = mm
        bodyXref.text = xref
        bodyFits.text = fits
        bodyLotNr.text = lotNumber
        bodyCustomerDetail.text = stickerDetail
        bodyCustomerPartNumber.text = customerPartNumber
        bodyProductModel.text = productModel
        bottomNweightTextField.text = netWeight
        bottomGweightTextField.text = grossWeight
        bottomLotNr1TextField.text = lotNumber
        bottomPartNumber.text = partNumber

        // Sleeve fields
        sleeveBodyCustomerDetail.text = stickerDetail
        sleeveTopProductModel.text = productModel
        sleeveTopPartNumber.text = partNumber
        sleeveTopChainPitch.text = pitch
        sleeveTopMountGaugeMm.text = mm
        sleeveTopMountGauge.text = gauge
        sleeveTopBarDimCmTextField.text = cm
        sleeveTopBarDimInTextField.text = inch
        sleeveMountWidth.text = mountWidth
        sleeveTopMountType.text = mount
        sleeveFits.text = fits
        sleeveXref.text = xref
        sleeveCustomerPartNumber.text = customerPartNumber
        sleeveLotNrTextField.text = lotNumber
        sleeveBarcodeImage.image = barcodeSleeveImage

        // Chain Box Fields
        chBoxProductModel1.text = productModel
        chBoxPartNumber.text = partNumber
        chBoxGW.text = grossWeight
        chBoxCm.text = cm
        chBoxXref.text = xref
        chBoxNW.text = netWeight
        chBoxDL.text = dl
        chBoxPitch.text = pitch
        chBoxMm.text = mm
        chBoxGauge.text = gauge
        chBoxMountType.text = mount
        chBoxLotNr.text = lotNumber
        chBoxInch.text = inch
        chBlockchainFits.text = fits
        chBoxCustomerDetails.text = stickerDetail
        chBoxCustomerPartNumber.text = customerPartNumber
        chBoxProductModel2.text = productModel
        chBoxLot.text = lotNumber
        chBottomBoxProductModel.text = partNumber
        chBoxBarcodeImageView.image = barcodeBoxImage

        chSleeveProductModel.text = productModel
        chSleevePartNumber.text = partNumber
        chSleeveCm.text = cm
        chSleeveXref.text = xref
        chSleeveDL.text = dl
        chSleevePitch.text = pitch
        chSleeveMm.text = mm
        chSleeveGauge.text = gauge
        chSleeveMountType.text = mount
        chSleeveLotNr.text = lotNumber
        chSleeveInch.text = inch
        chSleeveFits.text = fits
        chSleeveCustomerDetails.text = stickerDetail
        chSleeveBarcodeImageView.image = barcodeSleeveImage
        chSleeveCustomerPartNumber.text = customerPartNumber

    }


    private fun printLabel() {
        if (allExcelData.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No data available for printing!", "Error", JOptionPane.ERROR_MESSAGE)
            return
        }

        val progressDialog = Alert(Alert.AlertType.INFORMATION)
        progressDialog.title = "Printing Labels"
        progressDialog.headerText = "Please wait, printing labels..."
        val progressBar = ProgressBar(0.0)
        val progressLabel = Label("Processing 0 of ${selectedItems.size}")
        progressDialog.dialogPane.content = VBox(10.0, progressBar, progressLabel)
        progressDialog.show()

        val task = object : Task<Void>() {
            override fun call(): Void? {
                val document =
                    PDDocument() // Open the document at the beginning and avoid using `use` here to control closing

                try {
                    val totalItems = selectedItems.count { it.checkBox.isSelected }
                    val tempFile = File.createTempFile("printed_label", ".pdf")  // Define tempFile here

                    if (totalItems == 0) {
                        val latch = CountDownLatch(1)
                        Platform.runLater {
                            val pane = getCurrentPane()
                            if (pane == null) {
                                JOptionPane.showMessageDialog(
                                    null,
                                    "No pane selected!",
                                    "Error",
                                    JOptionPane.ERROR_MESSAGE
                                )
                                latch.countDown()
                                return@runLater
                            }
                            val (widthInches, heightInches) = when {
                                boxTab.isSelected || sleeveTab.isSelected -> Pair(4.01575, 5.98425)
                                chainSleeveTab.isSelected || chainBoxTab.isSelected -> Pair(2.99213, 5.98425)
                                else -> Pair(4.01575, 5.98425)  // Default size
                            }
                            Print.saveMultiplePanes(pane, document, widthInches, heightInches, 300)

                            latch.countDown()
                        }
                        latch.await()
                    } else {
                        var processedItems = 0
                        selectedItems.forEach { selectedItem ->
                            if (selectedItem.checkBox.isSelected) {
                                val selectedSheetIndex = selectedItem.sheetIndex
                                val selectedRowIndex = selectedItem.rowIndex
                                val selectedSheetData = allExcelData[selectedSheetIndex].second

                                val latch = CountDownLatch(1)
                                Platform.runLater {
                                    populateTextFieldsWithSelectedItem(selectedRowIndex, selectedSheetData)
                                    val pane = getCurrentPane()
                                    if (pane == null) {
                                        JOptionPane.showMessageDialog(
                                            null,
                                            "No pane selected!",
                                            "Error",
                                            JOptionPane.ERROR_MESSAGE
                                        )
                                        latch.countDown()
                                        return@runLater
                                    }
                                    val (widthInches, heightInches) = when {
                                        boxTab.isSelected || sleeveTab.isSelected -> Pair(4.01575, 5.98425)
                                        chainSleeveTab.isSelected || chainBoxTab.isSelected -> Pair(2.99213, 5.98425)
                                        else -> Pair(4.01575, 5.98425)  // Default size
                                    }
                                    Print.saveMultiplePanes(pane, document, widthInches, heightInches, 300)
                                    latch.countDown()
                                }
                                latch.await()

                                processedItems++
                                updateProgress(processedItems.toLong(), totalItems.toLong())
                                updateMessage("Processing $processedItems of $totalItems")
                            }
                        }
                    }


                    document.save(tempFile)
                    Platform.runLater {
                        Print.openPDFInDefaultViewer(tempFile)  // Open temp file for user
                        progressDialog.close()
                    }
                } finally {
                    document.close()
                }
                return null
            }
        }

        progressBar.progressProperty().bind(task.progressProperty())
        progressLabel.textProperty().bind(task.messageProperty())
        Thread(task).start()
    }

    private fun saveSelectedItemsAsBulkPDF() {
        if (allExcelData.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No data available for saving!", "Error", JOptionPane.ERROR_MESSAGE)
            return
        }

        val fileChooser = FileChooser()
        fileChooser.extensionFilters.add(FileChooser.ExtensionFilter("PDF Files", "*.pdf"))
        val file = fileChooser.showSaveDialog(null) ?: return

        val progressDialog = Alert(Alert.AlertType.INFORMATION)
        progressDialog.title = "Saving Labels"
        progressDialog.headerText = "Please wait, saving labels..."
        val progressBar = ProgressBar(0.0)
        val progressLabel = Label("Processing 0 of ${selectedItems.size}")
        progressDialog.dialogPane.content = VBox(10.0, progressBar, progressLabel)
        progressDialog.show()

        val task = object : Task<Void>() {
            override fun call(): Void? {
                val document = PDDocument()

                try {
                    val totalItems = selectedItems.count { it.checkBox.isSelected }
                    val isNoItemsSelected = totalItems == 0

                    if (isNoItemsSelected) {
                        val latch = CountDownLatch(1)
                        Platform.runLater {
                            val pane = getCurrentPane()
                            if (pane == null) {
                                JOptionPane.showMessageDialog(
                                    null,
                                    "No pane selected!",
                                    "Error",
                                    JOptionPane.ERROR_MESSAGE
                                )
                                latch.countDown()
                                return@runLater
                            }
                            val (widthInches, heightInches) = when {
                                boxTab.isSelected || sleeveTab.isSelected -> Pair(4.01575, 5.98425)
                                chainSleeveTab.isSelected || chainBoxTab.isSelected -> Pair(2.99213, 5.98425)
                                else -> Pair(4.01575, 5.98425)  // Default size
                            }
                            Print.saveMultiplePanes(pane, document, widthInches, heightInches, 300)
                            latch.countDown()
                        }
                        latch.await() // Wait until UI operations complete
                    } else {
                        var processedItems = 0
                        selectedItems.forEach { selectedItem ->
                            if (selectedItem.checkBox.isSelected) {
                                val selectedSheetIndex = selectedItem.sheetIndex
                                val selectedRowIndex = selectedItem.rowIndex
                                val selectedSheetData = allExcelData[selectedSheetIndex].second

                                val latch = CountDownLatch(1)
                                Platform.runLater {
                                    populateTextFieldsWithSelectedItem(selectedRowIndex, selectedSheetData)
                                    val pane = getCurrentPane()
                                    if (pane == null) {
                                        JOptionPane.showMessageDialog(
                                            null,
                                            "No pane selected!",
                                            "Error",
                                            JOptionPane.ERROR_MESSAGE
                                        )
                                        latch.countDown()
                                        return@runLater
                                    }
                                    val (widthInches, heightInches) = when {
                                        boxTab.isSelected || sleeveTab.isSelected -> Pair(4.01575, 5.98425)
                                        chainSleeveTab.isSelected || chainBoxTab.isSelected -> Pair(2.99213, 5.98425)
                                        else -> Pair(4.01575, 5.98425)  // Default size
                                    }
                                    Print.saveMultiplePanes(pane, document, widthInches, heightInches, 300)
                                    latch.countDown()
                                }
                                latch.await()

                                processedItems++
                                updateProgress(processedItems.toLong(), totalItems.toLong())
                                updateMessage("Processing $processedItems of $totalItems")
                            }
                        }
                    }

                    document.save(file)
                    Platform.runLater {
                        progressDialog.close()
                        JOptionPane.showMessageDialog(
                            null,
                            "PDF saved successfully!",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE
                        )
                    }
                } finally {
                    document.close()
                }
                return null
            }
        }

        progressBar.progressProperty().bind(task.progressProperty())
        progressLabel.textProperty().bind(task.messageProperty())
        Thread(task).start()
    }


    private fun getCurrentPane(): Pane? {
        return when {
            boxTab.isSelected -> stickerPane
            sleeveTab.isSelected -> sleevePane
            chainSleeveTab.isSelected -> chainSleevePane
            chainBoxTab.isSelected -> chainBoxPane
            else -> null
        }
    }
    

    private fun setupCustomTab() {
        val vbox = VBox(10.0)
        vbox.style = "-fx-padding: 10;"

        addCustomFieldBtn = Button("Add Custom Field")
        saveCustomBtn = Button("Save Custom Fields")
        exportCustomBtn = Button("Export Custom Fields")
        customListView = ListView<HBox>()
        VBox.setVgrow(customListView, Priority.ALWAYS)


        vbox.children.addAll(addCustomFieldBtn, saveCustomBtn, exportCustomBtn, customListView)
        customTab.content = vbox
    }



    private fun populateCustomFields() {
        customListView.items.clear()
        val tabPane = TabPane()
        tabPane.side = Side.RIGHT

        val chainBoxFields = mutableMapOf(
            "Product Model (Chain Box)" to chBoxProductModel1,
            "Part Number (Chain Box)" to chBoxPartNumber,
            "Gross Weight (Chain Box)" to chBoxGW,
            "Cm (Chain Box)" to chBoxCm,
            "Xref (Chain Box)" to chBoxXref,
            "Net Weight (Chain Box)" to chBoxNW,
            "DL (Chain Box)" to chBoxDL,
            "Pitch (Chain Box)" to chBoxPitch,
            "Mm (Chain Box)" to chBoxMm,
            "Gauge (Chain Box)" to chBoxGauge,
            "Mount Type (Chain Box)" to chBoxMountType,
            "Lot Number (Chain Box)" to chBoxLotNr,
            "Inch (Chain Box)" to chBoxInch,
            "Fits (Chain Sleeve)" to chBlockchainFits,
            "Customer Detail (Chain Box)" to chBoxCustomerDetails,
            "Customer Part Number (Chain Box)" to chBoxCustomerPartNumber,
            "Product Model Bottom (Chain Box)" to chBoxProductModel2,
            "Part Number Bottom (Chain Box)" to chBottomBoxProductModel,
            "Lot (Chain Box)" to chBoxLot,
            "Barcode" to TextField()
        )

        val chainSleeveFields = mutableMapOf(
            "Product Model (Chain Sleeve)" to chSleeveProductModel,
            "Part Number (Chain Sleeve)" to chSleevePartNumber,
            "Cm (Chain Sleeve)" to chSleeveCm,
            "Xref (Chain Sleeve)" to chSleeveXref,
            "DL (Chain Sleeve)" to chSleeveDL,
            "Pitch (Chain Sleeve)" to chSleevePitch,
            "Mm (Chain Sleeve)" to chSleeveMm,
            "Gauge (Chain Sleeve)" to chSleeveGauge,
            "Mount Type (Chain Sleeve)" to chSleeveMountType,
            "Lot Number (Chain Sleeve)" to chSleeveLotNr,
            "Inch (Chain Sleeve)" to chSleeveInch,
            "Fits (Chain Sleeve)" to chSleeveFits,
            "Customer Detail (Chain Sleeve)" to chSleeveCustomerDetails,
            "Customer Part Number (Chain Sleeve)" to chSleeveCustomerPartNumber,
            "Barcode" to TextField()
        )

        val sleeveFields = mutableMapOf(
            "Product Model (Sleeve)" to sleeveTopProductModel,
            "Part Number (Sleeve)" to sleeveTopPartNumber,
            "Chain Pitch (Sleeve)" to sleeveTopChainPitch,
            "Mount Gauge mm (Sleeve)" to sleeveTopMountGaugeMm,
            "Mount Gauge (Sleeve)" to sleeveTopMountGauge,
            "Bar Dimension cm (Sleeve)" to sleeveTopBarDimCmTextField,
            "Bar Dimension inch (Sleeve)" to sleeveTopBarDimInTextField,
            "Mount Width (Sleeve)" to sleeveMountWidth,
            "Mount Type (Sleeve)" to sleeveTopMountType,
            "Fits (Sleeve)" to sleeveFits,
            "Xref (Sleeve)" to sleeveXref,
            "Customer Part Number (Sleeve)" to sleeveCustomerPartNumber,
            "Lot Number (Sleeve)" to sleeveLotNrTextField,
            "Customer Detail (Sleeve)" to sleeveBodyCustomerDetail,
            "Barcode" to TextField()
        )

        val otherFields = mutableMapOf(
            "Product Model (Top)" to topProductModel,
            "Part Number (Top)" to topPartNumber,
            "Chain Pitch (Top)" to topChainPitch,
            "Mount Width (Top)" to topMountWidth,
            "Mount Type (Top)" to topMountType,
            "Bar Dimension cm (Top)" to topBarDimCmTextField,
            "Bar Dimension inch (Top)" to topBarDimInTextField,
            "Mount Gauge (Top)" to topMountGauge,
            "Mount Gauge mm (Top)" to topMountGaugeMm,
            "Xref (Body)" to bodyXref,
            "Fits (Body)" to bodyFits,
            "Lot Number (Body)" to bodyLotNr,
            "Lot Number (Bottom)" to bottomLotNr1TextField,
            "Customer Detail (Body)" to bodyCustomerDetail,
            "Customer Part Number (Body)" to bodyCustomerPartNumber,
            "Product Model (Bottom)" to bodyProductModel,
            "Net Weight (Bottom)" to bottomNweightTextField,
            "Gross Weight (Bottom)" to bottomGweightTextField,
            "Part Number (Bottom) " to bottomPartNumber,
            "Barcode" to TextField()
        )

        addFieldsToTab(chainBoxFields, "Chainsaw Bar Box", tabPane)
        addFieldsToTab(chainSleeveFields, "Chainsaw Bar Sleeve", tabPane)
        addFieldsToTab(sleeveFields, "Harvester Bar Sleeve", tabPane)
        addFieldsToTab(otherFields, "Harvester Bar Box", tabPane)

        customTab.content = tabPane
    }

    private fun addFieldsToTab(fields: Map<String, Control>, tabName: String, tabPane: TabPane) {
        val tab = Tab(tabName)
        val vbox = VBox(10.0)
        vbox.style = "-fx-padding: 10;"

        fields.forEach { (key, field) ->
            val customField = customFields.find { it.key == key } ?: CustomField(key, (field as TextInputControl).text).also { customFields.add(it) }
            val valueField = TextField(customField.value)

            valueField.textProperty().addListener { _, _, newValue ->
                (field as TextInputControl).text = newValue
                customField.value = newValue

                if (key == "Barcode") {
                    updateBarcodeImages()
                }
            }

            (field as TextInputControl).textProperty().addListener { _, _, newValue ->
                valueField.text = newValue
            }

            val keyField = TextField(key).apply {
                isEditable = false
            }

            val clearButton = Button("Clear").apply {
                setOnAction {
                    valueField.text = ""
                    field.text = ""
                    customFields.find { it.key == key }?.value = ""
                }
            }

            val hbox = HBox(10.0, keyField, valueField, clearButton)
            vbox.children.add(hbox)
        }

        tab.content = vbox
        tabPane.tabs.add(tab)
    }

    private fun updateBarcodeImages() {
        val barcodeGenerator = BarcodeGenerator()
        val barcodeValue = customFields.find { it.key == "Barcode" }?.value ?: ""

        println("Updating barcode with value: $barcodeValue")

        try {
            if (barcodeValue.length == 12) {
                val barcodeImage = barcodeGenerator.generateEAN13Barcode(barcodeValue)

                chSleeveBarcodeImageView.image = barcodeImage
                sleeveBarcodeImage.image = barcodeImage
                chBoxBarcodeImageView.image = barcodeImage
                barcodeImageView.image = barcodeImage

                println("Barcode generated successfully")
            } else {
                println("Invalid Barcode Length")
            }
        } catch (e: Exception) {
            println("Error generating barcode: ${e.message}")
        }
    }
}




