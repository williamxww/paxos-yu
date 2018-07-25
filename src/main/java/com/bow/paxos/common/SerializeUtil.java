package com.bow.paxos.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * @author vv
 * @since 2018/7/22.
 */
public class SerializeUtil {

	public static byte[] marshal(Object object) throws IOException {
		byte[] bytes = null;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(bos);
		oos.writeObject(object);
		oos.flush();
		bytes = bos.toByteArray();
		oos.close();
		bos.close();
		return bytes;
	}

	public static <T> T unmarshal(byte[] byteArray, Class<T> type) throws ClassNotFoundException, IOException {
		T obj = null;
		ByteArrayInputStream bis = new ByteArrayInputStream(byteArray);
		ObjectInputStream ois = new ObjectInputStream(bis);
		obj = (T) ois.readObject();
		ois.close();
		bis.close();
		return obj;
	}

}
