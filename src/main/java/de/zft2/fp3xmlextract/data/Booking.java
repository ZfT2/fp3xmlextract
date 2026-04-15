package de.zft2.fp3xmlextract.data;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class Booking {

	public enum Typ {

		REBOOKING_IN("Umbuchung (Eingang)"), 
		REBOOKING_OUT("Umbuchung (Ausgang)"), 
		INTEREST("Zinsen"),
		INTEREST_CHARGE("Zinsbelastung"), 
		TAX("Steuern"), 
		TAX_REFUND("Steuerrückerstattung"),
		DIVIDENDS("Dividende"),
		UNKNOWN(null);

		public static Typ forString(String strValue) {
			for (Typ x : values()) {
				if (x.translation.equals(strValue))
					return x;
			}
			return null;
		}

		private final String translation;

		private Typ(String translation) {
			this.translation = translation;
		}

		@Override
		public final String toString() {
			return translation;
		}
	}
	
	public enum SepaTyp {

		BANK_TRANSFER("Überweisung", new String[] { "GUTSCHRIFT", "EINZELÜBERWEISUNG", "DTA ÜBERWEISUNG", "SEPA Gutschrift", "SEPA Gutschrift Bank", "Überweisungsgutschr.", "UEBERWEISUNGSGUTSCHRIFT", "UEBERWEISUNG", "GUTSCHR. UEBERWEISUNG", "Überweisungsauftrag", "SDIREKT-UEBERWEISUNG" }),
		BANK_TRANSFER_ONLINE("Überweisung (Online)", new String[] { "ONLINE-UEBERWEISUNG", "Online Einzelüberweisung", "Online SEPA-Überweisung" }),
		BANK_TRANSFER_EU("EU Überweisung", new String[] { "EU UEBERWEISUNGSAUFTRAG" }),
		DIRECT_DEBIT("SEPA Lastschrift", new String[] { "LASTSCHRIFT", "BASISLASTSCHRIFT", "FOLGELASTSCHRIFT" }),
		DIRECT_DEBIT_OTHER("SEPA Lastschrift (sonstige)", new String[] { "SONSTIGER EINZUG" }),
		STANDING_ORDER("Dauerauftrag", new String[] { "DAUERAUFTRAG", "Dauerauftragsgutschr", "RINP" }),
		REBOOKING("Umbuchung", new String[] { "Umbuchung" }), 
		ACCOUNT_COMPLETION("Abschluss", new String[] { "Abschluss", "Kontoabschluß" }),
		INTEREST("Zins", new String[] { "ZINSEN", "HABENZINSEN" }), 
		TAX_CAPITALGAINS("Steuern (Kapitalertragsteuer)", new String[] { "Kapitalertragsteuer" }),
		TAX_SOLIDARITY_SURCHARGE("Steuern (Solidaritätszuschlag)", new String[] { "Solidaritätszuschlag", "Solidaritaetszuschlag" }),
		TAX_CHURCH("Steuern (Kirchensteuer)", new String[] { "Kirchensteuer" }), 
		CANCELLATION("Storno", new String[] { "Storno" });

		public static SepaTyp forString(String strValue) {
			for (SepaTyp x : values()) {
				for (String purpose : x.purposesList) {
					if (purpose.equalsIgnoreCase(strValue)) {
						return x;
					}
				}
			}
			return null;
		}

		private final String description;
		private final String[] purposesList;

		private SepaTyp(String description, String[] purposesList) {
			this.description = description;
			this.purposesList = purposesList;
		}

		@Override
		public final String toString() {
			return description;
		}
	}

	/** 0 1 2 3 4 5 6 7 8 9
	new String[] { datum, datumBuchung, datumWert, verwendungszweck, betrag, gegenKontoIban, gegenKontoBic, "Typ", "NamePP", "Filename" } **/

	private String date;
	private String dateBooking;
	private String dateValue;
	private String purpose;
	private BigDecimal amount;
	private String crossAccountIBAN;
	private String crossAccountBIC;
	private String crossReceiverName;
	private String crossBankName;
	private String crossAccountNumber;
	private String crossBlz;
	private String category;
	
	private String sepaCustomerRef;
	private String sepaCreditorId;
	private String sepaEndToEnd;
	private String sepaMandate;
	private String sepaPersonId;
	private String sepaPurpose;
	private SepaTyp sepaTyp;
	
	private Typ typ;
	private String crossAccountNamePP;
	private String fileName;
	private String accountNamePP; // for full booking list
	
	private Booking crossBooking;
	
	public Booking(String dateBooking, String dateValue, String purpose, BigDecimal amount,
			String crossAccountIBAN, String crossAccountBIC, String accountNamePP) {
		this.date = dateValue != null ? dateValue : dateBooking;
		this.dateBooking = dateBooking;
		this.dateValue = dateValue;
		this.purpose = purpose;
		this.amount = amount;
		this.crossAccountIBAN = crossAccountIBAN;
		this.crossAccountBIC = crossAccountBIC;
		this.accountNamePP = accountNamePP;
	}
	
	public Booking(Booking bookingToCopy) {
		this.date = bookingToCopy.date;
		this.dateBooking = bookingToCopy.dateBooking;
		this.dateValue = bookingToCopy.dateValue;
		this.purpose = bookingToCopy.purpose;
		this.amount = bookingToCopy.amount;
		this.crossAccountIBAN = bookingToCopy.crossAccountIBAN;
		this.crossAccountBIC = bookingToCopy.crossAccountBIC;
		this.crossReceiverName = bookingToCopy.crossReceiverName;
		this.crossBankName = bookingToCopy.crossBankName;
		this.typ = bookingToCopy.typ;
		this.crossAccountNamePP = bookingToCopy.crossAccountNamePP;
		this.fileName = bookingToCopy.fileName;
		this.accountNamePP = bookingToCopy.accountNamePP;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getDateBooking() {
		return dateBooking;
	}

	public void setDateBooking(String dateBooking) {
		this.dateBooking = dateBooking;
	}

	public String getDateValue() {
		return dateValue;
	}

	public void setDateValue(String dateValue) {
		this.dateValue = dateValue;
	}

	public String getPurpose() {
		return purpose;
	}

	public void setPurpose(String purpose) {
		this.purpose = purpose;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public String getCrossAccountIBAN() {
		return crossAccountIBAN;
	}

	public void setCrossAccountIBAN(String crossAccountIBAN) {
		this.crossAccountIBAN = crossAccountIBAN;
	}

	public String getCrossAccountBIC() {
		return crossAccountBIC;
	}

	public void setCrossAccountBIC(String crossAccountBIC) {
		this.crossAccountBIC = crossAccountBIC;
	}

	public String getCrossReceiverName() {
		return crossReceiverName;
	}

	public void setCrossReceiverName(String crossReceiverName) {
		this.crossReceiverName = crossReceiverName;
	}

	public String getCrossBankName() {
		return crossBankName;
	}

	public void setCrossBankName(String crossBankName) {
		this.crossBankName = crossBankName;
	}

	public String getCrossAccountNumber() {
		return crossAccountNumber;
	}

	public void setCrossAccountNumber(String crossAccountNumber) {
		this.crossAccountNumber = crossAccountNumber;
	}

	public String getCrossBlz() {
		return crossBlz;
	}

	public void setCrossBlz(String crossBlz) {
		this.crossBlz = crossBlz;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getSepaCustomerRef() {
		return sepaCustomerRef;
	}

	public void setSepaCustomerRef(String sepaCustomerRef) {
		this.sepaCustomerRef = sepaCustomerRef;
	}

	public String getSepaCreditorId() {
		return sepaCreditorId;
	}

	public void setSepaCreditorId(String sepaCreditorId) {
		this.sepaCreditorId = sepaCreditorId;
	}

	public String getSepaEndToEnd() {
		return sepaEndToEnd;
	}

	public void setSepaEndToEnd(String sepaEndToEnd) {
		this.sepaEndToEnd = sepaEndToEnd;
	}

	public String getSepaMandate() {
		return sepaMandate;
	}

	public void setSepaMandate(String sepaMandate) {
		this.sepaMandate = sepaMandate;
	}

	public String getSepaPersonId() {
		return sepaPersonId;
	}

	public void setSepaPersonId(String sepaPersonId) {
		this.sepaPersonId = sepaPersonId;
	}

	public String getSepaPurpose() {
		return sepaPurpose;
	}

	public void setSepaPurpose(String sepaPurpose) {
		this.sepaPurpose = sepaPurpose;
	}

	public SepaTyp getSepaTyp() {
		return sepaTyp;
	}

	public void setSepaTyp(SepaTyp sepaTyp) {
		this.sepaTyp = sepaTyp;
	}

	public Typ getTyp() {
		return typ;
	}

	public void setTyp(Typ typ) {
		this.typ = typ;
	}

	public String getCrossAccountNamePP() {
		return crossAccountNamePP;
	}

	public void setCrossAccountNamePP(String crossAccountNamePP) {
		this.crossAccountNamePP = crossAccountNamePP;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getAccountNamePP() {
		return accountNamePP;
	}

	public void setAccountNamePP(String accountNamePP) {
		this.accountNamePP = accountNamePP;
	}

	public Booking getCrossBooking() {
		return crossBooking;
	}

	public void setCrossBooking(Booking crossBooking) {
		this.crossBooking = crossBooking;
	}

	public String getAmountStr() {
		return String.format("%.2f", amount.setScale(2, RoundingMode.DOWN));
	}
	
	public String getTypStr() {
		return typ != null ? typ.toString() : null;
	}
	
	public static int compareBookingByAccountThenDate(Booking b1, Booking b2) {
		int value1 = b1.getCrossAccountNamePP().compareTo(b2.getCrossAccountNamePP());
		if (value1 == 0) {
			int value2 = b1.getAccountNamePP().compareTo(b2.getAccountNamePP());
			if (value2 == 0) {
				return LocalDate.parse(b1.getDate(), DateTimeFormatter.ofPattern("dd.MM.uu"))
						.compareTo(LocalDate.parse(b2.getDate(), DateTimeFormatter.ofPattern("dd.MM.uu")));
			} else {
				return value2;
			}
		}
		return value1;
	}
	
	

	@Override
	public int hashCode() {
		return Objects.hash(accountNamePP, amount, crossAccountBIC, crossAccountIBAN, crossAccountNamePP, date, dateBooking,
				dateValue, fileName, purpose, typ);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Booking other = (Booking) obj;
		return Objects.equals(accountNamePP, other.accountNamePP) && Objects.equals(amount, other.amount)
				&& Objects.equals(crossAccountBIC, other.crossAccountBIC)
				&& Objects.equals(crossAccountIBAN, other.crossAccountIBAN)
				&& Objects.equals(crossAccountNamePP, other.crossAccountNamePP) && Objects.equals(date, other.date)
				&& Objects.equals(dateBooking, other.dateBooking) && Objects.equals(dateValue, other.dateValue)
				&& Objects.equals(fileName, other.fileName) && Objects.equals(purpose, other.purpose)
				&& typ == other.typ;
	}
}
