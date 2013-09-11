package co.sfng.android.calendarwidget;

import java.util.logging.Logger;

import android.annotation.TargetApi;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;


public class CalendarWidgetProvider extends AppWidgetProvider {

	private final static Logger log = Logger.getLogger(CalendarWidgetProvider.class.toString());
	
	@Override
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId,
			Bundle newOptions) {
		super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId,	newOptions);
		log.info("onAppWidgetOptionsChanges()");
	}

	@Override
	public void onEnabled(Context context) {
		super.onEnabled(context);
		log.info("onEnabled");
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		log.info("onUpdate()");
	}
}
