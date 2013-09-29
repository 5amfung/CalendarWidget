package co.sfng.calendarwidget;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;


public class ConfigureActivity extends Activity {

	private static final String TAG = ConfigureActivity.class.toString();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.appwidget_settings);
		findViewById(R.id.ok_button).setOnClickListener(mOnOkClickListener);
		findViewById(R.id.cancel_button).setOnClickListener(mOnCancelClickListener);

		Log.i(TAG, "onCreate()");
	}

	private OnClickListener mOnOkClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Log.i(TAG, "Ok button is clicked.");

			// TODO: Save settings to SharedPreferences.

			// Obtain all app widget IDs.
			Context context = getApplicationContext();
			AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
			ComponentName cn = new ComponentName(context, CalendarWidgetProvider.class);
			int[] appWidgetIds = appWidgetManager.getAppWidgetIds(cn);

			// Broadcast update action to all running app widgets.
			Intent intent = new Intent();
			intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
			intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
			context.sendBroadcast(intent);

			finish();
		}
	};

	private OnClickListener mOnCancelClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			finish();
		}
	};
}
