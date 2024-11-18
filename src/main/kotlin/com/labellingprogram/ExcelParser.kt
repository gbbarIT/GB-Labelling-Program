package com.labellingprogram

import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.ss.usermodel.CellType

import java.io.File

/**
 * A parser for reading and extracting data from Excel files (.xlsx, .xls).
 */
class ExcelParser {

    /**
     * Parses an Excel file and extracts data from multiple sheets.
     *
     * @param file The Excel file to parse.
     * @return A list of pairs, where each pair contains the sheet name and a map.
     *         The map has headers as keys and lists of column values as values.
     */
    fun parseExcelFileMultipleSheets(file: File): List<Pair<String, Map<String, List<String>>>> {
        val result = mutableListOf<Pair<String, Map<String, List<String>>>>()

        // Create workbook from file
        val workbook = WorkbookFactory.create(file)

        // Iterate through all sheets
        for (sheetIndex in 0 until workbook.numberOfSheets) {
            val sheet = workbook.getSheetAt(sheetIndex)
            val sheetData = mutableMapOf<String, MutableList<String>>()

            // Skip empty sheets
            if (sheet.physicalNumberOfRows == 0) continue

            // Get header row
            val headerRow = sheet.getRow(0)
            if (headerRow == null || headerRow.physicalNumberOfCells == 0) continue

            val headers = mutableListOf<String>()

            // Extract headers
            for (cell in headerRow) {
                val headerValue = getCellValueAsString(cell)
                headers.add(headerValue)
                sheetData[headerValue] = mutableListOf()
            }

            // Extract data rows
            for (i in 1 until sheet.physicalNumberOfRows) {
                val row = sheet.getRow(i) ?: continue

                for (j in headers.indices) {
                    val cell = row.getCell(j)
                    val header = headers[j]
                    sheetData[header]?.add(getCellValueAsString(cell))
                }
            }

            // Add sheet data to result
            result.add(Pair(sheet.sheetName, sheetData))
        }

        // Close workbook to free resources
        workbook.close()
        return result
    }

    /**
     * Converts the value of a cell to a string.
     *
     * @param cell The cell to get the value from.
     * @return The string representation of the cell value.
     */
    private fun getCellValueAsString(cell: Cell?): String {
        return when (cell?.cellType) {
            CellType.STRING -> cell.stringCellValue
            CellType.NUMERIC -> cell.numericCellValue.toString()
            CellType.BOOLEAN -> cell.booleanCellValue.toString()
            CellType.FORMULA -> cell.cellFormula
            else -> ""
        }
    }
}

