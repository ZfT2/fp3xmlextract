package de.zft2.fp3xmlextract.convert;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import de.zft2.fp3xmlextract.data.BankAccount;
import de.zft2.fp3xmlextract.data.Booking;
import de.zft2.fp3xmlextract.data.Booking.SepaTyp;
import de.zft2.fp3xmlextract.exception.ConfigurationException;

public class Converter extends AccountProcessor {

	private static Logger log = LogManager.getLogger(Converter.class);
	
	private static final String TAG_KONTO = "KONTO";
	private static final String TAG_IBAN = "IBAN";
	private static final String TAG_KONTONR = "KONTONR";
	private static final String TAG_BLZ = "BLZ";
	private static final String TAG_TYPE = "KONTOART";
	private static final String TAG_BANKNAME = "BANKNAME";
	private static final String TAG_BEZEICHNUNG = "BEZEICHNUNG";
	private static final String TAG_KONTOSTAND = "KONTOSTAND";
	
	private ConverterConfig config;
	
	public ConverterConfig getConfig() {
		return config;
	}

	public void setConfig(ConverterConfig config) {
		this.config = config;
	}
	
	public Converter() throws ConfigurationException {
		
		super();
		
		setConfig(new ConverterConfig(false, true));
	}
	
	public Converter(ConverterConfig config) throws ConfigurationException {
		
		super();
		
		new Converter();
		setConfig(config);
	}

	public Collection<Booking> convertFp3ToCsvEntries(String fp3File)
			throws ParserConfigurationException, SAXException, IOException {

		Document doc = getDocument(fp3File);

		NodeList list = doc.getElementsByTagName("b2");

		ArrayList<Booking> records = new ArrayList<>();

		for (int temp = 0; temp < list.getLength(); temp++) {

			Node node = list.item(temp);

			if (node.getNodeType() == Node.ELEMENT_NODE) {

				Element element = (Element) node;

				if (element.getElementsByTagName("m21").item(0) != null) {
					String purpose = ((Element) element.getElementsByTagName("m21").item(0)).getAttribute("u");
					String amountStr = ((Element) element.getElementsByTagName("m24").item(0)).getAttribute("u");
					String dateBooking = ((Element) element.getElementsByTagName("m27").item(0)).getAttribute("u");
					String crossIban = ((Element) element.getElementsByTagName("m28").item(0)).getAttribute("u");
					String dateValueStr = null;

					if (dateBooking.split("\n").length > 1) {
						dateValueStr = dateBooking.split("\n")[1];
						dateBooking = dateBooking.split("\n")[0];
					}
					purpose = purpose.replace("\n", " ");

					String gegenKontoBic = null;
					if (crossIban.split("\n").length > 1) {
						gegenKontoBic = crossIban.split("\n")[1];
						crossIban = crossIban.split("\n")[0];
					}

					log.printf(Level.INFO, "Element: %s DatumBuchung: %-8s DatumWert: %-8s Verwendungszweck: %-175s Betrag: %10s GegenkontoIban: %-22s GegenkontoBic: %-11s", 
							node.getNodeName(), dateBooking, dateValueStr, purpose, amountStr, crossIban, gegenKontoBic);
					
					records.add(new Booking(dateBooking, dateValueStr, purpose, new BigDecimal(amountStr.replaceAll("\\.+", "").replaceAll(",+", ".")), crossIban,
							gegenKontoBic, ""));
				}
			}
		}
		return records;
	}

	public Collection<BankAccount> convertXmlToCsvEntries(String xmlFile) throws ParserConfigurationException, SAXException, IOException {

		Document doc = getDocument(xmlFile);

		Collection<BankAccount> accountList = new ArrayList<>();

		NodeList listKonten = doc.getElementsByTagName(TAG_KONTO);

		for (int temp = 0; temp < listKonten.getLength(); temp++) {

			Node node = listKonten.item(temp);

			if (node.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}

			Element element = (Element) node;

			BankAccount account = new BankAccount();

			String iban = extractNodeText(element, TAG_IBAN);
			account.setIban(iban);
			
			String number = extractNodeText(element, TAG_KONTONR);
			account.setNumber(number);
			
			String blz =  extractNodeText(element, TAG_BLZ);
			account.setBlz(blz);
			
			String bic =  extractNodeText(element, "BIC");
			account.setBic(bic);
			
			String type =  extractNodeText(element, TAG_TYPE);
			account.setType(type);

			String bankName =  extractNodeText(element, TAG_BANKNAME);
			account.setBankName(bankName);
			
			String bezeichnung =  extractNodeText(element, TAG_BEZEICHNUNG);
			account.setBezeichnung(bezeichnung);
			
			String balance =  extractNodeText(element, TAG_KONTOSTAND);
			account.setBalance(balance != null ? new BigDecimal(balance.replaceAll("\\.+", "").replaceAll(",+", ".")): null);
			
			//account.setBankName(element.getElementsByTagName("BANKNAME").item(0) != null ? element.getElementsByTagName("BANKNAME").item(0).getTextContent() : null); // f. PayPal
			
			Node first = element.getElementsByTagName("KONTOBUCH").item(0);
			if (propsSkip.get(account.getIdentifier()) == null && first != null) {

				final String accountNamePP = findAccountNamePP(account.getIdentifier());
				account.setNamePP(accountNamePP != null ? accountNamePP : account.getBezeichnung());

				log.trace("\n\nElement: {} IBAN: {} KONTONR: {}", node.getNodeName(), iban, account.getNumber());

				NodeList listBuchungen = first.getChildNodes();

				List<Booking> records = extractBookings(node, account, listBuchungen);

				if (account != null && !records.isEmpty()) {
					account.setBookings(records);
					accountList.add(account);
				}
			}
		}

		return accountList;
	}


	private String extractNodeText(Element element, String nodeName) {
		String nodeValue = null;
		Node node = element.getElementsByTagName(nodeName).item(0);
		if (node != null && node.getParentNode().getNodeName().equals(TAG_KONTO)) {
			nodeValue = node.getTextContent();
			nodeValue = nodeValue.replace("*", "x");
		}
		return nodeValue;
	}


	private List<Booking> extractBookings(Node node, BankAccount account, NodeList listBuchungen) {
		
		ArrayList<Booking> records = new ArrayList<>();
		
		for (int tempB = 0; tempB < listBuchungen.getLength(); tempB++) {

			Node nodeB = listBuchungen.item(tempB);

			if (nodeB.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}

			Element elementBuchung = (Element) nodeB;
			
			String purpose = extractElementText(elementBuchung, "ZWECK", "kein Verwendungszweck");
			
			String amountStr = extractElementText(elementBuchung, "BETRAG", "0");
			
			String dateBooking = elementBuchung.getElementsByTagName("DATUM").item(0).getTextContent();
			
			String crossIban = null;
			if(elementBuchung.getElementsByTagName(TAG_IBAN).item(0) != null) {
				crossIban = elementBuchung.getElementsByTagName(TAG_IBAN).item(0).getTextContent();
			}
			if(crossIban == null && elementBuchung.getElementsByTagName(TAG_KONTONR).item(0) != null) {
				crossIban = elementBuchung.getElementsByTagName(TAG_KONTONR).item(0).getTextContent();
			}
			
			String crossBic = extractElementText(elementBuchung, "BIC", null);
			
			String crossReceiverName = extractElementText(elementBuchung, "NAME", null);
			String crossBankName = extractElementText(elementBuchung, TAG_BANKNAME, null);
			String crossAccountNumber = extractElementText(elementBuchung, TAG_KONTONR, null);
			String crossBlz = extractElementText(elementBuchung, "BLZ", null);
			
			try {
				// Gegenbuchungen auf gleiches Konto verhindern (bei Sammelüberweisungen?)
				if (!config.isWithCancelBookings() && crossIban != null && isCrossBookingOnSameAccount(account, crossIban)) {
					crossIban = null;
				}
			} catch (NumberFormatException nfe) {
				log.error("Account number {} / cross number {} could not be parsed for comparisation!", account.getIban(), crossIban, nfe);
			} catch (Exception e) {
				log.error("General error in cross number comparisation! Iban: {} , crossIban: {}", account.getIban(), crossIban, e);
			}

			String dateValueStr = extractElementText(elementBuchung, "VALUTA", null);

			purpose = purpose.replace("\n", " ");
			
			String category = extractElementText(elementBuchung, "KATEGORIE", null);

			log.printf(Level.TRACE, "Element: %s DatumBuchung: %-8s DatumWert: %-8s Verwendungszweck: %-175s Betrag: %10s GegenkontoIban: %-22s GegenkontoBic: %-11s", 
					node.getNodeName(), dateBooking, dateValueStr, purpose, amountStr, crossIban, crossBic);
			
			Booking booking = new Booking(dateBooking, dateValueStr, purpose, new BigDecimal(amountStr.replaceAll("\\.+", "").replaceAll(",+", ".")), crossIban,
					crossBic, account.getNamePP());
			
			addAdditionalBookingCounterpartDetails(booking, crossReceiverName, crossBankName, crossAccountNumber, crossBlz);
			addAdditionalBookingCategory(booking, category);
			
			addAdditionalBookingSepaInformation(booking, config.isRemoveSepaFieldsFromPurpose());
			
			records.add(booking);
		}
		return records;
	}
	
	boolean isCrossBookingOnSameAccount(BankAccount account, String crossIdentifier) {

		if (accountNumbersMap.get(account.getNamePP()) != null && accountNumbersMap.get(account.getNamePP()).contains(crossIdentifier)) {
			return true;
		}

		String accountIdentifier = account.getIban() != null ? account.getIban() : account.getNumber();
		if (accountIdentifier != null) {
			if (accountIdentifier.equalsIgnoreCase(crossIdentifier)) {
				return true;
			}

			String numberWithZeros = accountIdentifier.length() > 12 ? accountIdentifier.substring(12) : accountIdentifier;

			numberWithZeros = numberWithZeros.length() < 10 ? String.format("%010d", Integer.parseInt(numberWithZeros)) : numberWithZeros;
			crossIdentifier = crossIdentifier.length() < 10 ? String.format("%010d", Integer.parseInt(crossIdentifier)) : crossIdentifier;

			if (numberWithZeros.endsWith(crossIdentifier)) {
				return true;
			}
		}
		return false;
	}


	private void addAdditionalBookingCounterpartDetails(Booking booking, String crossReceiverName, String crossBankName, String crossAccountNumber, String crossBlz) {
		booking.setCrossReceiverName(crossReceiverName);
		booking.setCrossBankName(crossBankName);
		booking.setCrossAccountNumber(crossAccountNumber);
		booking.setCrossBlz(crossBlz);
	}
	
	private void addAdditionalBookingCategory(Booking booking, String category) {
		booking.setCategory(category);
	}

	void addAdditionalBookingSepaInformation(Booking booking, boolean removeFromPurpose) {
		if (booking.getPurpose() == null)
			return;

		String purpose = booking.getPurpose();
		if (purpose.matches("(?s).*Payment-Information-ID[\\s]{2}-?[\\d]+.*")) {			
			purpose = purpose.replaceAll("Payment-Information-ID[\\s]{2}", "Payment-Information-ID ");
		}
		String[] sepaFields = purpose.split(" {2}");
		StringBuilder remainingPurpose = new StringBuilder();
		for (String sepaField : sepaFields) {
			setSepaFields(booking, removeFromPurpose, remainingPurpose, sepaField);
		}
		if (removeFromPurpose && !remainingPurpose.isEmpty()) {
			booking.setPurpose(remainingPurpose.toString());
		}
	}

	private void setSepaFields(Booking booking, boolean removeFromPurpose, StringBuilder remainingPurpose, String sepaField) {
		if (sepaField.startsWith("Kundenref.:") || sepaField.startsWith("KREF+")) {
			booking.setSepaCustomerRef(sepaField.replace("Kundenref.: ", "").replace("KREF+", ""));
		} else if (sepaField.startsWith("EndtoEnd:") || sepaField.startsWith("EREF+")) {
			booking.setSepaEndToEnd(sepaField.replace("EndtoEnd: ", "").replace("EREF+", ""));
		} else if (sepaField.startsWith("Mandatsref.:") || sepaField.startsWith("MREF+")) {
			booking.setSepaMandate(sepaField.replace("Mandatsref.: ", "").replace("MREF+", ""));
		} else if (sepaField.startsWith("Creditor-ID:") || sepaField.startsWith("CRED+")) {
			booking.setSepaCreditorId(sepaField.replace("Creditor-ID: ", "").replace("CRED+", ""));
		} else if (sepaField.startsWith("Purpose:")) {
			booking.setSepaPurpose(sepaField.replace("Purpose: ", ""));
		} else if (sepaField.startsWith("Personen-ID:")) {
			booking.setSepaPersonId(sepaField.replace("Personen-ID: ", ""));
		} else {
			setSepaTyp(booking, removeFromPurpose, remainingPurpose, sepaField);
		}
	}

	private void setSepaTyp(Booking booking, boolean removeFromPurpose, StringBuilder remainingPurpose, String sepaField) {
		SepaTyp sepaTyp = SepaTyp.forString(sepaField);
		if (sepaTyp != null) {
			booking.setSepaTyp(sepaTyp);
		} else {
			if (removeFromPurpose) {
				if (!remainingPurpose.isEmpty())
					remainingPurpose.append("  ");
				remainingPurpose.append(sepaField.replace("SVWZ+", ""));
			}
		}
	}

	private String extractElementText(Element elementBuchung, String elementName, String defaultValue) {
		String elementValue = null;
		if (elementBuchung.getElementsByTagName(elementName).item(0) != null) {
			elementValue = elementBuchung.getElementsByTagName(elementName).item(0).getTextContent();
			return elementValue;
		} else {
			return defaultValue;
		}
	}

	private Document getDocument(String fileName) throws ParserConfigurationException, SAXException, IOException {

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	    dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
	    dbf.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
	    dbf.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");

		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.parse(Paths.get(fileName).toFile());
		doc.getDocumentElement().normalize();

		log.info("Root Element: {}", doc.getDocumentElement().getNodeName());
		log.info("------");
		return doc;
	}
	
	public void checkAndCorrectInputFile(String inputFile) {

		StringBuilder stringBuilder = new StringBuilder();

		try (BufferedReader reader = Files.newBufferedReader(Paths.get(inputFile))) {
			String str = reader.readLine();
			if (str != null) {
				if (!str.contains("<KONTEN>")) {
					stringBuilder.append("<KONTEN>" + System.lineSeparator());
					stringBuilder.append(str);
					while ((str = reader.readLine()) != null) {
						stringBuilder.append(str);
						stringBuilder.append(System.lineSeparator());
					}
				} else {
					log.info("{} has already <KONTEN> Tag.", inputFile);
					return;
				}
			} else {
				log.error("File {} is empty", inputFile);
				return;
			}
		} catch (IOException e) {
			log.error("IOException: {}", e.getMessage());
		}

		try (RandomAccessFile fileWriter = new RandomAccessFile(inputFile, "rw")) {
			fileWriter.seek(0); // to the beginning
			fileWriter.write(stringBuilder.toString().getBytes());
			fileWriter.seek(fileWriter.length());
			fileWriter.write("</KONTEN>".getBytes());
			log.info("added <KONTEN> Tag to {}", inputFile);
		} catch (IOException e) {
			log.error("Error writing File. Could not SAVE {}", inputFile);
		}
	}
	
}
