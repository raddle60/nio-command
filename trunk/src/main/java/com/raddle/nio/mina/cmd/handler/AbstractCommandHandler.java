package com.raddle.nio.mina.cmd.handler;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

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
	protected int maxTaskThreads = 10;
	protected ExecutorService executorService = null;
	protected Map<String, ExecutorService> queueMap = new HashMap<String, ExecutorService>();

	@Override
	public void messageReceived(final IoSession session, final Object message) throws Exception {
		if (message != null && message instanceof CommandBodyWrapper) {
			final CommandBodyWrapper wrapper = (CommandBodyWrapper) message;
			if(executorService == null){
				executorService = new ThreadPoolExecutor(0, maxTaskThreads, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>() , new DaemonThreadFactory());
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
				ExecutorService queueExecutor = queueMap.get(queueName);
				if(queueExecutor == null){
					queueExecutor = Executors.newSingleThreadExecutor(new DaemonThreadFactory());
					queueMap.put(queueName, queueExecutor);
				}
				queueExecutor.execute(new Runnable() {
					@Override
					public void run() {
						processMessage(session, wrapper);
					}
				});
			}
		}
	}

	private void processMessage(final IoSession session, final CommandBodyWrapper wrapper) {
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
	
    static class DaemonThreadFactory implements ThreadFactory {
        static final AtomicInteger poolNumber = new AtomicInteger(1);
        final ThreadGroup group;
        final AtomicInteger threadNumber = new AtomicInteger(1);
        final String namePrefix;

        DaemonThreadFactory() {
            SecurityManager s = System.getSecurityManager();
            group = (s != null)? s.getThreadGroup() :
                                 Thread.currentThread().getThreadGroup();
            namePrefix = "daemon-pool-" +
                          poolNumber.getAndIncrement() +
                         "-thread-";
        }

        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r,
                                  namePrefix + threadNumber.getAndIncrement(),
                                  0);
            t.setDaemon(true);
            if (t.getPriority() != Thread.NORM_PRIORITY)
                t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }
    }

	public int getMaxTaskThreads() {
		return maxTaskThreads;
	}

	public void setMaxTaskThreads(int maxTaskThreads) {
		this.maxTaskThreads = maxTaskThreads;
	}

}
