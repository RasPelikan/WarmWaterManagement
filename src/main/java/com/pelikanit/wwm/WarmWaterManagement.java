package com.pelikanit.wwm;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.pelikanit.wwm.admin.HttpsAdmin;
import com.pelikanit.wwm.management.SensorStatus;
import com.pelikanit.wwm.management.SensorWatcher;
import com.pelikanit.wwm.utils.ConfigurationUtils;
import com.pi4j.wiringpi.GpioUtil;

/**
 * Main-class. Responsible for properly startup and
 * shutdown of all services:
 * <ul>
 * <li>Administration-HttpServer
 * <li>Gpio-Management
 * </ul>
 * 
 * @author stephan
 */
public class WarmWaterManagement implements Shutdownable {

	private static final Logger logger = Logger.getLogger(
			WarmWaterManagement.class.getCanonicalName());

	private static Thread shutdownHook;
	
	private static final Object shutdownMutex = new Object();
	
	private volatile boolean shutdown;
	
	private HttpsAdmin httpsAdmin;
	
	private SensorWatcher sensorWatcher;
	
	private float litreAvailable;
	
	private float bathWaterTemperature;
	
	private float coldWaterTemperature;
	
	private float bathSizeInLitre;

	/**
	 * Entry-point at startup.
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(final String[] args) throws Exception {
		
		// process arguments
		final ConfigurationUtils config = new ConfigurationUtils(args);
		
		// build central instance
		WarmWaterManagement main = new WarmWaterManagement();
		try {
			
			// initialize admin-httpserver, sensors,...
			main.initialize(config);
			
			// wait until shutdown
			while (!main.isShutdown()) {
				
				// go asleep
				synchronized (shutdownMutex) {
					
					try {
						shutdownMutex.wait();
					} catch (InterruptedException e) {
						// expected
					}
					
				}
				
			}
			
		}
		catch (Throwable e) {
			
			logger.log(Level.SEVERE, "Error running application", e);

			Runtime.getRuntime().removeShutdownHook(shutdownHook);
			
		}
		// shutdown
		finally {
			
			// stop admin-httpserver
			main.stop();
			
		}

	}
	
	// constructor
	public WarmWaterManagement() {
		
		// initialize properties
		shutdown = false;
		
		// capture "kill" commands and shutdown regularly
		shutdownHook = new Thread() {
					
					@Override
					public void run() {
						
						// shutdown
						shutdown();
						
					}
					
				};
		Runtime.getRuntime().addShutdownHook(shutdownHook);
				
	}
	
	/**
	 * @return whether anything caused a shutdown
	 */
	public boolean isShutdown() {
		
		return shutdown;
		
	}
	
	/**
	 * Shut's the mowing-robot down
	 */
	@Override
	public void shutdown() {
		
		// wake-up main-method to shutdown all services immediately
		synchronized (shutdownMutex) {
			
			shutdown = true;
			shutdownMutex.notify();
			
		}
		
	}
	
	private void stop() {
		
		if (httpsAdmin != null) {
			httpsAdmin.stop();
		}
		
		if (sensorWatcher != null) {
			sensorWatcher.shutdown();
		}
		
		logger.info("Shutdown complete");
		
	}
		
	private void initialize(final ConfigurationUtils config) throws Exception {
	
		initializeGpio(config);
		
		initializeProperties(config);
		
		initializeSensorWatcher(config);
		
		initializeAdminHttpServer(config);
		
		logger.info("Init complete");
		
	}

	private void initializeGpio(final ConfigurationUtils config) {
		
		GpioUtil.enableNonPrivilegedAccess();
		
	}

	private void initializeProperties(final ConfigurationUtils config) {
		
		litreAvailable = config.getLitreAvailable();
		bathWaterTemperature = config.getBathWaterTemperature();
		coldWaterTemperature = config.getColdWaterTemperature();
		bathSizeInLitre = config.getBathSizeInLitre();
		
	}
	
	private void initializeSensorWatcher(final ConfigurationUtils config) throws Exception {
		
		sensorWatcher = new SensorWatcher(
				config.getTopSensor(),
				config.getMiddleSensor(),
				config.getBottomSensor(),
				config.getSensorTemperature());
		
		sensorWatcher.start();
		
		logger.info("Init of SensorWatcher complete");
		
	}
	
	/**
	 * Initialize the Admin-HttpServer
	 * 
	 * @throws Exception
	 */
	private void initializeAdminHttpServer(
			final ConfigurationUtils config) throws Exception {

		httpsAdmin = new HttpsAdmin(config, this);
		
		httpsAdmin.start();
		
	}
	
	public Status getStatus() {
		
		final SensorStatus sensorStatus = sensorWatcher.getSensorStatus();
		
		final float bathWaterInLitre = getBathWaterInLitre(sensorStatus);
		
		final float bathWaterTemperature = getBathWaterTemperature();
		
		final float numberOfBathesPossible = getNumberOfBathesPossible(bathWaterInLitre);
		
		return new Status(
				sensorStatus.getTopSensorValue(),
				sensorStatus.getMiddleSensorValue(),
				sensorStatus.getBottomSensorValue(),
				sensorStatus.getSensorHeight(),
				bathWaterInLitre,
				bathWaterTemperature,
				numberOfBathesPossible);
		
	}

	private float getBathWaterTemperature() {
		
		return bathWaterTemperature;
		
	}

	private float getBathWaterInLitre(final SensorStatus sensorStatus) {
		
		final float topSectionLitre = litreAvailable * (1 - sensorStatus.getSensorHeight());
		final float bottomSectionLitre = litreAvailable * sensorStatus.getSensorHeight();
		
		final float currentTopSectionTemperature =
				(sensorStatus.getTopSensorValue() + sensorStatus.getMiddleSensorValue()) / 2;
		final float currentBottomSectionTemperature =
				(sensorStatus.getBottomSensorValue() + sensorStatus.getMiddleSensorValue()) / 2;
		
		final float calculatedTopSectionLitre;
		final float topSectionTemperature;
		if (currentTopSectionTemperature > bathWaterTemperature) {
			
			/*
			ww ... warm water
			wt ... warm water temperature
			cw ... cold water
			ct ... cold water temperature
			bt ... bath temperature
			
			ww * wt + cw * ct = (ww + cw) * bt
			ww * wt + cw * ct = ww * bt + cw * bt
			cw * ct - cw * bt = ww * bt - ww * wt
			cw * (ct - bt) = ww * bt - ww * wt
			cw = (ww * bt - ww * wt) / (ct - bt)
			*/
			calculatedTopSectionLitre = topSectionLitre +
					((topSectionLitre * getBathWaterTemperature()
							- topSectionLitre * currentTopSectionTemperature)
							/ (coldWaterTemperature - bathWaterTemperature));
			topSectionTemperature = bathWaterTemperature;
			
		} else {
			calculatedTopSectionLitre = topSectionLitre;
			topSectionTemperature = currentTopSectionTemperature;
		}
		
		final float calculatedBottomSectionLitre;
		final float bottomSectionTemperature;
		if (currentBottomSectionTemperature > bathWaterTemperature) {
			
			/*
			ww ... warm water
			wt ... warm water temperature
			cw ... cold water
			ct ... cold water temperature
			bt ... bath temperature
			
			ww * wt + cw * ct = (ww + cw) * bt
			ww * wt + cw * ct = ww * bt + cw * bt
			cw * ct - cw * bt = ww * bt - ww * wt
			cw * (ct - bt) = ww * bt - ww * wt
			cw = (ww * bt - ww * wt) / (ct - bt)
			*/
			calculatedBottomSectionLitre = bottomSectionLitre +
					((bottomSectionLitre * getBathWaterTemperature()
							- bottomSectionLitre * currentBottomSectionTemperature)
							/ (coldWaterTemperature - bathWaterTemperature));
			bottomSectionTemperature = bathWaterTemperature;
			
		} else {
			calculatedBottomSectionLitre = bottomSectionLitre;
			bottomSectionTemperature = currentBottomSectionTemperature;
		}
		
		// mix top and bottom section
		
		final float bathWater =
				(calculatedTopSectionLitre * topSectionTemperature
				+ calculatedBottomSectionLitre * bottomSectionTemperature) / getBathWaterTemperature();
		
		return bathWater;
		
	}
	
	private float getNumberOfBathesPossible(final float bathWaterInLitre) {
		
		return bathWaterInLitre / bathSizeInLitre;
		
	}
	
}
