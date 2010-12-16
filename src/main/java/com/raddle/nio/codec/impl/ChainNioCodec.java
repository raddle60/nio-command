/**
 * 
 */
package com.raddle.nio.codec.impl;

import java.nio.ByteBuffer;
import java.util.LinkedList;

import com.raddle.nio.codec.NioCodec;
import com.raddle.nio.codec.NioCodecContext;

/**
 * @author xurong
 * 
 */
public class ChainNioCodec implements NioCodec {
	private LinkedList<NioCodec> codecChain = new LinkedList<NioCodec>();

	public void addFirst(NioCodec codec) {
		codecChain.addFirst(codec);
	}

	public void addLast(NioCodec codec) {
		codecChain.addLast(codec);
	}

	@Override
	public Object encode(NioCodecContext context, Object preEncoded) throws Exception {
		Object result = preEncoded;
		for (NioCodec codec : codecChain) {
			result = codec.encode(context, result);
		}
		return result;
	}

	@Override
	public Object decode(NioCodecContext context, Object preDecoded, ByteBuffer remainBytes) throws Exception {
		Object result = preDecoded;
		for (NioCodec codec : codecChain) {
			result = codec.decode(context, result, remainBytes);
		}
		return result;
	}
}
