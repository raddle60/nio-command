package com.raddle.nio.mina.cmd.handler;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;

import com.raddle.nio.mina.cmd.CommandBodyWrapper;
import com.raddle.nio.mina.cmd.CommandContext;
import com.raddle.nio.mina.cmd.ResponseWaiting;
import com.raddle.nio.mina.cmd.SessionCommandSender;

public abstract class AbstractCommandHandler extends IoHandlerAdapter {

	@Override
	public void messageReceived(IoSession session, Object message) throws Exception {
		if (message != null && message instanceof CommandBodyWrapper) {
			CommandBodyWrapper wrapper = (CommandBodyWrapper) message;
			Object body = wrapper.getBody();
			try {
				CommandContext.setCommandSender(new SessionCommandSender(session));
				if (wrapper.isRequest()) {
					Object result = processCommand(body);
					if (result != null) {
						CommandContext.getCommandSender().sendResponse(wrapper.getId(), result);
					}
				} else {
					ResponseWaiting.responseReceived(wrapper.getId(), body);
				}
			} finally {
				CommandContext.setCommandSender(null);
			}
		}
	}

	/**
	 * 处理命令
	 * 
	 * @param command
	 * @return 处理结果,null不发送数据给client
	 */
	protected abstract Object processCommand(Object command);

}
