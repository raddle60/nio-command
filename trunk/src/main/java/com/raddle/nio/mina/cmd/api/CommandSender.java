/**
 * 
 */
package com.raddle.nio.mina.cmd.api;

/**
 * @author xurong
 * 
 */
public interface CommandSender {
	/**
	 * 发送命令
	 * 
	 * @param <C>
	 * @param <R>
	 * @param command
	 * @param callback
	 */
	public <C, R> void sendCommand(C command, CommandCallback<C, R> callback);

	/**
	 * 发送命令
	 * 
	 * @param <C>
	 * @param <R>
	 * @param command
	 * @param callback
	 */
	public <C, R> void sendCommand(C command, int timeoutSeconds, CommandCallback<C, R> callback);

	/**
	 * 发送命令
	 * 
	 * @param command
	 * @param callback
	 */
	public void sendCommand(Object command);

	/**
	 * 发送响应
	 * 
	 * @param commandId
	 * @param response
	 */
	public void sendResponse(String commandId, Object response);
	
	/**
	 * 发送异常响应
	 * 
	 * @param commandId
	 * @param exception
	 */
	public void sendExceptionResponse(String commandId, Exception exception);
}
