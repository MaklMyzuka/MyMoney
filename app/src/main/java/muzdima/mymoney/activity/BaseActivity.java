package muzdima.mymoney.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import muzdima.mymoney.R;
import muzdima.mymoney.activity.dictionary.DictionariesActivity;
import muzdima.mymoney.utils.ConfigurationPreferences;
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
        getMenuInflater().inflate(R.menu.main_menu, menu);
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
        if (!languageCode.equals(ConfigurationPreferences.getLanguageCode(this))) {
            runOnUiThread(() -> {
                Loading.dismiss();
                recreating = true;
                recreate();
            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        } else if (id == R.id.menu_actions) {
            Intent intent = new Intent(this, ActionsActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.menu_draft) {
            Intent intent = new Intent(this, DraftActivity.class);
            intent.putExtra("action", 0);
            startActivity(intent);
            return true;
        } else if (id == R.id.menu_dictionaries) {
            Intent intent = new Intent(this, DictionariesActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.menu_import_export) {
            Intent intent = new Intent(this, ImportExportActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.menu_statistics) {
            Intent intent = new Intent(this, StatisticsActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.menu_statistics_monthly) {
            Intent intent = new Intent(this, StatisticsMonthlyActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.menu_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
