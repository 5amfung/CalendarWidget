package co.sfng.calendarwidget;

import android.app.Activity;
import android.os.Bundle;


public class ConfigureActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setResult(RESULT_CANCELED);
		setContentView(R.layout.appwidget_settings);

	}

}
