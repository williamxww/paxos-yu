package com.bow.paxos.common;

/**
 * @author vv
 * @since 2018/7/22.
 */
public class PaxosException extends RuntimeException {

	public PaxosException(String msg) {
		super(msg);
	}

	public PaxosException(Throwable cause) {
		super(cause);
	}

	public PaxosException(String message, Throwable cause) {
		super(message, cause);
	}
}
