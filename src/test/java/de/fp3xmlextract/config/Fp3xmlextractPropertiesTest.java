package de.fp3xmlextract.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class Fp3xmlextractPropertiesTest {

	private static final String DIR = "properties/";

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

		Fp3xmlextractProperties propertiesIntern = Fp3xmlextractProperties.getInstance(/* BASE_PATH, */ filenameInputCancel, true);

		assertTrue(findInFile(propertiesIntern.getBaseDir(), filenameInputCancel, "Sparkarte VISA D-Bank"));

		File fileOutputIntern = new File(propertiesIntern.getBaseDir() + DIR + filenameOutputCancel);
		assertTrue(fileOutputIntern.exists());

		assertTrue(findInFile(propertiesIntern.getBaseDir(), filenameOutputCancel, "Sparkarte\\u0020VISA\\u0020D-Bank"));

		assertNotNull(propertiesIntern.get("Girokonto D-Bank"));
	}

	@Test
	void testProcessTransferProperties() throws Exception {

		Fp3xmlextractProperties propertiesIntern = Fp3xmlextractProperties.getInstance(/* BASE_PATH, */ filenameInputTransfer, true);

		assertTrue(findInFile(propertiesIntern.getBaseDir(), filenameInputTransfer, "Sparkonto Plus BN-Bank"));

		File fileOutputIntern = new File(propertiesIntern.getBaseDir() + DIR + filenameOutputTansfer);
		assertTrue(fileOutputIntern.exists());

		assertTrue(findInFile(propertiesIntern.getBaseDir(), filenameOutputTansfer, "Sparkonto\\u0020Plus\\u0020BN-Bank"));
		assertNotNull(propertiesIntern.get("Sparkonto Plus BN-Bank"));
	}

	private boolean findInFile(String baseDir, String fileName, String propertyStr) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(baseDir + DIR + fileName));
		String line;
		while ((line = br.readLine()) != null) {
			if (line.startsWith(propertyStr)) {
				br.close();
				return true;
			}
		}
		br.close();
		return false;
	}

}
