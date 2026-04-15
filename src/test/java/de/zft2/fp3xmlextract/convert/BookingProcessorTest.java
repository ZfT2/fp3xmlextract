package de.zft2.fp3xmlextract.convert;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import de.zft2.fp3xmlextract.data.BankAccount;
import de.zft2.fp3xmlextract.data.Booking;
import de.zft2.fp3xmlextract.exception.ConfigurationException;

class BookingProcessorTest {

	private static String filenameSameDayNoDiff01;
	private static String filenameSameDayNoDiff02;
	private static String filenameSameDayNoDiff03;

	private static String filenameOneDayDiff01;
	private static String filenameOneDayDiff02;
	private static String filenameOneDayDiff03;
	private static String filenameOneDayDiff04;
	private static String filenameOneDayDiff05;
	private static String filenameOneDayDiff06;
	private static String filenameOneDayDiff07;

	private static String filenameTwoDaysDiff01;

	private static String filenameThreeDaysDiff01;

	private static String filenameCancellation01;
	private static String filenameCancellation02;
	private static String filenameCancellation03;
	private static String filenameCancellation04;

	private static String filenameSixAccounts;

	private static String filenameSameDayNoDiffTwoRebookings01;
	private static String filenameSameDayNoDiffTwoRebookings02;

	private static Converter converter;
	private static BookingProcessor bookingProcessor;

	private static final int DAYS_REBOOKING = 6;

	@BeforeAll
	static void beforeClass() {
		filenameSameDayNoDiff01 = "src/test/resources/testdata/konto_umbuchung_same_day_test_01.xml";
		filenameSameDayNoDiff02 = "src/test/resources/testdata/konto_umbuchung_same_day_test_02.xml";
		filenameSameDayNoDiff03 = "src/test/resources/testdata/konto_umbuchung_same_day_test_03.xml";

		filenameOneDayDiff01 = "src/test/resources/testdata/konto_umbuchung_one_day_difference_test01.xml";
		filenameOneDayDiff02 = "src/test/resources/testdata/konto_umbuchung_one_day_difference_test02.xml";
		filenameOneDayDiff03 = "src/test/resources/testdata/konto_umbuchung_one_day_difference_test03.xml";
		filenameOneDayDiff04 = "src/test/resources/testdata/konto_umbuchung_one_day_difference_test04.xml";
		filenameOneDayDiff05 = "src/test/resources/testdata/konto_umbuchung_one_day_difference_test05.xml";
		filenameOneDayDiff06 = "src/test/resources/testdata/konto_umbuchung_one_day_difference_test06.xml";
		filenameOneDayDiff07 = "src/test/resources/testdata/konto_umbuchung_one_day_difference_test07.xml";

		filenameTwoDaysDiff01 = "src/test/resources/testdata/konto_umbuchung_two_days_difference_test01.xml";

		filenameThreeDaysDiff01 = "src/test/resources/testdata/konto_umbuchung_three_days_difference_test01.xml";

		filenameCancellation01 = "src/test/resources/testdata/konto_umbuchung_same_day_AND_cancellation_test01.xml";
		filenameCancellation02 = "src/test/resources/testdata/konto_umbuchung_same_day_AND_cancellation_test02.xml";
		filenameCancellation03 = "src/test/resources/testdata/konto_umbuchung_same_day_AND_cancellation_test03.xml";
		filenameCancellation04 = "src/test/resources/testdata/konto_umbuchung_same_day_AND_cancellation_test04.xml";

		filenameSixAccounts = "src/test/resources/testdata/konto_umbuchung_6_konten.xml";

		filenameSameDayNoDiffTwoRebookings01 = "src/test/resources/testdata/konto_umbuchung_same_day_two_test_01.xml";
		filenameSameDayNoDiffTwoRebookings02 = "src/test/resources/testdata/konto_umbuchung_same_day_two_test_02.xml";

		try {
			ConverterConfig config = new ConverterConfig(true, false);
			converter = new Converter(config);
			bookingProcessor = new BookingProcessor();
		} catch (ConfigurationException e) {
			e.printStackTrace();
		}

	}

	@Test
	void testRemoveDoubleRebookings_01() throws Exception {

		Collection<BankAccount> kontenList = converter.convertXmlToCsvEntries(filenameSameDayNoDiff01);
		bookingProcessor.addBookingTypesToAccountBookings(kontenList);
		bookingProcessor.generateCrossBookings(kontenList, true, DAYS_REBOOKING);

		assertNotNull(kontenList);

		assertEquals(2, kontenList.size());

		List<Booking> bookings = new ArrayList<Booking>(checkKontoHBank(getKontoFromResultList(kontenList, "Girokonto H-Bank")).getBookings());
		bookings.addAll(checkKontoJBank(getKontoFromResultList(kontenList, "Tagesgeld J-Bank")).getBookings());

		assertEquals(2, bookings.size());

		checkBooking(bookings.get(0), "04.10.24", "Auszahlung 044 KT Direktbank  UEBERWEISUNG  EndtoEnd: NOTPROVIDED", "50,00", "DE55500150010006290050",
				"Umbuchung (Eingang)");
		checkBooking(bookings.get(1), "04.10.24",
				"Auszahlung 044 KT Direktbank TAN1:420904 IBAN: DE92500617410200174051 BIC: GENODE99ABC  Überweisungsauftrag  Kundenref.: Payment-Information-ID-697  EndtoEnd: NOTPROVIDED",
				"-50,00", "DE92500617410200174051", "Umbuchung (Ausgang)");

		bookingProcessor.removeDoubleSameDayRebookings(kontenList);

		bookings.clear();
		bookings = checkKontoHBank(getKontoFromResultList(kontenList, "Girokonto H-Bank")).getBookings();
		bookings.addAll(checkKontoJBank(getKontoFromResultList(kontenList, "Tagesgeld J-Bank")).getBookings());

		assertEquals(1, bookings.size());

		checkBooking(bookings.get(0), "04.10.24", "Auszahlung 044 KT Direktbank  UEBERWEISUNG  EndtoEnd: NOTPROVIDED", "50,00", "DE55500150010006290050",
				"Umbuchung (Eingang)");
	}

	@Test
	void testBookingProcessorUmbuchungOnSameDay_01() throws Exception {

		Collection<BankAccount> kontenList = converter.convertXmlToCsvEntries(filenameSameDayNoDiff01);
		bookingProcessor.addBookingTypesToAccountBookings(kontenList);
		bookingProcessor.generateCrossBookings(kontenList, true, DAYS_REBOOKING);

		assertNotNull(kontenList);

		assertEquals(2, kontenList.size());

		BankAccount konto = checkKontoHBank(getKontoFromResultList(kontenList, "Girokonto H-Bank"));
		assertEquals(1, konto.getBookings().size());

		assertTrue(konto.getBookings().stream().anyMatch(booking -> checkBookingInList(booking, "04.10.24",
				"Auszahlung 044 KT Direktbank  UEBERWEISUNG  EndtoEnd: NOTPROVIDED", "50,00", "DE55500150010006290050", "Umbuchung (Eingang)")));

		konto = checkKontoJBank(getKontoFromResultList(kontenList, "Tagesgeld J-Bank"));
		assertEquals(1, konto.getBookings().size());

		assertTrue(konto.getBookings().stream().anyMatch(booking -> checkBookingInList(booking, "04.10.24",
				"Auszahlung 044 KT Direktbank TAN1:420904 IBAN: DE92500617410200174051 BIC: GENODE99ABC  Überweisungsauftrag  Kundenref.: Payment-Information-ID-697  EndtoEnd: NOTPROVIDED",
				"-50,00", "DE92500617410200174051", "Umbuchung (Ausgang)")));
	}

	@Test
	void testBookingProcessorUmbuchungOnSameDay_02() throws Exception {

		Collection<BankAccount> kontenList = converter.convertXmlToCsvEntries(filenameSameDayNoDiff02);
		bookingProcessor.addBookingTypesToAccountBookings(kontenList);
		bookingProcessor.generateCrossBookings(kontenList, true, DAYS_REBOOKING);

		assertNotNull(kontenList);

		assertEquals(2, kontenList.size());

		// Konto (Basis)
		BankAccount konto = checkKontoSK(getKontoFromResultList(kontenList, "Girokonto SK K-Stadt"));
		assertEquals(1, konto.getBookings().size());

		assertTrue(konto.getBookings().stream().anyMatch(booking -> checkBookingInList(booking, "22.03.06",
				"UEBERWEISUNGSGUTSCHRIFT  KTO 1201854647  AUSZAHLUNG WEGEN KONTOAUFLO", "0,84", "DE89270200001201854647", "Umbuchung (Eingang)")));

		// Konto (Gegenkonto)
		konto = checkKontoVW(getKontoFromResultList(kontenList, "Tagesgeld Mobil Bank"));
		assertEquals(1, konto.getBookings().size());

		assertTrue(konto.getBookings().stream().anyMatch(booking -> checkBookingInList(booking, "22.03.06",
				"UEBERWEISUNGSGUTSCHRIFT  KTO 1201854647  AUSZAHLUNG WEGEN KONTOAUFLO", "-0,84", "1061525816", "Umbuchung (Ausgang)")));
	}

	@Test
	void testBookingProcessorUmbuchungOnSameDay_03() throws Exception {

		Collection<BankAccount> kontenList = converter.convertXmlToCsvEntries(filenameSameDayNoDiff03);
		bookingProcessor.addBookingTypesToAccountBookings(kontenList);
		bookingProcessor.generateCrossBookings(kontenList, true, DAYS_REBOOKING);

		assertNotNull(kontenList);

		assertEquals(2, kontenList.size());

		// Konto (Basis)
		BankAccount konto = checkKontoCB(getKontoFromResultList(kontenList, "Girokonto C-Bank"));
		assertEquals(1, konto.getBookings().size());

		assertTrue(konto.getBookings().stream()
				.anyMatch(booking -> checkBookingInList(booking, "06.03.06", "Einzahlung COMB (C-Bank)", "-100,00", "7004277541", "Umbuchung (Ausgang)")));
		List<Booking> bookingAllStatistics = new ArrayList<Booking>();
		bookingAllStatistics.addAll(konto.getBookings());

		// Konto (Gegenkonto)
		konto = checkKontoBM(getKontoFromResultList(kontenList, "Tagesgeld B--Bank"));
		assertEquals(2, konto.getBookings().size());

		assertTrue(konto.getBookings().stream()
				.anyMatch(booking -> checkBookingInList(booking, "06.03.06", "Einzahlung COMB (C-Bank)", "100,00", "0100657000", "Umbuchung (Eingang)")));

		bookingAllStatistics.addAll(konto.getBookings());

		bookingProcessor.printMissingDoubleSameDayRebookings(bookingAllStatistics);
	}

	@Test
	void testBookingProcessorUmbuchungWithOneDayDiffference_01() throws Exception {

		Collection<BankAccount> kontenList = converter.convertXmlToCsvEntries(filenameOneDayDiff01);
		bookingProcessor.addBookingTypesToAccountBookings(kontenList);
		bookingProcessor.generateCrossBookings(kontenList, true, DAYS_REBOOKING);

		assertNotNull(kontenList);

		assertEquals(3, kontenList.size());

		// Konto (Basis)
		BankAccount konto = checkKontoHBank(getKontoFromResultList(kontenList, "Girokonto H-Bank"));
		assertEquals(1, konto.getBookings().size());

		assertTrue(konto.getBookings().stream().anyMatch(booking -> checkBookingInList(booking, "22.05.24",
				"EndtoEnd: NOTPROVIDED  Auszahlung 022 KT Direktbank  UEBERWEISUNG", "100,00", "DE00000000", "Umbuchung (Eingang)")));
		// Konto (Gegenkonto)
		konto = checkKontoJBank(getKontoFromResultList(kontenList, "Tagesgeld J-Bank"));

		assertTrue(konto.getBookings().stream().anyMatch(booking -> checkBookingInList(booking, "21.05.24",
				"Kundenref.: Payment-Information-ID-3390  EndtoEnd: NOTPROVIDED  Auszahlung 022 KT Direktbank TAN1:446213 IBAN: DE92500617410200174051 BIC: GENODE99ABC  Überweisungsauftrag",
				"-100,00", "DE00000000", "Umbuchung (Ausgang)")));
		// Transferkonto
		konto = checkKontoTransfer(getKontoFromResultList(kontenList, "--TRANSFER--"));

		assertEquals(2, konto.getBookings().size());

		assertTrue(konto.getBookings().stream().anyMatch(booking -> checkBookingInList(booking, "22.05.24",
				"EndtoEnd: NOTPROVIDED  Auszahlung 022 KT Direktbank  UEBERWEISUNG", "-100,00", "DE92500617410200174051", "Umbuchung (Ausgang)")));
		assertTrue(konto.getBookings().stream().anyMatch(booking -> checkBookingInList(booking, "21.05.24",
				"Kundenref.: Payment-Information-ID-3390  EndtoEnd: NOTPROVIDED  Auszahlung 022 KT Direktbank TAN1:446213 IBAN: DE92500617410200174051 BIC: GENODE99ABC  Überweisungsauftrag",
				"100,00", "DE55500150010006290050", "Umbuchung (Eingang)")));

	}

	@Test
	void testBookingProcessorUmbuchungWithOneDayDiffference_02() throws Exception {

		Collection<BankAccount> kontenList = converter.convertXmlToCsvEntries(filenameOneDayDiff02);
		bookingProcessor.addBookingTypesToAccountBookings(kontenList);
		bookingProcessor.generateCrossBookings(kontenList, true, DAYS_REBOOKING);

		assertNotNull(kontenList);

		assertEquals(3, kontenList.size());

		// Konto (Basis)
		BankAccount konto = checkKontoDBank(getKontoFromResultList(kontenList, "Girokonto D-Bank"));
		assertEquals(2, konto.getBookings().size());

		assertTrue(konto.getBookings().stream().anyMatch(booking -> checkBookingInList(booking, "29.09.11",
				"EINZAHLUNG 05 2011  DATUM 28.09.2011, 20.17 UHR  1.TAN 474396  ONLINE-UEBERWEISUNG", "-5000,00", "DE00000000", "Umbuchung (Ausgang)")));
		assertTrue(konto.getBookings().stream().anyMatch(booking -> checkBookingInList(booking, "15.11.11", "AUSZAHLUNG 01 2011  UEBERWEISUNGSGUTSCHRIFT",
				"40371,02", "DE00000000", "Umbuchung (Eingang)")));

		// Konto (Gegenkonto)
		konto = checkKontoAK(getKontoFromResultList(kontenList, "Tagesgeld AK-Bank"));

		assertTrue(konto.getBookings().stream().anyMatch(
				booking -> checkBookingInList(booking, "30.09.11", "EINZAHLUNG 05 2011  GUTSCHRIFT", "5000,00", "DE00000000", "Umbuchung (Eingang)")));
		assertTrue(konto.getBookings().stream().anyMatch(booking -> checkBookingInList(booking, "30.09.11",
				"HABENZINSEN  V. 31.08.2011 B. 30.09.2011  ZINSEN ZU  2,400 %  KONTOABSCHLUß", "70,43", "0000060060", "Zinsen")));
		assertTrue(konto.getBookings().stream().anyMatch(booking -> checkBookingInList(booking, "31.10.11",
				"HABENZINSEN  V. 30.09.2011 B. 31.10.2011  ZINSEN ZU  2,400 %  KONTOABSCHLUß", "83,26", "0000060060", "Zinsen")));
		assertTrue(konto.getBookings().stream().anyMatch(
				booking -> checkBookingInList(booking, "14.11.11", "AUSZAHLUNG 01 2011  DTA ÜBERWEISUNG", "-40371,02", "DE00000000", "Umbuchung (Ausgang)")));

		// Transferkonto
		konto = checkKontoTransfer(getKontoFromResultList(kontenList, "--TRANSFER--"));

		assertEquals(4, konto.getBookings().size());

		assertTrue(konto.getBookings().stream().anyMatch(booking -> checkBookingInList(booking, "29.09.11",
				"EINZAHLUNG 05 2011  DATUM 28.09.2011, 20.17 UHR  1.TAN 474396  ONLINE-UEBERWEISUNG", "5000,00", "0018884058", "Umbuchung (Eingang)")));
		assertTrue(konto.getBookings().stream()
				.anyMatch(booking -> checkBookingInList(booking, "30.09.11", "EINZAHLUNG 05 2011  GUTSCHRIFT", "-5000,00", "10052517", "Umbuchung (Ausgang)")));
		assertTrue(konto.getBookings().stream().anyMatch(booking -> checkBookingInList(booking, "15.11.11", "AUSZAHLUNG 01 2011  UEBERWEISUNGSGUTSCHRIFT",
				"-40371,02", "0018884058", "Umbuchung (Ausgang)")));
		assertTrue(konto.getBookings().stream().anyMatch(
				booking -> checkBookingInList(booking, "14.11.11", "AUSZAHLUNG 01 2011  DTA ÜBERWEISUNG", "40371,02", "10052517", "Umbuchung (Eingang)")));
	}

	@Test
	void testBookingProcessorUmbuchungWithOneDayDiffference_03() throws Exception {

		Collection<BankAccount> kontenList = converter.convertXmlToCsvEntries(filenameOneDayDiff03);
		bookingProcessor.addBookingTypesToAccountBookings(kontenList);
		bookingProcessor.generateCrossBookings(kontenList, true, DAYS_REBOOKING);

		assertNotNull(kontenList);

		assertEquals(3, kontenList.size());

		// Konto (Basis)
		BankAccount konto = checkKontoDBank(getKontoFromResultList(kontenList, "Girokonto D-Bank"));
		assertEquals(1, konto.getBookings().size());

		assertTrue(konto.getBookings().stream().anyMatch(booking -> checkBookingInList(booking, "21.04.09",
				"EINZAHLUNG 1/2009  DATUM 21.04.2009, 00.02 UHR  1.TAN 862210  SDIREKT-UEBERWEISUNG", "-11500,00", "DE00000000", "Umbuchung (Ausgang)")));

		// Konto (Gegenkonto)
		konto = checkKontoGa(getKontoFromResultList(kontenList, "Tagesgeld Ga-Bank"));
		assertEquals(1, konto.getBookings().size());

		assertTrue(konto.getBookings().stream()
				.anyMatch(booking -> checkBookingInList(booking, "22.04.09", "Einzahlung 1/2009", "11500,00", "DE00000000", "Umbuchung (Eingang)")));

		// Transferkonto
		konto = checkKontoTransfer(getKontoFromResultList(kontenList, "--TRANSFER--"));

		assertEquals(2, konto.getBookings().size());

		assertTrue(konto.getBookings().stream().anyMatch(booking -> checkBookingInList(booking, "21.04.09",
				"EINZAHLUNG 1/2009  DATUM 21.04.2009, 00.02 UHR  1.TAN 862210  SDIREKT-UEBERWEISUNG", "11500,00", "18884058", "Umbuchung (Eingang)")));
		assertTrue(konto.getBookings().stream()
				.anyMatch(booking -> checkBookingInList(booking, "22.04.09", "Einzahlung 1/2009", "-11500,00", "4000545165", "Umbuchung (Ausgang)")));
	}

	@Test
	void testBookingProcessorUmbuchungWithOneDayDiffference_04() throws Exception {

		Collection<BankAccount> kontenList = converter.convertXmlToCsvEntries(filenameOneDayDiff04);
		bookingProcessor.addBookingTypesToAccountBookings(kontenList);
		bookingProcessor.generateCrossBookings(kontenList, true, DAYS_REBOOKING);

		assertNotNull(kontenList);

		assertEquals(3, kontenList.size());

		// Konto (Basis)
		BankAccount konto = checkKontoDBank(getKontoFromResultList(kontenList, "Girokonto D-Bank"));
		assertEquals(1, konto.getBookings().size());

		assertTrue(konto.getBookings().stream()
				.anyMatch(booking -> checkBookingInList(booking, "24.08.06", "EINZAHLUNG SPAR 1  GUTSCHRIFT", "1400,00", "DE00000000", "Umbuchung (Eingang)")));

		// Konto (Gegenkonto)
		konto = checkKontoSK(getKontoFromResultList(kontenList, "Girokonto SK K-Stadt"));
		assertEquals(1, konto.getBookings().size());

		assertTrue(konto.getBookings().stream().anyMatch(booking -> checkBookingInList(booking, "23.08.06",
				"SDIREKT-UEBERWEISUNG  EINZAHLUNG SPAR 1  DATUM 23.08.2006, 14.16 UHR  1.TAN 432832", "-1400,00", "DE00000000", "Umbuchung (Ausgang)")));

		// Transferkonto
		konto = checkKontoTransfer(getKontoFromResultList(kontenList, "--TRANSFER--"));

		assertEquals(2, konto.getBookings().size());

		assertTrue(konto.getBookings().stream()
				.anyMatch(booking -> checkBookingInList(booking, "24.08.06", "EINZAHLUNG SPAR 1  GUTSCHRIFT", "-1400,00", "18884058", "Umbuchung (Ausgang)")));
		assertTrue(konto.getBookings().stream().anyMatch(booking -> checkBookingInList(booking, "23.08.06",
				"SDIREKT-UEBERWEISUNG  EINZAHLUNG SPAR 1  DATUM 23.08.2006, 14.16 UHR  1.TAN 432832", "1400,00", "1061525816", "Umbuchung (Eingang)")));
	}

	@Test
	void testBookingProcessorUmbuchungWithOneDayDiffference_05() throws Exception {

		Collection<BankAccount> kontenList = converter.convertXmlToCsvEntries(filenameOneDayDiff05);
		bookingProcessor.addBookingTypesToAccountBookings(kontenList);
		bookingProcessor.generateCrossBookings(kontenList, true, DAYS_REBOOKING);

		assertNotNull(kontenList);

		assertEquals(3, kontenList.size());

		// Konto (Basis)
		BankAccount konto = checkKontoHBank(getKontoFromResultList(kontenList, "Girokonto H-Bank"));
		assertEquals(1, konto.getBookings().size());

		assertTrue(konto.getBookings().stream().anyMatch(booking -> checkBookingInList(booking, "03.04.24",
				"EndtoEnd: NOTPROVIDED  Einzahlung 0002 VR-HT EineBank Giro  UEBERWEISUNG", "34,19", "DE00000000", "Umbuchung (Eingang)")));

		// Konto (Gegenkonto)
		konto = checkKontoDBank(getKontoFromResultList(kontenList, "Girokonto D-Bank"));
		assertEquals(1, konto.getBookings().size());

		assertTrue(konto.getBookings().stream()
				.anyMatch(booking -> checkBookingInList(booking, "02.04.24",
						"Kundenref.: Payment-Information-ID  -5089  Einzahlung 0002 VR-HT  EineBank Giro  DATUM 28.03.2024, 23.05 UHR  ONLINE-UEBERWEISUNG",
						"-34,19", "DE00000000", "Umbuchung (Ausgang)")));

		// Transferkonto
		konto = checkKontoTransfer(getKontoFromResultList(kontenList, "--TRANSFER--"));

		assertEquals(2, konto.getBookings().size());

		assertTrue(konto.getBookings().stream().anyMatch(booking -> checkBookingInList(booking, "03.04.24",
				"EndtoEnd: NOTPROVIDED  Einzahlung 0002 VR-HT EineBank Giro  UEBERWEISUNG", "-34,19", "DE92500617410200174051", "Umbuchung (Ausgang)")));
		assertTrue(konto.getBookings().stream()
				.anyMatch(booking -> checkBookingInList(booking, "02.04.24",
						"Kundenref.: Payment-Information-ID  -5089  Einzahlung 0002 VR-HT  EineBank Giro  DATUM 28.03.2024, 23.05 UHR  ONLINE-UEBERWEISUNG",
						"34,19", "DE30120300000018884058", "Umbuchung (Eingang)")));
	}

	@Test
	void testBookingProcessorUmbuchungWithOneDayDiffference_06() throws Exception {

		Collection<BankAccount> kontenList = converter.convertXmlToCsvEntries(filenameOneDayDiff06);
		bookingProcessor.addBookingTypesToAccountBookings(kontenList);
		bookingProcessor.generateCrossBookings(kontenList, true, DAYS_REBOOKING);

		assertNotNull(kontenList);

		assertEquals(3, kontenList.size());

		// Konto (Basis)
		BankAccount konto = checkKontoDBank(getKontoFromResultList(kontenList, "Girokonto D-Bank"));
		assertEquals(1, konto.getBookings().size());

		assertTrue(konto.getBookings().stream().anyMatch(booking -> checkBookingInList(booking, "09.07.09", "18884058  UEBERWEISUNG  UEBERWEISUNGSGUTSCHRIFT",
				"13000,00", "DE00000000", "Umbuchung (Eingang)")));
		// Konto (Gegenkonto)
		konto = checkKontoGa(getKontoFromResultList(kontenList, "Tagesgeld Ga-Bank"));
		assertEquals(1, konto.getBookings().size());

		assertTrue(konto.getBookings().stream()
				.anyMatch(booking -> checkBookingInList(booking, "08.07.09", "Ueberweisung", "-13000,00", "DE00000000", "Umbuchung (Ausgang)")));
		// Transferkonto
		konto = checkKontoTransfer(getKontoFromResultList(kontenList, "--TRANSFER--"));

		assertEquals(2, konto.getBookings().size());

		assertTrue(konto.getBookings().stream().anyMatch(booking -> checkBookingInList(booking, "09.07.09", "18884058  UEBERWEISUNG  UEBERWEISUNGSGUTSCHRIFT",
				"-13000,00", "18884058", "Umbuchung (Ausgang)")));
		assertTrue(konto.getBookings().stream()
				.anyMatch(booking -> checkBookingInList(booking, "08.07.09", "Ueberweisung", "13000,00", "4000545165", "Umbuchung (Eingang)")));
	}

	@Test
	void testBookingProcessorUmbuchungWithOneDayDiffference_07() throws Exception {

		Collection<BankAccount> kontenList = converter.convertXmlToCsvEntries(filenameOneDayDiff07);
		bookingProcessor.addBookingTypesToAccountBookings(kontenList);
		bookingProcessor.generateCrossBookings(kontenList, true, DAYS_REBOOKING);

		assertNotNull(kontenList);

		assertEquals(3, kontenList.size());

		// Konto (Basis)
		BankAccount konto = checkKontoDBank(getKontoFromResultList(kontenList, "Girokonto D-Bank"));
		assertEquals(1, konto.getBookings().size());

		assertTrue(konto.getBookings().stream().anyMatch(
				booking -> checkBookingInList(booking, "02.02.10", "AG D-Bank 01-10  UEBERWEISUNGSGUTSCHRIFT", "25518,14", "DE00000000", "Umbuchung (Eingang)")));

		// Konto (Gegenkonto)
		konto = checkKontoDeDa(getKontoFromResultList(kontenList, "Tagesgeld I DeDa"));
		assertEquals(1, konto.getBookings().size());

		assertTrue(konto.getBookings().stream()
				.anyMatch(booking -> checkBookingInList(booking, "01.02.10", "AG D-Bank 01-10  Überweisung", "-25518,14", "DE00000000", "Umbuchung (Ausgang)")));

		// Transferkonto
		konto = checkKontoTransfer(getKontoFromResultList(kontenList, "--TRANSFER--"));

		assertEquals(2, konto.getBookings().size());

		assertTrue(konto.getBookings().stream().anyMatch(
				booking -> checkBookingInList(booking, "02.02.10", "AG D-Bank 01-10  UEBERWEISUNGSGUTSCHRIFT", "-25518,14", "0018884058", "Umbuchung (Ausgang)")));
		assertTrue(konto.getBookings().stream()
				.anyMatch(booking -> checkBookingInList(booking, "01.02.10", "AG D-Bank 01-10  Überweisung", "25518,14", "990651720", "Umbuchung (Eingang)")));
	}

	@Test
	void testBookingProcessorUmbuchungWithTwoDaysDiffference_01() throws Exception {

		Collection<BankAccount> kontenList = converter.convertXmlToCsvEntries(filenameTwoDaysDiff01);
		bookingProcessor.addBookingTypesToAccountBookings(kontenList);
		bookingProcessor.generateCrossBookings(kontenList, true, DAYS_REBOOKING);

		assertNotNull(kontenList);

		assertEquals(3, kontenList.size());

		// Konto (Basis)
		BankAccount konto = checkKontoSK(getKontoFromResultList(kontenList, "Girokonto SK K-Stadt"));
		assertEquals(1, konto.getBookings().size());

		assertTrue(konto.getBookings().stream()
				.anyMatch(booking -> checkBookingInList(booking, "21.08.06",
						"SDIREKT-UEBERWEISUNG  4,90762702524695E+015  MAX MUSTER  DATUM 21.08.2006, 19.19 UHR  1.TAN 440625", "-10,00", "DE00000000",
						"Umbuchung (Ausgang)")));

		// Konto (Gegenkonto)
		konto = checkKontoDSparkarte(getKontoFromResultList(kontenList, "Sparkarte VISA D-Bank"));
		assertEquals(1, konto.getBookings().size());

		assertTrue(konto.getBookings().stream()
				.anyMatch(booking -> checkBookingInList(booking, "23.08.06", "Einzahlung", "10,00", "DE00000000", "Umbuchung (Eingang)")));

		// Transferkonto
		konto = checkKontoTransfer(getKontoFromResultList(kontenList, "--TRANSFER--"));

		assertEquals(2, konto.getBookings().size());

		assertTrue(konto.getBookings().stream()
				.anyMatch(booking -> checkBookingInList(booking, "21.08.06",
						"SDIREKT-UEBERWEISUNG  4,90762702524695E+015  MAX MUSTER  DATUM 21.08.2006, 19.19 UHR  1.TAN 440625", "10,00", "1061525816",
						"Umbuchung (Eingang)")));
		assertTrue(konto.getBookings().stream()
				.anyMatch(booking -> checkBookingInList(booking, "23.08.06", "Einzahlung", "-10,00", "1999333", "Umbuchung (Ausgang)")));
	}

	@Test
	void testBookingProcessorUmbuchungWithThreeDaysDiffference_01() throws Exception {

		Collection<BankAccount> kontenList = converter.convertXmlToCsvEntries(filenameThreeDaysDiff01);
		bookingProcessor.addBookingTypesToAccountBookings(kontenList);
		bookingProcessor.generateCrossBookings(kontenList, true, DAYS_REBOOKING);

		assertNotNull(kontenList);

		assertEquals(3, kontenList.size());

		// Konto (Basis)
		BankAccount konto = checkKontoDBank(getKontoFromResultList(kontenList, "Girokonto D-Bank"));
		assertEquals(1, konto.getBookings().size());

		assertTrue(konto.getBookings().stream().anyMatch(booking -> checkBookingInList(booking, "12.04.19", "Purpose: RINP  Vers. PKV Abschlag  DAUERAUFTRAG",
				"-616,00", "DE00000000", "Umbuchung (Ausgang)")));

		// Konto (Gegenkonto)
		konto = checkKontoVT(getKontoFromResultList(kontenList, "Tagesgeld VT-Bank"));
		assertEquals(1, konto.getBookings().size());

		assertTrue(konto.getBookings().stream()
				.anyMatch(booking -> checkBookingInList(booking, "15.04.19",
						"Purpose: RINP  EndtoEnd: NOTPROVIDED  Vers. PKV Abschlag Dauerauftrag-Gutschrift  Summenbeleg", "616,00", "DE00000000",
						"Umbuchung (Eingang)")));

		// Transferkonto
		konto = checkKontoTransfer(getKontoFromResultList(kontenList, "--TRANSFER--"));

		assertEquals(2, konto.getBookings().size());

		assertTrue(konto.getBookings().stream().anyMatch(booking -> checkBookingInList(booking, "12.04.19", "Purpose: RINP  Vers. PKV Abschlag  DAUERAUFTRAG",
				"616,00", "DE30120300000018884058", "Umbuchung (Eingang)")));
		assertTrue(konto.getBookings().stream()
				.anyMatch(booking -> checkBookingInList(booking, "15.04.19",
						"Purpose: RINP  EndtoEnd: NOTPROVIDED  Vers. PKV Abschlag Dauerauftrag-Gutschrift  Summenbeleg", "-616,00", "DE98501234000126541100",
						"Umbuchung (Ausgang)")));
	}

	@Test
	void testBookingProcessorUmbuchungWithCancellation_01() throws Exception {

		Collection<BankAccount> kontenList = converter.convertXmlToCsvEntries(filenameCancellation01);
		bookingProcessor.addBookingTypesToAccountBookings(kontenList);

		bookingProcessor.revertCancellationRebookings(kontenList);

		bookingProcessor.generateCrossBookings(kontenList, true, DAYS_REBOOKING);

		assertNotNull(kontenList);

		assertEquals(2, kontenList.size());

		// Konto (Basis)
		BankAccount konto = checkKontoHBank(getKontoFromResultList(kontenList, "Girokonto H-Bank"));
		assertEquals(3, konto.getBookings().size());

		assertTrue(konto.getBookings().stream().anyMatch(booking -> checkBookingInList(booking, "15.01.24",
				"Kundenref.: 0200174051/000000002/  V00001  Purpose: RINP  EndtoEnd: NOTPROVIDED  Vers. PKV Abschlag / DA-2  IBAN: DE79701308000004751027 BIC: KTBADEFFXXX  DAUERAUFTRAG",
				"-900,00", "DE79701308000004751027", null)));
		assertTrue(konto.getBookings().stream().anyMatch(booking -> checkBookingInList(booking, "16.01.24",
				"EndtoEnd: NOTPROVIDED  Retoure SEPA Ueberweisung vom 15.01.2024, Rueckgabegrund: AC01 IBAN fehlerhaft und ungültig SVWZ: RETURN, Vers. PKV Abschlag IBAN: DE79701308000004751027 BIC: KTBADEFFXXX  RETOUREN",
				"900,00", "DE79701308000004751027", null)));
		assertTrue(konto.getBookings().stream().anyMatch(booking -> checkBookingInList(booking, "19.01.24",
				"Kundenref.: 0200174051/000000002/  V00002  Purpose: RINP  EndtoEnd: NOTPROVIDED  Vers. PKV Abschlag / DA-2  IBAN: DE79701308000004751027 BIC: GENODEF1M06  DAUERAUFTRAG",
				"-900,00", "DE79701308000004751027", "Umbuchung (Ausgang)")));

		// Konto (Gegenkonto)
		konto = checkKontoMK(getKontoFromResultList(kontenList, "Tagesgeld MeBank"));

		assertTrue(konto.getBookings().stream().anyMatch(booking -> checkBookingInList(booking, "19.01.24",
				"Purpose: RINP  EndtoEnd: NOTPROVIDED  Vers. PKV Abschlag  DAUERAUFTRAG", "900,00", "DE92500617410200174051", "Umbuchung (Eingang)")));
	}

	@Test
	void testBookingProcessorUmbuchungWithCancellation_02() throws Exception {

		Collection<BankAccount> kontenList = converter.convertXmlToCsvEntries(filenameCancellation02);
		bookingProcessor.addBookingTypesToAccountBookings(kontenList);

		bookingProcessor.revertCancellationRebookings(kontenList);

		bookingProcessor.generateCrossBookings(kontenList, true, DAYS_REBOOKING);

		assertNotNull(kontenList);

		assertEquals(2, kontenList.size());

		// Konto (Basis)
		BankAccount konto = checkKontoHBank(getKontoFromResultList(kontenList, "Girokonto H-Bank"));
		assertEquals(4, konto.getBookings().size());

		assertTrue(konto.getBookings().stream().anyMatch(booking -> checkBookingInList(booking, "20.02.25",
				"Cashback zave.it - Max Muster - 2025-02-20 13:14:37 - 81b2288b-fb24-484d-a485-4c253e4c41b3 EREF: d62a19db35ac4edb9c40b2a62bc72b04  UEBERWEISUNG  Purpose: OTHR  EndtoEnd: d62a19db35ac4edb9c40b2a62bc72b04",
				"12,00", "DE33110101015506511807", null)));
		assertTrue(konto.getBookings().stream().anyMatch(booking -> checkBookingInList(booking, "24.02.25",
				"Einzahlung DeDa 01 2025 TAN1:841399 IBAN: DE71500105170990651720 BIC: DEFDDEFFXXX  UEBERWEISUNG  Kundenref.: Payment-Information-ID-3518  EndtoEnd: NOTPROVIDED",
				"-15000,00", "DE71500105170990651720", null)));
		assertTrue(konto.getBookings().stream().anyMatch(booking -> checkBookingInList(booking, "24.02.25",
				"Storno der Buchung des Auftragsbetrages 15000.00 EUR vom 24.02.2025 SVWZ: Einzahlung DeDa 01 2025 TAN1:841399 IBAN: DE71500105170990651720 BIC: DEFDDEFFXXX  STORNO  EndtoEnd: NOTPROVIDED",
				"15000,00", null, null)));
		assertTrue(konto.getBookings().stream().anyMatch(booking -> checkBookingInList(booking, "24.02.25",
				"Einzahlung DeDa 01 2025 TAN1:524502 IBAN: DE71500105170990651720 BIC: DEFDDEFFXXX  UEBERWEISUNG  Kundenref.: Payment-Information-ID-5306  EndtoEnd: NOTPROVIDED",
				"-15000,00", "DE71500105170990651720", "Umbuchung (Ausgang)")));

		// Konto (Gegenkonto)
		konto = checkKontoDeDa(getKontoFromResultList(kontenList, "Tagesgeld I DeDa"));

		assertTrue(konto.getBookings().stream().anyMatch(booking -> checkBookingInList(booking, "24.02.25", "Einzahlung DeDa 01 2025  Gutschrift", "15000,00",
				"DE92500617410200174051", "Umbuchung (Eingang)")));
	}

	@Test
	void testBookingProcessorUmbuchungWithCancellation_03() throws Exception {

		Collection<BankAccount> kontenList = converter.convertXmlToCsvEntries(filenameCancellation03);
		bookingProcessor.addBookingTypesToAccountBookings(kontenList);

		bookingProcessor.revertCancellationRebookings(kontenList);

		bookingProcessor.generateCrossBookings(kontenList, true, DAYS_REBOOKING);

		assertNotNull(kontenList);

		assertEquals(4, kontenList.size());

		// Konto (Basis)
		BankAccount konto = checkKontoDBank(getKontoFromResultList(kontenList, "Girokonto D-Bank"));
		assertEquals(5, konto.getBookings().size());

		assertTrue(konto.getBookings().stream().anyMatch(booking -> checkBookingInList(booking, "25.07.14",
				"SVWZ+Sparrate 6000510554  SONSTIGE BELASTUNGSBUCHUNG", "-25,00", "DE40120300006000510554", "Umbuchung (Ausgang)")));
		assertTrue(konto.getBookings().stream().anyMatch(booking -> checkBookingInList(booking, "25.07.14",
				"KREF+Payment-Information-ID  -1361  SVWZ+VORAUSZAHLUNG SPARPLAN  1 2014  DATUM 25.07.2014, 11.20 UHR  1.TAN 386180  ONLINE-UEBERWEISUNG",
				"-3600,26", "DE32120300006000510084", null)));
		assertTrue(konto.getBookings().stream()
				.anyMatch(booking -> checkBookingInList(booking, "25.07.14",
						"2524 2100 0140 1492 VISA-RU  ECKUEBERWEISUNG UEBER INTER  NETBANKING VOM 25.07.2014 1  124  UMBUCHUNG", "3471,98", "1999333",
						"Umbuchung (Eingang)")));
		assertTrue(konto.getBookings().stream().anyMatch(booking -> checkBookingInList(booking, "25.07.14",
				"SVWZ+VORAUSZAHLUNG SPARPLAN  1 2014  RUECKUEBERWEISUNG  ZAHLUNGSART FUER KTO UNZUL  RUECKUEBERWEISUNG", "3600,26", null, null)));
		assertTrue(konto.getBookings().stream().anyMatch(booking -> checkBookingInList(booking, "31.07.14",
				"KREF+Payment-Information-ID -2232 SVWZ+. VORAUSZAHLUNG SPARPLAN 1 2014 (2. Vers.) DATUM 31.07.2014, 01.25 UHR 1.TAN 243203 ONLINE-UEBERWEISUNG",
				"-3600,26", "DE32120300006000510084", "Umbuchung (Ausgang)")));

		// Konto (Gegenkonto)
		konto = checkKontoDSpar1(getKontoFromResultList(kontenList, "Sparplan 1000 1 D-Bank"));

		assertTrue(konto.getBookings().stream()
				.anyMatch(booking -> checkBookingInList(booking, "31.07.14",
						"KREF+Payment-Information-ID  -2232  SVWZ+. VORAUSZAHLUNG SPARPL  AN 1 2014 (2. Vers.)  GUTSCHRIFT", "3600,26",
						"DE30120300000018884058", "Umbuchung (Eingang)")));
		konto = checkKontoDSpar25(getKontoFromResultList(kontenList, "Sparplan 25 D-Bank"));

		assertTrue(konto.getBookings().stream()
				.anyMatch(booking -> checkBookingInList(booking, "25.07.14", "SONST.GUTSCHRIFT", "25,00", "DE30120300000018884058", "Umbuchung (Eingang)")));
		konto = checkKontoDSparkarte(getKontoFromResultList(kontenList, "Sparkarte VISA D-Bank"));

		assertTrue(konto.getBookings().stream()
				.anyMatch(booking -> checkBookingInList(booking, "25.07.14", "Auszahlung", "-3471,98", "DE30120300000018884058", "Umbuchung (Ausgang)")));
	}

	@Test
	void testBookingProcessorUmbuchungWithCancellation_04() throws Exception {

		Collection<BankAccount> kontenList = converter.convertXmlToCsvEntries(filenameCancellation04);
		bookingProcessor.addBookingTypesToAccountBookings(kontenList);

		bookingProcessor.revertCancellationRebookings(kontenList);

		bookingProcessor.generateCrossBookings(kontenList, true, DAYS_REBOOKING);

		assertNotNull(kontenList);

		assertEquals(2, kontenList.size());

		// Konto (Basis)
		BankAccount konto = checkKontoDBank(getKontoFromResultList(kontenList, "Girokonto D-Bank"));
		assertEquals(3, konto.getBookings().size());

		assertTrue(konto.getBookings().stream().anyMatch(booking -> checkBookingInList(booking, "18.09.08",
				"2705 4090 1424 4614 VISA-RU ECKUEBERWEISUNG UEBER INTERNETBANKING VOM 17.09. 1940 UMBUCHUNG", "30,00", "1999333", "Umbuchung (Eingang)")));
		assertTrue(konto.getBookings().stream().anyMatch(booking -> checkBookingInList(booking, "26.09.08",
				"2705409014244614 MAX MUSTER DATUM 25.09.2008, 19.43 UHR 1.TAN 034183 SDIREKT-UEBERWEISUNG", "-1600,00", "1999333", "Umbuchung (Ausgang)")));
		assertTrue(konto.getBookings().stream()
				.anyMatch(booking -> checkBookingInList(booking, "29.09.08", "2705409014244614 MAX MUSTER", "-800,00", "1999333", "Umbuchung (Ausgang)")));

		// Konto (Gegenkonto)
		konto = checkKontoDSparkarte(getKontoFromResultList(kontenList, "Sparkarte VISA D-Bank"));
		assertEquals(10, konto.getBookings().size());

		assertTrue(konto.getBookings().stream()
				.anyMatch(booking -> checkBookingInList(booking, "18.09.08", "Auszahlung", "-30,00", "18884058", "Umbuchung (Ausgang)")));
		assertTrue(konto.getBookings().stream().anyMatch(booking -> checkBookingInList(booking, "23.09.08", "Zinsen geschaetzt", "0,91", null, "Zinsen")));
		assertTrue(konto.getBookings().stream().anyMatch(booking -> checkBookingInList(booking, "23.09.08", "Storno Auszahlung", "30,00", null, null)));
		assertTrue(konto.getBookings().stream().anyMatch(booking -> checkBookingInList(booking, "23.09.08", "Auszahlung", "-30,00", null, null)));
		assertTrue(konto.getBookings().stream().anyMatch(booking -> checkBookingInList(booking, "26.09.08", "Einzahlung", "1600,00", "18884058", null)));
		assertTrue(konto.getBookings().stream().anyMatch(booking -> checkBookingInList(booking, "26.09.08", "Storno Einzahlung", "-1600,00", null, null)));
		assertTrue(konto.getBookings().stream()
				.anyMatch(booking -> checkBookingInList(booking, "26.09.08", "Einzahlung", "1600,00", "18884058", "Umbuchung (Eingang)")));
		assertTrue(konto.getBookings().stream().anyMatch(booking -> checkBookingInList(booking, "29.09.08", "Einzahlung", "800,00", "18884058", null)));
		assertTrue(konto.getBookings().stream()
				.anyMatch(booking -> checkBookingInList(booking, "29.09.08", "Einzahlung", "800,00", "18884058", "Umbuchung (Eingang)")));
		assertTrue(konto.getBookings().stream().anyMatch(booking -> checkBookingInList(booking, "29.09.08", "Storno Einzahlung", "-800,00", null, null)));
	}

	@Test
	void testBookingProcessorUmbuchungSixAccounts() throws Exception {

		Collection<BankAccount> kontenList = converter.convertXmlToCsvEntries(filenameSixAccounts);
		bookingProcessor.addBookingTypesToAccountBookings(kontenList);
		bookingProcessor.generateCrossBookings(kontenList, true, DAYS_REBOOKING);

		assertEquals(7, kontenList.size());

		BankAccount konto = checkKontoDBank(getKontoFromResultList(kontenList, "Girokonto D-Bank"));
		assertEquals(4, konto.getBookings().size());

		konto = checkKontoDeba(getKontoFromResultList(kontenList, "Tagesgeld I DeDa"));
		assertEquals(6, konto.getBookings().size());

		konto = checkKontoAK(getKontoFromResultList(kontenList, "Tagesgeld AK-Bank"));
		assertEquals(4, konto.getBookings().size());

		konto = checkKontoHBank(getKontoFromResultList(kontenList, "Girokonto H-Bank"));
		assertEquals(6, konto.getBookings().size());

		konto = checkKontoJBank(getKontoFromResultList(kontenList, "Tagesgeld J-Bank"));
		assertEquals(3, konto.getBookings().size());

		konto = checkKontoMK(getKontoFromResultList(kontenList, "Tagesgeld MeBank"));
		assertEquals(9, konto.getBookings().size());

		konto = checkKontoTransfer(getKontoFromResultList(kontenList, "--TRANSFER--"));
		assertEquals(10, konto.getBookings().size());

		assertTrue(konto.getBookings().stream().anyMatch(booking -> checkBookingInList(booking, "14.10.08",
				"EINZAHLUNG 01  DATUM 14.10.2008, 00.06 UHR  1.TAN 798597  SDIREKT-UEBERWEISUNG", "10000,00", "0018884058", "Umbuchung (Eingang)")));
		assertTrue(konto.getBookings().stream()
				.anyMatch(booking -> checkBookingInList(booking, "15.10.08", "EINZAHLUNG 01  Gutschrift", "-10000,00", "990651720", "Umbuchung (Ausgang)")));
		assertTrue(konto.getBookings().stream().anyMatch(booking -> checkBookingInList(booking, "15.10.08",
				"EINZAHLUNG 02  DATUM 15.10.2008, 00.26 UHR  1.TAN 926006  SDIREKT-UEBERWEISUNG", "5150,00", "0018884058", "Umbuchung (Eingang)")));
		assertTrue(konto.getBookings().stream()
				.anyMatch(booking -> checkBookingInList(booking, "16.10.08", "EINZAHLUNG 02  Gutschrift", "-5150,00", "990651720", "Umbuchung (Ausgang)")));
		assertTrue(konto.getBookings().stream().anyMatch(booking -> checkBookingInList(booking, "29.09.11",
				"EINZAHLUNG 05 2011  DATUM 28.09.2011, 20.17 UHR  1.TAN 474396  ONLINE-UEBERWEISUNG", "5000,00", "0018884058", "Umbuchung (Eingang)")));
		assertTrue(konto.getBookings().stream()
				.anyMatch(booking -> checkBookingInList(booking, "30.09.11", "EINZAHLUNG 05 2011  GUTSCHRIFT", "-5000,00", "10052517", "Umbuchung (Ausgang)")));
		assertTrue(konto.getBookings().stream().anyMatch(booking -> checkBookingInList(booking, "15.11.11", "AUSZAHLUNG 01 2011  UEBERWEISUNGSGUTSCHRIFT",
				"-40371,02", "0018884058", "Umbuchung (Ausgang)")));
		assertTrue(konto.getBookings().stream().anyMatch(
				booking -> checkBookingInList(booking, "14.11.11", "AUSZAHLUNG 01 2011  DTA ÜBERWEISUNG", "40371,02", "10052517", "Umbuchung (Eingang)")));
		assertTrue(konto.getBookings().stream().anyMatch(booking -> checkBookingInList(booking, "22.05.24",
				"EndtoEnd: NOTPROVIDED  Auszahlung 022 KT Direktbank  UEBERWEISUNG", "-100,00", "DE92500617410200174051", "Umbuchung (Ausgang)")));
		assertTrue(konto.getBookings().stream().anyMatch(booking -> checkBookingInList(booking, "21.05.24",
				"Kundenref.: Payment-Information-ID-3390  EndtoEnd: NOTPROVIDED  Auszahlung 022 KT Direktbank TAN1:446213 IBAN: DE92500617410200174051 BIC: GENODE99ABC  Überweisungsauftrag",
				"100,00", "DE55500150010006290050", "Umbuchung (Eingang)")));
	}

	@Test
	void testBookingProcessorUmbuchungOnSameDayTwoRebookings_01() throws Exception {

		Collection<BankAccount> kontenList = converter.convertXmlToCsvEntries(filenameSameDayNoDiffTwoRebookings01);
		bookingProcessor.addBookingTypesToAccountBookings(kontenList);
		bookingProcessor.generateCrossBookings(kontenList, true, DAYS_REBOOKING);

		assertNotNull(kontenList);

		assertEquals(2, kontenList.size());

		// Konto (Basis)
		BankAccount konto = checkKontoSK(getKontoFromResultList(kontenList, "Girokonto SK K-Stadt"));
		assertEquals(2, konto.getBookings().size());

		assertTrue(konto.getBookings().stream().anyMatch(booking -> checkBookingInList(booking, "27.01.06",
				"SDIREKT-UEBERWEISUNG  EINZAHLUNG 1-2 / 2006  DATUM 27.01.2006, 15.06 UHR  1.TAN 593508", "-200,00", "7004277541", "Umbuchung (Ausgang)")));
		assertTrue(konto.getBookings().stream().anyMatch(booking -> checkBookingInList(booking, "31.01.06",
				"SDIREKT-UEBERWEISUNG  EINZAHLUNG 3 / 2006  DATUM 31.01.2006, 02.38 UHR  1.TAN 003608", "-200,00", "7004277541", "Umbuchung (Ausgang)")));

		// Konto (Gegenkonto)
		konto = checkKontoBM(getKontoFromResultList(kontenList, "Tagesgeld B--Bank"));
		assertEquals(2, konto.getBookings().size());

		assertTrue(konto.getBookings().stream().anyMatch(booking -> checkBookingInList(booking, "27.01.06",
				"SDIREKT-UEBERWEISUNG  EINZAHLUNG 1-2 / 2006  DATUM 27.01.2006, 15.06 UHR  1.TAN 593508", "200,00", "1061525816", "Umbuchung (Eingang)")));
		assertTrue(konto.getBookings().stream().anyMatch(booking -> checkBookingInList(booking, "31.01.06",
				"SDIREKT-UEBERWEISUNG  EINZAHLUNG 3 / 2006  DATUM 31.01.2006, 02.38 UHR  1.TAN 003608", "200,00", "1061525816", "Umbuchung (Eingang)")));
	}

	@Test
	void testBookingProcessorUmbuchungOnSameDayTwoRebookings_01_WithoutTransferAccount() throws Exception {

		Collection<BankAccount> kontenList = converter.convertXmlToCsvEntries(filenameSameDayNoDiffTwoRebookings01);
		bookingProcessor.addBookingTypesToAccountBookings(kontenList);
		bookingProcessor.generateCrossBookings(kontenList, false, DAYS_REBOOKING);

		assertNotNull(kontenList);

		assertEquals(2, kontenList.size());

		// Konto (Basis)
		BankAccount konto = checkKontoSK(getKontoFromResultList(kontenList, "Girokonto SK K-Stadt"));
		assertEquals(2, konto.getBookings().size());

		assertTrue(konto.getBookings().stream().anyMatch(booking -> checkBookingInList(booking, "27.01.06",
				"SDIREKT-UEBERWEISUNG  EINZAHLUNG 1-2 / 2006  DATUM 27.01.2006, 15.06 UHR  1.TAN 593508", "-200,00", "7004277541", "Umbuchung (Ausgang)")));
		assertTrue(konto.getBookings().stream().anyMatch(booking -> checkBookingInList(booking, "31.01.06",
				"SDIREKT-UEBERWEISUNG  EINZAHLUNG 3 / 2006  DATUM 31.01.2006, 02.38 UHR  1.TAN 003608", "-200,00", "7004277541", "Umbuchung (Ausgang)")));

		assertTrue(konto.getBookings().stream().anyMatch(booking -> booking.getCrossBooking() != null));

		// Konto (Gegenkonto)
		konto = checkKontoBM(getKontoFromResultList(kontenList, "Tagesgeld B--Bank"));
		assertEquals(2, konto.getBookings().size());

		assertTrue(konto.getBookings().stream().anyMatch(booking -> checkBookingInList(booking, "27.01.06",
				"SDIREKT-UEBERWEISUNG  EINZAHLUNG 1-2 / 2006  DATUM 27.01.2006, 15.06 UHR  1.TAN 593508", "200,00", "1061525816", "Umbuchung (Eingang)")));
		assertTrue(konto.getBookings().stream().anyMatch(booking -> checkBookingInList(booking, "31.01.06",
				"SDIREKT-UEBERWEISUNG  EINZAHLUNG 3 / 2006  DATUM 31.01.2006, 02.38 UHR  1.TAN 003608", "200,00", "1061525816", "Umbuchung (Eingang)")));

		assertTrue(konto.getBookings().stream().anyMatch(booking -> booking.getCrossBooking() != null));
	}

	@Test
	void testBookingProcessorUmbuchungOnSameDayTwoRebookings_02_WithoutTransferAccount() throws Exception {

		Collection<BankAccount> kontenList = converter.convertXmlToCsvEntries(filenameSameDayNoDiffTwoRebookings02);
		bookingProcessor.addBookingTypesToAccountBookings(kontenList);
		bookingProcessor.generateCrossBookings(kontenList, false, DAYS_REBOOKING);

		assertNotNull(kontenList);

		assertEquals(2, kontenList.size());

		// Konto (Basis)
		BankAccount konto = checkKontoSK(getKontoFromResultList(kontenList, "Girokonto SK K-Stadt"));
		assertEquals(2, konto.getBookings().size());

		assertTrue(konto.getBookings().stream()
				.anyMatch(booking -> checkBookingInList(booking, "03.03.04", "MAX MUSTER AG", "-2,00", "100657000", "Umbuchung (Ausgang)")));

		assertTrue(konto.getBookings().stream().anyMatch(booking -> booking.getCrossBooking() != null));

		// Konto (Gegenkonto)
		konto = checkKontoCB(getKontoFromResultList(kontenList, "Girokonto C-Bank"));
		assertEquals(2, konto.getBookings().size());

		assertTrue(
				konto.getBookings().stream().anyMatch(booking -> checkBookingInList(booking, "04.03.04", "AG", "2,00", "1061525816", "Umbuchung (Eingang)")));

		assertTrue(konto.getBookings().stream().anyMatch(booking -> booking.getCrossBooking() != null));

	}

	private void checkBooking(Booking booking, String date, String purpose, String amountStr, String crossAccountIBAN, String typStr) {
		assertEquals(date, booking.getDate());
		assertEquals(purpose, booking.getPurpose());
		assertEquals(amountStr, booking.getAmountStr());
		assertEquals(crossAccountIBAN, booking.getCrossAccountIBAN());
		assertEquals(typStr, booking.getTyp().toString());
	}

	private boolean checkBookingInList(Booking booking, String date, String purpose, String amountStr, String crossAccountIBAN, String typStr) {
		return date.equalsIgnoreCase(booking.getDate()) && purpose.equalsIgnoreCase(booking.getPurpose()) && amountStr.equalsIgnoreCase(booking.getAmountStr())
				&& (crossAccountIBAN == null || crossAccountIBAN.equalsIgnoreCase(booking.getCrossAccountIBAN()))
				&& (typStr == null || typStr.equalsIgnoreCase(booking.getTyp().toString()));
	}

	private BankAccount getKontoFromResultList(Collection<BankAccount> kontenList, String kontoName) {
		for (BankAccount konto : kontenList) {
			if (konto.getNamePP().equalsIgnoreCase(kontoName)) {
				return konto;
			}
		}
		return null;
	}

	private BankAccount checkKontoTransfer(BankAccount konto) {
		assertEquals("DE00000000", konto.getIban());
		assertEquals("0000", konto.getNumber());
		return konto;
	}

	private BankAccount checkKontoJBank(BankAccount konto) {
		assertEquals("DE55500150010006290050", konto.getIban());
		assertEquals("6290050", konto.getNumber());
		return konto;
	}

	private BankAccount checkKontoHBank(BankAccount konto) {
		assertEquals("DE92500617410200174051", konto.getIban());
		assertEquals("200174051", konto.getNumber());
		return konto;
	}

	private BankAccount checkKontoDBank(BankAccount konto) {
		assertEquals("DE30120300000018884058", konto.getIban());
		assertEquals("18884058", konto.getNumber());
		return konto;
	}

	private BankAccount checkKontoDSpar1(BankAccount konto) {
		assertEquals("DE32120300006000510084", konto.getIban());
		assertEquals("6000510084", konto.getNumber());
		return konto;
	}

	private BankAccount checkKontoDSpar25(BankAccount konto) {
		assertEquals("DE40120300006000510554", konto.getIban());
		assertEquals("6000510554", konto.getNumber());
		return konto;
	}

	private BankAccount checkKontoDSparkarte(BankAccount konto) {
		assertNull(konto.getIban());
		assertEquals("4748430003203674", konto.getNumber());
		return konto;
	}

	private BankAccount checkKontoDeba(BankAccount konto) {
		assertEquals("DE71500105170990651720", konto.getIban());
		assertEquals("0990651720", konto.getNumber());
		return konto;
	}

	private BankAccount checkKontoAK(BankAccount konto) {
		assertNull(konto.getIban());
		assertEquals("10052517", konto.getNumber());
		return konto;
	}

	private BankAccount checkKontoMK(BankAccount konto) {
		assertEquals("DE79701308000004751027", konto.getIban());
		assertEquals("4751027", konto.getNumber());
		return konto;
	}

	private BankAccount checkKontoGa(BankAccount konto) {
		assertEquals("DE49507504004000545165", konto.getIban());
		assertEquals("4000545165", konto.getNumber());
		return konto;
	}

	private BankAccount checkKontoSK(BankAccount konto) {
		assertNull(konto.getIban());
		assertEquals("1061525816", konto.getNumber());
		return konto;
	}

	private BankAccount checkKontoCB(BankAccount konto) {
		assertEquals("DE96370400440100657000", konto.getIban());
		assertEquals("100657000", konto.getNumber());
		return konto;
	}

	private BankAccount checkKontoVW(BankAccount konto) {
		assertEquals("DE89270200001201854647", konto.getIban());
		return konto;
	}

	private BankAccount checkKontoDeDa(BankAccount konto) {
		assertEquals("DE71500105170990651720", konto.getIban());
		return konto;
	}

	private BankAccount checkKontoVT(BankAccount konto) {
		assertEquals("DE98501234000126541100", konto.getIban());
		return konto;
	}

	private BankAccount checkKontoBM(BankAccount konto) {
		assertEquals("7004277541", konto.getNumber());
		assertNull(konto.getIban());
		return konto;
	}
}
