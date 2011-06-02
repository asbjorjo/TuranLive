package no.turan.live.android;

public interface ICollectorService {
	public boolean isLive();
	public boolean isCollecting();
	public void goLive();
	public void goOff();
}
