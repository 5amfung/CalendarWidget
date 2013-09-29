package co.sfng.calendarwidget;

public class ResourceHelper {

	@SuppressWarnings("unused")
	private static final int DARK_THEME = 0;
	private static final int LIGHT_THEME = 1;

	public static int layoutCellDay(int theme) {
		return theme == LIGHT_THEME ? R.layout.cell_day_light : R.layout.cell_day_dark;
	}

	public static int layoutCellToday(int theme) {
		return theme == LIGHT_THEME ? R.layout.cell_today_light : R.layout.cell_today_dark;
	}

	public static int layoutCellInMonth(int theme) {
		return theme == LIGHT_THEME ? R.layout.cell_in_month_light : R.layout.cell_in_month_dark;
	}

	public static int layoutWidget(int theme) {
		return theme == LIGHT_THEME ? R.layout.widget_light : R.layout.widget_dark;
	}

}
