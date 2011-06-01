package no.turan.live;

public abstract class Constants {
	/**
	 * Used by log statements.
	 */
	public static final String TAG = "TuranLive";
	public static final int DEAD_SAMPLE_THRESHOLD = 10;
	public static final Integer EXERCISE_ID = 4711;
	
	public static final String SAMPLE_TIME_KEY = "no.turan.live.android.TIME";
	public static final String SAMPLE_EXERCISE_KEY = "no.turan.live.android.EXERCISE";
	public static final String SAMPLE_HR_KEY = "no.turan.live.android.HR";
	public static final String SAMPLE_SPEED_KEY = "no.turan.live.android.SPEED";
	public static final String SAMPLE_CADENCE_KEY = "no.turan.live.android.CADENCE";
	public static final String SAMPLE_POWER_KEY = "no.turan.live.android.POWER";
	public static final String SAMPLE_ALTITUDE_KEY = "no.turan.live.android.ALTITUDE";
	public static final String SAMPLE_LATITUDE_KEY = "no.turan.live.android.LATITUDE";
	public static final String SAMPLE_LONGITUDE_KEY = "no.turan.live.android.LONGITUDE";
	
	/**	
	 * This is an abstract utility class.
	 */
	protected Constants() { }
}
