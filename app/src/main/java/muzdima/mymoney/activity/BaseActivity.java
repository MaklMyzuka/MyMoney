package muzdima.mymoney.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuCompat;

import muzdima.mymoney.R;
import muzdima.mymoney.activity.dictionaries.dictionary.AccountGroupsDictionaryActivity;
import muzdima.mymoney.activity.dictionaries.dictionary.AccountsDictionaryActivity;
import muzdima.mymoney.activity.dictionaries.dictionary.CategoriesDictionaryActivity;
import muzdima.mymoney.activity.dictionaries.dictionary.CurrenciesDictionaryActivity;
import muzdima.mymoney.repository.Repository;
import muzdima.mymoney.utils.ConfigurationPreferences;
import muzdima.mymoney.utils.ImportExport;
import muzdima.mymoney.utils.Loading;

public abstract class BaseActivity extends AppCompatActivity {
    private boolean recreating = false;
    private String languageCode;

    public boolean isRecreating() {
        return recreating;
    }

    protected boolean isBackButtonSupported() {
        return true;
    }

    protected boolean isMenuButtonSupported() {
        return true;
    }

    protected String getMenuTitle() {
        return null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        recreating = false;
        languageCode = ConfigurationPreferences.getLanguageCode(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (isMenuButtonSupported()) {
            getMenuInflater().inflate(R.menu.main_menu, menu);
            try{
                if(Repository.getRepository().isDraftEmptyFastCheck()) {
                    MenuItem menuDraft = menu.findItem(R.id.menuDraft);
                    menuDraft.setVisible(false);
                }
            }catch(Exception ignored){}
            MenuCompat.setGroupDividerEnabled(menu, true);
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            if (isBackButtonSupported()) actionBar.setDisplayHomeAsUpEnabled(true);
            if (getMenuTitle() != null) actionBar.setTitle(getMenuTitle());
        }
        return true;
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        Context context = ConfigurationPreferences.changeConfiguration(newBase);
        super.attachBaseContext(context);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (languageCode!=null && !languageCode.equals(ConfigurationPreferences.getLanguageCode(this))) {
            runOnUiThread(() -> {
                Loading.dismiss();
                recreating = true;
                recreate();
            });
        }
        invalidateOptionsMenu();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        } else if (id == R.id.menuActions) {
            Intent intent = new Intent(this, ActionsActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.menuDraft) {
            Intent intent = new Intent(this, DraftActivity.class);
            intent.putExtra("action", 0);
            startActivity(intent);
            return true;
        } else if (id == R.id.menuDictionaryCurrencies) {
            Intent intent = new Intent(this, CurrenciesDictionaryActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.menuDictionaryCategories) {
            Intent intent = new Intent(this, CategoriesDictionaryActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.menuDictionaryAccounts) {
            Intent intent = new Intent(this, AccountsDictionaryActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.menuDictionaryAccountGroups) {
            Intent intent = new Intent(this, AccountGroupsDictionaryActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.menuImportDatabase) {
            Intent intent = new Intent(this, ImportExportActivity.class);
            intent.putExtra("request",  ImportExport.IMPORT_DATABASE_REQUEST);
            startActivity(intent);
            return true;
        } else if (id == R.id.menuExportDatabase) {
            Intent intent = new Intent(this, ImportExportActivity.class);
            intent.putExtra("request", ImportExport.EXPORT_DATABASE_REQUEST);
            startActivity(intent);
            return true;
        } else if (id == R.id.menuExportExcel) {
            Intent intent = new Intent(this, ImportExportActivity.class);
            intent.putExtra("request", ImportExport.EXPORT_EXCEL_REQUEST);
            startActivity(intent);
            return true;
        } else if (id == R.id.menuStatistics) {
            Intent intent = new Intent(this, StatisticsActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.menuStatisticsMonthly) {
            Intent intent = new Intent(this, StatisticsMonthlyActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.menuSettings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
