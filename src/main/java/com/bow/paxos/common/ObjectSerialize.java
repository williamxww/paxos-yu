package com.bow.paxos.common;

import java.io.IOException;

/**
 * @author vv
 * @since 2018/7/22.
 */
public interface ObjectSerialize {

	public byte[] objectToObjectArray(Object object) throws IOException;

	public <T> T byteArrayToObject(byte[] byteArray, Class<T> type) throws ClassNotFoundException, IOException;
}
