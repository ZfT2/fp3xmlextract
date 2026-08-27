package de.zft2.fp3xmlextract.convert;

public class ConverterConfig {

	public ConverterConfig(boolean withEmptyAccounts, boolean withCancelBookings, boolean removeSepaInfoFromPurpose) {
		this.withEmptyAccounts = withEmptyAccounts;
		this.withCancelBookings = withCancelBookings;
		this.removeSepaFieldsFromPurpose = removeSepaInfoFromPurpose;
	}

	private boolean withEmptyAccounts = false;
	private boolean withCancelBookings = false;
	private boolean removeSepaFieldsFromPurpose = false;

	public boolean isWithEmptyAccounts() {
		return withEmptyAccounts;
	}

	public void setWithEmptyAccounts(boolean withEmptyAccounts) {
		this.withEmptyAccounts = withEmptyAccounts;
	}

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
