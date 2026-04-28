package de.zft2.fp3xmlextract.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Properties;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.zft2.fp3xmlextract.exception.ConfigurationException;

public class Fp3xmlextractProperties extends Properties {

	private static final Logger log = LogManager.getLogger(Fp3xmlextractProperties.class);

	/**
	 * 
	 */
	private static final long serialVersionUID = 1735959545579391865L;

	private static final Path SUB_PATH = Paths.get("properties");
	private static final Path TEST_SUB_PATH = Paths.get("src", "test", "properties");
	private static final String INTERN_SUB_PATH = "intern";

	private static String baseDir;

	private static Collection<Fp3xmlextractProperties> instances = new ArrayList<>();
	private String fileName;
	private String resolvedPath;

	private Fp3xmlextractProperties() {
	}

	private static void initBaseDir(String propertiesFile) {
		Path propertiesBaseDir = resolvePropertiesBaseDir(propertiesFile);
		baseDir = propertiesBaseDir.toString() + File.separator;
		log.debug("basePath: {}", baseDir);
	}

	private static Path resolvePropertiesBaseDir(String propertiesFile) {
		if (Fp3xmlextractProperties.class.getResource("/basePath.properties") != null
				&& Files.isRegularFile(TEST_SUB_PATH.resolve(propertiesFile))) {
			return TEST_SUB_PATH.toAbsolutePath().normalize().getParent();
		}
		return SUB_PATH.toAbsolutePath().normalize().getParent();
	}

	private static Path resolvePropertiesFile(String propertiesFile, boolean intern) {
		Path propertiesDir = resolvePropertiesBaseDir(propertiesFile).resolve(SUB_PATH.getFileName());
		if (intern) {
			return propertiesDir.resolve(INTERN_SUB_PATH).resolve(propertiesFile);
		}
		return propertiesDir.resolve(propertiesFile);
	}

	public String getBaseDir() {
		return baseDir;
	}

	public static Fp3xmlextractProperties getInstance(String propertiesFile, boolean intern) throws ConfigurationException {
		initBaseDir(propertiesFile);
		log.log(Level.INFO, "Properties base dir: {}", baseDir);

		Path propertiesFilePath = resolvePropertiesFile(propertiesFile, intern);
		Fp3xmlextractProperties instance = getInstanceForFile(propertiesFilePath);
		if (instance == null) {
			if (intern) {
				copyFile(resolvePropertiesFile(propertiesFile, false));
			}
			try (InputStream is = new FileInputStream(propertiesFilePath.toFile())) {
				instance = new Fp3xmlextractProperties();
				instance.load(new InputStreamReader(is, StandardCharsets.UTF_8));
				instance.setFileName(propertiesFile);
				instance.setResolvedPath(propertiesFilePath.toString());
				instances.add(instance);
			} catch (FileNotFoundException fnfe) {
				log.error("FileNotFoundException: ", fnfe);
				throw new ConfigurationException(propertiesFilePath.toString(), fnfe);
			} catch (IOException e) {
				log.error("IOException: ", e);
			}
		}
		return instance;
	}

	public String getProp(String key) {
		return this.getProperty(key);
	}

	private static Fp3xmlextractProperties getInstanceForFile(Path propertiesFilePath) {
		for (Fp3xmlextractProperties instance : instances) {
			if (instance.getResolvedPath().equalsIgnoreCase(propertiesFilePath.toString())) {
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

	private String getResolvedPath() {
		return resolvedPath;
	}

	private void setResolvedPath(String resolvedPath) {
		this.resolvedPath = resolvedPath;
	}

	private static void copyFile(Path propertiesFileSrc) {
		Path propertiesFileDest = propertiesFileSrc.getParent().resolve(INTERN_SUB_PATH).resolve(propertiesFileSrc.getFileName());

		try {
			Files.createDirectories(propertiesFileDest.getParent());
		} catch (IOException e) {
			log.error("IOException (copyFile/createDirectories): ", e);
			return;
		}

		try (BufferedReader br = Files.newBufferedReader(propertiesFileSrc, StandardCharsets.UTF_8);
				OutputStream os = new FileOutputStream(propertiesFileDest.toFile(), false);
				PrintWriter pw = new PrintWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8))) {

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
