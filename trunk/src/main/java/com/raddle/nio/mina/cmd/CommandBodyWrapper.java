/**
 * 
 */
package com.raddle.nio.mina.cmd;

import java.io.Serializable;

/**
 * @author xurong
 * 
 */
public class CommandBodyWrapper implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String id;
	private boolean request;
	private boolean exception = false;
	private boolean requireResponse = false;

	public CommandBodyWrapper(String id, boolean request, Object body) {
		this.id = id;
		this.request = request;
		this.body = body;
	}

	public boolean isRequest() {
		return request;
	}

	public void setRequest(boolean request) {
		this.request = request;
	}

	private Object body;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Object getBody() {
		return body;
	}

	public void setBody(Object body) {
		this.body = body;
	}
	

	public boolean isException() {
		return exception;
	}

	public void setException(boolean exception) {
		this.exception = exception;
	}

	public boolean isRequireResponse() {
		return requireResponse;
	}

	public void setRequireResponse(boolean requireResponse) {
		this.requireResponse = requireResponse;
	}

}
