/**
 * 
 */
package com.raddle.nio.mina.cmd.api;

import com.raddle.nio.mina.exception.ReceivedClientException;
import com.raddle.nio.mina.exception.WaitingTimeoutException;

/**
 * @author xurong
 * 
 */
public interface CommandSender {
	/**
	 * 发送命令, 同步等待返回结果
	 * @param <C>
	 * @param <R>
	 * @param command
	 * @param timeoutSeconds
	 * @return
	 * @throws WaitingTimeoutException 等待返回超时
	 * @throws ReceivedClientException 客户端返回的结果为异常对象
	 */
	public <C, R> R sendSyncCommand(C command, int timeoutSeconds) throws WaitingTimeoutException, ReceivedClientException;

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
