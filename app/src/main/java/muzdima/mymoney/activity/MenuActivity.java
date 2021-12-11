package muzdima.mymoney.activity;

import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import muzdima.mymoney.R;

public abstract class MenuActivity extends AppCompatActivity {

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_actions) {
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
            Intent intent = new Intent(this,  StatisticsActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.menu_statistics_monthly) {
            Intent intent = new Intent(this,  StatisticsMonthlyActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
