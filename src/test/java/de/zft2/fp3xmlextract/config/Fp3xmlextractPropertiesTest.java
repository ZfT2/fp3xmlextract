package de.zft2.fp3xmlextract.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class Fp3xmlextractPropertiesTest {

	private static final Path DIR = Path.of("properties");

	private static String filenameInputCancel;
	private static String filenameOutputCancel;

	private static String filenameInputTransfer;
	private static String filenameOutputTansfer;

	@BeforeAll
	static void beforeClass() {
		filenameInputCancel = "cancel.properties";
		filenameOutputCancel = "intern/cancel.properties";
		filenameInputTransfer = "transfer.properties";
		filenameOutputTansfer = "intern/transfer.properties";
	}

	@Test
	void testProcessCancelProperties() throws Exception {

		Fp3xmlextractProperties propertiesIntern = Fp3xmlextractProperties.getInstance(filenameInputCancel, true);

		assertTrue(findInFile(propertiesIntern.getBaseDir(), filenameInputCancel, "Sparkarte VISA D-Bank"));

		Path fileOutputIntern = Path.of(propertiesIntern.getBaseDir()).resolve(DIR).resolve(filenameOutputCancel);
		assertTrue(Files.exists(fileOutputIntern));

		assertTrue(findInFile(propertiesIntern.getBaseDir(), filenameOutputCancel, "Sparkarte\\u0020VISA\\u0020D-Bank"));

		assertNotNull(propertiesIntern.get("Girokonto D-Bank"));
	}

	@Test
	void testProcessTransferProperties() throws Exception {

		Fp3xmlextractProperties propertiesIntern = Fp3xmlextractProperties.getInstance(filenameInputTransfer, true);

		assertTrue(findInFile(propertiesIntern.getBaseDir(), filenameInputTransfer, "Sparkonto Plus BN-Bank"));

		Path fileOutputIntern = Path.of(propertiesIntern.getBaseDir()).resolve(DIR).resolve(filenameOutputTansfer);
		assertTrue(Files.exists(fileOutputIntern));

		assertTrue(findInFile(propertiesIntern.getBaseDir(), filenameOutputTansfer, "Sparkonto\\u0020Plus\\u0020BN-Bank"));
		assertNotNull(propertiesIntern.get("Sparkonto Plus BN-Bank"));
	}

	private boolean findInFile(String baseDir, String fileName, String propertyStr) throws IOException {
		Path filePath = Path.of(baseDir).resolve(DIR).resolve(fileName);
		try (BufferedReader br = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
			String line;
			while ((line = br.readLine()) != null) {
				if (line.startsWith(propertyStr)) {
					return true;
				}
			}
		}
		return false;
	}

}
