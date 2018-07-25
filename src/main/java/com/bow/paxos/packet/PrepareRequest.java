package com.bow.paxos.packet;

import java.io.Serializable;

/**
 * @see PrepareRequest prepare阶段的proposer发送的请求
 * @see PrepareResponse prepare阶段的acceptor给出的响应
 * @author vv
 * @since 2018/7/22.
 */
public class PrepareRequest implements Serializable {
	/**
	 * acceptor id
	 */
	private int peerId;
	/**
	 * proposer状态实例的实例号
	 */
	private int instance;
	/**
	 * 提案号
	 */
	private int ballot;

	public PrepareRequest(int peerId, int instance, int ballot) {
		this.peerId = peerId;
		this.instance = instance;
		this.ballot = ballot;
	}

	public int getPeerId() {
		return peerId;
	}

	public void setPeerId(int peerId) {
		this.peerId = peerId;
	}

	public int getInstance() {
		return instance;
	}

	public void setInstance(int instance) {
		this.instance = instance;
	}

	public int getBallot() {
		return ballot;
	}

	public void setBallot(int ballot) {
		this.ballot = ballot;
	}

	@Override
	public String toString() {
		return "PrepareRequest{" +
				"peerId=" + peerId +
				", instance=" + instance +
				", ballot=" + ballot +
				'}';
	}
}
