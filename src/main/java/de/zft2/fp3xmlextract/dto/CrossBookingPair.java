package de.zft2.fp3xmlextract.dto;

import de.zft2.fp3xmlextract.data.Fp3XmlBooking;

public class CrossBookingPair {

	private String baseKontoNamePP;
	private Fp3XmlBooking baseBooking;

	private String crossKontoNamePP;
	private Fp3XmlBooking crossBooking;

	public CrossBookingPair(String baseKontoNamePP, String crossKontoNamePP) {
		this.baseKontoNamePP = baseKontoNamePP;
		this.crossKontoNamePP = crossKontoNamePP;
	}

	public Fp3XmlBooking getBaseBooking() {
		return baseBooking;
	}

	public Fp3XmlBooking getCrossBooking() {
		return crossBooking;
	}

	public void setBaseBooking(Fp3XmlBooking baseBooking) {
		this.baseBooking = baseBooking;
	}

	public void setCrossBooking(Fp3XmlBooking crossBooking) {
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
