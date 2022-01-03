package muzdima.mymoney.utils;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;

import java.util.Locale;

import muzdima.mymoney.R;

public class ConfigurationPreferences {

    private static FormatPreferences formatPreferences = null;

    public static ContextWrapper changeConfiguration(Context context) {
        Resources resources = context.getResources();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String languageCode = preferences.getString(resources.getString(R.string.preference_language), resources.getString(R.string.language_system_value));
        String nightMode = preferences.getString(resources.getString(R.string.preference_night_mode), resources.getString(R.string.preference_night_mode_system_value));

        // NightMode
        int currentNightMode = AppCompatDelegate.getDefaultNightMode();
        int newNightMode = (nightMode.equals(resources.getString(R.string.preference_night_mode_system_value))
                ? AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM :
                (nightMode.equals(resources.getString(R.string.preference_night_mode_on_value))
                        ? AppCompatDelegate.MODE_NIGHT_YES
                        : AppCompatDelegate.MODE_NIGHT_NO));
        if (currentNightMode != newNightMode) {
            AppCompatDelegate.setDefaultNightMode(newNightMode);
        }

        // Language
        Configuration config = new Configuration(resources.getConfiguration());
        Locale sysLocale = config.getLocales().get(0);
        if (!languageCode.equals(resources.getString(R.string.language_system_value)) && !sysLocale.getLanguage().equals(languageCode)) {
            Locale locale = new Locale(languageCode);
            Locale.setDefault(locale);
            config.setLocale(locale);
            context = context.createConfigurationContext(config);
        }

        return new ContextWrapper(context);
    }

    public static String getLanguageCode(Context context) {
        Resources resources = context.getResources();
        return PreferenceManager
                .getDefaultSharedPreferences(context)
                .getString(resources.getString(R.string.preference_language), resources.getString(R.string.language_system_value));
    }

    public static void updateFormatPreferences(Context context, String key, String value) {
        Resources resources = context.getResources();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String numberDecimal = resources.getString(R.string.preference_format_number_decimal).equals(key) ? value : preferences.getString(resources.getString(R.string.preference_format_number_decimal), resources.getString(R.string.preference_format_number_decimal_point2_value));
        String numberThousand = resources.getString(R.string.preference_format_number_thousand).equals(key) ? value : preferences.getString(resources.getString(R.string.preference_format_number_thousand), resources.getString(R.string.preference_format_number_thousand_space_value));
        String date = resources.getString(R.string.preference_format_date).equals(key) ? value : preferences.getString(resources.getString(R.string.preference_format_date), resources.getString(R.string.preference_format_date_dd_mm_yyyy_value));
        String time = resources.getString(R.string.preference_format_time).equals(key) ? value : preferences.getString(resources.getString(R.string.preference_format_time), resources.getString(R.string.preference_format_time_hh_mm_value));
        FormatPreferences.FormatNumberDecimal formatNumberDecimal = FormatPreferences.FormatNumberDecimal.Point2;
        FormatPreferences.FormatNumberThousand formatNumberThousand = FormatPreferences.FormatNumberThousand.Space;
        FormatPreferences.FormatDate formatDate = FormatPreferences.FormatDate.DD_MM_YYYY;
        FormatPreferences.FormatTime formatTime = FormatPreferences.FormatTime.HH_MM;
        if (numberDecimal.equals(resources.getString(R.string.preference_format_number_decimal_zero_value)))
            formatNumberDecimal = FormatPreferences.FormatNumberDecimal.Zero;
        if (numberDecimal.equals(resources.getString(R.string.preference_format_number_decimal_comma1_value)))
            formatNumberDecimal = FormatPreferences.FormatNumberDecimal.Comma1;
        if (numberDecimal.equals(resources.getString(R.string.preference_format_number_decimal_point1_value)))
            formatNumberDecimal = FormatPreferences.FormatNumberDecimal.Point1;
        if (numberDecimal.equals(resources.getString(R.string.preference_format_number_decimal_comma2_value)))
            formatNumberDecimal = FormatPreferences.FormatNumberDecimal.Comma2;
        if (numberDecimal.equals(resources.getString(R.string.preference_format_number_decimal_point2_value)))
            formatNumberDecimal = FormatPreferences.FormatNumberDecimal.Point2;
        if (numberThousand.equals(resources.getString(R.string.preference_format_number_thousand_delta_value)))
            formatNumberThousand = FormatPreferences.FormatNumberThousand.Delta;
        if (numberThousand.equals(resources.getString(R.string.preference_format_number_thousand_comma_value)))
            formatNumberThousand = FormatPreferences.FormatNumberThousand.Comma;
        if (numberThousand.equals(resources.getString(R.string.preference_format_number_thousand_none_value)))
            formatNumberThousand = FormatPreferences.FormatNumberThousand.None;
        if (numberThousand.equals(resources.getString(R.string.preference_format_number_thousand_space_value)))
            formatNumberThousand = FormatPreferences.FormatNumberThousand.Space;
        if (date.equals(resources.getString(R.string.preference_format_date_yyyy_mm_dd_value)))
            formatDate = FormatPreferences.FormatDate.YYYY_MM_DD;
        if (date.equals(resources.getString(R.string.preference_format_date_mm_dd_yyyy_value)))
            formatDate = FormatPreferences.FormatDate.MM_DD_YYYY;
        if (date.equals(resources.getString(R.string.preference_format_date_dd_mm_yyyy_value)))
            formatDate = FormatPreferences.FormatDate.DD_MM_YYYY;
        if (time.equals(resources.getString(R.string.preference_format_time_none_value)))
            formatTime = FormatPreferences.FormatTime.None;
        if (time.equals(resources.getString(R.string.preference_format_time_hh_mm_ss_value)))
            formatTime = FormatPreferences.FormatTime.HH_MM_SS;
        if (time.equals(resources.getString(R.string.preference_format_time_hh_mm_value)))
            formatTime = FormatPreferences.FormatTime.HH_MM;
        formatPreferences = new FormatPreferences(formatNumberDecimal, formatNumberThousand, formatDate, formatTime);
    }

    public static FormatPreferences getFormatPreferences(Context context) {
        if (formatPreferences == null) {
            updateFormatPreferences(context, null, null);
        }
        return formatPreferences;
    }

    public static class FormatPreferences {
        public final FormatNumberDecimal formatNumberDecimal;
        public final FormatNumberThousand formatNumberThousand;
        public final FormatDate formatDate;
        public final FormatTime formatTime;

        FormatPreferences(FormatNumberDecimal formatNumberDecimal,
                          FormatNumberThousand formatNumberThousand,
                          FormatDate formatDate,
                          FormatTime formatTime) {
            this.formatNumberDecimal = formatNumberDecimal;
            this.formatNumberThousand = formatNumberThousand;
            this.formatDate = formatDate;
            this.formatTime = formatTime;
        }

        public enum FormatNumberDecimal {Point2, Comma2, Point1, Comma1, Zero}

        public enum FormatNumberThousand {Space, None, Comma, Delta}

        public enum FormatDate {DD_MM_YYYY, MM_DD_YYYY, YYYY_MM_DD}

        public enum FormatTime {HH_MM, HH_MM_SS, None}
    }
}
