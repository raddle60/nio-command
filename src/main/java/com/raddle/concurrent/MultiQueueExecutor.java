/**
 * 
 */
package com.raddle.concurrent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 并行管理多个队列，多线程并行运行，并保证每个队列中的任务安放入的顺序执行<br>
 * 每30秒清除一次空队列
 * 
 * @author xurong
 * 
 */
public class MultiQueueExecutor {
	private static final Logger logger = LoggerFactory.getLogger(MultiQueueExecutor.class);
	private int maxThreads = 0;
	private Map<String, QueueInfo> queueMap = new HashMap<String, QueueInfo>();
	private ThreadPoolExecutor poolExecutor = null;
	private ScheduledExecutorService scheduledExecutorService = null;

	public MultiQueueExecutor(int maxThreads) {
		this.maxThreads = maxThreads;
		init(maxThreads, null);
	}

	public MultiQueueExecutor(int maxThreads, ThreadFactory threadFactory) {
		this.maxThreads = maxThreads;
		init(maxThreads, threadFactory);
	}

	private void init(int maxThreads, ThreadFactory threadFactory) {
		if (threadFactory == null) {
			scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
			poolExecutor = new ThreadPoolExecutor(0, maxThreads, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());
		} else {
			scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(threadFactory);
			poolExecutor = new ThreadPoolExecutor(0, maxThreads, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), threadFactory);
		}
		scheduledExecutorService.scheduleWithFixedDelay(new CatchedRunable() {
			@Override
			public void runInCatch() {
				// 定期清除空队列
				synchronized (queueMap) {
					for (Iterator<String> iterator = queueMap.keySet().iterator(); iterator.hasNext();) {
						String key = (String) iterator.next();
						QueueInfo queueInfo = queueMap.get(key);
						if (!queueInfo.isRunning() && queueInfo.getQueue().size() == 0) {
							iterator.remove();
							logger.debug("remove queue [{}]", key);
						}
					}
				}

			}
		}, 0, 30, TimeUnit.SECONDS);
	}

	/**
	 * 在队列中执行
	 * 
	 * @param queueName 队列名称
	 * @param runnable 执行的任务
	 */
	public void executeInQueue(String queueName, Runnable runnable) {
		QueueInfo queueInfo = null;
		synchronized (queueMap) {
			// 判断是否已创建队列
			queueInfo = queueMap.get(queueName);
			if (queueInfo == null) {
				queueInfo = new QueueInfo(queueName);
				queueMap.put(queueName, queueInfo);
				logger.debug("create queue [{}]", queueName);
			}
			queueInfo.getQueue().add(runnable);
			logger.debug("added task into queue [{}]", queueName);
		}
		synchronized (queueInfo) {
			// 判断是否在运行
			if (!queueInfo.isRunning()) {
				logger.debug("run queue [{}] in new thread", queueName);
				queueInfo.setRunning(true);
				final QueueInfo executeInfo = queueInfo;
				poolExecutor.execute(new Runnable() {
					@Override
					public void run() {
						while (true) {
							Runnable task = executeInfo.getQueue().poll();
							while (task != null) {
								try {
									task.run();
								} catch (Throwable e) {
									e.printStackTrace();
								}
								task = executeInfo.getQueue().poll();
							}
							synchronized (executeInfo) {
								if (executeInfo.getQueue().size() == 0) {
									executeInfo.setRunning(false);
									logger.debug("exit thread for queue [{}]", executeInfo.getQueueName());
									break;
								}
							}
						}
					}
				});
			}
		}
	}
	
	/**
	 * 获得当前在poo里的线程
	 * @return
	 */
	public int getPoolSize(){
		return poolExecutor.getPoolSize();
	}

	/**
	 * 获得当前队列个数
	 * @return
	 */
	public int getQueueSize(){
		return queueMap.size();
	}
	
	public void shutdown() {
		poolExecutor.shutdown();
		scheduledExecutorService.shutdown();
	}

	private class QueueInfo {
		private String queueName;
		private Queue<Runnable> queue = new LinkedList<Runnable>();
		private boolean running = false;

		public QueueInfo(String queueName) {
			this.queueName = queueName;
		}

		public Queue<Runnable> getQueue() {
			return queue;
		}

		public boolean isRunning() {
			return running;
		}

		public void setRunning(boolean isRunning) {
			this.running = isRunning;
		}

		public String getQueueName() {
			return queueName;
		}

	}

	public int getMaxThreads() {
		return maxThreads;
	}
}
