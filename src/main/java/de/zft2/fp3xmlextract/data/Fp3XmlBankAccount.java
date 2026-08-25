package de.zft2.fp3xmlextract.data;

import java.math.BigDecimal;
import java.util.List;

import de.zft2.core.dto.Account;

public class Fp3XmlBankAccount implements Account<Fp3XmlBooking> {

	private String iban;
	private String bic;
	private String number;
	private String blz;
	private String bankName;
	private String type;
	private String accountName;
	private String parentAccount;
	private BigDecimal balance;
	private String baseCurrency;
	private String namePP;

	private List<Fp3XmlBooking> bookings;

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

	public String getAccountName() {
		return accountName;
	}

	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}

	public String getParentAccount() {
		return parentAccount;
	}

	public void setParentAccount(String parentAccount) {
		this.parentAccount = parentAccount;
	}

	public BigDecimal getBalance() {
		return balance;
	}

	public void setBalance(BigDecimal balance) {
		this.balance = balance;
	}

	public String getBaseCurrency() {
		return baseCurrency;
	}

	public void setBaseCurrency(String baseCurrency) {
		this.baseCurrency = baseCurrency;
	}

	public String getNamePP() {
		return namePP;
	}

	public void setNamePP(String namePP) {
		this.namePP = namePP;
	}

	public List<Fp3XmlBooking> getBookings() {
		return bookings;
	}

	public void setBookings(List<Fp3XmlBooking> bookings) {
		this.bookings = bookings;

	}
}
