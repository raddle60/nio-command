/**
 * 
 */
package com.raddle.nio.mina.exception;

/**
 * @author xurong
 *
 */
public class DecodingException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	public DecodingException() {
	}

	/**
	 * @param message
	 */
	public DecodingException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public DecodingException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public DecodingException(String message, Throwable cause) {
		super(message, cause);
	}

}
