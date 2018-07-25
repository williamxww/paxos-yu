package com.bow.paxos.packet;

/**
 * @author vv
 * @since 2018/7/22.
 */
public class AcceptRequest {

	/**
	 * node id
	 */
	private int id;
	/**
	 * 状态实例号
	 */
	private int instance;
	/**
	 * 提案号
	 */
	private int ballot;
	/**
	 * 提案
	 */
	private Value value;

	public AcceptRequest(int id, int instance, int ballot, Value value) {
		this.id = id;
		this.instance = instance;
		this.ballot = ballot;
		this.value = value;
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

	public int getBallot() {
		return ballot;
	}

	public void setBallot(int ballot) {
		this.ballot = ballot;
	}

	public Value getValue() {
		return value;
	}

	public void setValue(Value value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return "AcceptRequest{" +
				"id=" + id +
				", instance=" + instance +
				", ballot=" + ballot +
				", value=" + value +
				'}';
	}
}
