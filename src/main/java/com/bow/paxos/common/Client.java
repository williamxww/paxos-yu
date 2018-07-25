package com.bow.paxos.common;

import java.io.IOException;

/**
 * @author vv
 * @since 2018/7/22.
 */
public interface Client {

	/**
	 * 给指定地址发送信息
	 * 
	 * @param ip
	 * @param port
	 * @param msg
	 * @throws IOException e
	 */
	void sendTo(String ip, int port, byte[] msg) throws IOException;
}
