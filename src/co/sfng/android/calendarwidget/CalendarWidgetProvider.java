package co.sfng.android.calendarwidget;

import java.util.Calendar;

import android.annotation.TargetApi;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.RemoteViews;


public class CalendarWidgetProvider extends AppWidgetProvider {

	private static final String LOG_TAG = CalendarWidgetProvider.class.toString();
	@SuppressWarnings("unused")
	private static final String PREVIOUS_ACTION =
			"co.sfng.android.calendarwidget.action.PREVIOUS_MONTH";
	@SuppressWarnings("unused")
	private static final String NEXT_ACTION =
			"co.sfng.android.calendarwidget.action.NEXT_MONTH";
	private static final int WEEKS = 6;

	@Override
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager,
			int appWidgetId, Bundle newOptions) {
		Log.i(LOG_TAG, "onAppWidgetOptionsChanged()");
		super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId,	newOptions);
		render(context, appWidgetId);
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		Log.i(LOG_TAG, "onUpdate()");
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		for (int appWidgetId: appWidgetIds) {
			render(context, appWidgetId);
		}
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);
		String action = intent.getAction();
		Log.i(LOG_TAG, "action = " + action);
	}

	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		Log.i(LOG_TAG, "onDeleted()");
		super.onDeleted(context, appWidgetIds);

		for (int appWidgetId: appWidgetIds) {
			Log.i(LOG_TAG, "Clear all preferences for widget ID " + appWidgetId);
			SharedPreferences pref = context.getSharedPreferences(
					getPreferenceFileName(context, appWidgetId), Context.MODE_PRIVATE);
			SharedPreferences.Editor editor = pref.edit();
			editor.clear().apply();
		}
	}

	private String getPreferenceFileName(Context context, int appWidgetId) {
		return String.format(context.getString(R.string.preference_file_key), appWidgetId);
	}

	private void render(Context context, int appWidgetId) {
		// TODO:
		//   x Update current month-year label.
		//   x Render current month (6 weeks).
		//   - Attach action to month label.
		//   - Attach action to previous and next button.
		//   - Store selected month.  If displayed month is current, no need to store.
		//     Always show current month if no month is stored in SharedPreference.

		RemoteViews widgetView = new RemoteViews(context.getPackageName(), R.layout.widget);

		// Obtain last selected time from SharedPreferences.
		Calendar cal = Calendar.getInstance();
		int today = cal.get(Calendar.DAY_OF_YEAR);
		int todayYear = cal.get(Calendar.YEAR);
		Log.i(LOG_TAG, "today of the year = " + today);
		Log.i(LOG_TAG, "year = " + todayYear);

		SharedPreferences pref = context.getSharedPreferences(
				getPreferenceFileName(context, appWidgetId), Context.MODE_PRIVATE);
		long selectedTime = pref.getLong(
				context.getString(R.string.selected_time), cal.getTimeInMillis());
		cal.setTimeInMillis(selectedTime);

		// TODO: Use full month name (MMMM) for wider widget.
		widgetView.setTextViewText(R.id.month_year_label, DateFormat.format("MMM yyyy", cal));
		widgetView.removeAllViews(R.id.calendar);

		// Remember what the selected month is.
		int selectedMonth = cal.get(Calendar.MONTH);
		Log.i(LOG_TAG, "selected month = " + selectedMonth);

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

		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		appWidgetManager.updateAppWidget(appWidgetId, widgetView);
	}









}
