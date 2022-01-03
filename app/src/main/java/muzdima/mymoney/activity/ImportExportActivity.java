package muzdima.mymoney.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import muzdima.mymoney.utils.InfoDialog;
import muzdima.mymoney.utils.Loading;
import muzdima.mymoney.R;
import muzdima.mymoney.utils.Restart;
import muzdima.mymoney.utils.Worker;
import muzdima.mymoney.repository.Repository;
import muzdima.mymoney.repository.model.ExportResult;
import muzdima.mymoney.repository.model.ImportResult;
import muzdima.mymoney.utils.ErrorDialog;

public class ImportExportActivity extends BaseActivity {

    @Override
    protected String getMenuTitle() {
        return getString(R.string.menu_import_export);
    }

    private final ActivityResultLauncher<String> requestExportPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
        if (isGranted)
            Export();
        else
            ErrorDialog.showError(this, R.string.permission_not_granted, null);
    });

    private final ActivityResultLauncher<String> requestImportPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
        if (isGranted)
            Import();
        else
            ErrorDialog.showError(this, R.string.permission_not_granted, null);
    });

    private void Export() {
        Worker.run(this, () -> {
            ExportResult result = Repository.getRepository().exportDatabase();
            runOnUiThread(() -> showResult(getString(R.string.export_to) + result.filePath, result.exception, null));
        });
    }

    private void Import() {
        Worker.run(this, () -> {
            ImportResult result = Repository.getRepository().importDatabase();
            runOnUiThread(() -> showResult(getString(R.string.import_from) + result.filePath, result.exception, ()->{
                Restart.restart(this);
            }));
        });
    }

    private void showResult(String message, Exception exception, Runnable callback) {
        Loading.dismiss();
        if (exception != null) {
            ErrorDialog.showError(this, message, exception, callback);
            return;
        }
        InfoDialog.show(this, R.string.success, message, callback);
    }

    private void runWithPermission(Runnable runnable, String permission, ActivityResultLauncher<String> requestPermissionLauncher) {
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            runnable.run();
        } else {
            requestPermissionLauncher.launch(permission);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import_export);
        ((Button) findViewById(R.id.export_button)).setOnClickListener(view ->
                runWithPermission(this::Export, Manifest.permission.WRITE_EXTERNAL_STORAGE, requestExportPermissionLauncher)
        );
        ((Button) findViewById(R.id.import_button)).setOnClickListener(view ->
                runWithPermission(this::Import, Manifest.permission.READ_EXTERNAL_STORAGE, requestImportPermissionLauncher)
        );
    }
}