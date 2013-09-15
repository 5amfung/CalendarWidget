package co.sfng.calendarwidget;

import java.util.Calendar;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.RemoteViews;


public class CalendarWidgetProvider extends AppWidgetProvider {

	private static final String LOG_TAG = CalendarWidgetProvider.class.toString();

	private static final String ACTION_PREVIOUS_MONTH =
			"co.sfng.calendarwidget.ACTION_PREVIOUS_MONTH";
	private static final String ACTION_NEXT_MONTH =
			"co.sfng.calendarwidget.ACTION_NEXT_MONTH";
	private static final String ACTION_TODAY =
			"co.sfng.calendarwidget.ACTION_TODAY";

	private static final String PREFERENCE_FILE =
			"co.sfng.calendarwidget.PREFERENCE_WIDGET_";
	private static final String SELECTED_TIME = "selected_time";
	private static final String IS_WIDE = "is_wide";

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
			pref.edit().putBoolean(IS_WIDE, isWide).apply();
		}

		render(context, appWidgetId);
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);

		for (int appWidgetId: appWidgetIds) {
			render(context, appWidgetId);
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
		int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);

		if (ACTION_PREVIOUS_MONTH.equals(action)) {
			previousMonth(context, appWidgetManager, appWidgetId);
		} else if (ACTION_NEXT_MONTH.equals(action)) {
			nextMonth(context, appWidgetManager, appWidgetId);
		} else if (ACTION_TODAY.equals(action)) {
			today(context, appWidgetManager, appWidgetId);
		}
	}

	private void previousMonth(Context context, AppWidgetManager appWidgetManager,
			int appWidgetId) {
		Calendar cal = Calendar.getInstance();
		SharedPreferences pref = context.getSharedPreferences(
				getPreferenceFileName(appWidgetId), Context.MODE_PRIVATE);
		cal.setTimeInMillis(pref.getLong(SELECTED_TIME, cal.getTimeInMillis()));
		cal.add(Calendar.MONTH, -1);
		pref.edit().putLong(SELECTED_TIME, cal.getTimeInMillis()).apply();
		render(context, appWidgetId);
	}

	private void nextMonth(Context context, AppWidgetManager appWidgetManager,
			int appWidgetId) {
		Calendar cal = Calendar.getInstance();
		SharedPreferences pref = context.getSharedPreferences(
				getPreferenceFileName(appWidgetId), Context.MODE_PRIVATE);
		cal.setTimeInMillis(pref.getLong(SELECTED_TIME, cal.getTimeInMillis()));
		cal.add(Calendar.MONTH, 1);
		pref.edit().putLong(SELECTED_TIME, cal.getTimeInMillis()).apply();
		render(context, appWidgetId);
	}

	private void today(Context context, AppWidgetManager appWidgetManager,
			int appWidgetId) {
		SharedPreferences pref = context.getSharedPreferences(
				getPreferenceFileName(appWidgetId), Context.MODE_PRIVATE);
		pref.edit().remove(SELECTED_TIME).apply();
		render(context, appWidgetId);
	}

	private String getPreferenceFileName(int appWidgetId) {
		return PREFERENCE_FILE + appWidgetId;
	}

	private void render(Context context, int appWidgetId) {
		Calendar cal = Calendar.getInstance();
		int today = cal.get(Calendar.DAY_OF_YEAR);
		int todayYear = cal.get(Calendar.YEAR);

		// Obtain last selected time from SharedPreferences.
		SharedPreferences pref = context.getSharedPreferences(
				getPreferenceFileName(appWidgetId), Context.MODE_PRIVATE);
		long selectedTime = pref.getLong(SELECTED_TIME, cal.getTimeInMillis());
		cal.setTimeInMillis(selectedTime);

		RemoteViews widgetView = new RemoteViews(context.getPackageName(), R.layout.widget);
		widgetView.removeAllViews(R.id.calendar);

		if (pref.getBoolean(IS_WIDE, false)) {
			widgetView.setTextViewText(R.id.month_year_label, DateFormat.format("MMMM yyyy", cal));
		} else {
			widgetView.setTextViewText(R.id.month_year_label, DateFormat.format("MMM yyyy", cal));
		}

		// Keep track of what the selected month is.
		int selectedMonth = cal.get(Calendar.MONTH);

		// Set date to the first Sunday or Sunday of the first week of previous month.
		cal.set(Calendar.DATE, 1);
		cal.add(Calendar.DATE, 1 - cal.get(Calendar.DAY_OF_WEEK));

		for (int i = 0; i < WEEKS; i++) {
			RemoteViews rowView = new RemoteViews(context.getPackageName(), R.layout.row_week);

			for (int y = 0; y < 7; y++) {
				boolean isToday = cal.get(Calendar.DAY_OF_YEAR) == today &&
							cal.get(Calendar.YEAR) == todayYear;
				boolean inSelectedMonth = cal.get(Calendar.MONTH) == selectedMonth;

				int layoutId = R.layout.cell_day;
				if (isToday) {
					layoutId = R.layout.cell_today;
				} else if (inSelectedMonth) {
					layoutId = R.layout.cell_current_month_day;
				}

				RemoteViews dateView = new RemoteViews(context.getPackageName(), layoutId);
				dateView.setTextViewText(
						android.R.id.text1, Integer.toString(cal.get(Calendar.DATE)));
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

    private PendingIntent createPendingIntent(Context context, int appWidgetId, String action) {
    	Intent intent = new Intent(context, CalendarWidgetProvider.class);
    	intent.setAction(action);
    	intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
    	return PendingIntent.getBroadcast(context, appWidgetId, intent,
    			PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
