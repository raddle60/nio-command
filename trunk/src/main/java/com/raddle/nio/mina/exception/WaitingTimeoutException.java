/**
 * 
 */
package com.raddle.nio.mina.exception;

/**
 * @author xurong
 * 
 */
public class WaitingTimeoutException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public WaitingTimeoutException() {
		super();
	}

	public WaitingTimeoutException(String message, Throwable cause) {
		super(message, cause);
	}

	public WaitingTimeoutException(String message) {
		super(message);
	}

	public WaitingTimeoutException(Throwable cause) {
		super(cause);
	}

}
