package com.raddle.nio.codec;

import java.nio.ByteBuffer;

public interface NioCodec {
	/**
	 * 编码对象
	 * 
	 * @param context
	 * @param preEncoded 需要编码的对象
	 * @return 编码后的对象
	 * @throws Exception
	 */
	public Object encode(NioCodecContext context, Object preEncoded) throws Exception;

	/**
	 * 解码对象
	 * 
	 * @param context
	 * @param preDecoded 前一个解码器解码以后的对象，可能为null
	 * @param remainBytes 剩余的未解码的对象
	 * @return 解码后的对象
	 * @throws Exception
	 */
	public Object decode(NioCodecContext context, Object preDecoded, ByteBuffer remainBytes) throws Exception;
}