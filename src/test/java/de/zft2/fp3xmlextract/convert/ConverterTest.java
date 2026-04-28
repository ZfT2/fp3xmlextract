package de.zft2.fp3xmlextract.convert;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Iterator;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import de.zft2.fp3xmlextract.data.BankAccount;
import de.zft2.fp3xmlextract.data.Booking;
import de.zft2.fp3xmlextract.data.Booking.SepaTyp;
import de.zft2.fp3xmlextract.data.Booking.Typ;
import de.zft2.fp3xmlextract.exception.ConfigurationException;

class ConverterTest {

	private static String filename01;
	private static String filename02;
	private static String filename03;
	private static String filename04;
	private static String filename05;
	private static String filename06;

	private static Converter converter;
	private static BookingProcessor bookingProcessor;
	private static AccountProcessor accountProcessor;

	@BeforeAll
	static void beforeClass() {
		filename01 = "src/test/resources/testdata/konto_buchungen_test.xml";
		filename02 = "src/test/resources/testdata/AlleKontenFL.xml";
		filename03 = "src/test/resources/testdata/konto_buchungen_steuern_test.xml";
		filename04 = "src/test/resources/testdata/konto_umbuchung_one_day_difference_test01.xml";
		filename05 = "src/test/resources/testdata/konto_festgeldkonten_test01.xml";
		filename06 = "src/test/resources/testdata/konto_stornobuchung_test.xml";

		try {
			ConverterConfig converterConfig = new ConverterConfig(false, false); /** default for tests: without SEPA extraction **/
			converter = new Converter(converterConfig);
			bookingProcessor = new BookingProcessor();
			accountProcessor = new AccountProcessor();
		} catch (ConfigurationException e) {
			e.printStackTrace();
		}
	}

	@Test
	void testConverter01() throws Exception {

		Collection<BankAccount> kontenList = converter.convertXmlToCsvEntries(filename01);

		assertNotNull(kontenList);

		Iterator<BankAccount> iterator = kontenList.iterator();

		BankAccount konto = (BankAccount) iterator.next();
		assertEquals("DE30120300000018884058", konto.getIban());
		assertEquals("18884058", konto.getNumber());
		assertEquals(5, konto.getBookings().size());
		assertEquals("24.08.06", konto.getBookings().get(0).getDate());

		konto = (BankAccount) iterator.next();
		assertEquals("DE71500105170990651720", konto.getIban());
		assertEquals("0990651720", konto.getNumber());
		assertEquals(43, konto.getBookings().size());
		assertEquals("30.12.03", konto.getBookings().get(0).getDate());

	}

	@Test
	void testConverter02() throws Exception {

		Collection<BankAccount> kontenList = converter.convertXmlToCsvEntries(filename02);

		assertNotNull(kontenList);

		assertEquals(2, kontenList.size());

		Iterator<BankAccount> iterator = kontenList.iterator();

		BankAccount konto = (BankAccount) iterator.next();
		assertEquals(null, konto.getIban());
		assertEquals("1002522291", konto.getNumber());
		assertEquals(7, konto.getBookings().size());
		assertEquals("01.04.19", konto.getBookings().get(0).getDate());

	}

	@Test
	void testConverter03() throws Exception {

		Collection<BankAccount> kontenList = converter.convertXmlToCsvEntries(filename03);
		bookingProcessor.addBookingTypesToAccountBookings(kontenList);

		assertNotNull(kontenList);

		assertEquals(1, kontenList.size());

		Iterator<BankAccount> iterator = kontenList.iterator();

		BankAccount konto = (BankAccount) iterator.next();
		assertEquals("DE41500105176004514751", konto.getIban());
		assertEquals("6004514751", konto.getNumber());

		assertEquals(3, konto.getBookings().size());

		assertEquals("31.12.23", konto.getBookings().get(0).getDate());
		assertEquals("-0,62", konto.getBookings().get(0).getAmountStr());
		assertEquals("Kapitalertragsteuer", konto.getBookings().get(0).getPurpose());
		assertEquals(Typ.TAX, konto.getBookings().get(0).getTyp());

		assertEquals("31.12.23", konto.getBookings().get(1).getDate());
		assertEquals("-0,03", konto.getBookings().get(1).getAmountStr());
		assertEquals("Solidaritaetszuschlag", konto.getBookings().get(1).getPurpose());
		assertEquals(Typ.TAX, konto.getBookings().get(1).getTyp());

		assertEquals("31.12.23", konto.getBookings().get(2).getDate());
		assertEquals("-0,05", konto.getBookings().get(2).getAmountStr());
		assertEquals("Kirchensteuer", konto.getBookings().get(2).getPurpose());
		assertEquals(Typ.TAX, konto.getBookings().get(2).getTyp());

	}

	@Test
	void testConverterIsCrossBookingOnSameAccount() {

		if (converter == null) {
			try {
				converter = new Converter();
			} catch (ConfigurationException e) {
				e.printStackTrace();
			}
		}

		boolean result;
		String crossIban;

		BankAccount bankAccount = new BankAccount();

		bankAccount.setIban("1234567890");
		crossIban = "1234567890";
		result = converter.isCrossBookingOnSameAccount(bankAccount, crossIban);
		assertTrue(result);

		bankAccount.setIban("DE2211111100023456789012");
		crossIban = "DE2211111100023456789012";
		result = converter.isCrossBookingOnSameAccount(bankAccount, crossIban);
		assertTrue(result);

		bankAccount.setIban("DE2211111100023456789012");
		crossIban = "23456789012";
		result = converter.isCrossBookingOnSameAccount(bankAccount, crossIban);
		assertTrue(result);

		bankAccount.setIban("DE2211111100023456789012");
		crossIban = "023456789012";
		result = converter.isCrossBookingOnSameAccount(bankAccount, crossIban);
		assertTrue(result);

		bankAccount.setIban("DE22111111000456789012");
		crossIban = "0456789012";
		result = converter.isCrossBookingOnSameAccount(bankAccount, crossIban);
		assertTrue(result);

		bankAccount.setIban("DE2211111100056789012");
		crossIban = "0056789012";
		result = converter.isCrossBookingOnSameAccount(bankAccount, crossIban);
		assertTrue(result);

		bankAccount.setIban("2719049");
		crossIban = "62719049";
		result = converter.isCrossBookingOnSameAccount(bankAccount, crossIban);
		assertFalse(result);

		bankAccount.setIban("62719049");
		crossIban = "2719049";
		result = converter.isCrossBookingOnSameAccount(bankAccount, crossIban);
		assertFalse(result);

		bankAccount.setIban(null);

		bankAccount.setNumber("260030620");
		crossIban = "260030620";
		result = converter.isCrossBookingOnSameAccount(bankAccount, crossIban);
		assertTrue(result);

		bankAccount.setNumber("260030620");
		crossIban = "0260030620";
		result = converter.isCrossBookingOnSameAccount(bankAccount, crossIban);
		assertTrue(result);
	}

	@Test
	void testAddAdditionalBookingSepaInformation() {

		Booking booking = new Booking("01.01.25", "02.01.25",
				"Kundenref.: Payment-Information-ID-4265  EndtoEnd: NOTPROVIDED  Auszahlung 011 KT Direktbank TAN1:493284 IBAN: DE92500617410200174051 BIC: GENODE99ABC  Überweisungsauftrag",
				BigDecimal.ONE, null, null, null);
		converter.addAdditionalBookingSepaInformation(booking, true);
		assertEquals("Auszahlung 011 KT Direktbank TAN1:493284 IBAN: DE92500617410200174051 BIC: GENODE99ABC", booking.getPurpose());

		booking = new Booking("29.02.24", "29.02.24", "ABSCHLUSS PER 29.02.2024  Abschluss", BigDecimal.ONE, null, null, null);
		converter.addAdditionalBookingSepaInformation(booking, true);
		assertEquals("ABSCHLUSS PER 29.02.2024", booking.getPurpose());

		booking = new Booking("01.01.25", "02.01.25", "EndtoEnd: NOTPROVIDED  Einzahlung 016 KT Direktbank  Überweisungsgutschr.", BigDecimal.ONE, null, null,
				null);
		converter.addAdditionalBookingSepaInformation(booking, true);
		assertEquals("Einzahlung 016 KT Direktbank", booking.getPurpose());

		booking = new Booking("01.01.25", "02.01.25",
				"Auszahlung 043 KT Direktbank TAN1:404689 IBAN: DE92500617410200174051 BIC: GENODE99ABC  Überweisungsauftrag  Kundenref.: Payment-Information-ID-219  EndtoEnd: NOTPROVIDED",
				BigDecimal.ONE, null, null, null);
		converter.addAdditionalBookingSepaInformation(booking, true);
		assertEquals("Auszahlung 043 KT Direktbank TAN1:404689 IBAN: DE92500617410200174051 BIC: GENODE99ABC", booking.getPurpose());

		booking = new Booking("30.06.21", "30.06.21",
				"Abrechnung 30.06.2021  siehe AnlageAbrechnung 30.06.2021Information zur AbrechnungKontostand am 30.06.2021  329,38 +Abrechnungszeitraum vom 01.04.2021 bis 30.06.2021Zinsen für eingeräumte Kontoüberziehung  0,37- 6,6500 v.H. Kred-Zins  bis 29.06.2021Abrechnung 30.06.2021  0,37-Sollzinssätze am 30.06.2021 6,6500 v.H. für eingeräumte Kontoüberziehung(aktuell eingeräumte Kontoüberziehung  13.500,00) 6,6500 v.H. für geduldete Kontoüberziehungüber die eingeräumte Kontoüberziehung hinausEs handelt sich hierbei um eine umsatzsteuerfreie Leistung.Kontostand/Rechnungsabschluss am 30.06.2021  329,01 +Rechnungsnummer: 20210630-BY111-00105192218  ABSCHLUSS",
				BigDecimal.ONE, null, null, null);
		converter.addAdditionalBookingSepaInformation(booking, true);
		assertEquals(
				"Abrechnung 30.06.2021  siehe AnlageAbrechnung 30.06.2021Information zur AbrechnungKontostand am 30.06.2021  329,38 +Abrechnungszeitraum vom 01.04.2021 bis 30.06.2021Zinsen für eingeräumte Kontoüberziehung  0,37- 6,6500 v.H. Kred-Zins  bis 29.06.2021Abrechnung 30.06.2021  0,37-Sollzinssätze am 30.06.2021 6,6500 v.H. für eingeräumte Kontoüberziehung(aktuell eingeräumte Kontoüberziehung  13.500,00) 6,6500 v.H. für geduldete Kontoüberziehungüber die eingeräumte Kontoüberziehung hinausEs handelt sich hierbei um eine umsatzsteuerfreie Leistung.Kontostand/Rechnungsabschluss am 30.06.2021  329,01 +Rechnungsnummer: 20210630-BY111-00105192218",
				booking.getPurpose());

		booking = new Booking("01.01.25", "02.01.25",
				"EndtoEnd: 126561176 - MUSTER, MAX MUSTER,GEOR  126561176 - MUSTER, MAX MUSTER,MAX  VL; GVC: SEPA Credit Transfer (Einzelbuchung-Haben)",
				BigDecimal.ONE, null, null, null);
		converter.addAdditionalBookingSepaInformation(booking, true);
		assertEquals("126561176 - MUSTER, MAX MUSTER,MAX  VL; GVC: SEPA Credit Transfer (Einzelbuchung-Haben)", booking.getPurpose());

		booking = new Booking("01.01.25", "02.01.25", "LASTSCHRIFT  3,1005688853E+026  13980917/1834681103", BigDecimal.ONE, null, null, null);
		converter.addAdditionalBookingSepaInformation(booking, true);
		assertEquals("3,1005688853E+026  13980917/1834681103", booking.getPurpose());

		booking = new Booking("31.10.07", "31.10.07", "LASTSCHRIFT  206010331970 000007135488  NR.4240536407/31.10.2007  RECHNUNG VOM 31.10.07", BigDecimal.ONE,
				null, null, null);
		converter.addAdditionalBookingSepaInformation(booking, true);
		assertEquals("206010331970 000007135488  NR.4240536407/31.10.2007  RECHNUNG VOM 31.10.07", booking.getPurpose());

		booking = new Booking("01.01.25", "02.01.25", "SONSTIGER EINZUG  EC 67007699 23.11 09.01 CE1", BigDecimal.ONE, null, null, null);
		converter.addAdditionalBookingSepaInformation(booking, true);
		assertEquals("EC 67007699 23.11 09.01 CE1", booking.getPurpose());

		booking = new Booking("15.05.15", "15.05.15",
				"KREF+Payment-Information-ID  -3683  SVWZ+4748430003203674 Max  Muster  DATUM 15.05.2015, 12.00 UHR  1.TAN 130162  ONLINE-UEBERWEISUNG",
				BigDecimal.ONE, null, null, null);
		converter.addAdditionalBookingSepaInformation(booking, true);
		assertEquals("Payment-Information-ID -3683", booking.getSepaCustomerRef());
		assertEquals("4748430003203674 Max  Muster  DATUM 15.05.2015, 12.00 UHR  1.TAN 130162", booking.getPurpose());
	}

	@Test
	void testConverter_SEPA_Fields_WITHOUT_extraction() throws Exception {

		Collection<BankAccount> kontenList = converter.convertXmlToCsvEntries(filename04);
		bookingProcessor.addBookingTypesToAccountBookings(kontenList);

		assertNotNull(kontenList);

		assertEquals(2, kontenList.size());

		Iterator<BankAccount> iterator = kontenList.iterator();

		BankAccount konto = (BankAccount) iterator.next();
		assertEquals("DE92500617410200174051", konto.getIban());
		assertEquals("200174051", konto.getNumber());

		assertEquals(1, konto.getBookings().size());

		assertEquals("22.05.24", konto.getBookings().get(0).getDate());
		assertEquals("100,00", konto.getBookings().get(0).getAmountStr());
		assertEquals("EndtoEnd: NOTPROVIDED  Auszahlung 022 KT Direktbank  UEBERWEISUNG", konto.getBookings().get(0).getPurpose());
		assertEquals("NOTPROVIDED", konto.getBookings().get(0).getSepaEndToEnd());
		assertEquals(SepaTyp.BANK_TRANSFER, konto.getBookings().get(0).getSepaTyp());
		assertEquals(Typ.REBOOKING_IN, konto.getBookings().get(0).getTyp());

		konto = (BankAccount) iterator.next();
		assertEquals("DE55500150010006290050", konto.getIban());
		assertEquals("6290050", konto.getNumber());

		assertEquals("21.05.24", konto.getBookings().get(0).getDate());
		assertEquals("-100,00", konto.getBookings().get(0).getAmountStr());
		assertEquals(
				"Kundenref.: Payment-Information-ID-3390  EndtoEnd: NOTPROVIDED  Auszahlung 022 KT Direktbank TAN1:446213 IBAN: DE92500617410200174051 BIC: GENODE99ABC  Überweisungsauftrag",
				konto.getBookings().get(0).getPurpose());
		assertEquals("NOTPROVIDED", konto.getBookings().get(0).getSepaEndToEnd());
		assertEquals("Payment-Information-ID-3390", konto.getBookings().get(0).getSepaCustomerRef());
		assertEquals(SepaTyp.BANK_TRANSFER, konto.getBookings().get(0).getSepaTyp());
		assertEquals(Typ.REBOOKING_OUT, konto.getBookings().get(0).getTyp());

	}

	@Test
	void testConverter_SEPA_Fields_WITH_extraction() throws Exception {

		ConverterConfig converterConfig = new ConverterConfig(false, true);
		converter.setConfig(converterConfig);

		Collection<BankAccount> kontenList = converter.convertXmlToCsvEntries(filename04);
		bookingProcessor.addBookingTypesToAccountBookings(kontenList);

		assertNotNull(kontenList);

		assertEquals(2, kontenList.size());

		Iterator<BankAccount> iterator = kontenList.iterator();

		BankAccount konto = (BankAccount) iterator.next();
		assertEquals("DE92500617410200174051", konto.getIban());
		assertEquals("200174051", konto.getNumber());

		assertEquals(1, konto.getBookings().size());

		assertEquals("22.05.24", konto.getBookings().get(0).getDate());
		assertEquals("100,00", konto.getBookings().get(0).getAmountStr());
		assertEquals("Auszahlung 022 KT Direktbank", konto.getBookings().get(0).getPurpose());
		assertEquals("NOTPROVIDED", konto.getBookings().get(0).getSepaEndToEnd());
		assertEquals(SepaTyp.BANK_TRANSFER, konto.getBookings().get(0).getSepaTyp());
		assertEquals(Typ.REBOOKING_IN, konto.getBookings().get(0).getTyp());

		konto = (BankAccount) iterator.next();
		assertEquals("DE55500150010006290050", konto.getIban());
		assertEquals("6290050", konto.getNumber());

		assertEquals("21.05.24", konto.getBookings().get(0).getDate());
		assertEquals("-100,00", konto.getBookings().get(0).getAmountStr());
		assertEquals("Auszahlung 022 KT Direktbank TAN1:446213 IBAN: DE92500617410200174051 BIC: GENODE99ABC", konto.getBookings().get(0).getPurpose());
		assertEquals("NOTPROVIDED", konto.getBookings().get(0).getSepaEndToEnd());
		assertEquals("Payment-Information-ID-3390", konto.getBookings().get(0).getSepaCustomerRef());
		assertEquals(SepaTyp.BANK_TRANSFER, konto.getBookings().get(0).getSepaTyp());
		assertEquals(Typ.REBOOKING_OUT, konto.getBookings().get(0).getTyp());
	}

	@Test
	void testConverter_Account_Grouping01() throws Exception {

		converter.getConfig().setRemoveSepaFieldsFromPurpose(true);
		Collection<BankAccount> kontenList = converter.convertXmlToCsvEntries(filename05);
		bookingProcessor.addBookingTypesToAccountBookings(kontenList);

		accountProcessor.addParentAccounts(kontenList);

		assertNotNull(kontenList);

		assertEquals(2, kontenList.size());

		Iterator<BankAccount> iterator = kontenList.iterator();

		BankAccount konto = (BankAccount) iterator.next();
		assertEquals("724741470", konto.getNumber());

		assertEquals("Festgelder B12-Bank", konto.getParentAccount());

		assertEquals(7, konto.getBookings().size());

		assertBooking(konto.getBookings().get(0), "25.01.23", null, "0,00", "Eröffnungsbuchung", null);

		assertBooking(konto.getBookings().get(1), "25.01.23", "25.01.23", "17500,00", "Umbuchung IBAN DE27305305000724741400 Muster Max",
				Typ.REBOOKING_IN);

		assertBooking(konto.getBookings().get(2), "31.03.23", "31.03.23", "-56,88",
				"Umbuchung Abrechnung Abrechnung per 31.03.2023 auf Konto 724741400 Kontoinhaber(in) Max Muster", Typ.REBOOKING_OUT);

		assertBooking(konto.getBookings().get(3), "31.03.23", null, "56,88", "Zinsen", null);

		assertBooking(konto.getBookings().get(4), "27.04.23", "28.04.23", "-24,50",
				"Umbuchung Abrechnung Abrechnung per 28.04.2023 auf Konto 724741400 Kontoinhaber(in) Max Muster", Typ.REBOOKING_OUT);

		assertBooking(konto.getBookings().get(5), "27.04.23", null, "24,50", "Zinsen", null);

		assertBooking(konto.getBookings().get(6), "28.04.23", null, "-17500,00", "Gesamtkündigung", Typ.REBOOKING_OUT);

		konto = (BankAccount) iterator.next();

		assertEquals("724741460", konto.getNumber());
		assertEquals("Festgelder B12-Bank", konto.getParentAccount());
	}

	@Test
	void testConverter_Account_Cancel_Booking01() throws Exception {

		converter.getConfig().setRemoveSepaFieldsFromPurpose(false);
		Collection<BankAccount> kontenList = converter.convertXmlToCsvEntries(filename06);
		bookingProcessor.addBookingTypesToAccountBookings(kontenList);

		assertNotNull(kontenList);

		assertEquals(1, kontenList.size());

		Iterator<BankAccount> iterator = kontenList.iterator();

		BankAccount konto = (BankAccount) iterator.next();
		assertEquals("200174051", konto.getNumber());

		assertEquals(1, konto.getBookings().size());

		Booking booking = konto.getBookings().get(0);

		assertBooking(booking, "24.02.25", "24.02.25", "15000,00",
				"Storno der Buchung des Auftragsbetrages 15000.00 EUR vom 24.02.2025 SVWZ: Einzahlung DiBa 01 2025 TAN1:841399 IBAN: DE71500105170990651720 BIC: INGDDEFFXXX  STORNO  EndtoEnd: NOTPROVIDED",
				null);
		assertBookingRecipient(booking, "Max Muster", "DE92500617410200174051", "GENODE99ABC", null, null);

	}

	private void assertBooking(Booking booking, String dateBooking, String dateValue, String amountStr, String purpose, Typ typ) {
		assertEquals(dateBooking, booking.getDateBooking());
		assertEquals(dateValue, booking.getDateValue());
		assertEquals(amountStr, booking.getAmountStr());
		assertEquals(purpose, booking.getPurpose());
		assertEquals(typ, booking.getTyp());
	}

	private void assertBookingRecipient(Booking booking, String crossReceiverName, String crossAccountIBAN, String crossAccountBIC, String crossAccountNumber,
			String crossBlz) {
		assertEquals(crossReceiverName, booking.getCrossReceiverName());
		assertEquals(crossAccountIBAN, booking.getCrossAccountIBAN());
		assertEquals(crossAccountBIC, booking.getCrossAccountBIC());
		assertEquals(crossAccountNumber, booking.getCrossAccountNumber());
		assertEquals(crossBlz, booking.getCrossBlz());
	}
}
