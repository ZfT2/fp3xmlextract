package de.zft2.fp3xmlextract.convert;

import java.util.Collection;
import java.util.function.BiPredicate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.zft2.core.dto.Account;
import de.zft2.core.dto.Counterpart;
import de.zft2.core.dto.DefaultCounterpart;
import de.zft2.core.exception.ConfigurationException;
import de.zft2.core.process.BookingProcessor;
import de.zft2.fp3xmlextract.data.Fp3XmlBankAccount;
import de.zft2.fp3xmlextract.data.Fp3XmlBooking;

public class Fp3XmlBookingProcessor extends BookingProcessor<Fp3XmlBooking, Fp3XmlBankAccount> {

	private static Logger log = LogManager.getLogger(Fp3XmlBookingProcessor.class);

	public Fp3XmlBookingProcessor() throws ConfigurationException {
		super();
	}

	@Override
	public void generateAndLinkCrossBookingsOnTransferAccount(Account<Fp3XmlBooking> accountTansfer, de.zft2.core.dto.Booking booking,
			de.zft2.core.dto.Booking crossBookingToTransfer) {

		log.debug("generateAndLinkCrossBookingsOnTransferAccount()");

		Fp3XmlBooking bookingForTransfer = new Fp3XmlBooking(booking);
		bookingForTransfer.setAmount(crossBookingToTransfer.getAmount());
		setCounterpartAccount(bookingForTransfer, crossBookingToTransfer.getCounterpart());
		bookingForTransfer.setTyp(crossBookingToTransfer.getTyp());
		bookingForTransfer.setCrossAccountName(crossBookingToTransfer.getCrossAccountName());

		Fp3XmlBooking crossBookingForTransfer = new Fp3XmlBooking(crossBookingToTransfer);
		crossBookingForTransfer.setAmount(booking.getAmount());
		setCounterpartAccount(crossBookingForTransfer, booking.getCounterpart());
		crossBookingForTransfer.setTyp(booking.getTyp());
		crossBookingForTransfer.setCrossAccountName(booking.getCrossAccountName());

		accountTansfer.getBookings().add(bookingForTransfer);
		accountTansfer.getBookings().add(crossBookingForTransfer);

		// modify original
		setCounterpartAccount(booking, accountTansfer.getIban(), accountTansfer.getBic(), accountTansfer.getNumber(), accountTansfer.getBlz());
		booking.setCrossAccountName(accountTansfer.getNamePP());
		booking.setCrossBooking(crossBookingToTransfer);

		setCounterpartAccount(crossBookingToTransfer, accountTansfer.getIban(), accountTansfer.getBic(), accountTansfer.getNumber(),
				accountTansfer.getBlz());
		crossBookingToTransfer.setCrossAccountName(accountTansfer.getNamePP());
		crossBookingToTransfer.setCrossBooking(booking);
	}

	private void setCounterpartAccount(de.zft2.core.dto.Booking booking, Counterpart sourceCounterpart) {
		setCounterpartAccount(booking, Counterpart.ibanOf(sourceCounterpart), Counterpart.bicOf(sourceCounterpart),
				sourceCounterpart != null ? sourceCounterpart.getAccountNumber() : null, sourceCounterpart != null ? sourceCounterpart.getBlz() : null);
	}

	private void setCounterpartAccount(de.zft2.core.dto.Booking booking, String iban, String bic, String accountNumber, String blz) {
		booking.setCounterpart(DefaultCounterpart.withAccount(booking.getCounterpart(), iban, bic, accountNumber, blz));
	}

	public void generateCrossBookings(Collection<Fp3XmlBankAccount> accountList, boolean withTransferAccount, int daysRebooking) {
		Fp3XmlBankAccount transferAccount = null;
		if (withTransferAccount)
			transferAccount = new Fp3XmlBankAccount();
		generateCrossBookings(accountList, daysRebooking, transferAccount);
	}

	public void generateCrossBookings(Collection<Fp3XmlBankAccount> accountList, boolean withTransferAccount, int daysRebooking,
			BiPredicate<Fp3XmlBankAccount, Fp3XmlBankAccount> missingRecipientRoute) {
		Fp3XmlBankAccount transferAccount = null;
		if (withTransferAccount)
			transferAccount = new Fp3XmlBankAccount();
		generateCrossBookings(accountList, daysRebooking, transferAccount, missingRecipientRoute);
	}

}
