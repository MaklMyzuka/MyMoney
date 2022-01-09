package muzdima.mymoney.utils;

import android.content.Context;
import android.database.Cursor;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.util.Arrays;
import java.util.function.Function;

public class Excel {

    public static void setBorder(XSSFCellStyle style, BorderStyle borderStyle, short color) {
        style.setBorderLeft(borderStyle);
        style.setBorderRight(borderStyle);
        style.setBorderTop(borderStyle);
        style.setBorderBottom(borderStyle);
        style.setLeftBorderColor(color);
        style.setRightBorderColor(color);
        style.setTopBorderColor(color);
        style.setBottomBorderColor(color);
    }

    public static short getDateTimeFormat(Context context, XSSFWorkbook workbook) {
        ConfigurationPreferences.FormatPreferences preferences = ConfigurationPreferences.getFormatPreferences(context);
        String dateTimeFormatString = "";
        switch (preferences.formatDate) {
            default:
            case DD_MM_YYYY:
                dateTimeFormatString += "dd.MM.yyyy";
                break;
            case MM_DD_YYYY:
                dateTimeFormatString += "MM.dd.yyyy";
                break;
            case YYYY_MM_DD:
                dateTimeFormatString += "yyyy.MM.dd";
                break;
        }
        switch (preferences.formatTime) {
            default:
            case HH_MM:
                dateTimeFormatString += " HH:mm";
                break;
            case HH_MM_SS:
                dateTimeFormatString += " HH:mm:ss";
                break;
            case None:
                break;
        }
        return workbook.createDataFormat().getFormat(dateTimeFormatString);
    }

    private static XSSFCell createCell(int i, XSSFRow row, CellType cellType, TableColumn column, Object value) {
        XSSFCell cell = row.createCell(i, cellType);
        cell.setCellStyle(column.dataStyle);
        if (column.cellStyle != null && value != null) {
            XSSFCellStyle style = column.cellStyle.apply(value);
            if (style != null) cell.setCellStyle(style);
        }
        return cell;
    }

    public static XSSFSheet createSheetWithTable(XSSFWorkbook workbook, Cursor cursor, String sheetName, TableColumn[] columns) {
        int[] columnIndexes = Arrays.stream(columns).mapToInt(i -> cursor.getColumnIndex(i.source)).toArray();
        int[] maxCharacterCount = Arrays.stream(columns).mapToInt(i -> 0).toArray();
        XSSFSheet sheet = workbook.createSheet(sheetName);
        XSSFRow headerRow = sheet.createRow(0);
        for (int i = 0; i < columns.length; i++) {
            XSSFCell cell = headerRow.createCell(i, CellType.STRING);
            cell.setCellStyle(columns[i].headerStyle);
            cell.setCellValue(columns[i].name);
            maxCharacterCount[i] = Math.max(maxCharacterCount[i], columns[i].name.length());
        }
        int n = 1;
        while (cursor.moveToNext()) {
            XSSFRow row = sheet.createRow(n);
            for (int i = 0; i < columns.length; i++) {
                int index = columnIndexes[i];
                if (index == -1) {
                    continue;
                }
                XSSFCell cell;
                if (cursor.isNull(index)) {
                    createCell(i, row, CellType.BLANK, columns[i], null);
                    continue;
                }
                switch (columns[i].type) {
                    case String:
                        String valueString = cursor.getString(index);
                        cell = createCell(i, row, CellType.STRING, columns[i], valueString);
                        cell.setCellValue(valueString);
                        maxCharacterCount[i] = Math.max(maxCharacterCount[i], valueString.length());
                        break;
                    case Integer10000:
                        long valueInteger10000 = cursor.getLong(index);
                        cell = createCell(i, row, CellType.NUMERIC, columns[i], valueInteger10000);
                        cell.setCellValue(valueInteger10000 / 10000.0);
                        maxCharacterCount[i] = Math.max(maxCharacterCount[i], String.valueOf(valueInteger10000).length());
                        break;
                    case DateTime:
                        long valueDateTime = cursor.getLong(index);
                        cell = createCell(i, row, CellType.NUMERIC, columns[i], valueDateTime);
                        cell.setCellValue(DateTime.convertUTCToLocal(valueDateTime).toLocalDateTime());
                        maxCharacterCount[i] = Math.max(maxCharacterCount[i], 20);
                        break;
                }
            }
            n++;
        }
        for (int i = 0; i < columns.length; i++) {
            int width = ((int)(maxCharacterCount[i] * 1.14388)) * 256;
            if (width!=0)
                sheet.setColumnWidth(i, width);
        }
        return sheet;
    }

    public static class TableColumn {
        public final String source;
        public final String name;
        public final Type type;
        public final XSSFCellStyle headerStyle;
        public final XSSFCellStyle dataStyle;
        public final Function<Object, XSSFCellStyle> cellStyle;

        TableColumn(String source, String name, Type type, XSSFCellStyle headerStyle, XSSFCellStyle dataStyle, Function<Object, XSSFCellStyle> cellStyle) {
            this.source = source;
            this.name = name;
            this.type = type;
            this.headerStyle = headerStyle;
            this.dataStyle = dataStyle;
            this.cellStyle = cellStyle;
        }

        public enum Type {
            String, Integer10000, DateTime
        }
    }
}
