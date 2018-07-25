package com.bow.paxos.packet;

/**
 * @author vv
 * @since 2018/7/22.
 */
public class AcceptResponse {

	/**
	 * node id
	 */
	private int id;
	/**
	 * 状态实例号
	 */
	private int instance;
	/**
	 * 是否正确响应
	 */
	private boolean ok;

	public AcceptResponse(int id, int instance, boolean ok) {
		this.id = id;
		this.instance = instance;
		this.ok = ok;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getInstance() {
		return instance;
	}

	public void setInstance(int instance) {
		this.instance = instance;
	}

	public boolean isOk() {
		return ok;
	}

	public void setOk(boolean ok) {
		this.ok = ok;
	}
}
