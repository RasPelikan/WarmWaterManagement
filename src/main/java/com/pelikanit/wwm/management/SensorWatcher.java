package com.pelikanit.wwm.management;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.pelikanit.wwm.Shutdownable;
import com.pelikanit.wwm.sensors.DS18B20;

public class SensorWatcher extends Thread implements Shutdownable {

	private static final Logger logger = Logger.getLogger(
			SensorWatcher.class.getCanonicalName());
	
	private static long FASTCHANGE_TIMEOUT = 1;
	private static long SLOWCHANGE_TIMEOUT = 15;
	
	private static Object sleepMutex = new Object();
	private static Object shutdownMutex = new Object();
	
	private DS18B20 topSensor;
	
	private DS18B20 middleSensor;

	private DS18B20 bottomSensor;

	private boolean fastChange;
	
	private volatile boolean shutdown;
	
	private volatile float topSensorValue;

	private volatile float bottomSensorValue;

	private volatile float middleSensorValue;
	
	private long heightInSteps = 4711; // to be measured
	
	private volatile float sensorHeight = 0.4f;
	
	private long sensorHeightInSteps;
	
	private float sensorTemperature;
	
	public SensorWatcher(final DS18B20 topSensor, final DS18B20 middleSensor,
			final DS18B20 bottomSensor, final float sensorTemperature) {
		
		this.topSensor = topSensor;
		this.middleSensor = middleSensor;
		this.bottomSensor = bottomSensor;
		this.topSensor = topSensor;
		this.sensorTemperature = sensorTemperature;
		
		fastChange = false;
		shutdown = false;
		
	}
	
	private long getTimeout() {
		
		final long timeoutInSeconds =
				fastChange ? FASTCHANGE_TIMEOUT : SLOWCHANGE_TIMEOUT;
		return timeoutInSeconds * 1000;
		
	}
	
	@Override
	public void run() {
		
		try {
			
			initializeSensors();
			
			long timeToWait = getTimeout();
			long remainingTime = timeToWait;
			
			synchronized (sleepMutex) {
				
				while (!shutdown) {
					
					final long before = System.currentTimeMillis();
					try {
						
						sleepMutex.wait(remainingTime);
						
					} catch (InterruptedException e) {
						// never mind
					}
					final long diff = System.currentTimeMillis() - before;
					if (diff < timeToWait) {
						
						remainingTime -= diff;
						timeToWait -= diff;
						
					} else {
						
						processSensors();
						
						timeToWait = getTimeout();
						remainingTime = timeToWait;
						
					}
					
				}
				
			}
			
			synchronized (shutdownMutex) {
				
				shutdownMutex.notify();
				
			}
			
		} catch (Exception e) {
			
			logger.log(Level.SEVERE,
					"Could not watch sensors!",
					e);
			
		}
		
	}
	
	public void shutdown() {
		
		synchronized (sleepMutex) {
			
			shutdown = true;
			sleepMutex.notify();
			
		}
		
		synchronized (shutdownMutex) {
			
			try {
				
				shutdownMutex.wait();
				
			} catch (InterruptedException e) {
				// never mind
			}
			
		}
		
		logger.info("Shutdown of SensorWatcher done...");
	}
	
	private void initializeSensors() throws Exception {
		
		processSensors();
		
	}
	
	private void processSensors() throws Exception {
		
		topSensorValue = topSensor.getTemperature();
		middleSensorValue = middleSensor.getTemperature();
		bottomSensorValue = bottomSensor.getTemperature();
		
	}
	
	public SensorStatus getSensorStatus() {
		
		return new SensorStatus(
				topSensorValue,
				middleSensorValue,
				bottomSensorValue,
				sensorHeight);
		
	}

}
