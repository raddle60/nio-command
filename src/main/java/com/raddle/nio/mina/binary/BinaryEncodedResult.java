/**
 * 
 */
package com.raddle.nio.mina.binary;

/**
 * @author xurong
 * 
 */
public class BinaryEncodedResult {
	private long encodedBytes;
	private byte encodedType;
	private Object encodedObject;

	public long getEncodedBytes() {
		return encodedBytes;
	}

	public void setEncodedBytes(long encodedBytes) {
		this.encodedBytes = encodedBytes;
	}

	public byte getEncodedType() {
		return encodedType;
	}

	public void setEncodedType(byte encodedType) {
		this.encodedType = encodedType;
	}

	public Object getEncodedObject() {
		return encodedObject;
	}

	public void setEncodedObject(Object encodedObject) {
		this.encodedObject = encodedObject;
	}
}