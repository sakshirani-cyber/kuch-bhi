package com.fileparser.FileParser.util;

import org.apache.poi.ss.usermodel.*;

public final class ExcelCellUtil {

    private ExcelCellUtil() {
    }

    public static Object getCellValue(Cell cell) {

        if (cell == null) {
            return null;
        }

        return switch (cell.getCellType()) {

            case STRING -> cell.getStringCellValue().trim();

            case NUMERIC -> {

                if (DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getLocalDateTimeCellValue();
                }

                double value = cell.getNumericCellValue();

                if (value == (long) value) {
                    yield (long) value;
                }

                yield value;
            }

            case BOOLEAN -> cell.getBooleanCellValue();

            case FORMULA -> {

                FormulaEvaluator evaluator = cell
                        .getSheet()
                        .getWorkbook()
                        .getCreationHelper()
                        .createFormulaEvaluator();

                CellValue evaluated = evaluator.evaluate(cell);

                yield switch (evaluated.getCellType()) {

                    case STRING -> evaluated.getStringValue();

                    case NUMERIC -> {

                        double value = evaluated.getNumberValue();

                        if (value == (long) value) {
                            yield (long) value;
                        }

                        yield value;
                    }

                    case BOOLEAN -> evaluated.getBooleanValue();

                    default -> null;
                };
            }

            case BLANK -> "";

            default -> null;
        };
    }
}