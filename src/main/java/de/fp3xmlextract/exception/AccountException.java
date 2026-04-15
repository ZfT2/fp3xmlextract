package de.fp3xmlextract.exception;

public class AccountException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 9055823553571465695L;

	final String message;

	public AccountException(String message) {
		super(message);
		this.message = message;
	}

	@Override
	public String getMessage() {
		return message;
	}
}
