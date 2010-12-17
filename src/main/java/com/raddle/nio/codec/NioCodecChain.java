/**
 * 
 */
package com.raddle.nio.codec;

import java.nio.ByteBuffer;

/**
 * @author xurong
 * 
 */
public interface NioCodecChain {
	/**
	 * 是否有下一个编码器
	 * 
	 * @return
	 */
	public boolean hasNextEncoder();

	/**
	 * 是否有下一个解码器
	 * 
	 * @return
	 */
	public boolean hasNextDecoder();

	/**
	 * 编码对象
	 * 
	 * @param context
	 * @param preEncoded 需要编码的对象
	 * @return 返回对象
	 * @throws Exception
	 */
	public Object nextEncode(NioCodecContext context, Object preEncoded) throws Exception;

	/**
	 * 解码对象
	 * 
	 * @param context
	 * @param preDecoded 前一个解码器解码以后的对象，可能为null
	 * @param remainBytes 剩余的未解码的对象
	 * @return 返回对象
	 * @throws Exception
	 */
	public Object nextDecode(NioCodecContext context, Object preDecoded, ByteBuffer remainBytes) throws Exception;
}
