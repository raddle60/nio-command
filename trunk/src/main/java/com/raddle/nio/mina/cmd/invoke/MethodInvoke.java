package com.raddle.nio.mina.cmd.invoke;

public class MethodInvoke {
	private Object target;
	private String method;
	private Object[] args;

	public MethodInvoke() {
		
	}
	
	public MethodInvoke(Object target, String method, Object[] args) {
		this.target = target;
		this.method = method;
		this.args = args;
	}

	public Object getTarget() {
		return target;
	}

	public void setTarget(Object target) {
		this.target = target;
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

	public void setArgs(Object[] args) {
		this.args = args;
	}
}