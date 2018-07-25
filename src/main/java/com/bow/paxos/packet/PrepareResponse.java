package com.bow.paxos.packet;

import java.io.Serializable;

/**
 * @see PrepareRequest prepare阶段的proposer发送的请求
 * @see PrepareResponse prepare阶段的acceptor给出的响应
 * @author vv
 * @since 2018/7/22.
 */
public class PrepareResponse implements Serializable {

	/**
	 * acceptor的节点id
	 */
	private int id;
	/**
	 * 针对proposer的哪个状态实例做出的响应
	 */
	private int instance;
	/**
	 *
	 */
	private boolean ok;
	/**
	 * 已接收的票号(accept ballot)
	 */
	private int ab;
	/**
	 * 已接收到提案值(accept value)
	 */
	private Value av;

    public PrepareResponse(int id, int instance, boolean ok, int ab, Value av) {
        this.id = id;
        this.instance = instance;
        this.ok = ok;
        this.ab = ab;
        this.av = av;
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

    public int getAb() {
        return ab;
    }

    public void setAb(int ab) {
        this.ab = ab;
    }

    public Value getAv() {
        return av;
    }

    public void setAv(Value av) {
        this.av = av;
    }

    @Override
    public String toString() {
        return "PrepareResponse{" +
                "id=" + id +
                ", instance=" + instance +
                ", ok=" + ok +
                ", ab=" + ab +
                ", av=" + av +
                '}';
    }
}
