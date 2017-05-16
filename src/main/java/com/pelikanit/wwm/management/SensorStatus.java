package com.pelikanit.wwm.management;

public class SensorStatus {

	private float topSensorValue;
	
	private float middleSensorValue;
	
	private float bottomSensorValue;
	
	private float sensorHeight;

	public SensorStatus(float topSensorValue, float middleSensorValue,
			float bottomSensorValue, float sensorHeight) {
		this.topSensorValue = topSensorValue;
		this.middleSensorValue = middleSensorValue;
		this.bottomSensorValue = bottomSensorValue;
		this.sensorHeight = sensorHeight;
	}

	public float getTopSensorValue() {
		return topSensorValue;
	}

	public float getMiddleSensorValue() {
		return middleSensorValue;
	}

	public float getBottomSensorValue() {
		return bottomSensorValue;
	}

	public float getSensorHeight() {
		return sensorHeight;
	}
	
}
