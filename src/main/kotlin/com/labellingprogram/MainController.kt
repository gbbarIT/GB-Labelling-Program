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
    lateinit var chainBoxPane2: Pane
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

    lateinit var chBox2PartNumber: TextField
    lateinit var chBox2LotNr: TextField
    lateinit var chBox2DL: TextField
    lateinit var barcodeImageView2: ImageView
    lateinit var chBoxCustomerDetails1: TextArea

    private var allExcelData = mutableListOf<Pair<String, Map<String, List<String>>>>()


    lateinit var partsTab: Tab
    lateinit var chainBoxTab: Tab
    lateinit var chainSleeveTab: Tab
    lateinit var boxTab: Tab
    lateinit var sleeveTab: Tab
    lateinit var chainBoxTab2: Tab

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

        // Retrieve and process previously imported Excel file paths
        val importedFilePaths = getImportedFilePaths()
        if (importedFilePaths.isNotEmpty()) {
            val parser = ExcelParser()

            // Display loading dialog to the user
            val loadingDialog = Alert(Alert.AlertType.INFORMATION)
            loadingDialog.title = "Loading Labels"
            loadingDialog.headerText = "Please wait, loading previous labels..."
            val loadingLabel = Label("Loaded 0 items")
            loadingDialog.dialogPane.content = VBox(loadingLabel)
            loadingDialog.show()

            // Task to load and parse Excel files in the background
            val loadTask = object : Task<Void>() {
                override fun call(): Void? {
                    importedFilePaths.forEachIndexed { index, filePath ->
                        val file = File(filePath)

                        if (file.exists()) {
                            val parsedData = parser.parseExcelFileMultipleSheets(file)
                            Platform.runLater {
                                allExcelData.addAll(parsedData)
                                createNewTabForFile(file.name, parsedData)

                                // Update loading dialog with progress
                                updateMessage("Loaded ${index + 1} of ${importedFilePaths.size} files")
                            }
                        } else {
                            // Remove non-existing file path from properties
                            removeImportedFilePath(filePath)
                        }
                    }
                    return null
                }
            }

            // Bind loading label text to task message
            loadingLabel.textProperty().bind(loadTask.messageProperty())

            // Close the loading dialog upon task completion
            loadTask.setOnSucceeded {
                loadingDialog.close()
            }

            // Start the loading task in a new thread
            Thread(loadTask).start()
        }
    }

    private val configFilePath = "config.properties"

    /**
     * Saves the last accessed folder path to a configuration file.
     *
     * @param folderPath The path of the last accessed folder.
     */
    private fun saveLastAccessedFolder(folderPath: String) {
        val properties = Properties()
        if (Files.exists(Paths.get(configFilePath))) {
            properties.load(Files.newInputStream(Paths.get(configFilePath)))
        }
        properties["lastAccessedFolder"] = folderPath
        properties.store(Files.newOutputStream(Paths.get(configFilePath)), null)
    }

    /**
     * Retrieves the last accessed folder path from the configuration file.
     *
     * @return The path of the last accessed folder, or an empty string if not found.
     */
    private fun getLastAccessedFolder(): String {
        val properties = Properties()
        if (Files.exists(Paths.get(configFilePath))) {
            properties.load(Files.newInputStream(Paths.get(configFilePath)))
            return properties.getProperty("lastAccessedFolder", "")
        }
        return ""
    }

    private val importedFilesKey = "importedFiles"

    /**
     * Saves the imported file paths into the configuration file.
     *
     * @param filePaths A list of file paths to be saved.
     */
    private fun saveImportedFilePaths(filePaths: List<String>) {
        val properties = Properties()
        if (Files.exists(Paths.get(configFilePath))) {
            properties.load(Files.newInputStream(Paths.get(configFilePath)))
        }
        properties[importedFilesKey] = filePaths.joinToString(",")
        properties.store(Files.newOutputStream(Paths.get(configFilePath)), null)
    }

    /**
     * Retrieves the list of imported file paths from the configuration file.
     *
     * @return A list of imported file paths, or an empty list if none found.
     */
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

    /**
     * Handles the importing of Excel files, and processes them accordingly.
     *
     * @param onLoaded A callback function to be executed once the files are loaded.
     */
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

    /**
     * Removes the specified file path from the list of imported files in the configuration file.
     *
     * @param fileName The name of the file to be removed.
     */
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

    /**
     * Creates a new tab for the specified Excel file and displays its data.
     *
     * @param fileName The name of the Excel file.
     * @param data The parsed data from the Excel file.
     */
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

    /**
     * Displays the data in a list view for each sheet in the Excel file.
     *
     * @param data The parsed data from the Excel file.
     * @param tabPane The tab pane to which the list views will be added.
     */
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

    /**
     * Adds a row to the provided ListView with the given sheet data and index.
     *
     * @param sheetIndex The index of the sheet from which data is being read.
     * @param sheetData A map containing the sheet data where the key is the column name and the value is a list of column values.
     * @param rowIndex The index of the row from which data should be read.
     * @param checkBox The CheckBox associated with the row for selection purposes.
     * @param listView The ListView to which the row will be added.
     */
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

    /**
     * Toggles the selection state of all checkboxes.
     *
     * @param selectAll If true, all checkboxes will be selected; if false, all checkboxes will be deselected.
     */
    private fun toggleAllCheckboxes(selectAll: Boolean) {
        selectedItems.forEach { (_, checkBox) ->
            checkBox.isSelected = selectAll
        }
    }


    /**
     * Populates the text fields and images with the data from the selected row in the sheet.
     *
     * @param rowIndex The index of the row in the sheet.
     * @param selectedSheetData The data from the sheet where the key is the column name and the value is a list of column values.
     */
    private fun populateTextFieldsWithSelectedItem(rowIndex: Int, selectedSheetData: Map<String, List<String>>) {
        // Extract values from the selected sheet data based on the row index
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
        val stickerDetail = selectedSheetData["Sticker Detail"]?.getOrNull(rowIndex) ?: ""
        val customerPartNumber = selectedSheetData["Customer Part Number"]?.getOrNull(rowIndex) ?: ""
        var dl = selectedSheetData["DL"]?.getOrNull(rowIndex) ?: ""
        val netWeight = selectedSheetData["Net Weight"]?.getOrNull(rowIndex) ?: ""
        val grossWeight = selectedSheetData["Gross Weight"]?.getOrNull(rowIndex) ?: ""

        // Extract barcode data
        val barcodeSleeve = selectedSheetData["Barcode"]?.getOrNull(rowIndex) ?: ""
        val barcodePackage = selectedSheetData["Barcode Package"]?.getOrNull(rowIndex) ?: ""

        // Initialize barcode generator
        val barcodeGenerator = BarcodeGenerator()

        // Variables to hold generated barcode images
        var barcodeBoxImage: Image? = null
        var barcodeSleeveImage: Image? = null

        // Generate box barcode image if the barcodePackage is not blank
        if (barcodePackage.isNotBlank()) {
            try {
                barcodeBoxImage = barcodeGenerator.generateEAN13Barcode(barcodePackage)
            } catch (e: Exception) {
                println("Error generating box barcode: ${e.message}")
            }
        }

        // Remove trailing .0 if present in dl
        if (dl.endsWith(".0")) {
            dl = dl.dropLast(2)
        }

        // Generate sleeve barcode image if the barcodeSleeve is not blank
        if (barcodeSleeve.isNotBlank()) {
            try {
                barcodeSleeveImage = barcodeGenerator.generateEAN13Barcode(barcodeSleeve)
            } catch (e: Exception) {
                println("Error generating sleeve barcode: ${e.message}")
            }
        }

        // Set barcode image on the view
        barcodeImageView.image = barcodeBoxImage

        // Populate various text fields and images in the UI with the extracted values
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

        // Populate sleeve fields
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

        // Populate chain box fields
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

        // Populate sleeve chain fields
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

        //Chain Box
        chBox2PartNumber.text = partNumber
        chBox2LotNr.text = lotNumber
        chBox2DL.text = dl
        barcodeImageView2.image = barcodeSleeveImage
        barcodeImageView2.isPreserveRatio = false
        barcodeImageView2.fitHeight = 90.0
        barcodeImageView2.fitWidth = 330.0
        chBoxCustomerDetails1.text = stickerDetail

    }


    private fun printLabel() {
        if (allExcelData.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No data available for printing!", "Error", JOptionPane.ERROR_MESSAGE)
            return
        }

        // Create and display a progress dialog
        val progressDialog = Alert(Alert.AlertType.INFORMATION)
        progressDialog.title = "Printing Labels"
        progressDialog.headerText = "Please wait, printing labels..."
        val progressBar = ProgressBar(0.0)
        val progressLabel = Label("Processing 0 of ${selectedItems.size}")
        progressDialog.dialogPane.content = VBox(10.0, progressBar, progressLabel)
        progressDialog.show()

        // Define a background task for printing labels
        val task = object : Task<Void>() {
            override fun call(): Void? {
                // Initialize a new PDF document
                val document = PDDocument()
                try {
                    // Count total items selected for printing
                    val totalItems = selectedItems.count { it.checkBox.isSelected }
                    // Create a temporary file for the PDF
                    val tempFile = File.createTempFile("printed_label", ".pdf")

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
                            // Determine the size of the labels

                            val (widthInches, heightInches) = when {
                                boxTab.isSelected || sleeveTab.isSelected -> Pair(4.01575, 5.98425)
                                chainSleeveTab.isSelected || chainBoxTab.isSelected -> Pair(2.99213, 5.98425)
                                chainBoxTab2.isSelected -> Pair(3.9, 2.9)
                                else -> Pair(4.01575, 5.98425)  // Default size
                            }
                            // Save the currently displayed pane to the PDF document
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
                                    // Populate UI with data from the selected row
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
                                    // Determine the size of the labels
                                    val (widthInches, heightInches) = when {
                                        boxTab.isSelected || sleeveTab.isSelected -> Pair(4.01575, 5.98425)
                                        chainSleeveTab.isSelected || chainBoxTab.isSelected -> Pair(2.99213, 5.98425)
                                        chainBoxTab2.isSelected -> Pair(3.9, 2.9)
                                        else -> Pair(4.01575, 5.98425)  // Default size
                                    }
                                    // Save the populated pane to the PDF document
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

                    // Save the PDF document to the temporary file
                    document.save(tempFile)
                    Platform.runLater {
                        // Open the saved PDF file for the user
                        Print.openPDFInDefaultViewer(tempFile)
                        // Close the progress dialog
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
        // Start the background task
        Thread(task).start()
    }

    /**
     * Saves the selected items as a bulk PDF.
     * If no data is available, an error message is shown.
     * A file chooser is opened for the user to select a save location.
     * A progress dialog is displayed during the save operation.
     */
    private fun saveSelectedItemsAsBulkPDF() {
        if (allExcelData.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No data available for saving!", "Error", JOptionPane.ERROR_MESSAGE)
            return
        }

        val fileChooser = FileChooser()
        fileChooser.extensionFilters.add(FileChooser.ExtensionFilter("PDF Files", "*.pdf"))
        val file = fileChooser.showSaveDialog(null) ?: return

        // Display a progress dialog to the user
        val progressDialog = Alert(Alert.AlertType.INFORMATION)
        progressDialog.title = "Saving Labels"
        progressDialog.headerText = "Please wait, saving labels..."
        val progressBar = ProgressBar(0.0)
        val progressLabel = Label("Processing 0 of ${selectedItems.size}")
        progressDialog.dialogPane.content = VBox(10.0, progressBar, progressLabel)
        progressDialog.show()

        val task = object : Task<Void>() {
            /**
             * Background task for saving the selected items as a PDF.
             */
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
                            // Determine size of the labels
                            val (widthInches, heightInches) = when {
                                boxTab.isSelected || sleeveTab.isSelected -> Pair(4.01575, 5.98425)
                                chainSleeveTab.isSelected || chainBoxTab.isSelected -> Pair(2.99213, 5.98425)
                                chainBoxTab2.isSelected -> Pair(3.9, 2.9)
                                else -> Pair(4.01575, 5.98425)  // Default size
                            }
                            // Save the currently displayed pane to the PDF document
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
                                    // Populate UI with data from the selected row
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
                                    // Determine size of the labels
                                    val (widthInches, heightInches) = when {
                                        boxTab.isSelected || sleeveTab.isSelected -> Pair(4.01575, 5.98425)
                                        chainSleeveTab.isSelected || chainBoxTab.isSelected -> Pair(2.99213, 5.98425)
                                        chainBoxTab2.isSelected -> Pair(3.9, 2.9)
                                        else -> Pair(4.01575, 5.98425)  // Default size
                                    }
                                    // Save the populated pane to the PDF document
                                    Print.saveMultiplePanes(pane, document, widthInches, heightInches, 300)
                                    latch.countDown()
                                }
                                latch.await()

                                processedItems++
                                // Update progress and status message
                                updateProgress(processedItems.toLong(), totalItems.toLong())
                                updateMessage("Processing $processedItems of $totalItems")
                            }
                        }
                    }

                    // Save the PDF document
                    document.save(file)
                    Platform.runLater {
                        // Close the progress dialog and notify success
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
        // Start the background task
        Thread(task).start()
    }

    private fun getCurrentPane(): Pane? {
        // Returns the currently selected pane based on the selected tab
        return when {
            boxTab.isSelected -> stickerPane
            sleeveTab.isSelected -> sleevePane
            chainSleeveTab.isSelected -> chainSleevePane
            chainBoxTab.isSelected -> chainBoxPane
            chainBoxTab2.isSelected -> chainBoxPane2
            else -> null // If no matching tab is selected, return null      
        }
    }

    private fun setupCustomTab() {
        // Setup the custom tab with buttons and a ListView
        val vbox = VBox(10.0).apply {
            style = "-fx-padding: 10;"
        }

        // Initialize custom tab buttons
        addCustomFieldBtn = Button("Add Custom Field")
        saveCustomBtn = Button("Save Custom Fields")
        exportCustomBtn = Button("Export Custom Fields")

        // Initialize the ListView for custom fields
        customListView = ListView<HBox>().apply {
            VBox.setVgrow(this, Priority.ALWAYS) // Make ListView grow with the VBox
        }

        // Add buttons and ListView to the VBox
        vbox.children.addAll(addCustomFieldBtn, saveCustomBtn, exportCustomBtn, customListView)

        // Set the VBox as the content for the customTab
        customTab.content = vbox
    }


    private fun populateCustomFields() {
        customListView.items.clear()
        val tabPane = TabPane()
        tabPane.side = Side.RIGHT

        // Define Chain Box fields with necessary fields and corresponding controls.
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

        // Define Chain Sleeve fields with necessary fields and corresponding controls.
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

        // Define Harvester Sleeve fields with necessary fields and corresponding controls.
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

        // Define other Harvester Box fields with necessary fields and corresponding controls.
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

        // Add the defined fields to the respective tabs in the TabPane.
        addFieldsToTab(chainBoxFields, "Chainsaw Bar Box", tabPane)
        addFieldsToTab(chainSleeveFields, "Chainsaw Bar Sleeve", tabPane)
        addFieldsToTab(sleeveFields, "Harvester Bar Sleeve", tabPane)
        addFieldsToTab(otherFields, "Harvester Bar Box", tabPane)

        // Set the created TabPane to be the content of the customTab.
        customTab.content = tabPane
    }

    private fun addFieldsToTab(fields: Map<String, Control>, tabName: String, tabPane: TabPane) {
        // Create a new Tab and VBox for the contents
        val tab = Tab(tabName)
        val vbox = VBox(10.0)
        vbox.style = "-fx-padding: 10;"

        // Iterate over the fields map and add each field to the VBox
        fields.forEach { (key, field) ->
            // Find or create a CustomField for each key
            val customField = customFields.find { it.key == key } ?: CustomField(
                key,
                (field as TextInputControl).text
            ).also { customFields.add(it) }

            // Create a TextField and bind it to the customField value
            val valueField = TextField(customField.value)

            // Listener to update the original field and customField when the valueField changes
            valueField.textProperty().addListener { _, _, newValue ->
                (field as TextInputControl).text = newValue
                customField.value = newValue

                // Update barcode images if the field is a Barcode
                if (key == "Barcode") {
                    updateBarcodeImages()
                }
            }

            // Listener to update the valueField when the original field changes
            (field as TextInputControl).textProperty().addListener { _, _, newValue ->
                valueField.text = newValue
            }

            // Create a TextField to display the key, set to be non-editable
            val keyField = TextField(key).apply {
                isEditable = false
            }

            // Create a Clear button to clear the valueField and original field
            val clearButton = Button("Clear").apply {
                setOnAction {
                    valueField.text = ""
                    field.text = ""
                    customFields.find { it.key == key }?.value = ""
                }
            }

            // Add the keyField, valueField, and clearButton to an HBox, then add the HBox to the VBox
            val hbox = HBox(10.0, keyField, valueField, clearButton)
            vbox.children.add(hbox)
        }

        // Set the VBox as the content of the Tab and add the Tab to the TabPane
        tab.content = vbox
        tabPane.tabs.add(tab)
    }

    /**
     * Updates the barcode images displayed in various image views.
     * This function retrieves the current barcode value from the custom fields,
     * generates a barcode image, and updates the image views accordingly.
     */
    private fun updateBarcodeImages() {
        val barcodeGenerator = BarcodeGenerator()
        // Retrieve the barcode value from the custom fields; if not found, default to an empty string
        val barcodeValue = customFields.find { it.key == "Barcode" }?.value ?: ""

        println("Updating barcode with value: $barcodeValue")

        try {
            // Check if the barcode value meets the required length (12 characters) for EAN-13 barcodes
            if (barcodeValue.length == 12) {
                // Generate the barcode image using the EAN-13 standard
                val barcodeImage = barcodeGenerator.generateEAN13Barcode(barcodeValue)

                // Update the various image views with the newly generated barcode image
                chSleeveBarcodeImageView.image = barcodeImage
                sleeveBarcodeImage.image = barcodeImage
                chBoxBarcodeImageView.image = barcodeImage
                barcodeImageView.image = barcodeImage

                println("Barcode generated successfully")
            } else {
                println("Invalid Barcode Length")
            }
        } catch (e: Exception) {
            // Handle any exceptions that may occur during barcode generation
            println("Error generating barcode: ${e.message}")
        }
    }
}




