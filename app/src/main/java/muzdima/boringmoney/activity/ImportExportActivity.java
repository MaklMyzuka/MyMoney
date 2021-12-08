package muzdima.boringmoney.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import muzdima.boringmoney.utils.InfoDialog;
import muzdima.boringmoney.utils.Loading;
import muzdima.boringmoney.R;
import muzdima.boringmoney.utils.Worker;
import muzdima.boringmoney.repository.Repository;
import muzdima.boringmoney.repository.model.ExportResult;
import muzdima.boringmoney.repository.model.ImportResult;
import muzdima.boringmoney.utils.ErrorDialog;

public class ImportExportActivity extends AppCompatActivity {

    private final ActivityResultLauncher<String> requestExportPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
        if (isGranted)
            Export();
        else
            ErrorDialog.showError(this, R.string.permission_not_granted);
    });

    private final ActivityResultLauncher<String> requestImportPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
        if (isGranted)
            Import();
        else
            ErrorDialog.showError(this, R.string.permission_not_granted);
    });

    private void Export() {
        Worker.run(this, () -> {
            ExportResult result = Repository.getRepository().exportDatabase();
            runOnUiThread(() -> showResult(getString(R.string.export_to) + result.filePath, result.exception));
        });
    }

    private void Import() {
        Worker.run(this, () -> {
            ImportResult result = Repository.getRepository().importDatabase();
            runOnUiThread(() -> showResult(getString(R.string.import_from) + result.filePath, result.exception));
        });
    }

    private void showResult(String message, Exception exception) {
        Loading.dismiss();
        if (exception != null) {
            ErrorDialog.showError(this, message, exception);
            return;
        }
        InfoDialog.show(this, R.string.success, message);
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