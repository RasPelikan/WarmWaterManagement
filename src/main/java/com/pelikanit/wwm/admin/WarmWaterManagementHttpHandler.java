package com.pelikanit.wwm.admin;

public class WarmWaterManagementHttpHandler extends CGIHttpHandler {

	public static final String PATH = "/wwm";
	
	private static final String RESOURCE_PACKAGE = "com/pelikanit";
	
	@Override
	protected String getResourcePackage() {
		
		return RESOURCE_PACKAGE;
		
	}

}
