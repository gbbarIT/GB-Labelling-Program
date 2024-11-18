package com.labellingprogram

import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.ss.usermodel.CellType

import java.io.File


class ExcelParser {

    fun parseExcelFileMultipleSheets(file: File): List<Pair<String, Map<String, List<String>>>> {
        val result = mutableListOf<Pair<String, Map<String, List<String>>>>()

        val workbook = WorkbookFactory.create(file)

        for (sheetIndex in 0 until workbook.numberOfSheets) {
            val sheet = workbook.getSheetAt(sheetIndex)
            val sheetData = mutableMapOf<String, MutableList<String>>()

            if (sheet.physicalNumberOfRows == 0) continue

            val headerRow = sheet.getRow(0)
            if (headerRow == null || headerRow.physicalNumberOfCells == 0) continue

            val headers = mutableListOf<String>()

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

            result.add(Pair(sheet.sheetName, sheetData))
        }

        workbook.close()
        return result
    }



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

