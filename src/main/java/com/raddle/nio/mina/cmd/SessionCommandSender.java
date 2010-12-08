/**
 * 
 */
package com.raddle.nio.mina.cmd;

import java.util.UUID;

import org.apache.mina.core.session.IoSession;
import org.omg.CORBA.BooleanHolder;

import com.raddle.nio.mina.cmd.api.CommandCallback;
import com.raddle.nio.mina.cmd.api.CommandSender;
import com.raddle.nio.mina.exception.ExceptionWrapper;
import com.raddle.nio.mina.exception.ReceivedClientException;
import com.raddle.nio.mina.exception.WaitingTimeoutException;

/**
 * @author xurong
 * 
 */
public class SessionCommandSender implements CommandSender {
	private IoSession session;

	public SessionCommandSender(IoSession session) {
		this.session = session;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <C, R> R sendSyncCommand(C command, int timeoutSeconds) {
		final ObjectHolder result = new ObjectHolder();
		final BooleanHolder responsed = new BooleanHolder(false);
		final ObjectHolder exception = new ObjectHolder();
		sendCommand(command, timeoutSeconds, new CommandCallback<C, R>(){

			@Override
			public void commandResponse(C command, R response) {
				result.value = response;
				responsed.value = true;
				synchronized (result) {
					result.notify();
				}
			}

			@Override
			public void responseException(C command, String type, String message) {
				exception.value = new ReceivedClientException(type, message);
				synchronized (result) {
					result.notify();
				}
			}

			@Override
			public void responseTimeout(C command) {
				synchronized (result) {
					result.notify();
				}
			}
			
		});
		// 等待结果返回
		synchronized (result) {
			try {
				// 多等1.5秒，responseTimeout应该在这超时之前发生
				result.wait(timeoutSeconds * 1000 + 1500);
			} catch (InterruptedException e) {
				throw new WaitingTimeoutException("waiting for result interrupted", e);
			}
		}
		if (responsed.value) {
			return (R) result.value;
		} else if (exception.value != null) {
			throw (ReceivedClientException) exception.value;
		} else {
			throw new WaitingTimeoutException("waiting for result timeout after " + timeoutSeconds + "s");
		}
	}
	
	@Override
	public <C, R> void sendCommand(C command, int timeoutSeconds, CommandCallback<C, R> callback) {
		CommandBodyWrapper wrapper = new CommandBodyWrapper(UUID.randomUUID().toString(), true, command);
		wrapper.setRequireResponse(true);
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

	class ObjectHolder {
		private Object value;

		public Object getValue() {
			return value;
		}

		public void setValue(Object value) {
			this.value = value;
		}
	}
}
