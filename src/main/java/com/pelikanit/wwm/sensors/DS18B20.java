package com.pelikanit.wwm.sensors;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DS18B20 {

	private static final Logger logger = Logger.getLogger(
			DS18B20.class.getCanonicalName());
	
	private static final Pattern tempLinePattern = Pattern.compile(
			" t=([0-9]+)");
	
	private File w1_slave;
	
	public DS18B20(final File w1_slave) {
		
		if (w1_slave == null) {
			throw new RuntimeException(
					"No 'w1_slave' file given for initialization of DS18B20");
		}
		if (!w1_slave.exists()) {
			throw new RuntimeException(
					"w1_slave-file '"
					+ w1_slave.getAbsolutePath()
					+ "' does not exist!");
		}
		if (!w1_slave.isFile()) {
			throw new RuntimeException(
					"w1_slave-file '"
					+ w1_slave.getAbsolutePath()
					+ "' is not a file!");
		}
		if (!w1_slave.canRead()) {
			throw new RuntimeException(
					"Cannot read from w1_slave-file '"
					+ w1_slave.getAbsolutePath()
					+ "' - maybe permissions do not suit!");
		}
		
		this.w1_slave = w1_slave;
		
	}
	
	public float getTemperature() throws Exception {
		
		// read temperature, gives '43375' for 43.375°
		final String temperatureAsString = readTemperature();
		
		// now 43375.0 as float
		final float temperatureAsFloat = Float.parseFloat(temperatureAsString);
		
		// divide by 1000 to get 43.375
		return temperatureAsFloat / 1000;
		
	}
	
	private String readTemperature() throws Exception {
		
		final StringBuilder lines = new StringBuilder();
		
		BufferedReader reader = null;
		try {
			
			reader = new BufferedReader(new FileReader(w1_slave));
			
			String line = null;
			while ((line = reader.readLine()) != null) {
				
				final Matcher matcher = tempLinePattern.matcher(line);
				if (matcher.find()) {
					
					return matcher.group(1);
					
				} else {
					
					lines.append(line);
					lines.append('\n');
					
				}
				
			}
			
		} finally {
			
			if (reader != null) {
				
				try {
					reader.close();
				} catch (Throwable e) {
					logger.log(Level.WARNING,
							"Could not close reader for w1_slave-file!",
							e);
				}
				
			}
			
		}
		
		throw new Exception(
				"Could not find temperature in w1_slave-output '"
				+ lines.toString()
				+ "'!");
		
	}
	
}
