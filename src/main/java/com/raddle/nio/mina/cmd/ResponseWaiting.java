/**
 * 
 */
package com.raddle.nio.mina.cmd;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.LoggerFactory;

import com.raddle.nio.mina.cmd.api.CommandCallback;

/**
 * @author xurong
 * 
 */
public class ResponseWaiting {
	private static Map<String, WaitingItem> waitingMap = new HashMap<String, WaitingItem>();
	private static ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor(new DaemonThreadFactory());

	static {
		scheduledExecutor.scheduleWithFixedDelay(new Runnable() {
			@Override
			@SuppressWarnings("unchecked")
			public void run() {
				try {
					// 检查超时
					List<String> keys = new LinkedList<String>(waitingMap.keySet());
					for (String key : keys) {
						WaitingItem item = waitingMap.get(key);
						if (item.getExpireTime() < System.currentTimeMillis()) {
							waitingMap.remove(key);
							item.getCallback().responseTimeout(item.getCommand());
						}
					}
				} catch (Throwable e) {
					// 一定要捕获异常，出异常后，定时执行将停止
					LoggerFactory.getLogger(ResponseWaiting.class).error(e.getMessage(), e);
				}
			}
		}, 5, 2, TimeUnit.SECONDS);
	}

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

	// modify from Executors > DefaultThreadFactory
	static class DaemonThreadFactory implements ThreadFactory {
		static final AtomicInteger poolNumber = new AtomicInteger(1);
		final ThreadGroup group;
		final AtomicInteger threadNumber = new AtomicInteger(1);
		final String namePrefix;

		DaemonThreadFactory() {
			SecurityManager s = System.getSecurityManager();
			group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
			namePrefix = "daemon-pool-" + poolNumber.getAndIncrement() + "-thread-";
		}

		public Thread newThread(Runnable r) {
			Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
			if (!t.isDaemon())
				t.setDaemon(true);
			if (t.getPriority() != Thread.NORM_PRIORITY)
				t.setPriority(Thread.NORM_PRIORITY);
			return t;
		}
	}
}
