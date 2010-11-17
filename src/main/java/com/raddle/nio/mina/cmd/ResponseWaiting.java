/**
 * 
 */
package com.raddle.nio.mina.cmd;

import java.util.HashMap;
import java.util.Map;

import com.raddle.nio.mina.cmd.api.CommandCallback;

/**
 * @author xurong
 * 
 */
public class ResponseWaiting {
	private static Map<String, WaitingItem> waitingMap = new HashMap<String, WaitingItem>();

	/**
	 * 加入等待列表
	 * 
	 * @param commandId
	 * @param command
	 * @param timeoutSeconds
	 * @param callback
	 */
	@SuppressWarnings("unchecked")
	public static void addWaiting(String commandId, Object command, int timeoutSeconds, CommandCallback callback) {
		if (waitingMap.containsKey(commandId)) {
			throw new IllegalArgumentException("命令[" + commandId + "]已存在");
		}
		waitingMap.put(commandId, new WaitingItem(commandId, command, timeoutSeconds, System.currentTimeMillis() + (timeoutSeconds * 1000), callback));
	}

	/**
	 * 收到了响应
	 * 
	 * @param commandId
	 * @param response
	 */
	@SuppressWarnings("unchecked")
	public static void responseReceived(String commandId, Object response) {
		if (waitingMap.containsKey(commandId)) {
			WaitingItem item = waitingMap.get(commandId);
			waitingMap.remove(commandId);
			item.getCallback().commandResponse(item.getCommand(), response);
		}
	}

	@SuppressWarnings("unchecked")
	static class WaitingItem {
		private String commandId;
		private Object command;
		private CommandCallback callback;
		private int timeoutSeconds;
		// 过期时间，毫秒数
		private long expireTime;

		public WaitingItem(String commandId, Object command, int timeoutSeconds, long expireTime, CommandCallback callback) {
			this.commandId = commandId;
			this.command = command;
			this.timeoutSeconds = timeoutSeconds;
			this.expireTime = expireTime;
			this.callback = callback;
		}

		public int getTimeoutSeconds() {
			return timeoutSeconds;
		}

		public void setTimeoutSeconds(int timeoutSeconds) {
			this.timeoutSeconds = timeoutSeconds;
		}

		public long getExpireTime() {
			return expireTime;
		}

		public void setExpireTime(long expireTime) {
			this.expireTime = expireTime;
		}

		public String getCommandId() {
			return commandId;
		}

		public void setCommandId(String commandId) {
			this.commandId = commandId;
		}

		public Object getCommand() {
			return command;
		}

		public void setCommand(Object command) {
			this.command = command;
		}

		public CommandCallback getCallback() {
			return callback;
		}

		public void setCallback(CommandCallback callback) {
			this.callback = callback;
		}
	}
}
