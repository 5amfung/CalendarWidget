package co.sfng.android.calendarwidget;

import java.util.Calendar;

import android.annotation.TargetApi;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.RemoteViews;


public class CalendarWidgetProvider extends AppWidgetProvider {

	private static final String LOG_TAG = CalendarWidgetProvider.class.toString();

	@Override
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager,
			int appWidgetId, Bundle newOptions) {
		Log.i(LOG_TAG, "onAppWidgetOptionsChanges()");
		super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId,	newOptions);

		Bundle options = appWidgetManager.getAppWidgetOptions(appWidgetId);
		int minWidthInDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
		int maxWidthInDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH);

		int minHeightInDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT);
		int maxHeightInDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT);

		Log.i(LOG_TAG, "minWidthInDp = " + minWidthInDp);
		Log.i(LOG_TAG, "maxWidthInDp = " + maxWidthInDp);

		Log.i(LOG_TAG, "minHeightInDp = " + minHeightInDp);
		Log.i(LOG_TAG, "maxHeightInDp = " + maxHeightInDp);

		render(context, appWidgetId);
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		Log.i(LOG_TAG, "onUpdate()");
		super.onUpdate(context, appWidgetManager, appWidgetIds);

		for (int appWidgetId: appWidgetIds) {
			Log.i(LOG_TAG, "app widget id = " + appWidgetId);
			render(context, appWidgetId);
		}
	}

	private void render(Context context, int appWidgetId) {
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		RemoteViews view = new RemoteViews(context.getPackageName(), R.layout.widget);

		Calendar cal = Calendar.getInstance();
		view.setTextViewText(R.id.month_year_label, DateFormat.format("MMM yyyy", cal));
		view.removeAllViews(R.id.calendar);

		appWidgetManager.updateAppWidget(appWidgetId, view);
	}
}
