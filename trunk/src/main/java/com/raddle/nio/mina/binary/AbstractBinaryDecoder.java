package com.raddle.nio.mina.binary;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.AttributeKey;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

public abstract class AbstractBinaryDecoder extends CumulativeProtocolDecoder {
	private final AttributeKey PROCESS = new AttributeKey(getClass(), "PROCESS");

	@Override
	protected boolean doDecode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws Exception {
		ProcessInfo info = getProcessInfo(session);
		if (info.isProcessHeader()) {
			processRemainHeaders(session, in, info, out);
		} else if (info.isProcessBody()) {
			return processBody(session, in, info, out);
		} else {
			watingForHeader(session, in, out, info);
		}
		return false;
	}

	private void watingForHeader(IoSession session, IoBuffer in, ProtocolDecoderOutput out, ProcessInfo info) throws Exception {
		if (in.remaining() >= 6) {
			// 標誌
			short flag = in.getShort();
			if (flag == Short.MAX_VALUE) {
				// 版本
				short version = in.getShort();
				if (version == 1) {
					// 頭長度
					short headerBytes = in.getShort();
					// 設置處理信息
					info.setProcessHeader(true);
					info.setProcessBody(false);
					info.setWaitingHeaderBytes((short) (headerBytes - 6));
					// 處理剩下的頭部信息
					processRemainHeaders(session, in, info, out);
				} else {
					clearProcessInfo(session);
					session.close(true);
					throw new IllegalStateException("不支持的协议版本，只支持版本[" + 1 + "]的解码，当前数据版本为[" + version + "]");
				}
			} else {
				clearProcessInfo(session);
				session.close(true);
				throw new IllegalStateException("协议起始标志位不正确，应该以[" + Short.MAX_VALUE + "]开头，但是读取到[" + flag + "]");
			}
		}
	}

	private void processRemainHeaders(IoSession session, IoBuffer in, ProcessInfo info ,ProtocolDecoderOutput out) throws Exception  {
		if (in.remaining() >= info.getWaitingHeaderBytes()) {
			byte encodeType = in.get();
			int bodyBytes = in.getInt();
			info.setEncodeType(encodeType);
			info.setWaitingBodyBytes(bodyBytes);
			info.setProcessHeader(false);
			info.setProcessBody(true);
			processBody(session, in, info, out);
		}
	}

	private boolean processBody(IoSession session, IoBuffer in, ProcessInfo info ,ProtocolDecoderOutput out) throws Exception {
		if (in.remaining() >= info.getWaitingBodyBytes()) {
			try {
				if(info.getWaitingBodyBytes() == 0){
					return true;
				} else {
					// 處理body
					IoBuffer buffer = IoBuffer.allocate((int) info.getWaitingBodyBytes());
					in.get(buffer.array());
					buffer.limit(info.getWaitingBodyBytes());
					Object body = decodeBody(info.getEncodeType(), buffer);
					out.write(body);
				}
			} finally {
				clearProcessInfo(session);
			}
			return true;
		}
		return false;
	}

	protected abstract Object decodeBody(byte encodeType, IoBuffer ioBuffer) throws Exception ;

	public ProcessInfo getProcessInfo(IoSession session) {
		if (session.getAttribute(PROCESS) != null) {
			return (ProcessInfo) session.getAttribute(PROCESS);
		} else {
			ProcessInfo p = new ProcessInfo();
			session.setAttribute(PROCESS, p);
			return p;
		}
	}

	public void clearProcessInfo(IoSession session) {
		session.removeAttribute(PROCESS);
	}

	private class ProcessInfo {
		private boolean processHeader;
		private boolean processBody;
		private short waitingHeaderBytes;
		private int waitingBodyBytes;
		private byte encodeType;

		public byte getEncodeType() {
			return encodeType;
		}

		public void setEncodeType(byte encodeType) {
			this.encodeType = encodeType;
		}

		public boolean isProcessHeader() {
			return processHeader;
		}

		public void setProcessHeader(boolean processHeader) {
			this.processHeader = processHeader;
		}

		public boolean isProcessBody() {
			return processBody;
		}

		public void setProcessBody(boolean processBody) {
			this.processBody = processBody;
		}

		public short getWaitingHeaderBytes() {
			return waitingHeaderBytes;
		}

		public void setWaitingHeaderBytes(short waitingHeaderBytes) {
			this.waitingHeaderBytes = waitingHeaderBytes;
		}

		public int getWaitingBodyBytes() {
			return waitingBodyBytes;
		}

		public void setWaitingBodyBytes(int waitingBodyBytes) {
			this.waitingBodyBytes = waitingBodyBytes;
		}
	}

}
