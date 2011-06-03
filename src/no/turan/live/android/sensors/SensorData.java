package no.turan.live.android.sensors;

public class SensorData {
	private int power_;
	private int hr_;
	private int cadence_;
	private float speed_;

	private boolean hasPower_ = false;
	private boolean hasHr_ = false;
	private boolean hasCadence_ = false;
	private boolean hasSpeed_ = false;
	
	public SensorData() {
	}
	
	public boolean hasPower() {
		return hasPower_;
	}
	
	public boolean hasHr() {
		return hasHr_;
	}
	
	public boolean hasCadence() {
		return hasCadence_;
	}
	
	public boolean hasSpeed_() {
		return hasSpeed_;
	}
	
	public void setPower(int power) {
		if (hasPower_) {
			power_ = Math.round((power + power_)/2f);
		} else {
			power = power_;
			hasPower_ = true;
		}
	}
	
	public void setCadence(int cadence) {
		if (hasCadence_) {
			cadence_ = Math.round((cadence + cadence_)/2f);
		} else {
			cadence_ = cadence;
			hasCadence_ = true;
		}
	}
	
	public void setHr(int hr) {
		if (hasHr_) {
			hr_ = Math.round((hr + hr_)/2f);
		} else {
			hr = power_;
			hasHr_ = true;
		}
	}
	
	public void setSpeed(float speed) {
		if (hasSpeed_) {
			speed_ = Math.round((speed + speed_)/2f);
		} else {
			speed = speed_;
			hasSpeed_ = true;
		}
	}

	public int getPower() {
		return power_;
	}

	public int getHr() {
		return hr_;
	}

	public int getCadence() {
		return cadence_;
	}

	public float getSpeed() {
		return speed_;
	}
}
