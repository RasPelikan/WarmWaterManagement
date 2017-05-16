package com.pelikanit.wwm.sensors;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

public class DS18B20Test {

	private DS18B20 sensor;
	
	@Before
	public void setUp() {
		
		final File testInput = new File("./src/test/resources/DS18B20-output.txt");
		sensor = new DS18B20(testInput);
		
	}
	
	@Test
	public void testInitialization() {
		
		// null-file
		try {
			new DS18B20(null);
			fail("Expecting to get an runtime-expected but DS18B20-constructor suceeded!");
		} catch (RuntimeException e) {
			// expected
		}

		// non-existing file
		try {
			new DS18B20(new File("does-not-exist"));
			fail("Expecting to get an runtime-expected but DS18B20-constructor suceeded!");
		} catch (RuntimeException e) {
			// expected
		}

		// not a file, but a directory
		try {
			new DS18B20(new File("./src"));
			fail("Expecting to get an runtime-expected but DS18B20-constructor suceeded!");
		} catch (RuntimeException e) {
			// expected
		}
		
	}
	
	@Test
	public void testReadingValue() throws Exception {
		
		final float temp = sensor.getTemperature();
		assertEquals("Got another temperature then expected!", 43.375f, temp, 0);
		
	}

}
