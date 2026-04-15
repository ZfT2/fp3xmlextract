package de.fp3xmlextract.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Properties;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.fp3xmlextract.exception.ConfigurationException;

public class Fp3xmlextractProperties extends Properties {

	private static Logger log = LogManager.getLogger(Fp3xmlextractProperties.class);

	/**
	 * 
	 */
	private static final long serialVersionUID = 1735959545579391865L;

	private static String baseDir;
	private static final String SUB_PATH = "properties/";

	private static Collection<Fp3xmlextractProperties> instances = new ArrayList<>();
	private String fileName;

	private Fp3xmlextractProperties() {
	}

	private static void initBaseDir(String propertiesFile) {
		
		URL divergentBase = Fp3xmlextractProperties.class.getResource("/basePath.properties");
		if (divergentBase != null) {
			Properties basePathProperties = new Properties();
			try {
				basePathProperties.load(Fp3xmlextractProperties.class.getResourceAsStream("/basePath.properties"));
			} catch (IOException e) {
				e.printStackTrace();
			}
			String userProfile = System.getenv("USERPROFILE");
			baseDir = userProfile + basePathProperties.getProperty("basePath");
			log.debug("basePath: {}", baseDir);
		}
		
		if (baseDir == null) {
			if (new File(SUB_PATH + propertiesFile).exists()) { // für laden aus Eclipse Run
				baseDir = new File(SUB_PATH + propertiesFile).getAbsoluteFile().getParentFile().getParent() + "/";
			} else { // für laden aus Jar File
				baseDir = new File(".").getAbsoluteFile().getParentFile().getParent() + "/";
			}
		}
	}
	
	public String getBaseDir() {
		return baseDir;
	}

	public static Fp3xmlextractProperties getInstance(String propertiesFile, boolean intern) throws ConfigurationException {
		initBaseDir(propertiesFile);
		log.log(Level.INFO, "Properties base dir: {}", baseDir);

		Fp3xmlextractProperties instance = getInstanceForFile(propertiesFile);
		String subPath = SUB_PATH;
		if (instance == null) {
			if (intern) {
				copyFile(baseDir + subPath + propertiesFile);
				subPath = SUB_PATH + "intern/";
			}
			try (InputStream is = new FileInputStream(baseDir + subPath + propertiesFile);) {
				instance = new Fp3xmlextractProperties();
				instance.load(new InputStreamReader(is, StandardCharsets.UTF_8));
				instance.setFileName(propertiesFile);
				instances.add(instance);
			} catch (FileNotFoundException fnfe) {
				log.error("FileNotFoundException: ", fnfe);
				throw new ConfigurationException(propertiesFile, fnfe);
			} catch (IOException e) {
				log.error("IOException: ", e);
			}
		}
		return instance;
	}

	public String getProp(String key) {
		return this.getProperty(key);
	}

	private static Fp3xmlextractProperties getInstanceForFile(String propertiesFile) {
		for (Fp3xmlextractProperties instance : instances) {
			if (instance.getFileName().equalsIgnoreCase(propertiesFile)) {
				return instance;
			}
		}
		return null;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	private static void copyFile(String propertiesFileSrc) {
		String propertiesFileDest = new File(propertiesFileSrc).getParent() + "/intern/"
				+ new File(propertiesFileSrc).getName();

		try (BufferedReader br = new BufferedReader(new FileReader(propertiesFileSrc));
				OutputStream os = new FileOutputStream(propertiesFileDest, false);
				PrintWriter pw = new PrintWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8));) {

			pw.println("# file automatically created at: "
					+ new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime())
					+ " DO NOT MODIFY!\n");
			String line;
			while ((line = br.readLine()) != null) {
				final int posEqSign = line.indexOf("=");
				if (posEqSign > -1) {
					pw.println(line.substring(0, posEqSign).replace(" ", "\\u0020") + line.substring(posEqSign));
				} else {
					pw.println(line);
				}
				pw.flush();
			}
		} catch (FileNotFoundException e) {
			log.error("FileNotFoundException (copyFile): ", e);
		} catch (IOException e) {
			log.error("IOException (copyFile): ", e);
		}
	}

	@Override
	public synchronized boolean equals(Object other) {
		return super.equals(other);
	}
	
	@Override
	public synchronized int hashCode() {
		return entrySet().hashCode();
	}
}
