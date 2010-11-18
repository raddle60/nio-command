/**
 * 
 */
package com.raddle.nio.mina.cmd;

import com.raddle.nio.mina.cmd.api.CommandSender;

/**
 * @author xurong
 * 
 */
public class CommandContext {
	private static ThreadLocal<CommandSender> commandSender = new ThreadLocal<CommandSender>();

	public static CommandSender getCommandSender() {
		return commandSender.get();
	}

	public static void setCommandSender(CommandSender commandSender) {
		CommandContext.commandSender.set(commandSender);
	}
}
