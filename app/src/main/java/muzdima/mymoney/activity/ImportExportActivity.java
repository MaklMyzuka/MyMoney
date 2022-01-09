package muzdima.mymoney.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;

import muzdima.mymoney.R;
import muzdima.mymoney.utils.ImportExport;
import muzdima.mymoney.utils.Loading;
import muzdima.mymoney.utils.Worker;

public class ImportExportActivity extends BaseActivity {

    @Override
    protected String getMenuTitle() {
        return getString(R.string.import_export);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        int action = extras.getInt("request", 0);
        switch (action){
            case ImportExport.IMPORT_DATABASE_REQUEST: ImportExport.importDatabase(this); break;
            case ImportExport.EXPORT_DATABASE_REQUEST: ImportExport.exportDatabase(this); break;
            case ImportExport.EXPORT_EXCEL_REQUEST: ImportExport.exportExcel(this); break;
            default: finish(); break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ImportExport.IMPORT_DATABASE_REQUEST
                || requestCode == ImportExport.EXPORT_DATABASE_REQUEST
                || requestCode == ImportExport.EXPORT_EXCEL_REQUEST) {
            Loading.dismiss();
            if (resultCode != RESULT_OK || data == null || data.getData() == null) {
                finish();
                return;
            }
            Uri uri = data.getData();
            Worker.run(this, () -> {
                switch (requestCode) {
                    case ImportExport.IMPORT_DATABASE_REQUEST:
                        ImportExport.importDatabase(this, uri);
                        break;
                    case ImportExport.EXPORT_DATABASE_REQUEST:
                        ImportExport.exportDatabase(this, uri);
                        break;
                    case ImportExport.EXPORT_EXCEL_REQUEST:
                        ImportExport.exportExcel(this, uri);
                        break;
                }
            });
        }
    }
}
