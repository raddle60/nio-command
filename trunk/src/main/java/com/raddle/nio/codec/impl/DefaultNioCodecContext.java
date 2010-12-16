/**
 * 
 */
package com.raddle.nio.codec.impl;

import java.util.HashMap;
import java.util.Map;

import com.raddle.nio.codec.NioCodecContext;

/**
 * @author xurong
 *
 */
public class DefaultNioCodecContext implements NioCodecContext {
	private Map<String, Object> attributes = new HashMap<String, Object>();

	@Override
	public Object getAttribute(String key) {
		return attributes.get(key);
	}

	@Override
	public void setAttribute(String key, Object value) {
		attributes.put(key, value);
	}
	
}
