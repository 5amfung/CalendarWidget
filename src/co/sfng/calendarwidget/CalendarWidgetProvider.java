package co.sfng.calendarwidget;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Events;
import android.text.format.DateFormat;
import android.widget.RemoteViews;

import java.util.Calendar;
import java.util.List;


public class CalendarWidgetProvider extends AppWidgetProvider {

    private static final String ACTION_PREVIOUS_MONTH =
            "co.sfng.calendarwidget.ACTION_PREVIOUS_MONTH";
    private static final String ACTION_NEXT_MONTH = "co.sfng.calendarwidget.ACTION_NEXT_MONTH";
    private static final String ACTION_TODAY = "co.sfng.calendarwidget.ACTION_TODAY";
    private static final String ACTION_DATE_CLICK = "co.sfng.calendarwidget.ACTION_DATE_CLICK";

    private static final String EXTRA_YEAR = "co.sfng.calendarwidget.EXTRA_YEAR";
    private static final String EXTRA_MONTH = "co.sfng.calendarwidget.EXTRA_MONTH";
    private static final String EXTRA_DATE = "co.sfng.calendarwidget.EXTRA_DATE";

    private static final String PREFERENCE_FILE =
            "co.sfng.calendarwidget.PREFERENCE_WIDGET_";
    private static final String PREF_SELECTED_TIME = "selected_time";
    private static final String PREF_IS_WIDE = "is_wide";

    // Constants for Date click actions.
    private static final int VIEW_CALENDAR = 1;
    private static final int CREATE_EVENT = 2;

    private static final int WEEKS = 6;

    @Override
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager,
            int appWidgetId, Bundle newOptions) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId,	newOptions);

        // Obtain widget width and set flag in SharePreferences.
        Bundle bundle = appWidgetManager.getAppWidgetOptions(appWidgetId);

        if (bundle != null) {
            int minWidth = bundle.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);

            Resources res = context.getResources();
            boolean isWide = minWidth > res.getInteger(R.integer.minimum_wide_width);

            SharedPreferences pref = context.getSharedPreferences(
                    getPreferenceFileName(appWidgetId), Context.MODE_PRIVATE);
            pref.edit().putBoolean(PREF_IS_WIDE, isWide).apply();
        }

        render(context, appWidgetId, getTheme(context), getWeekStartDay(context));
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        int theme = getTheme(context);
        int weekStartDay = getWeekStartDay(context);

        for (int appWidgetId: appWidgetIds) {
            render(context, appWidgetId, theme, weekStartDay);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);

        for (int appWidgetId: appWidgetIds) {
            SharedPreferences pref = context.getSharedPreferences(
                    getPreferenceFileName(appWidgetId), Context.MODE_PRIVATE);
            pref.edit().clear().apply();
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        String action = intent.getAction();
        int appWidgetId = intent.getIntExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);

        if (ACTION_PREVIOUS_MONTH.equals(action)) {
            previousMonth(context, appWidgetManager, appWidgetId);

        } else if (ACTION_NEXT_MONTH.equals(action)) {
            nextMonth(context, appWidgetManager, appWidgetId);

        } else if (ACTION_TODAY.equals(action)) {
            today(context, appWidgetManager, appWidgetId);

        } else if (ACTION_DATE_CLICK.equals(action)) {
            int onDateClickAction = getOnDateClickAction(context);

            if (onDateClickAction == VIEW_CALENDAR) {
                // View calendar
                Calendar cal = Calendar.getInstance();
                cal.set(intent.getIntExtra(EXTRA_YEAR, -1), intent.getIntExtra(EXTRA_MONTH, -1),
                        intent.getIntExtra(EXTRA_DATE, -1));
                Uri.Builder builder = CalendarContract.CONTENT_URI.buildUpon();
                builder.appendPath("time");
                ContentUris.appendId(builder, cal.getTimeInMillis());
                Intent calIntent = new Intent(Intent.ACTION_VIEW).setData(builder.build());
                calIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(calIntent);

            } else if (onDateClickAction == CREATE_EVENT) {
                // Create new event.
                Calendar cal = Calendar.getInstance();
                cal.set(intent.getIntExtra(EXTRA_YEAR, -1), intent.getIntExtra(EXTRA_MONTH, -1),
                        intent.getIntExtra(EXTRA_DATE, -1));
                Intent calIntent = new Intent(Intent.ACTION_INSERT, Events.CONTENT_URI);
                calIntent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, cal.getTimeInMillis());
                calIntent.putExtra(Events.AVAILABILITY, Events.AVAILABILITY_BUSY);
                calIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(calIntent);
            }
        }
    }

    private void previousMonth(Context context, AppWidgetManager appWidgetManager,
            int appWidgetId) {
        SharedPreferences pref = context.getSharedPreferences(
                getPreferenceFileName(appWidgetId), Context.MODE_PRIVATE);
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(pref.getLong(PREF_SELECTED_TIME, cal.getTimeInMillis()));
        cal.set(Calendar.DATE, 1);
        cal.add(Calendar.MONTH, -1);
        pref.edit().putLong(PREF_SELECTED_TIME, cal.getTimeInMillis()).apply();
        render(context, appWidgetId, getTheme(context), getWeekStartDay(context));
    }

    private void nextMonth(Context context, AppWidgetManager appWidgetManager,
            int appWidgetId) {
        SharedPreferences pref = context.getSharedPreferences(
                getPreferenceFileName(appWidgetId), Context.MODE_PRIVATE);
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(pref.getLong(PREF_SELECTED_TIME, cal.getTimeInMillis()));
        cal.set(Calendar.DATE, 1);
        cal.add(Calendar.MONTH, 1);
        pref.edit().putLong(PREF_SELECTED_TIME, cal.getTimeInMillis()).apply();
        render(context, appWidgetId, getTheme(context), getWeekStartDay(context));
    }

    private void today(Context context, AppWidgetManager appWidgetManager,
            int appWidgetId) {
        SharedPreferences pref = context.getSharedPreferences(
                getPreferenceFileName(appWidgetId), Context.MODE_PRIVATE);
        pref.edit().remove(PREF_SELECTED_TIME).apply();
        render(context, appWidgetId, getTheme(context), getWeekStartDay(context));
    }

    private String getPreferenceFileName(int appWidgetId) {
        return PREFERENCE_FILE + appWidgetId;
    }

    private int getTheme(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return Integer.parseInt(pref.getString(SettingsFragment.PREF_THEME, "0"));
    }

    private int getWeekStartDay(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        String s = pref.getString(SettingsFragment.PREF_WEEK_START_DAY,
                String.valueOf(Calendar.SUNDAY));
        return Integer.parseInt(s);
    }

    private int getOnDateClickAction(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        String s = pref.getString(SettingsFragment.PREF_ON_DAY_CLICK, "0");
        return Integer.parseInt(s);

    }

    private void render(Context context, int appWidgetId, int theme, int weekStartDay) {
        Calendar cal = Calendar.getInstance();
        int todayDate = cal.get(Calendar.DATE);
        int todayMonth = cal.get(Calendar.MONTH);
        int todayYear = cal.get(Calendar.YEAR);

        // Obtain last selected time from SharedPreferences.
        SharedPreferences pref = context.getSharedPreferences(
                getPreferenceFileName(appWidgetId), Context.MODE_PRIVATE);
        long selectedTime = pref.getLong(PREF_SELECTED_TIME, cal.getTimeInMillis());
        cal.setTimeInMillis(selectedTime);

        String pkgName = context.getPackageName();
        RemoteViews widgetView = new RemoteViews(pkgName, ResourceHelper.layoutWidget(theme));
        widgetView.removeAllViews(R.id.calendar);

        // Set month label.
        String fmt = pref.getBoolean(PREF_IS_WIDE, false) ? "MMMM yyyy" : "MMM yyyy";
        widgetView.setTextViewText(R.id.month_year_label, DateFormat.format(fmt, cal));

        // Render day of week.
        renderDayOfWeek(widgetView, weekStartDay);

        int selectedMonth = cal.get(Calendar.MONTH);
        cal.set(Calendar.DATE, 1);
        cal.add(Calendar.DATE, weekStartDay - cal.get(Calendar.DAY_OF_WEEK));

        for (int i = 0; i < WEEKS; i++) {
            RemoteViews rowView = new RemoteViews(pkgName, R.layout.row_week);

            for (int j = 0; j < 7; j++) {
                int date = cal.get(Calendar.DATE);
                int month = cal.get(Calendar.MONTH);
                int year = cal.get(Calendar.YEAR);

                boolean isWithinMonth = month == selectedMonth;
                boolean isToday = date == todayDate && month == todayMonth && year == todayYear;

                int layoutId = ResourceHelper.layoutCellDay(theme);
                if (isToday && isWithinMonth) {
                    layoutId = ResourceHelper.layoutCellToday(theme);
                } else if (isWithinMonth) {
                    layoutId = ResourceHelper.layoutCellInMonth(theme);
                }

                RemoteViews dateView = new RemoteViews(pkgName, layoutId);
                dateView.setTextViewText(android.R.id.text1, Integer.toString(date));
                dateView.setOnClickPendingIntent(
                            android.R.id.text1,
                            createDateClickPendingIntent(context, appWidgetId, year, month, date));
                rowView.addView(R.id.row_week, dateView);
                cal.add(Calendar.DATE, 1);
            }
            widgetView.addView(R.id.calendar, rowView);
        }

        // Attach action to buttons.
        widgetView.setOnClickPendingIntent(R.id.previous_month_button,
                createPendingIntent(context, appWidgetId, ACTION_PREVIOUS_MONTH));
        widgetView.setOnClickPendingIntent(R.id.next_month_button,
                createPendingIntent(context, appWidgetId, ACTION_NEXT_MONTH));
        widgetView.setOnClickPendingIntent(R.id.month_year_label,
                createPendingIntent(context, appWidgetId, ACTION_TODAY));

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        appWidgetManager.updateAppWidget(appWidgetId, widgetView);
    }

    private void renderDayOfWeek(RemoteViews rv, int weekStartDay) {
        List<String> list = DayOfWeekHelper.getDayOfWeek(weekStartDay);
        rv.setTextViewText(R.id.day_of_week0, list.get(0));
        rv.setTextViewText(R.id.day_of_week1, list.get(1));
        rv.setTextViewText(R.id.day_of_week2, list.get(2));
        rv.setTextViewText(R.id.day_of_week3, list.get(3));
        rv.setTextViewText(R.id.day_of_week4, list.get(4));
        rv.setTextViewText(R.id.day_of_week5, list.get(5));
        rv.setTextViewText(R.id.day_of_week6, list.get(6));
    }

    private PendingIntent createPendingIntent(Context context, int appWidgetId, String action) {
        Intent intent = new Intent(context, CalendarWidgetProvider.class);
        intent.setAction(action);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
        return PendingIntent.getBroadcast(context, appWidgetId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private PendingIntent createDateClickPendingIntent(Context context, int appWidgetId, int year,
            int month, int date) {
        Intent intent = new Intent(context, CalendarWidgetProvider.class);
        intent.setAction(ACTION_DATE_CLICK);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intent.putExtra(EXTRA_YEAR, year);
        intent.putExtra(EXTRA_MONTH, month);
        intent.putExtra(EXTRA_DATE, date);
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
        return PendingIntent.getBroadcast(
                context, appWidgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

}
