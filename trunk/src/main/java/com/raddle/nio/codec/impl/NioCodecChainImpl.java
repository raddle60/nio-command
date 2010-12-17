/**
 * 
 */
package com.raddle.nio.codec.impl;

import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.LinkedList;

import com.raddle.nio.codec.NioCodec;
import com.raddle.nio.codec.NioCodecChain;
import com.raddle.nio.codec.NioCodecContext;

/**
 * @author xurong
 * 
 */
public class NioCodecChainImpl implements NioCodecChain {
	private LinkedList<NioCodec> codecChain = new LinkedList<NioCodec>();
	private Iterator<NioCodec> encoderIterator = null;
	private Iterator<NioCodec> decoderIterator = null;

	public void addFirst(NioCodec codec) {
		codecChain.addFirst(codec);
	}

	public void addLast(NioCodec codec) {
		codecChain.addLast(codec);
	}

	@Override
	public boolean hasNextEncoder() {
		return encoderIterator.hasNext();
	}

	@Override
	public boolean hasNextDecoder() {
		return decoderIterator.hasNext();
	}

	@Override
	public Object nextEncode(NioCodecContext context, Object preEncoded) throws Exception {
		if (encoderIterator.hasNext()) {
			return encoderIterator.next().encode(context, this, preEncoded);
		}
		return null;
	}

	@Override
	public Object nextDecode(NioCodecContext context, Object preDecoded, ByteBuffer remainBytes) throws Exception {
		if (decoderIterator.hasNext()) {
			return decoderIterator.next().decode(context, this, preDecoded, remainBytes);
		}
		return null;
	}

	public Object encode(NioCodecContext context, Object preEncoded) throws Exception {
		encoderIterator = codecChain.iterator();
		return nextEncode(context, preEncoded);
	}

	public Object decode(NioCodecContext context, Object preDecoded, ByteBuffer remainBytes) throws Exception {
		decoderIterator = codecChain.descendingIterator();
		return nextDecode(context, preDecoded, remainBytes);
	}
	
}
