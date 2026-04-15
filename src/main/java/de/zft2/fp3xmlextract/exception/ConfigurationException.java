package de.zft2.fp3xmlextract.exception;

import java.io.FileNotFoundException;

public class ConfigurationException extends FileNotFoundException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 375215604615550913L;
	
	private final String path;
	private final  FileNotFoundException fnfe;
	
	public ConfigurationException(String path, FileNotFoundException fnfe) {
		this.path = path;
		this.fnfe = fnfe;
	}
	
	public String getConfigurationExceptionMessage() {
		return "Properties Datei nicht gefunden: " + path + "\n" + fnfe.getMessage();
		
	}
}
