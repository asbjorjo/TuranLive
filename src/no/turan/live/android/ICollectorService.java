package no.turan.live.android;

public interface ICollectorService {
	public boolean isLive();
	public boolean isCollecting();
	public void goLive(int exerciseId);
	public void goOff();
	public int getExercise();
}
