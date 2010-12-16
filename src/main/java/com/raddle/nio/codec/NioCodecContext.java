/**
 * 
 */
package com.raddle.nio.codec;

/**
 * @author xurong
 * 
 */
public interface NioCodecContext {
	public Object getAttribute(String key);

	public void setAttribute(String key, Object value);
}