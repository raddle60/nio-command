/**
 * 
 */
package com.raddle.concurrent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author xurong
 * 
 */
public abstract class CatchedRunable implements Runnable {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public void run() {
		try {
			runInCatch();
		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
		}
	}

	public abstract void runInCatch() throws Exception;

}
