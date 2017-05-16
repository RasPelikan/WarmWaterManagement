package com.pelikanit.wwm.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.pelikanit.wwm.sensors.DS18B20;

public class ConfigurationUtils {
	
	private static final Logger logger = Logger.getLogger(
			ConfigurationUtils.class.getCanonicalName());
	
	private static final String PROPS_SENSORS_TOP = "sensors.top";
	private static final String PROPS_SENSORS_MIDDLE = "sensors.middle";
	private static final String PROPS_SENSORS_BOTTOM = "sensors.bottom";
	private static final String PROPS_LITRE_AVAILABLE = "litre.available";
	private static final String PROPS_SENSOR_TEMPERATURE = "sensor.temperature";
	private static final String PROPS_BATHWATER_TEMPERATURE = "bathwater.temperature";
	private static final String PROPS_COLDWATER_TEMPERATURE = "coldwater.temperature";
	private static final String PROPS_BATHSIZE_INLITRE = "bathsize.inlitre";
	private static final String PROPS_HTTPSADMIN_KEYSTORE = "httpsadmin.keystore";
	private static final String PROPS_HTTPSADMIN_KEYSTOREPASSWORD = "httpsadmin.keystore.password";
	private static final String PROPS_HTTPSADMIN_HOST = "httpsadmin.host";
	private static final String PROPS_HTTPSADMIN_PORT = "httpsadmin.port";

	private Properties props;
	
	public ConfigurationUtils(final String[] args) {
		
		if (args.length < 1) {
			System.err.println("Expecting configuration-file as argument!");
		}
		if (args.length > 1) {
			System.err.println("Expecting configuration-file as argument!");
		}
		
		InputStream in = null;
		try {
			
			final File propsFile = new File(args[0]);
			in = new FileInputStream(propsFile);
			
			props = new Properties();
			props.load(in);
			
		} catch (Exception e) {
			
			logger.log(Level.SEVERE,
					"Could not parse properties '"
					+ args[0]
					+ "'",
					e);
			Runtime.getRuntime().exit(1);
			
		} finally {
			
			if (in != null) {
				
				try {
					in.close();
				} catch (Throwable e) {
					logger.log(Level.WARNING,
							"Could not close input-stream of properties '"
							+ args[1]
							+ "'",
							e);
				}
				
			}
			
		}
		
	}
	
	public DS18B20 getTopSensor() {
		
		return getSensor(PROPS_SENSORS_TOP);
		
	}
	
	public DS18B20 getMiddleSensor() {
		
		return getSensor(PROPS_SENSORS_MIDDLE);
		
	}

	public DS18B20 getBottomSensor() {
		
		return getSensor(PROPS_SENSORS_BOTTOM);
		
	}

	private DS18B20 getSensor(final String sensorProperty) {
		
		final String topSensorPath = props.getProperty(sensorProperty);
		
		final File topSensorFile = new File(topSensorPath);
		
		return new DS18B20(topSensorFile);
		
	}

	public float getLitreAvailable() {
		
		return getFloatPropertyAvailable(PROPS_LITRE_AVAILABLE);
		
	}
	
	public float getSensorTemperature() {
		
		return getFloatPropertyAvailable(PROPS_SENSOR_TEMPERATURE);
		
	}
	
	public float getBathWaterTemperature() {
		
		return getFloatPropertyAvailable(PROPS_BATHWATER_TEMPERATURE);
		
	}
	
	public float getColdWaterTemperature() {
		
		return getFloatPropertyAvailable(PROPS_COLDWATER_TEMPERATURE);
		
	}

	public float getBathSizeInLitre() {
		
		return getFloatPropertyAvailable(PROPS_BATHSIZE_INLITRE);
		
	}
	
	public String getHttpsAdminKeystore() {
		
		final String keystore = props.getProperty(PROPS_HTTPSADMIN_KEYSTORE);
		
		if (keystore == null) {
			throw new RuntimeException("No property '"
					+ PROPS_HTTPSADMIN_KEYSTORE
					+ "' given. Use 'keytool -genkey -keyalg RSA -alias selfsigned -keystore test.jks -storepass any_password_you_like -keysize 2048' to build it");
		}
		
		return keystore;
		
	}
	
	public String getHttpsAdminKeystorePassword() {
		
		return props.getProperty(PROPS_HTTPSADMIN_KEYSTOREPASSWORD);
		
	}

	public int getHttpsAdminPort() {
		
		return getIntPropertyAvailable(PROPS_HTTPSADMIN_PORT);
		
	}
	
	public String getHttpsAdminHost() {
		
		return props.getProperty(PROPS_HTTPSADMIN_HOST);
		
	}
	
	private float getFloatPropertyAvailable(final String propsName) {
		
		final String value = props.getProperty(propsName);
		if (value == null) {
			throw new NoSuchElementException(propsName);
		}
		try {
			
			return Float.parseFloat(value);
			
		} catch(Exception e) {
			
			throw new RuntimeException(
					"Could not read property '"
					+ propsName
					+ "' as float. Given value is '"
					+ value
					+ "'", e);
			
		}
		
	}
		
	private int getIntPropertyAvailable(final String propsName) {
		
		final String value = props.getProperty(propsName);
		if (value == null) {
			throw new NoSuchElementException(propsName);
		}
		try {
			
			return Integer.parseInt(value);
			
		} catch(Exception e) {
			
			throw new RuntimeException(
					"Could not read property '"
					+ propsName
					+ "' as int. Given value is '"
					+ value
					+ "'", e);
			
		}
		
	}

}
