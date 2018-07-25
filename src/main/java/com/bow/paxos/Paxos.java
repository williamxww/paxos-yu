package com.bow.paxos;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bow.paxos.common.Client;
import com.bow.paxos.common.PaxosException;
import com.bow.paxos.common.Server;
import com.bow.paxos.common.SimpleClient;
import com.bow.paxos.common.SimpleServer;
import com.bow.paxos.model.Acceptor;
import com.bow.paxos.model.Node;
import com.bow.paxos.model.Proposer;

/**
 * @author vv
 * @since 2018/7/22.
 */
public class Paxos {

	private static final Logger LOGGER = LoggerFactory.getLogger(Paxos.class);

	private Proposer proposer;

	private Acceptor acceptor;

	private Server server;

	private Client client;

	private Node localNode;

	private List<Node> cluster;

	public Paxos(int myid, List<Node> cluster) {
		for (Node node : cluster) {
			if (node.getId() == myid) {
				this.localNode = node;
			}
		}
		if (this.localNode == null) {
			throw new PaxosException("Can't find localNode");
		}
		this.cluster = cluster;
	}

	public void start() throws Exception {
		client = new SimpleClient();
		proposer = new Proposer(client, localNode, cluster);
		acceptor = new Acceptor(client, cluster);
		server = new SimpleServer(localNode.getPort(), proposer, acceptor);
		server.start();
		// proposer发出提案
		proposer.prepare();
	}

	public static void main(String[] args) throws Exception {
		List<Node> cluster = new ArrayList<>();
		Node n1 = new Node(1, "127.0.0.1", 9001);
		Node n2 = new Node(2, "127.0.0.1", 9002);
		Node n3 = new Node(3, "127.0.0.1", 9003);
		cluster.add(n1);
		cluster.add(n2);
		cluster.add(n3);
		Paxos paxos1 = new Paxos(1, cluster);
		Paxos paxos2 = new Paxos(2, cluster);
		paxos1.start();
		paxos2.start();
	}
}
