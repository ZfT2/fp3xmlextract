package de.zft2.fp3xmlextract.convert;

public class ConverterConfig {

	public ConverterConfig(boolean withCancelBookings, boolean removeSepaInfoFromPurpose) {
		this.withCancelBookings = withCancelBookings;
		this.removeSepaFieldsFromPurpose = removeSepaInfoFromPurpose;
	}

	private boolean withCancelBookings = false;
	private boolean removeSepaFieldsFromPurpose = false;

	public boolean isWithCancelBookings() {
		return withCancelBookings;
	}

	public void setWithCancelBookings(boolean withCancelBookings) {
		this.withCancelBookings = withCancelBookings;
	}

	public boolean isRemoveSepaFieldsFromPurpose() {
		return removeSepaFieldsFromPurpose;
	}

	public void setRemoveSepaFieldsFromPurpose(boolean removeSepaFieldsFromPurpose) {
		this.removeSepaFieldsFromPurpose = removeSepaFieldsFromPurpose;
	}
}
