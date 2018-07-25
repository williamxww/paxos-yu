package com.bow.paxos.common;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bow.paxos.model.Acceptor;
import com.bow.paxos.model.Proposer;
import com.bow.paxos.packet.Packet;

/**
 * @author vv
 * @since 2018/7/22.
 */
public class SimpleServer implements Server {

	private static final Logger LOGGER = LoggerFactory.getLogger(SimpleServer.class);

	private ServerSocket server;
	private int port;
	private Proposer proposer;
	private Acceptor acceptor;
	private ExecutorService pool = Executors.newCachedThreadPool();

	public SimpleServer(int port, Proposer proposer, Acceptor acceptor) {
		this.port = port;
		this.proposer = proposer;
		this.acceptor = acceptor;
		try {
			server = new ServerSocket(this.port, 128);
			LOGGER.info("Listen on port " + this.port);
		} catch (IOException e) {
			throw new PaxosException(e);
		}
	}

	@Override
	public void start() {

		// 主线程负责接收请求并转给工作线程
		new Thread(() -> {
			while (true) {
				try {
					Socket socket = server.accept();
					// 收到请求后交给工作线程处理
					pool.execute(new WorkThread(socket));
				} catch (Exception e) {
					throw new PaxosException(e);
				}
			}
		}, "server-main").start();


	}

	class WorkThread implements Runnable {

		private Socket socket;

		public WorkThread(Socket socket) {
			this.socket = socket;
		}

		@Override
		public void run() {
			try {
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				InputStream inputStream = this.socket.getInputStream();
				byte[] buf = new byte[4096];
				int n;
				while ((n = inputStream.read(buf)) >= 0) {
					stream.write(buf, 0, n);
				}

				byte[] data = stream.toByteArray();
				// this.queue.put(stream.toByteArray());
				Packet packet = SerializeUtil.unmarshal(data, Packet.class);
				LOGGER.info("Receive a packet "+packet.getPacketBean());
				switch (packet.getWorkerType()) {
				case ACCEPTER:
					acceptor.receive(packet.getPacketBean());
					break;
				case PROPOSER:
					proposer.receive(packet.getPacketBean());
					break;
				default:
					break;
				}
				inputStream.close();
				this.socket.close();
			} catch (IOException | InterruptedException | ClassNotFoundException e) {
				throw new PaxosException(e);
			}
		}
	}
}
