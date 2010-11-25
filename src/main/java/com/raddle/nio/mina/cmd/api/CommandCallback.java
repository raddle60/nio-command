/**
 * 
 */
package com.raddle.nio.mina.cmd.api;

/**
 * @author xurong
 * 
 */
public interface CommandCallback<C, R> {
	/**
	 * 命令响应
	 * 
	 * @param command
	 * @param response
	 */
	public void commandResponse(C command, R response);
	
	/**
	 * 命令响应异常
	 * @param command
	 * @param type
	 * @param message
	 */
	public void responseException(C command, String type, String message);

	/**
	 * 命令响应超时
	 * 
	 * @param command
	 * @param response
	 */
	public void responseTimeout(C command);
}
