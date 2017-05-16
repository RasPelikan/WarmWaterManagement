package com.pelikanit.wwm.admin.wwm;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import com.pelikanit.wwm.WarmWaterManagement;

@Path("/wwm")
@Produces(MediaType.APPLICATION_JSON)
public class WarmWaterManagementService {
	
	@Context
	private WarmWaterManagement warmWaterManagement;
	
	@GET
	@Path("/shutdown")
	public String shutdown() {
		
		warmWaterManagement.shutdown();
		
		/*
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				
				// wait until response is sent to the browser
				synchronized (shutdownMutex) {
					
					try {
						
						shutdownMutex.wait();
						
					} catch (InterruptedException e) {
						// never mind
					}
					
				}
				
				warmWaterManagement.shutdown();
				
			}
			
		}).start();
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// never mind
		}
		*/
		
		return "SHUTDOWN now...";
		
		/*
		if (shutdown) {
			
			synchronized (shutdownMutex) {
				
				shutdownMutex.notify();
				
			}
			
		}
		 */
	}
		
	@GET
	@Path("/status")
	public Status status() {
		
		final Status result = new Status();
		
		final com.pelikanit.wwm.Status status = warmWaterManagement.getStatus();
		
		final float topSensorValue = status.getTopSensorValue();
		result.setTopSensorValue(topSensorValue);
		result.setTopSensorColor(getColorForTemperature(topSensorValue));
		final float middleSensorValue = status.getMiddleSensorValue();
		result.setMiddleSensorValue(middleSensorValue);
		result.setMiddleSensorColor(getColorForTemperature(middleSensorValue));
		final float bottomSensorValue = status.getBottomSensorValue();
		result.setBottomSensorValue(bottomSensorValue);
		result.setBottomSensorColor(getColorForTemperature(bottomSensorValue));
		result.setBathWaterInLitre(
				Math.round(status.getBathWaterInLitre()));
		result.setBathWaterTemperature(
				Math.round(status.getBathWaterTemperature()));
		final float numberOfBathes =
				((float) Math.round(status.getNumberOfBathesPossible() * 100)) / 100;
		result.setNumberOfBathesPossible(numberOfBathes);
		
		return result;
		
	}
	
	/**
	 * 100° = 255/0/0<br>
	 * 40° = 255/200/0<br>
	 * 37° = 255/185/30<br>
	 * 0° = 0/0/255
	 * 
	 * @param temperature
	 * @return
	 */
	private String getColorForTemperature(final float temperature) {
		
		final int r;
		final int g;
		final int b;
		
		if (temperature >= 100) {
			
			r = 255;
			g = 0;
			b = 0;
			
		} else if (temperature >= 40) {
			
			r = 255;
			g = (int) (155 - (155 / 60 * (temperature - 40)));
			b = (int) (30 - (30 / 60 * (temperature - 40)));
			
		} else if (temperature >= 32) {
			
			r = 255;
			g = (int) (155 + (30 / 8 * (8 - (temperature - 32))));
			b = 30;
			
		} else if (temperature >= 25) {

			r = (int) (55 + (200 / 7 * (temperature - 25)));
			g = (int) (55 + (130 / 7 * (temperature - 25)));
			b = (int) (30 + (225 / 7 * (7 - (temperature - 25))));
			
		} else if (temperature >= 0) {
			
			r = (int) (255 / 25 * (25 - temperature)); 
			g = (int) (255 / 25 * (25 - temperature)); 
			b = 255;
			
		} else {
			
			r = 255;
			g = 255;
			b = 255;
			
		}
		
		return String.format("rgb(%d, %d, %d)", r, g, b);
		
	}
		
}
