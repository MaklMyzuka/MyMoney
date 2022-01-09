package muzdima.mymoney.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.FontScheme;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.xssf.usermodel.IndexedColorMap;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.extensions.XSSFCellBorder;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

import muzdima.mymoney.R;
import muzdima.mymoney.repository.Repository;

public class ImportExport {

    public static final int IMPORT_DATABASE_REQUEST = 1;
    public static final int EXPORT_DATABASE_REQUEST = 2;
    public static final int EXPORT_EXCEL_REQUEST = 3;

    public static void importDatabase(Activity activity) {
        Loading.show(activity);
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType(activity.getString(R.string.mime_import_database));
        activity.startActivityForResult(intent, IMPORT_DATABASE_REQUEST);
    }

    public static boolean transferFile(Activity activity, FileInputStream inputStream, FileOutputStream outputStream) {
        try (FileChannel inChannel = inputStream.getChannel();
             FileChannel outChannel = outputStream.getChannel()) {
            inChannel.transferTo(0, inChannel.size(), outChannel);
            return true;
        } catch (Exception exception) {
            activity.runOnUiThread(() -> ErrorDialog.showError(activity, R.string.error_write_file, activity::finish));
        }
        return false;
    }

    public static void importDatabase(Activity activity, Uri uri) {
        boolean ok = false;
        try {
            Repository.getRepository().close();
            ParcelFileDescriptor descriptor = activity.getContentResolver().openFileDescriptor(uri, "r");
            FileInputStream inputStream = new FileInputStream(descriptor.getFileDescriptor());
            FileOutputStream outputStream = new FileOutputStream(Repository.getRepository().getTempFilePath());
            if (!transferFile(activity, inputStream, outputStream)) return;
            outputStream.flush();
            outputStream.close();
            inputStream.close();
            descriptor.close();
            ok = true;
        } catch (FileNotFoundException exception) {
            activity.runOnUiThread(() -> ErrorDialog.showError(activity, R.string.error_file_not_found, activity::finish));
        } catch (Exception exception) {
            activity.runOnUiThread(() -> ErrorDialog.showError(activity, R.string.error_open_file, activity::finish));
        } finally {
            if (ok) {
                Repository.getRepository().openTemp(activity);
                activity.runOnUiThread(() -> InfoDialog.show(activity, R.string.success, R.string.success_import, () -> InfoDialog.show(activity, R.string.success, R.string.restart, () -> Restart.restart(activity))));
            } else {
                Repository.getRepository().open(activity);
            }
        }
    }

    public static void exportDatabase(Activity activity) {
        Loading.show(activity);
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType(activity.getString(R.string.mime_export_database));
        intent.putExtra(Intent.EXTRA_TITLE, String.format("%s %s.sqlite", activity.getString(R.string.app_name), DateTime.printDateTime(activity, DateTime.convertUTCToLocal(DateTime.getNowUTC()))));
        activity.startActivityForResult(intent, EXPORT_DATABASE_REQUEST);
    }

    public static void exportDatabase(Activity activity, Uri uri) {
        try {
            Repository.getRepository().close();
            ParcelFileDescriptor descriptor = activity.getContentResolver().openFileDescriptor(uri, "w");
            FileOutputStream outputStream = new FileOutputStream(descriptor.getFileDescriptor());
            FileInputStream inputStream = new FileInputStream(Repository.getRepository().getFilePath());
            if (!transferFile(activity, inputStream, outputStream)) return;
            outputStream.flush();
            outputStream.close();
            descriptor.close();
            inputStream.close();
            activity.runOnUiThread(() -> InfoDialog.show(activity, R.string.success, R.string.success_export, activity::finish));
        } catch (FileNotFoundException exception) {
            activity.runOnUiThread(() -> ErrorDialog.showError(activity, R.string.error_file_not_found, activity::finish));
        } catch (Exception exception) {
            activity.runOnUiThread(() -> ErrorDialog.showError(activity, R.string.error_open_file, activity::finish));
        } finally {
            Repository.getRepository().open(activity);
        }
    }

    public static void exportExcel(Activity activity) {
        Loading.show(activity);
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType(activity.getString(R.string.mime_export_excel));
        intent.putExtra(Intent.EXTRA_TITLE, String.format("%s %s.xlsx", activity.getString(R.string.app_name), DateTime.printDateTime(activity, DateTime.convertUTCToLocal(DateTime.getNowUTC()))));
        activity.startActivityForResult(intent, EXPORT_EXCEL_REQUEST);
    }

    public static void exportExcel(Activity activity, Uri uri) {
        try {
            ParcelFileDescriptor descriptor = activity.getContentResolver().openFileDescriptor(uri, "w");
            FileOutputStream outputStream = new FileOutputStream(descriptor.getFileDescriptor());
            try (XSSFWorkbook workbook = new XSSFWorkbook()) {
                exportExcel(activity, workbook);
                workbook.write(outputStream);
                outputStream.flush();
                outputStream.close();
            }
            descriptor.close();
            activity.runOnUiThread(() -> InfoDialog.show(activity, R.string.success, R.string.success_export, activity::finish));
        } catch (FileNotFoundException exception) {
            activity.runOnUiThread(() -> ErrorDialog.showError(activity, R.string.error_file_not_found, activity::finish));
        } catch (Exception exception) {
            activity.runOnUiThread(() -> ErrorDialog.showError(activity, R.string.error_write_file, activity::finish));
        }
    }

    public static void exportExcel(Context context, XSSFWorkbook workbook) {
        XSSFFont fontNormal = workbook.createFont();
        XSSFFont fontBold = workbook.createFont();
        XSSFFont fontBoldWhite = workbook.createFont();
        XSSFFont fontBoldPositive = workbook.createFont();
        XSSFFont fontBoldNegative = workbook.createFont();
        XSSFCellStyle headerStyle = workbook.createCellStyle();
        XSSFCellStyle normalStyle = workbook.createCellStyle();
        XSSFCellStyle boldStyle = workbook.createCellStyle();
        XSSFCellStyle dateTimeStyle = workbook.createCellStyle();
        XSSFCellStyle moneyPositiveStyle = workbook.createCellStyle();
        XSSFCellStyle moneyNegativeStyle = workbook.createCellStyle();

        BorderStyle borderNormal = BorderStyle.THIN;
        BorderStyle borderBold = BorderStyle.MEDIUM;
        short colorBlack = IndexedColors.BLACK.index;
        short colorWhile = IndexedColors.WHITE.index;
        short colorPositive = IndexedColors.GREEN.index;
        short colorNegative = IndexedColors.RED.index;
        short colorHeader = IndexedColors.DARK_BLUE.index;

        fontNormal.setColor(colorBlack);

        fontBold.setColor(colorBlack);
        fontBold.setBold(true);

        fontBoldWhite.setColor(colorWhile);
        fontBoldWhite.setBold(true);

        fontBoldPositive.setColor(colorPositive);
        fontBoldPositive.setBold(true);

        fontBoldNegative.setColor(colorNegative);
        fontBoldNegative.setBold(true);

        headerStyle.setFillForegroundColor(colorHeader);
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        Excel.setBorder(headerStyle, borderBold, colorBlack);
        headerStyle.setFont(fontBoldWhite);

        Excel.setBorder(normalStyle, borderNormal, colorBlack);
        normalStyle.setFont(fontNormal);

        Excel.setBorder(boldStyle, borderNormal, colorBlack);
        boldStyle.setFont(fontBold);

        Excel.setBorder(dateTimeStyle, borderNormal, colorBlack);
        dateTimeStyle.setFont(fontNormal);
        dateTimeStyle.setDataFormat(Excel.getDateTimeFormat(context, workbook));

        Excel.setBorder(moneyPositiveStyle, borderNormal, colorBlack);
        moneyPositiveStyle.setFont(fontBoldPositive);

        Excel.setBorder(moneyNegativeStyle, borderNormal, colorBlack);
        moneyNegativeStyle.setFont(fontBoldNegative);

        Excel.TableColumn[] columnsActions = new Excel.TableColumn[]{
                new Excel.TableColumn("type", context.getString(R.string.excel_actions_type), Excel.TableColumn.Type.String, headerStyle, normalStyle, null),
                new Excel.TableColumn("account", context.getString(R.string.excel_actions_account), Excel.TableColumn.Type.String, headerStyle, boldStyle, null),
                new Excel.TableColumn("sum10000", context.getString(R.string.excel_actions_sum10000), Excel.TableColumn.Type.Integer10000, headerStyle, boldStyle, (value) -> {
                    if ((long) value > 0) {
                        return moneyPositiveStyle;
                    }
                    if ((long) value < 0) {
                        return moneyNegativeStyle;
                    }
                    return null;
                }),
                new Excel.TableColumn("currency", context.getString(R.string.excel_actions_currency), Excel.TableColumn.Type.String, headerStyle, boldStyle, null),
                new Excel.TableColumn("category", context.getString(R.string.excel_actions_category), Excel.TableColumn.Type.String, headerStyle, normalStyle, null),
                new Excel.TableColumn("product", context.getString(R.string.excel_actions_product), Excel.TableColumn.Type.String, headerStyle, normalStyle, null),
                new Excel.TableColumn("created_at", context.getString(R.string.excel_actions_created_at), Excel.TableColumn.Type.DateTime, headerStyle, dateTimeStyle, null),
        };

        Repository.getRepository().excelSheetActions(workbook, context.getString(R.string.excel_actions), columnsActions);

        Excel.TableColumn[] columnsCategories = new Excel.TableColumn[]{
                new Excel.TableColumn("name", context.getString(R.string.excel_categories_name), Excel.TableColumn.Type.String, headerStyle, boldStyle, null),
                new Excel.TableColumn("parent", context.getString(R.string.excel_categories_parent), Excel.TableColumn.Type.String, headerStyle, normalStyle, null),
                new Excel.TableColumn("full", context.getString(R.string.excel_categories_full), Excel.TableColumn.Type.String, headerStyle, normalStyle, null),
        };

        Repository.getRepository().excelSheetCategories(workbook, context.getString(R.string.excel_categories), columnsCategories);
    }
}
