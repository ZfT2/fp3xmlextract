package de.fp3xmlextract.dto;

import de.fp3xmlextract.data.Booking;

public class CrossBookingPair {

	private String baseKontoNamePP;
	private Booking baseBooking;

	private String crossKontoNamePP;
	private Booking crossBooking;

	public CrossBookingPair(String baseKontoNamePP, String crossKontoNamePP) {
		this.baseKontoNamePP = baseKontoNamePP;
		this.crossKontoNamePP = crossKontoNamePP;
	}

	public Booking getBaseBooking() {
		return baseBooking;
	}

	public Booking getCrossBooking() {
		return crossBooking;
	}

	public void setBaseBooking(Booking baseBooking) {
		this.baseBooking = baseBooking;
	}

	public void setCrossBooking(Booking crossBooking) {
		this.crossBooking = crossBooking;
	}

	public String getBaseKontoNamePP() {
		return baseKontoNamePP;
	}

	public void setBaseKontoNamePP(String baseKontoNamePP) {
		this.baseKontoNamePP = baseKontoNamePP;
	}

	public String getCrossKontoNamePP() {
		return crossKontoNamePP;
	}

	public void setCrossKontoNamePP(String crossKontoNamePP) {
		this.crossKontoNamePP = crossKontoNamePP;
	}

}
