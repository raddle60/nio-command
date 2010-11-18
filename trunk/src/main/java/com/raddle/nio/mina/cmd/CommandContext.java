/**
 * 
 */
package com.raddle.nio.mina.cmd;

import org.apache.mina.core.session.IoSession;

import com.raddle.nio.mina.cmd.api.CommandSender;

/**
 * @author xurong
 * 
 */
public class CommandContext {
	private static ThreadLocal<CommandSender> commandSender = new ThreadLocal<CommandSender>();
	private static ThreadLocal<IoSession> ioSession = new ThreadLocal<IoSession>();

	public static CommandSender getCommandSender() {
		if (commandSender.get() == null && ioSession.get() != null) {
			commandSender.set(new SessionCommandSender(ioSession.get()));
		}
		return commandSender.get();
	}

	public static IoSession getIoSession() {
		return ioSession.get();
	}

	public static void setIoSession(IoSession ioSession) {
		CommandContext.ioSession.set(ioSession);
	}

	public static void clear() {
		CommandContext.ioSession.set(null);
		CommandContext.commandSender.set(null);
	}
}
