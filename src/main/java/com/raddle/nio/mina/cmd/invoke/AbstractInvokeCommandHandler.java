/**
 * 
 */
package com.raddle.nio.mina.cmd.invoke;

import java.util.HashMap;
import java.util.Map;

import com.raddle.nio.mina.cmd.handler.AbstractCommandHandler;

/**
 * @author xurong
 * 
 */
public abstract class AbstractInvokeCommandHandler extends AbstractCommandHandler {
	private Map<String, Object> objectMap = new HashMap<String, Object>();

	@Override
	protected Object processCommand(Object command) throws Exception {
		if (command != null && command instanceof InvokeCommand) {
			InvokeCommand invokeCommand = (InvokeCommand) command;
			Object target = getObject(invokeCommand.getTargetId());
			if (target == null) {
				throw new IllegalArgumentException("没有id为[" + invokeCommand.getTargetId() + "]的对象");
			}
			return invokeMethod(new MethodInvoke(invokeCommand.getTargetId(), target, invokeCommand.getMethod(), invokeCommand.getArgs()));
		}
		return null;
	}

	public void putObject(String id, Object target) {
		objectMap.put(id, target);
	}
	
	@Override
	protected String getExecuteQueue(Object command) {
		if (command != null && command instanceof InvokeCommand) {
			InvokeCommand invokeCommand = (InvokeCommand) command;
			Object target = getObject(invokeCommand.getTargetId());
			return getCommandQueue(new MethodInvoke(invokeCommand.getTargetId(), target, invokeCommand.getMethod(), invokeCommand.getArgs()));
		}
		return null;
	}
	
	protected Object getObject(String id) {
		return objectMap.get(id);
	}

	abstract protected Object invokeMethod(MethodInvoke methodInvoke) throws Exception;
	
	/**
	 * 获得执行队列，返回null将并发执行。每个队列是个独立线程，队列中的任务按顺序同步执行
	 * @param methodInvoke
	 * @return  null不再队列中执行，将并发执行。非null，在指定的队列中执行
	 */
	abstract protected String getCommandQueue(MethodInvoke methodInvoke);

}
