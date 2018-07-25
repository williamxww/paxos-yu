package com.bow.paxos.model;

/**
 * 节点信息
 * 
 * @author vv
 * @since 2018/7/22.
 */
public class Node {

	private int id;
	private String host;
	private int port;

	public Node(int id, String host, int port) {
		this.id = id;
		this.host = host;
		this.port = port;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	@Override
	public String toString() {
		return "Node{" + "id=" + id + ", host='" + host + '\'' + ", port=" + port + '}';
	}
}
