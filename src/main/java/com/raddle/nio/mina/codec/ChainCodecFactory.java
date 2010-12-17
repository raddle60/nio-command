/**
 * 
 */
package com.raddle.nio.mina.codec;

import java.lang.reflect.Array;
import java.nio.ByteBuffer;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

import com.raddle.nio.codec.NioCodec;
import com.raddle.nio.codec.NioCodecContext;
import com.raddle.nio.codec.impl.NioCodecChainImpl;
import com.raddle.nio.codec.impl.NioCodecContextImpl;
import com.raddle.nio.mina.binary.AbstractBinaryDecoder;
import com.raddle.nio.mina.binary.AbstractBinaryEncoder;
import com.raddle.nio.mina.binary.BinaryEncodedResult;

/**
 * 链式解码工厂
 * 
 * @author xurong
 * 
 */
public class ChainCodecFactory implements ProtocolCodecFactory {
	private NioCodecChainImpl chain = new NioCodecChainImpl();
	private ProtocolEncoder encoder = new AbstractBinaryEncoder() {
		@Override
		protected BinaryEncodedResult encodedObject(Object message) throws Exception {
			NioCodecContext context = new NioCodecContextImpl();
			Object encoded = chain.encode(context, message);
			BinaryEncodedResult result = new BinaryEncodedResult();
			result.setEncodedType((byte) 0);
			if (encoded == null) {
				result.setEncodedObject(null);
				result.setEncodedBytes(0);
			} else {
				if (encoded instanceof byte[]) {
					result.setEncodedBytes(Array.getLength(encoded));
					result.setEncodedObject(IoBuffer.wrap((byte[]) encoded));
				} else if (encoded instanceof ByteBuffer) {
					IoBuffer buffer = IoBuffer.wrap((ByteBuffer) encoded);
					result.setEncodedBytes(buffer.remaining());
					result.setEncodedObject(buffer);
				} else if (encoded instanceof IoBuffer) {
					result.setEncodedBytes(((IoBuffer) encoded).remaining());
					result.setEncodedObject(encoded);
				} else {
					throw new RuntimeException("unsupported result type [" + encoded.getClass() + "]");
				}
			}
			return result;
		}
	};

	private ProtocolDecoder decoder = new AbstractBinaryDecoder() {
		@Override
		protected Object decodeBody(byte encodeType, IoBuffer ioBuffer) throws Exception {
			NioCodecContext context = new NioCodecContextImpl();
			return chain.decode(context, null, ioBuffer.buf());
		}
	};

	@Override
	public ProtocolEncoder getEncoder(IoSession session) throws Exception {
		return encoder;
	}

	@Override
	public ProtocolDecoder getDecoder(IoSession session) throws Exception {
		return decoder;
	}

	public void addFirst(NioCodec codec) {
		chain.addFirst(codec);
	}

	public void addLast(NioCodec codec) {
		chain.addLast(codec);
	}
}
