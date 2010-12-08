/**
 * 
 */
package com.raddle.nio.mina.exception;

/**
 * @author xurong
 * 
 */
public class ReceivedClientException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	
	private String type;

	/**
	 * @param message
	 */
	public ReceivedClientException(String type, String message) {
		super(message);
		this.type = type;
	}

	/**
	 * @param message
	 * @param cause
	 */
	public ReceivedClientException(String type, String message, Throwable cause) {
		super(message, cause);
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

}
