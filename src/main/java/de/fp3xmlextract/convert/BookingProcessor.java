package de.fp3xmlextract.convert;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.fp3xmlextract.config.Fp3xmlextractProperties;
import de.fp3xmlextract.data.BankAccount;
import de.fp3xmlextract.data.Booking;
import de.fp3xmlextract.data.Booking.Typ;
import de.fp3xmlextract.exception.AccountException;
import de.fp3xmlextract.exception.ConfigurationException;

public class BookingProcessor extends AccountProcessor {

	private static Logger log = LogManager.getLogger(BookingProcessor.class);

	private static final String NAME_ACCOUNT_TRANSFER = "--TRANSFER--";

	private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yy", Locale.GERMAN);

	private static Fp3xmlextractProperties propsBookingTypes;
	private static Fp3xmlextractProperties propsCancel;

	public BookingProcessor() throws ConfigurationException {
		super();
		init();
	}

	private static void init() throws ConfigurationException {
		if (propsBookingTypes == null && propsCancel == null) {
			propsBookingTypes = Fp3xmlextractProperties.getInstance("bookings.properties", false);
			propsCancel = Fp3xmlextractProperties.getInstance("cancel.properties", true);
		}
	}

	public void addBookingTypesToAccountBookings(Collection<BankAccount> kontenList) {
		for (BankAccount konto : kontenList) {
			addBookingTypes(konto.getBookings());
		}
	}
	
	public void addBookingTypes(Collection<Booking> records) {

		for (Booking booking : records) {
			if (booking.getCrossAccountIBAN() != null) {
				booking.setCrossAccountNamePP(findAccountNamePP(booking.getCrossAccountIBAN()));
				if (booking.getCrossAccountNamePP() == null && booking.getCrossAccountIBAN().length() >= 15) {
					booking.setCrossAccountNamePP(
							findAccountNamePP(booking.getCrossAccountIBAN().substring(12).replaceFirst("^0+(?!$)", "")));
				}
			}
			determineBookingTyp(booking);
		}
	}

	private void determineBookingTyp(Booking booking) {
		if (booking.getAmount() == null) {
			booking.setTyp(Typ.UNKNOWN);
		} else if (isNotEmpty(booking.getCrossAccountNamePP())){
			booking.setTyp(booking.getAmount().compareTo(BigDecimal.ZERO) <= 0 ? Typ.REBOOKING_OUT : Typ.REBOOKING_IN);
		} else if (matchesBookingType(booking.getPurpose(), propsBookingTypes.getProp("INTEREST").split(";"), false)
				|| matchesBookingType(booking.getPurpose(), propsBookingTypes.getProp("INTEREST_WHOLE_WORD").split(";"), true)) {
			booking.setTyp(booking.getAmount().compareTo(BigDecimal.ZERO) <= 0 ? Typ.INTEREST_CHARGE : Typ.INTEREST);
		} else if (matchesBookingType(booking.getPurpose(), propsBookingTypes.getProp("TAX").split(";"), false)) {
			booking.setTyp(booking.getAmount().compareTo(BigDecimal.ZERO) <= 0 ? Typ.TAX : Typ.TAX_REFUND);
/**		} else if (matchesBookingType(booking.getPurpose(), propsBookingTypes.getProp("DIVIDENDS").split(";"), false)) {
			booking.setTyp(Typ.DIVIDENDS); **/
		} else {
			// booking.setTyp(Typ.UNKNOWN);
			booking.setTyp(null); /* must be Deposit or Removal */
		}
	}

	private boolean isNotEmpty(String value) {
		return (value != null && !"".equalsIgnoreCase(value));
	}

	
	private boolean matchesBookingType(String verwendungszweck, String[] musterList, boolean wholeWord) {
		for (String muster : musterList) {
			if (!wholeWord && verwendungszweck.contains(muster) || (wholeWord && verwendungszweck.trim().equals(muster))) {
				return true;
			}
		}
		return false;
	}
	
	public Collection<BankAccount> generateCrossBookings(Collection<BankAccount> accountList, boolean withTransferAccount, int daysRebooking) {
		
		if (setupTransferProperties(accountList)) {
			for (BankAccount account : accountList) {
				if (account.getNamePP() == null) {
					account.setNamePP(account.getBezeichnung());
				}
				addBookingTypes(account.getBookings());
			}
		}
		
		BankAccount accountTansfer = null;
		if (withTransferAccount) {
			accountTansfer = new BankAccount();

			accountTansfer.setIban("DE00000000");
			accountTansfer.setBic("BIC00");
			accountTansfer.setNumber("0000");
			accountTansfer.setNamePP(NAME_ACCOUNT_TRANSFER);
			accountTansfer.setBookings(new ArrayList<>());
		}

		for (BankAccount account : accountList) {

			if (account.getNamePP() == null) {
				continue;
			}

			Collection<BankAccount> kontenListToCompare = new ArrayList<>(accountList);
			kontenListToCompare.remove(account);

			generateCrossTransferBooking(accountTansfer, account, kontenListToCompare, withTransferAccount, daysRebooking);
		}

		if (accountTansfer != null && !accountTansfer.getBookings().isEmpty()) {
			accountList.add(accountTansfer);
		}

		return accountList;
	}

	private void generateCrossTransferBooking(BankAccount accountTansfer, BankAccount account, Collection<BankAccount> kontenListToCompare,
			boolean withTransferAccount, int daysRebooking) {
		for (Booking booking : account.getBookings()) {
			if (!NAME_ACCOUNT_TRANSFER.equalsIgnoreCase(booking.getCrossAccountNamePP())
					&& !account.getNamePP().equalsIgnoreCase(booking.getCrossAccountNamePP())
					&& (booking.getTyp() == Typ.REBOOKING_IN || booking.getTyp() == Typ.REBOOKING_OUT)) {

				Booking crossBookingToTransfer = null;
				try {
					BankAccount kontoCmp = getKontoByNamePP(kontenListToCompare, booking.getCrossAccountNamePP());
					crossBookingToTransfer = findCrossBooking(kontoCmp, booking, daysRebooking, withTransferAccount);
				} catch (AccountException ae) {
					log.error(
							"Booking (Account: {}) {} / {} / {} has cross reference to Account {}, "
									+ "but this Account does not exist in File!",
							account.getNamePP(), booking.getDate(), booking.getPurpose(), booking.getCrossAccountIBAN(),
							booking.getCrossAccountNamePP(), ae);
					continue;
				} catch (Exception e) {
					log.error("Error finding CMP Account: {}!", booking.getCrossAccountNamePP(), e);
				}

				if (crossBookingToTransfer != null) {
					Booking bookingForTransfer = new Booking(booking);
					bookingForTransfer.setAmount(crossBookingToTransfer.getAmount());
					bookingForTransfer.setCrossAccountIBAN(crossBookingToTransfer.getCrossAccountIBAN());
					bookingForTransfer.setCrossAccountBIC(crossBookingToTransfer.getCrossAccountBIC());
					bookingForTransfer.setTyp(crossBookingToTransfer.getTyp());
					bookingForTransfer.setCrossAccountNamePP(crossBookingToTransfer.getCrossAccountNamePP());

					Booking crossBookingForTransfer = new Booking(crossBookingToTransfer);
					crossBookingForTransfer.setAmount(booking.getAmount());
					crossBookingForTransfer.setCrossAccountIBAN(booking.getCrossAccountIBAN());
					crossBookingForTransfer.setCrossAccountBIC(booking.getCrossAccountBIC());
					crossBookingForTransfer.setTyp(booking.getTyp());
					crossBookingForTransfer.setCrossAccountNamePP(booking.getCrossAccountNamePP());

					accountTansfer.getBookings().add(bookingForTransfer);
					accountTansfer.getBookings().add(crossBookingForTransfer);

					// modify original
					booking.setCrossAccountIBAN(accountTansfer.getIban());
					booking.setCrossAccountBIC(accountTansfer.getBic());
					booking.setCrossAccountNamePP(accountTansfer.getNamePP());
					booking.setCrossBooking(crossBookingToTransfer);

					crossBookingToTransfer.setCrossAccountIBAN(accountTansfer.getIban());
					crossBookingToTransfer.setCrossAccountBIC(accountTansfer.getBic());
					crossBookingToTransfer.setCrossAccountNamePP(accountTansfer.getNamePP());
					crossBookingToTransfer.setCrossBooking(booking);
				}
			}
		}
	}
	
	private Booking findCrossBooking(BankAccount kontoCmp, Booking baseBooking, int maxTimeBetween, boolean withTransferAccount) {
		Booking rebookingCandidate = null;
		int days = maxTimeBetween;
		for (Booking bookingCmp : kontoCmp.getBookings()) {
			if (isCorrespondingRebooking(baseBooking.getTyp(), bookingCmp.getTyp())
					&& compareCorrespondingAmount(baseBooking.getAmount(), bookingCmp.getAmount())
					&& compareCrossAccount(baseBooking.getCrossAccountNamePP(), bookingCmp)
					&& compareBaseAccount(baseBooking.getAccountNamePP(), bookingCmp)) {				
				int daysBetweenFound = compareTransactionDates(baseBooking.getDate(), bookingCmp.getDate(), bookingCmp.getTyp(), maxTimeBetween);

				if (daysBetweenFound == 0) {
					baseBooking.setCrossBooking(bookingCmp);
					return null;
				} else if (daysBetweenFound > 0 && isDaysInRange(daysBetweenFound, maxTimeBetween) && daysBetweenFound < days) {
					rebookingCandidate = bookingCmp;
					days = daysBetweenFound;
				}
			}
		}

		if (withTransferAccount) {
			return rebookingCandidate;
		}
		baseBooking.setCrossBooking(rebookingCandidate);
		return null;
	}

	private boolean isUmbuchung(Typ bookingType) {
		return (bookingType != null && bookingType == Typ.REBOOKING_IN || bookingType == Typ.REBOOKING_OUT);
	}

	private boolean isCorrespondingRebooking(Typ bookingTypeBase, Typ bookingTypeCmp) {
		return (bookingTypeBase != null && bookingTypeCmp != null
				&& (bookingTypeBase == Typ.REBOOKING_IN && bookingTypeCmp == Typ.REBOOKING_OUT
						|| bookingTypeBase == Typ.REBOOKING_OUT && bookingTypeCmp == Typ.REBOOKING_IN));
	}

	private boolean compareAmount(BigDecimal amountBasebooking, BigDecimal amountCmpKonto) {
		return (amountBasebooking.abs().compareTo(amountCmpKonto.abs()) == 0);
	}

	private boolean compareCorrespondingAmount(BigDecimal amountBasebooking, BigDecimal amountCmpKonto) {
		return (amountBasebooking.multiply(BigDecimal.valueOf(-1L)).compareTo(amountCmpKonto) == 0);
	}
	
	private int compareTransactionDates(String dateBasebooking, String dateCmpKonto, Typ typ, int timeBetween) {
		LocalDate dateValueBasebooking = LocalDate.parse(dateBasebooking, formatter);
		LocalDate dateValueCmpbooking = LocalDate.parse(dateCmpKonto, formatter);

		if (dateValueBasebooking.isEqual(dateValueCmpbooking) && timeBetween > 0) {
			return 0; // direct Rebooking without transfer account
		} else if (typ == Typ.REBOOKING_IN) {
			return Math.abs((int) ChronoUnit.DAYS.between(dateValueCmpbooking, dateValueBasebooking));
		} else if (typ == Typ.REBOOKING_OUT) {
			return Math.abs((int) ChronoUnit.DAYS.between(dateValueCmpbooking, dateValueBasebooking));
		} else {
			return -1;
		}
	}

	private boolean compareCrossAccount(String crossAccountNamePP, Booking possibleReBooking) {
		return possibleReBooking.getAccountNamePP() != null
				&& possibleReBooking.getAccountNamePP().equals(crossAccountNamePP);
	}

	private boolean compareBaseAccount(String baseAccountNamePP, Booking possibleReBooking) {
		return possibleReBooking.getAccountNamePP() != null
				&& possibleReBooking.getCrossAccountNamePP().equals(baseAccountNamePP);
	}

	private BankAccount getKontoByNamePP(Collection<BankAccount> kontenList, String kontoNamePP) throws AccountException {
		for (BankAccount konto : kontenList) {
			if (kontoNamePP.equalsIgnoreCase(konto.getNamePP()) || konto.getNamePP().contains(kontoNamePP)) {
				return konto;
			}
		}
		throw new AccountException("Konto " + kontoNamePP + " not found!");
	}

	private boolean isDaysInRange(long value, int timeBetween) {
		return (value >= -timeBetween && value <= timeBetween);
	}

	public void revertCancellationRebookings(Collection<BankAccount> accountList) {

		Map<String, String[]> cancelBookingsMap = new HashMap<>();
		for (final String name : propsCancel.stringPropertyNames()) {
			cancelBookingsMap.put(name, propsCancel.getProperty(name).split(";"));
		}

		int cancellationBookingsCount = 0;
		for (BankAccount account : accountList) {
			String[] cancelPatterns = cancelBookingsMap.get(account.getNamePP());
			if (cancelPatterns == null) {
				continue;
			}
			for (String cancelPatternStr : cancelPatterns) {
				Pattern pattern = Pattern.compile(cancelPatternStr);
				for (Booking bookingCancel : account.getBookings()) {
					Matcher matcher = pattern.matcher(bookingCancel.getPurpose());
					if (matcher.find()) {
						String purposeToSearch = matcher.group(1);
						cancellationBookingsCount = searchAccountforCancelBooking(account, bookingCancel, purposeToSearch, cancellationBookingsCount);
					}
				}
			}
		}
		log.warn("Found Cancellation Bookings: {}\n", cancellationBookingsCount);
	}

	private int searchAccountforCancelBooking(BankAccount account, Booking bookingCancel, String purposeToSearch, int cancellationBookingsCount) {
		for (Booking bookingOriginal : account.getBookings()) {
			if (bookingOriginal.getPurpose().contains(purposeToSearch) 
					&& isDaysInRange(ChronoUnit.DAYS.between( LocalDate.parse(bookingCancel.getDate(), formatter), LocalDate.parse(bookingOriginal.getDate(), formatter)), 3)
					&& compareCorrespondingAmount(bookingCancel.getAmount(), bookingOriginal.getAmount())) {
				log.warn("Cancellation-Booking to revert found: (Account: {} ) {} / {} / {} / {} / {} \n,"
								+ "corresponding booking: {} / {}/ {} / {} / {} ",
						account.getNamePP(), bookingCancel.getDate(), bookingCancel.getAmountStr(),
						bookingCancel.getPurpose(), bookingCancel.getCrossAccountIBAN(),
						bookingCancel.getCrossAccountNamePP(), bookingOriginal.getDate(),
						bookingOriginal.getAmountStr(), bookingOriginal.getPurpose(),
						bookingOriginal.getCrossAccountIBAN(),
						bookingOriginal.getCrossAccountNamePP());

				bookingCancel.setTyp(Typ.UNKNOWN);
				bookingCancel.setCrossAccountNamePP(null);
				bookingOriginal.setTyp(Typ.UNKNOWN);
				bookingOriginal.setCrossAccountNamePP(null);

				cancellationBookingsCount++;

				break;
			}
		}
		return cancellationBookingsCount;
	}

	public String getAccountBalance(BankAccount account) {
		BigDecimal balanceAccount = BigDecimal.ZERO;
		for (Booking booking : account.getBookings()) {
			balanceAccount = balanceAccount.add(booking.getAmount());
		}
		return balanceAccount.toString();
	}

	public int countAccountBookings(BankAccount account, Typ typ) {
		int reBookings = 0;
		for (Booking booking : account.getBookings()) {
			if (!NAME_ACCOUNT_TRANSFER.equalsIgnoreCase(booking.getAccountNamePP())
					&& !NAME_ACCOUNT_TRANSFER.equalsIgnoreCase(booking.getCrossAccountNamePP())
					&& typ == booking.getTyp()
					|| (typ == null && (booking.getTyp() == null || booking.getTyp() == Typ.UNKNOWN))) {
				reBookings++;
			}
		}
		return reBookings;
	}

	public void removeDoubleSameDayRebookings(Collection<BankAccount> accountList) throws AccountException {
		int removeCount = 0;
		for (BankAccount account : accountList) {
			if (NAME_ACCOUNT_TRANSFER.equalsIgnoreCase(account.getNamePP())) {
				continue;
			}
			for (Booking possibleReBooking : account.getBookings()) {
				if (isUmbuchung(possibleReBooking.getTyp())
						&& !NAME_ACCOUNT_TRANSFER.equalsIgnoreCase(possibleReBooking.getCrossAccountNamePP())) {
					removeCount = removeBookingsFromCrossAccount(accountList, possibleReBooking, removeCount);
				}
			}
		}

		log.warn("Removed total {} cross Bookings", removeCount);
	}

	private int removeBookingsFromCrossAccount(Collection<BankAccount> accountList, Booking possibleReBooking, int removeCount) throws AccountException {
		List<Booking> crossAccountBookings = getKontoByNamePP(accountList, possibleReBooking.getCrossAccountNamePP()).getBookings();
		for (Booking bookingCrossAccount : crossAccountBookings) {
			if (isCorrespondingRebooking(possibleReBooking.getTyp(), bookingCrossAccount.getTyp())
					&& compareCrossAccount(bookingCrossAccount.getCrossAccountNamePP(), possibleReBooking)
					&& possibleReBooking.getDate().equalsIgnoreCase(bookingCrossAccount.getDate())
					&& compareCorrespondingAmount(possibleReBooking.getAmount(), bookingCrossAccount.getAmount())) {
				if (crossAccountBookings.remove(bookingCrossAccount)) {
					removeCount++;
				}
				break;
			}
		}
		return removeCount;
	}
	
	public void removeTransferAccountBookings(Collection<BankAccount> accountList) {
		for (BankAccount account : accountList) {
			if (NAME_ACCOUNT_TRANSFER.equalsIgnoreCase(account.getNamePP())) {
				accountList.remove(account);
			}
		}
	}

	public List<Booking> findMissingRebookings(Collection<Booking> bookingListWithoutTransfer) {

		int rebookingsFoundCount = 0;
		int rebookingsMissingCount = 0;

		List<Booking> missingReBookings = new ArrayList<>();

		for (Booking baseBooking : bookingListWithoutTransfer) {
			if (!isUmbuchung(baseBooking.getTyp())
					|| NAME_ACCOUNT_TRANSFER.equalsIgnoreCase(baseBooking.getCrossAccountNamePP())) {
				continue;
			}
			boolean foundRebooking = false;
			for (Booking possibleReBooking : bookingListWithoutTransfer) {
				if (!possibleReBooking.equals(baseBooking)
						&& compareTransactionDates(baseBooking.getDate(), possibleReBooking.getDate(),
								possibleReBooking.getTyp(), 0) == 0
						&& compareAmount(baseBooking.getAmount(), possibleReBooking.getAmount())
						&& compareCrossAccount(baseBooking.getCrossAccountNamePP(), possibleReBooking)) {
					foundRebooking = true;
					rebookingsFoundCount++;
					break;
				}
			}
			if (!foundRebooking) {
				rebookingsMissingCount++;

				missingReBookings.add(baseBooking);
			}
		}

		log.warn("Missing Rebookings: {}", rebookingsMissingCount);
		log.warn("Found Rebookings: {}", rebookingsFoundCount);

		return missingReBookings;
	}

	public List<Booking> findReBookings(Collection<Booking> bookingListWithoutTransfer) {
		List<Booking> reBookingsList = new ArrayList<>();
		for (Booking possibleReBooking : bookingListWithoutTransfer) {
			if (!NAME_ACCOUNT_TRANSFER.equalsIgnoreCase(possibleReBooking.getAccountNamePP())
					&& !NAME_ACCOUNT_TRANSFER.equalsIgnoreCase(possibleReBooking.getCrossAccountNamePP())
					&& isUmbuchung(possibleReBooking.getTyp())) {
				reBookingsList.add(possibleReBooking);
			}
		}
		return reBookingsList;
	}
	
	public void printMissingDoubleSameDayRebookings(List<Booking> missingBookings) {
		
		missingBookings.sort(Booking::compareBookingByAccountThenDate);

		for (Booking baseBooking : missingBookings) {
			log.printf(Level.WARN,
					"Re-Booking missing for (BaseAccount): %-25s / %8s / %9s / %-160s / %23s / %11s / %25s",
					baseBooking.getAccountNamePP(), baseBooking.getDate(), baseBooking.getAmountStr(),
					baseBooking.getPurpose().substring(0,
							baseBooking.getPurpose().length() > 160 ? 160 : baseBooking.getPurpose().length()),
					baseBooking.getCrossAccountIBAN(), baseBooking.getCrossAccountBIC(),
					baseBooking.getCrossAccountNamePP());
		}

	}
}
