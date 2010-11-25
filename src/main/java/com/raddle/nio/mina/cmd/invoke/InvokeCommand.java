/**
 * 
 */
package com.raddle.nio.mina.cmd.invoke;

import java.io.Serializable;

/**
 * @author xurong
 * 
 */
public class InvokeCommand implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private String targetId;
	private String method;
	private Object[] args;
	
	public InvokeCommand(){
		
	}
	
	public InvokeCommand(String targetId, String method, Object[] parameters) {
		this.targetId = targetId;
		this.method = method;
		this.args = parameters;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public Object[] getArgs() {
		return args;
	}

	public void setArgs(Object[] parameters) {
		this.args = parameters;
	}

	public String getTargetId() {
		return targetId;
	}

	public void setTargetId(String targetId) {
		this.targetId = targetId;
	}
}
