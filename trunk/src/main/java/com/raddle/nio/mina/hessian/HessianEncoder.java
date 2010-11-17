/**
 * 
 */
package com.raddle.nio.mina.hessian;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.mina.core.buffer.IoBuffer;

import com.caucho.hessian.io.Hessian2Output;
import com.caucho.hessian.io.SerializerFactory;
import com.raddle.nio.mina.binary.AbstractBinaryEncoder;
import com.raddle.nio.mina.binary.BinaryEncodedResult;
import com.raddle.nio.mina.exception.EncodingException;

/**
 * @author xurong
 * 
 */
public class HessianEncoder extends AbstractBinaryEncoder {
	private final byte type = 10;
	private static final SerializerFactory serializerFactory = new SerializerFactory();

	@Override
	protected BinaryEncodedResult encodedObject(Object message) {
		BinaryEncodedResult result = new BinaryEncodedResult();
		result.setEncodedType(type);
		if (message == null) {
			result.setEncodedBytes(0);
			result.setEncodedObject(null);
		} else {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			Hessian2Output out = new Hessian2Output(bos);
			out.setSerializerFactory(serializerFactory);
			try {
				out.writeObject(message);
				out.close();
			} catch (IOException e) {
				throw new EncodingException(e.getMessage(), e);
			}
			result.setEncodedBytes(bos.size());
			IoBuffer ioBuffer = IoBuffer.allocate(bos.size());
			ioBuffer.put(bos.toByteArray());
			ioBuffer.flip();
			result.setEncodedObject(ioBuffer);
		}
		return result;
	}

}
