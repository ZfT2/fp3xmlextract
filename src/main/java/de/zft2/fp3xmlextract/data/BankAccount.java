package de.zft2.fp3xmlextract.data;

import java.math.BigDecimal;
import java.util.List;

public class BankAccount {

	private String iban;
	private String bic;
	private String number;
	private String blz;
	private String bankName;
	private String type;
	private String bezeichnung;
	private BigDecimal balance;
	private String namePP;

	private List<Booking> bookings;

	public String getIdentifier() {
		String identifier = null;
		if (getIban() != null) {
			identifier = getIban();
		} else if (getNumber() != null) {
			identifier = getNumber();
		} else if (getBankName() != null) {
			identifier = getBankName();
		}
		return identifier;
	}

	public String getIban() {
		return iban;
	}

	public void setIban(String iban) {
		this.iban = iban;
	}

	public String getBic() {
		return bic;
	}

	public void setBic(String bic) {
		this.bic = bic;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public String getBlz() {
		return blz;
	}

	public void setBlz(String blz) {
		this.blz = blz;
	}

	public String getBankName() {
		return bankName;
	}

	public void setBankName(String bankName) {
		this.bankName = bankName;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getBezeichnung() {
		return bezeichnung;
	}

	public void setBezeichnung(String bezeichnung) {
		this.bezeichnung = bezeichnung;
	}

	public BigDecimal getBalance() {
		return balance;
	}

	public void setBalance(BigDecimal balance) {
		this.balance = balance;
	}

	public String getNamePP() {
		return namePP;
	}

	public void setNamePP(String namePP) {
		this.namePP = namePP;
	}

	public List<Booking> getBookings() {
		return bookings;
	}

	public void setBookings(List<Booking> bookings) {
		this.bookings = bookings;
	}

}
