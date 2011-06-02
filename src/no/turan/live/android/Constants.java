package no.turan.live.android;

public abstract class Constants {
	/**
	 * Used by log statements.
	 */
	public static final String TAG = "TuranLive";
	public static final int DEAD_SAMPLE_THRESHOLD = 10;
	
	public static final String SAMPLE_TIME_KEY = "no.turan.live.android.TIME";
	public static final String SAMPLE_EXERCISE_KEY = "no.turan.live.android.EXERCISE";
	public static final String SAMPLE_HR_KEY = "no.turan.live.android.HR";
	public static final String SAMPLE_SPEED_KEY = "no.turan.live.android.SPEED";
	public static final String SAMPLE_CADENCE_KEY = "no.turan.live.android.CADENCE";
	public static final String SAMPLE_POWER_KEY = "no.turan.live.android.POWER";
	public static final String SAMPLE_ALTITUDE_KEY = "no.turan.live.android.ALTITUDE";
	public static final String SAMPLE_LATITUDE_KEY = "no.turan.live.android.LATITUDE";
	public static final String SAMPLE_LONGITUDE_KEY = "no.turan.live.android.LONGITUDE";
	
	public static final float MIN_GPS_ACCURACY = 20f;
	public static final float MPS_TO_KPH = 3.6f;
	
	public enum ExerciseState{
		FINISHED ('F'),
		PAUSED   ('P'),
		LIVE     ('L');
		
		private final char flag_;
		ExerciseState(char flag) {
			flag_ = flag;
		}
		
		public char getFlag() {
			return flag_;
		}
	}
	/**	
	 * This is an abstract utility class.
	 */
	protected Constants() { }
}
