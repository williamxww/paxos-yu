package com.bow.paxos.model;

import java.io.Serializable;
/**
 * @author vv
 * @since 2018/7/22.
 */
public enum WorkerType implements Serializable {
	PROPOSER, ACCEPTER, LEARNER, SERVER
}
