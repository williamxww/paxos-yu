package com.bow.paxos.common;

import java.io.IOException;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author vv
 * @since 2018/7/22.
 */
public class SimpleClient implements Client {

	private static final Logger LOGGER = LoggerFactory.getLogger(SimpleClient.class);

	@Override
	public void sendTo(String ip, int port, byte[] msg) throws IOException {
		Socket socket = new Socket(ip, port);
		// socket.setSoTimeout(3000);
		socket.getOutputStream().write(msg);
		socket.getOutputStream().close();
		socket.close();
	}
}
