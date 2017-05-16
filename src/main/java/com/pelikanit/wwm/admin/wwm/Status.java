package com.pelikanit.wwm.admin.wwm;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "status")
public class Status {

	private float topSensorValue;
	
	private String topSensorColor;

	private float middleSensorValue;
	
	private String middleSensorColor;

	private float bottomSensorValue;
	
	private String bottomSensorColor;
	
	private int bathWaterInLitre;
	
	private int bathWaterTemperature;
	
	private float numberOfBathesPossible;

	public float getTopSensorValue() {
		return topSensorValue;
	}

	public void setTopSensorValue(float topSensorValue) {
		this.topSensorValue = topSensorValue;
	}

	public String getTopSensorColor() {
		return topSensorColor;
	}

	public void setTopSensorColor(String topSensorColor) {
		this.topSensorColor = topSensorColor;
	}

	public float getMiddleSensorValue() {
		return middleSensorValue;
	}

	public void setMiddleSensorValue(float middleSensorValue) {
		this.middleSensorValue = middleSensorValue;
	}

	public String getMiddleSensorColor() {
		return middleSensorColor;
	}

	public void setMiddleSensorColor(String middleSensorColor) {
		this.middleSensorColor = middleSensorColor;
	}

	public float getBottomSensorValue() {
		return bottomSensorValue;
	}

	public void setBottomSensorValue(float bottomSensorValue) {
		this.bottomSensorValue = bottomSensorValue;
	}

	public String getBottomSensorColor() {
		return bottomSensorColor;
	}

	public void setBottomSensorColor(String bottomSensorColor) {
		this.bottomSensorColor = bottomSensorColor;
	}

	public float getBathWaterInLitre() {
		return bathWaterInLitre;
	}

	public void setBathWaterInLitre(int bathWaterInLitre) {
		this.bathWaterInLitre = bathWaterInLitre;
	}

	public float getBathWaterTemperature() {
		return bathWaterTemperature;
	}

	public void setBathWaterTemperature(int bathWaterTemperature) {
		this.bathWaterTemperature = bathWaterTemperature;
	}

	public float getNumberOfBathesPossible() {
		return numberOfBathesPossible;
	}

	public void setNumberOfBathesPossible(float numberOfBathesPossible) {
		this.numberOfBathesPossible = numberOfBathesPossible;
	}
	
}
