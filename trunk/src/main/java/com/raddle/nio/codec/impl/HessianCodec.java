/**
 * 
 */
package com.raddle.nio.codec.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import com.caucho.hessian.io.SerializerFactory;
import com.raddle.nio.codec.NioCodec;
import com.raddle.nio.codec.NioCodecChain;
import com.raddle.nio.codec.NioCodecContext;
import com.raddle.nio.codec.exception.DecodingException;
import com.raddle.nio.codec.exception.EncodingException;

/**
 * @author xurong
 * 
 */
public class HessianCodec implements NioCodec {
	private final byte type = 10;
	private static final SerializerFactory serializerFactory = new SerializerFactory();

	@Override
	public Object encode(NioCodecContext context, NioCodecChain chain, Object preEncoded) throws Exception {
		if (preEncoded == null) {
			// 不需要编码
			return chain.nextEncode(context, preEncoded);
		}
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		Hessian2Output out = new Hessian2Output(bos);
		out.setSerializerFactory(serializerFactory);
		try {
			out.writeObject(preEncoded);
			out.close();
		} catch (IOException e) {
			throw new EncodingException(e.getMessage(), e);
		}
		ByteBuffer ioBuffer = ByteBuffer.allocate(bos.size() + 5);
		ioBuffer.put(type);// 编码类型
		ioBuffer.putInt(bos.size());// body长度
		ioBuffer.put(bos.toByteArray());
		ioBuffer.flip();
		return chain.nextEncode(context, ioBuffer);
	}

	@Override
	public Object decode(NioCodecContext context, NioCodecChain chain, Object preDecoded, ByteBuffer remainBytes) throws Exception {
		if (!remainBytes.hasRemaining()) {
			// 不需要解码
			return chain.nextDecode(context, preDecoded, remainBytes);
		}
		int cpos = remainBytes.position();
		byte eType = remainBytes.get(cpos);
		if (eType != type) {
			// 不支持的编码格式
			return chain.nextDecode(context, preDecoded, remainBytes);
		}
		// 开始处理解码
		eType = remainBytes.get();
		int size = remainBytes.getInt();
		if (remainBytes.remaining() < size) {
			throw new DecodingException("解码数据不够，需要[" + size + "]字节,只有[" + remainBytes.remaining() + "]字节");
		}
		byte[] data = new byte[size];
		remainBytes.get(data);
		ByteArrayInputStream is = new ByteArrayInputStream(data);
		Hessian2Input in = new Hessian2Input(is);
		in.setSerializerFactory(serializerFactory);
		Object obj = null;
		try {
			obj = in.readObject();
			in.close();
		} catch (IOException e) {
			throw new DecodingException(e.getMessage(), e);
		}
		return chain.nextDecode(context, obj, remainBytes);
	}

}
