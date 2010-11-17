/**
 * 
 */
package com.raddle.nio.mina.hessian;

import java.io.ByteArrayInputStream;

import org.apache.mina.core.buffer.IoBuffer;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.SerializerFactory;
import com.raddle.nio.mina.binary.AbstractBinaryDecoder;
import com.raddle.nio.mina.exception.DecodingException;

/**
 * @author xurong
 * 
 */
public class HessianDecoder extends AbstractBinaryDecoder {
	private final byte type = 10;
	private static final SerializerFactory serializerFactory = new SerializerFactory();

	@Override
	protected Object decodeBody(byte encodeType, IoBuffer ioBuffer) {
		if (type != encodeType) {
			throw new DecodingException("不支持的编码类型[" + encodeType + "]，支持的编码类型为[" + type + "](hessian)");
		}
		ByteArrayInputStream is = new ByteArrayInputStream(ioBuffer.array());
		Hessian2Input in = new Hessian2Input(is);
		in.setSerializerFactory(serializerFactory);
		Object obj = null;
		try {
			obj = in.readObject();
			in.close();
		} catch (Throwable e) {
			throw new DecodingException(e.getMessage(), e);
		}
		return obj;
	}

}