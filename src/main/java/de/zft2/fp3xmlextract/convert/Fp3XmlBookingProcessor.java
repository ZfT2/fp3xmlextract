package de.zft2.fp3xmlextract.convert;

import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.zft2.core.dto.Account;
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
		bookingForTransfer.setCrossAccountIBAN(crossBookingToTransfer.getCrossAccountIBAN());
		bookingForTransfer.setCrossAccountBIC(crossBookingToTransfer.getCrossAccountBIC());
		bookingForTransfer.setTyp(crossBookingToTransfer.getTyp());
		bookingForTransfer.setCrossAccountNamePP(crossBookingToTransfer.getCrossAccountNamePP());

		Fp3XmlBooking crossBookingForTransfer = new Fp3XmlBooking(crossBookingToTransfer);
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

	public void generateCrossBookings(Collection<Fp3XmlBankAccount> accountList, boolean withTransferAccount, int daysRebooking) {
		Fp3XmlBankAccount transferAccount = null;
		if (withTransferAccount)
			transferAccount = new Fp3XmlBankAccount();
		generateCrossBookings(accountList, daysRebooking, transferAccount);
	}

}
