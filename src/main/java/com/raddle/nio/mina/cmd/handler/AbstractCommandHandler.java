package com.raddle.nio.mina.cmd.handler;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.raddle.nio.mina.cmd.CommandBodyWrapper;
import com.raddle.nio.mina.cmd.CommandContext;
import com.raddle.nio.mina.cmd.ResponseWaiting;
import com.raddle.nio.mina.exception.ExceptionWrapper;

public abstract class AbstractCommandHandler extends IoHandlerAdapter {
	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public void messageReceived(IoSession session, Object message) throws Exception {
		if (message != null && message instanceof CommandBodyWrapper) {
			CommandBodyWrapper wrapper = (CommandBodyWrapper) message;
			Object body = wrapper.getBody();
			try {
				CommandContext.setIoSession(session);
				if (wrapper.isRequest()) {
					try {
						Object result = processCommand(body);
						if (result != null) {
							CommandContext.getCommandSender().sendResponse(wrapper.getId(), result);
						} else if (wrapper.isRequireResponse()) {
							CommandContext.getCommandSender().sendResponse(wrapper.getId(), null);
						}
					} catch (Exception e) {
						logger.error(e.getMessage(), e);
						CommandContext.getCommandSender().sendExceptionResponse(wrapper.getId(), e);
					}
				} else {
					if (wrapper.isException()) {
						ResponseWaiting.exceptionReceived(wrapper.getId(), (ExceptionWrapper) body);
					} else {
						ResponseWaiting.responseReceived(wrapper.getId(), body);
					}
				}
			} finally {
				CommandContext.clear();
			}
		}
	}

	/**
	 * 处理命令
	 * 
	 * @param command
	 * @return 处理结果,null不发送数据给client
	 */
	protected abstract Object processCommand(Object command) throws Exception;

}
