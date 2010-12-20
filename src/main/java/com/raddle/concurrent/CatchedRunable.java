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
	private static final Logger logger = LoggerFactory.getLogger(CatchedRunable.class);

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
