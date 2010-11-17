/**
 * 
 */
package com.raddle.nio.mina.binary;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.file.FileRegion;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

/**
 * @author xurong
 * 
 */
public abstract class AbstractBinaryEncoder implements ProtocolEncoder {

	@Override
	public void dispose(IoSession session) throws Exception {

	}

	@Override
	public void encode(IoSession session, Object message, ProtocolEncoderOutput out) throws Exception {
        // Bypass the encoding if the message is contained in a IoBuffer,
        // as it has already been encoded before
		if ((message instanceof IoBuffer) || (message instanceof FileRegion)) {
            return;
        }
		BinaryEncodedResult result = encodedObject(message);
		IoBuffer buf = IoBuffer.allocate(15);
		buf.putShort(Short.MAX_VALUE);// 起始標誌
		buf.putShort((short) 1);// 版本
		buf.putShort((short) 15);// head長度=起始標誌(2)+版本(2)+head(2)+編碼規則(1)+body長度(8)
		buf.put(result.getEncodedType());// 編碼規則
		buf.putLong(result.getEncodedBytes());// body長度
		buf.flip();
		synchronized (session) {
			// 防止併發，保證header和body連續寫入
			out.write(buf);
			if (result.getEncodedBytes() > 0) {
				out.write(result.getEncodedObject());
			}
		}
	}

	abstract protected BinaryEncodedResult encodedObject(Object message);
}
