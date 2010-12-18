package com.raddle.nio.mina.cmd.handler;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.raddle.concurrent.MultiQueueExecutor;
import com.raddle.nio.mina.cmd.CommandBodyWrapper;
import com.raddle.nio.mina.cmd.CommandContext;
import com.raddle.nio.mina.cmd.ResponseWaiting;
import com.raddle.nio.mina.exception.ExceptionWrapper;

public abstract class AbstractCommandHandler extends IoHandlerAdapter {
	protected final Logger logger = LoggerFactory.getLogger(this.getClass());
	protected int maxExecuteThreads = 10;
	protected int maxQueueThreads = 10;
	protected ThreadPoolExecutor executorService = null;
	protected MultiQueueExecutor queueExecutor = null;
	@Override
	public void messageReceived(final IoSession session, final Object message) throws Exception {
		if (message != null && message instanceof CommandBodyWrapper) {
			try {
				CommandContext.setIoSession(session);
				final CommandBodyWrapper wrapper = (CommandBodyWrapper) message;
				if(executorService == null){
					executorService = new ThreadPoolExecutor(0, maxExecuteThreads, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>() , new CommandHandlerThreadFactory());
				}
				if(queueExecutor == null){
					queueExecutor = new MultiQueueExecutor(maxQueueThreads, new CommandHandlerThreadFactory());
				}
				String queueName = getExecuteQueue(wrapper.getBody());
				if(queueName == null){
					executorService.execute(new Runnable() {
						@Override
						public void run() {
							processMessage(session, wrapper);
						}
					});
				} else {
					queueExecutor.executeInQueue(queueName ,new Runnable() {
						@Override
						public void run() {
							processMessage(session, wrapper);
						}
					});
				}
			} finally {
				CommandContext.clear();
			}
		}
	}

	private void processMessage(final IoSession session, final CommandBodyWrapper wrapper) {
		try {
			CommandContext.setIoSession(session);
			Object body = wrapper.getBody();
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

	/**
	 * 获得执行队列，返回null将并发执行。每个队列是个独立线程，队列中的任务按顺序同步执行
	 * @param command
	 * @return  null不再队列中执行，将并发执行。非null，在指定的队列中执行
	 */
	protected abstract String getExecuteQueue(Object command);
	
	/**
	 * 处理命令
	 * 
	 * @param command
	 * @return 处理结果,null不发送数据给client
	 */
	protected abstract Object processCommand(Object command) throws Exception;
	
    static class CommandHandlerThreadFactory implements ThreadFactory {
        final ThreadGroup group;
        final static AtomicInteger threadNumber = new AtomicInteger(1);
        final String namePrefix;

        CommandHandlerThreadFactory() {
            SecurityManager s = System.getSecurityManager();
            group = (s != null)? s.getThreadGroup() :
                                 Thread.currentThread().getThreadGroup();
            namePrefix = "cmdhandler-thread-";
        }

        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r,
                                  namePrefix + threadNumber.getAndIncrement(),
                                  0);
            t.setDaemon(false);
            if (t.getPriority() != Thread.NORM_PRIORITY)
                t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }
    }

	public int getMaxExecuteThreads() {
		return maxExecuteThreads;
	}

	public void setMaxExecuteThreads(int maxTaskThreads) {
		this.maxExecuteThreads = maxTaskThreads;
	}
	
	public void dispose(){
		if(executorService != null){
			executorService.shutdown();
		}
		if(queueExecutor != null){
			queueExecutor.shutdown();
		}
	}

	public int getMaxQueueThreads() {
		return maxQueueThreads;
	}

	public void setMaxQueueThreads(int maxQueueThreads) {
		this.maxQueueThreads = maxQueueThreads;
	}

	public ThreadPoolExecutor getExecutorService() {
		return executorService;
	}

	public MultiQueueExecutor getQueueExecutor() {
		return queueExecutor;
	}

}
