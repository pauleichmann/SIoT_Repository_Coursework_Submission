package cn.cjgl.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;

public class DateTimeUtil {

	private static String sdf10 = "yyyy-MM-dd";

	private static String sdf19 = "yyyy-MM-dd HH:mm:ss";

	private static String sdf23 = "yyyy-MM-dd HH:mm:ss:SSS";

	/**
	 * Calculate the number of minutes from midnight (12:00 AM)
	 *
	 * @param dateTime Containing dates and times LocalDateTime object
	 * @return the number of minutes from midnight
	 */
	public static int calculateMinutesFromMidnight(LocalDateTime dateTime) {
		// Obtain the midnight time of the day
		LocalDateTime midnight = dateTime.withHour(0).withMinute(0).withSecond(0).withNano(0);

		// Calculate the time difference and convert it to minutes
		long minutesDifference = java.time.Duration.between(midnight, dateTime).toMinutes();

		// Return the number of minutes (converted to int type)
		return (int) minutesDifference;
	}

	/**
	 * yyyy-MM-dd
	 * @return
	 */
	public static final String getDateTime10() {
		return getDateTime10(System.currentTimeMillis());
	}
	
	/**
	 * yyyy-MM-dd
	 * @param dataStr10
	 * @return
	 */
	public static final long getLongTime10(String dataStr10) {
		Date d = new Date();
		try {
			d = new SimpleDateFormat(sdf10).parse(dataStr10);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return d.getTime();
	}

	/**
	 * yyyy-MM-dd
	 * @param time
	 * @return
	 */
	public static final String getDateTime10(long time) {
		Date d = new Date(time);
		return new SimpleDateFormat(sdf10).format(d);
	}
	
	/**
	 * yyyy-MM-dd
	 * @param time
	 * @return
	 */
	public static final Date getDateTime10Str(String time) {
		Date d = new Date();
		try {
			d = new SimpleDateFormat(sdf10).parse(time);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return d;
	}

	/**
	 * yyyy-MM-dd HH:mm:ss
	 * @return
	 */
	public static final String getDateTime19() {
		return getDateTime19(System.currentTimeMillis());
	}
	
	/**
	 * yyyy-MM-dd HH:mm:ss
	 * @param time
	 * @return
	 */
	public static final String getDateTime19(long time) {
		Date d = new Date(time);
		return new SimpleDateFormat(sdf19).format(d);
	}
	
	/**
	 * yyyy-MM-dd HH:mm:ss
	 * @param time
	 * @return
	 */
	public static final Date getDateTime19Str(String time) {
		Date d = new Date();
		try {
			d = new SimpleDateFormat(sdf19).parse(time);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return d;
	}

	/**
	 * yyyy-MM-dd HH:mm:ss:SSS
	 * @return
	 */
	public static final String getDateTime23() {
		return getDateTime23(System.currentTimeMillis());
	}

	/**
	 * yyyy-MM-dd HH:mm:ss:SSS
	 * @param time
	 * @return
	 */
	public static final String getDateTime23(long time) {
		Date d = new Date(time);
		return new SimpleDateFormat(sdf23).format(d);
	}

	/**
	 * yyyy-MM-dd HH:mm:ss:SSS
	 * @param dataStr23
	 * @return
	 */
	public static final long getLongTime23(String dataStr23) {
		Date d = new Date();
		try {
			d = new SimpleDateFormat(sdf23).parse(dataStr23);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return d.getTime();
	}

	/**
	 * Calculate the start date of the week
	 * 
	 * @param date
	 * @return
	 */
	public static String getWeekDateStart(String date) {
		try {
			Calendar cal = Calendar.getInstance();
			cal.setTime(new SimpleDateFormat("yyyy-MM-dd").parse(date));
			int d = 0;
			if (cal.get(Calendar.DAY_OF_WEEK) == 1) {
				d = -6;
			} else {
				d = 2 - cal.get(Calendar.DAY_OF_WEEK);
			}
			cal.add(Calendar.DAY_OF_WEEK, d);

			// start date
			String weekStart = new SimpleDateFormat("yyyy-MM-dd").format(cal
					.getTime());

			return weekStart;

		} catch (Exception e) {
			e.printStackTrace();
		}

		return "";
	}

	/**
	 * Calculate the end date of a week
	 * 
	 * @param date
	 * @return
	 */
	public static String getWeekDateEnd(String date) {
		try {
			Calendar cal = Calendar.getInstance();
			cal.setTime(new SimpleDateFormat("yyyy-MM-dd").parse(date));
			int d = 0;
			if (cal.get(Calendar.DAY_OF_WEEK) == 1) {
				d = -6;
			} else {
				d = 2 - cal.get(Calendar.DAY_OF_WEEK);
			}
			cal.add(Calendar.DAY_OF_WEEK, d);

			// end date
			cal.add(Calendar.DAY_OF_WEEK, 6);
			String weekEnd = new SimpleDateFormat("yyyy-MM-dd").format(cal
					.getTime());

			return weekEnd;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return "";
	}

	/**
	 * Calculate the start date of a month
	 * 
	 * @param date
	 * @return
	 */
	public static String getMonthDateStart(String date) {
		try {
			String monthStart = date.substring(0, 8) + "01";
			return monthStart;

		} catch (Exception e) {
			e.printStackTrace();
		}

		return "";
	}

	/**
	 * Calculate the end date of a month
	 * 
	 * @param date
	 * @return
	 */
	public static String getMonthDateEnd(String date) {
		try {
			String monthStart = date.substring(0, 8) + "01";

			// Get the last day of this month
			Calendar cal = Calendar.getInstance();
			cal.setTime(new SimpleDateFormat("yyyy-MM-dd").parse(monthStart));
			cal.add(Calendar.MONTH, 1);
			cal.add(Calendar.DAY_OF_MONTH, -1);
			String monthEnd = new SimpleDateFormat("yyyy-MM-dd").format(cal
					.getTime());

			return monthEnd;

		} catch (Exception e) {
			e.printStackTrace();
		}

		return "";
	}

}
