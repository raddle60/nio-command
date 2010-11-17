/**
 * 
 */
package com.raddle.nio.mina.exception;

/**
 * @author xurong
 *
 */
public class EncodingException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	public EncodingException() {
	}

	/**
	 * @param message
	 */
	public EncodingException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public EncodingException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public EncodingException(String message, Throwable cause) {
		super(message, cause);
	}

}
