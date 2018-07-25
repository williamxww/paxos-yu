package com.bow.paxos.model;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
 * @author vv
 * @since 2018/7/22.
 */
public class Acceptor {

	private static final Logger LOGGER = LoggerFactory.getLogger(Acceptor.class);

    // accepter's id
    private int id;

	// accepter's state, contain each instances
	private Map<Integer, Instance> instanceState = new HashMap<>();
	// accepted value
	private Map<Integer, Value> acceptedValue = new HashMap<>();

	private Client client;

	private List<Node> proposerNodes;

	private BlockingQueue<PacketBean> msgQueue = new LinkedBlockingQueue<>();

	public Acceptor(Client client, List<Node> proposerNodes) {
		this.client = client;
		this.proposerNodes = proposerNodes;
		new Thread(() -> {
			while (true) {
				try {
					PacketBean msg = msgQueue.take();
					receivePacket(msg);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	/**
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
	 * @throws IOException
	 */
	private void receivePacket(PacketBean bean) throws IOException {
		switch (bean.getType()) {
		case "PreparePacket":
			PrepareRequest prepareRequest = (PrepareRequest) bean.getData();
			onPrepare(prepareRequest.getPeerId(), prepareRequest.getInstance(), prepareRequest.getBallot());
			break;
		case "AcceptPacket":
			AcceptRequest acceptRequest = (AcceptRequest) bean.getData();
			onAccept(acceptRequest);
			break;
		default:
			throw new IllegalArgumentException("Unknown type");
		}
	}

	/**
	 * handle prepare from proposer
	 *
	 * @param instance current instance
	 * @param ballot prepare ballot
	 * @throws IOException
	 * @throws UnknownHostException
	 */
    private void onPrepare(int peerId, int instance, int ballot) throws IOException {
		if (!instanceState.containsKey(instance)) {
			instanceState.put(instance, new Instance(ballot, null, 0));
			// 持久化到磁盘
			// instancePersistence();
			// 第一次接收到prepare请求，给出的响应
			PrepareResponse prepareResponse = new PrepareResponse(id, instance, true, 0, null);
			prepareResponse(peerId, prepareResponse);
		} else {
			Instance current = instanceState.get(instance);
			if (ballot > current.ballot) {
				// 接受新提案号
				current.ballot = ballot;
				// 持久化到磁盘
				// instancePersistence();
				PrepareResponse prepareResponse = new PrepareResponse(id, instance, true, current.acceptedBallot,
						current.value);
				prepareResponse(peerId, prepareResponse);
			} else {
				// 提案号小了，直接返回不通过
				PrepareResponse prepareResponse = new PrepareResponse(id, instance, false, current.ballot, null);
				prepareResponse(peerId, prepareResponse);
			}
		}
	}

	/**
	 * 将响应发送出去
	 * 
	 * @throws IOException
	 * @throws UnknownHostException
	 */
	private void prepareResponse(int peerId, PrepareResponse prepareResponse) throws UnknownHostException, IOException {
		PacketBean bean = new PacketBean("PrepareResponsePacket", prepareResponse);
		Node peer = getSpecInfoObect(peerId);
		this.client.sendTo(peer.getHost(), peer.getPort(),
				SerializeUtil.marshal(new Packet(bean, WorkerType.PROPOSER)));
	}

	/**
	 * handle accept from proposer
	 *
	 * @throws IOException
	 * @throws UnknownHostException
	 */
	private void onAccept(AcceptRequest request) throws IOException {
		LOGGER.info(request.toString());
		if (!this.instanceState.containsKey(request.getInstance())) {
			// 竟然没有找到对应的状态，说明没有经过prepare阶段
			AcceptResponse acceptResponse = new AcceptResponse(id, request.getInstance(), false);
			acceptResponse(request.getId(), acceptResponse);
		} else {
			Instance current = this.instanceState.get(request.getInstance());
			if (request.getBallot() == current.ballot) {
				current.acceptedBallot = request.getBallot();
				current.value = request.getValue();
				// 成功
				LOGGER.info("[onaccept success]");
				this.acceptedValue.put(request.getInstance(), request.getValue());
				if (!this.instanceState.containsKey(request.getInstance() + 1)) {
					// multi-paxos 中的优化，省去了连续成功后的prepare阶段
					this.instanceState.put(request.getInstance() + 1, new Instance(1, null, 0));
				}
				// 保存最后一次成功的instance的位置，用于proposer直接从这里开始执行
				// this.lastInstanceId = instance;
				// // 持久化到磁盘
				// instancePersistence();
				AcceptResponse acceptResponse = new AcceptResponse(id, request.getInstance(), true);
				acceptResponse(request.getId(), acceptResponse);
			} else {
				AcceptResponse acceptResponse = new AcceptResponse(id, request.getInstance(), false);
				acceptResponse(request.getId(), acceptResponse);
			}
		}
		LOGGER.info("[onaccept end]");
	}

	private void acceptResponse(int peerId, AcceptResponse acceptResponse) throws IOException {
		Node proposerNode = getSpecInfoObect(peerId);
		PacketBean bean = new PacketBean("AcceptResponsePacket", acceptResponse);
		this.client.sendTo(proposerNode.getHost(), proposerNode.getPort(),
				SerializeUtil.marshal(new Packet(bean, WorkerType.PROPOSER)));
	}


	/**
	 * 获取特定的info
	 *
	 * @param key
	 * @return
	 */
	private Node getSpecInfoObect(int key) {
		for (Node each : this.proposerNodes) {
			if (key == each.getId()) {
				return each;
			}
		}
		return null;
	}

	static class Instance {
		/**
		 * prepare阶段新接收的提案号
		 */
		private int ballot;
		/**
		 * 已接收提案的提案号
		 */
		private int acceptedBallot;
		/**
		 * 已接收的提案内容
		 */
		private Value value;

		public Instance(int ballot, Value value, int acceptedBallot) {
			this.ballot = ballot;
			this.acceptedBallot = acceptedBallot;
			this.value = value;
		}
	}
}
