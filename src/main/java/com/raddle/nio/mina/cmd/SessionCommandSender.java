/**
 * 
 */
package com.raddle.nio.mina.cmd;

import java.util.UUID;

import org.apache.mina.core.session.IoSession;

import com.raddle.nio.mina.cmd.api.CommandCallback;
import com.raddle.nio.mina.cmd.api.CommandSender;
import com.raddle.nio.mina.exception.ExceptionWrapper;

/**
 * @author xurong
 * 
 */
public class SessionCommandSender implements CommandSender {
	/**
	 * 默认超时时间60秒
	 */
	private final static int DEFAULT_TIMEOUT = 60;
	private IoSession session;

	public SessionCommandSender(IoSession session) {
		this.session = session;
	}

	@Override
	public <C, R> void sendCommand(C command, CommandCallback<C, R> callback) {
		CommandBodyWrapper wrapper = new CommandBodyWrapper(UUID.randomUUID().toString(), true, command);
		ResponseWaiting.addWaiting(wrapper.getId(), command, DEFAULT_TIMEOUT, callback);
		session.write(wrapper);
	}

	@Override
	public <C, R> void sendCommand(C command, int timeoutSeconds, CommandCallback<C, R> callback) {
		CommandBodyWrapper wrapper = new CommandBodyWrapper(UUID.randomUUID().toString(), true, command);
		ResponseWaiting.addWaiting(wrapper.getId(), command, timeoutSeconds, callback);
		session.write(wrapper);
	}

	@Override
	public void sendCommand(Object command) {
		CommandBodyWrapper wrapper = new CommandBodyWrapper(UUID.randomUUID().toString(), true, command);
		session.write(wrapper);
	}

	@Override
	public void sendResponse(String commandId, Object response) {
		CommandBodyWrapper wrapper = new CommandBodyWrapper(commandId, false, response);
		session.write(wrapper);
	}

	@Override
	public void sendExceptionResponse(String commandId, Exception exception) {
		CommandBodyWrapper wrapper = new CommandBodyWrapper(commandId, false, new ExceptionWrapper(exception.getClass().getName(), exception.getMessage()));
		wrapper.setException(true);
		session.write(wrapper);
	}

}
