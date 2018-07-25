package com.bow.paxos.model;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bow.paxos.common.Client;
import com.bow.paxos.common.ObjectSerialize;
import com.bow.paxos.common.SerializeUtil;
import com.bow.paxos.packet.AcceptRequest;
import com.bow.paxos.packet.AcceptResponse;
import com.bow.paxos.packet.Packet;
import com.bow.paxos.packet.PacketBean;
import com.bow.paxos.packet.PrepareRequest;
import com.bow.paxos.packet.PrepareResponse;
import com.bow.paxos.packet.Value;

/**
 * 选举一共分为两阶段：<br/>
 * 1.prepare阶段,Proposer发出prepare请求(只有提案号),acceptor promise 只接收提案号大的请求<br/>
 * 2.accept阶段,Proposer基于第一阶段认可的提案号，给acceptor发出提案；
 *
 * @author vv
 * @since 2018/7/22.
 */
public class Proposer {
	private static final Logger LOGGER = LoggerFactory.getLogger(Proposer.class);
	/**
	 * proposer的id
	 */
	private int id;

	private Node localNode;

	private List<Node> acceptorNodes;

	private BlockingQueue<PacketBean> msgQueue = new LinkedBlockingQueue<>();

	private Map<Integer, Instance> instanceState = new HashMap<>();

	private Client client;

	private Timer timer = new Timer("retry");
	/**
	 * 每阶段的超时时间
	 */
	private int timeout = 3000;

	public Proposer(Client client, Node localNode, List<Node> acceptors) {
		this.client = client;
		this.localNode = localNode;
		this.acceptorNodes = acceptors;
		this.id = localNode.getId();

		new Thread(() -> {
			while (true) {
				try {
					PacketBean msg = this.msgQueue.take();
					receivePacket(msg);
				} catch (Exception e) {
					LOGGER.error("", e);
				}
			}
		}).start();
	}

    /**
     * server收到请求后放置于队列中
     *
     * @param bean
     * @throws InterruptedException
     */
    public void receive(PacketBean bean) throws InterruptedException {
        this.msgQueue.put(bean);
    }

	/**
	 * 处理接收到的packetbean
	 *
	 * @param bean
	 * @throws InterruptedException
	 */
	private void receivePacket(PacketBean bean) throws InterruptedException {
		switch (bean.getType()) {
		case "PrepareResponsePacket":
			PrepareResponse prepareResponse = (PrepareResponse) bean.getData();
			onPrepareResponse(prepareResponse);
			break;
		case "AcceptResponsePacket":
			AcceptResponse acceptResponse = (AcceptResponse) bean.getData();
			onAcceptResponse(acceptResponse);
			break;
		default:
			throw new IllegalArgumentException("Unknown type");
		}
	}

	public void prepare() {
	    // 构建第一张选票
		PrepareRequest request = new PrepareRequest(id, 1, 1);
        prepare(request);
	}

	/**
	 * 将prepare请求发送给所有的accepter<br/>
	 * 设置超时, 如果超时，则判断阶段1是否完成.如果未完成，则ballot加一之后继续执行阶段一.
	 *
	 */
	public void prepare(PrepareRequest request) {
		Instance instance = new Instance(1, 0, null, false, State.READY,new HashSet<>());
		this.instanceState.put(request.getInstance(), instance);
	    // 进入PREPARE阶段
		this.instanceState.get(request.getInstance()).state = State.PREPARE;
		try {
			PacketBean bean = new PacketBean("PreparePacket", request);
			byte[] msg = SerializeUtil.marshal(new Packet(bean, WorkerType.ACCEPTER));
			// 发送给每个acceptors
			this.acceptorNodes.forEach((node) -> {
				try {
					this.client.sendTo(node.getHost(), node.getPort(), msg);
                    LOGGER.trace("Send " + request + " to " + node);
				} catch (IOException e) {
					LOGGER.trace("Fail sending " + request + " to " + node);
				}
			});
		} catch (IOException e) {
			LOGGER.error("Proposer#prepare ", e);
		}
//		setTimeout(new TimerTask() {
//			@Override
//			public void run() {
//				// 超时后，增加提案号，重试
//				Instance current = instanceState.get(request.getInstance());
//				if (current.state == State.PREPARE) {
//					current.ballot++;
//                    PrepareRequest retry = new PrepareRequest(id, request.getInstance(), current.ballot);
//					prepare(retry);
//				}
//			}
//		});
	}

	/**
	 * 接收到accepter对于prepare的回复
	 *
	 * @param response
	 * @throws InterruptedException e
	 */
	public void onPrepareResponse(PrepareResponse response) {
		Instance current = this.instanceState.get(response.getInstance());
		if (current.state != State.PREPARE) {
			return;
		}
		if (response.isOk()) {
			current.promiseNodes.add(response.getId());
			if (response.getAb() > current.valueBallot && response.getAv() != null) {
				// 返回了已被给出响应的acceptor接受的提案号和提案
				current.valueBallot = response.getAb();
				current.value = response.getAv();
				current.isSucc = false;
			}
			// 接受者超过半数，则提交提案
			if (current.promiseNodes.size() >= this.acceptorNodes.size() / 2 + 1) {
				// 若当前还没有任何提案
				if (current.value == null) {
					// todo 将自己的id做为提案
					Value object = null;
					current.value = object;
					current.isSucc = true;
				}

				// 将提案发出去
				AcceptRequest acceptRequest = new AcceptRequest(id, response.getInstance(), current.ballot,
						current.value);
				accept(acceptRequest);
			}
		}
	}

	/**
	 * 第二阶段(accept阶段)，发出真正提案；
	 *
	 * @param request 提案请求
	 */
	private void accept(AcceptRequest request) {
		this.instanceState.get(request.getInstance()).state = State.ACCEPT;
		try {
			PacketBean bean = new PacketBean("AcceptPacket", request);
			byte[] msg = SerializeUtil.marshal(new Packet(bean, WorkerType.ACCEPTER));
			this.acceptorNodes.forEach((info) -> {
				try {
					this.client.sendTo(info.getHost(), info.getPort(), msg);
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		setTimeout(new TimerTask() {
			@Override
			public void run() {
				Instance current = instanceState.get(request.getInstance());
				if (current.state == State.ACCEPT) {
					current.ballot++;
					// 增加提案号，然后重入第一阶段
                    PrepareRequest retry = new PrepareRequest(id, request.getInstance(), current.ballot);
					prepare(retry);
				}
			}
		});
	}

	/**
	 * 接收到accepter返回的accept响应
	 *
	 * @param response
	 * @throws InterruptedException
	 */
	public void onAcceptResponse(AcceptResponse response) throws InterruptedException {
		Instance current = this.instanceState.get(response.getInstance());
		if (current.state != State.ACCEPT)
			return;
		if (response.isOk()) {
			current.promiseNodes.add(response.getId());
			// 半数接受提案则流程结束
			if (current.promiseNodes.size() >= this.acceptorNodes.size() / 2 + 1) {
				done(response.getInstance());
				// if (localNode.isSucc) {
				// this.isLastSumbitSucc = true;
				// this.hasSummitQueue.put(this.readyToSubmitQueue.take());
				// } else {
				// // 说明这个instance的id已经被占有
				// this.isLastSumbitSucc = false;
				// beforPrepare();
				// }
			}
		}
	}

	/**
	 * 本次paxos选举结束
	 */
	public void done(int instance) {
		this.instanceState.get(instance).state = State.FINISH;
	}

	/**
	 * set timeout task
	 *
	 * @param task
	 */
	private void setTimeout(TimerTask task) {
		timer.schedule(task, this.timeout);
	}

	enum State {
		/**
		 * 投票前的状态
		 */
		READY,
		/**
		 * 第一阶段
		 */
		PREPARE,
		/**
		 * 第二阶段
		 */
		ACCEPT,
		/**
		 * 流程结束
		 */
		FINISH
	}

	class Instance {
        /**
         * 提案号
         */
		private int ballot;

		/**
		 * 已接收的提案号
		 */
		private int valueBallot;
		/**
		 * 已接收的提案
		 */
		private Value value;
		/**
		 * 是否是自己的提案获胜
		 */
		private boolean isSucc;
		/**
		 * 提案进入到的状态阶段
		 */
		private State state;
		/**
		 * 做出承诺的节点ID(prepare阶段，acceptor需要做出承诺不再接受比此提案号小的提案)
		 */
		private Set<Integer> promiseNodes;

        public Instance(int ballot, int valueBallot, Value value, boolean isSucc, State state, Set<Integer> promiseNodes) {
            this.ballot = ballot;
            this.valueBallot = valueBallot;
            this.value = value;
            this.isSucc = isSucc;
            this.state = state;
            this.promiseNodes = promiseNodes;
        }
    }
}
