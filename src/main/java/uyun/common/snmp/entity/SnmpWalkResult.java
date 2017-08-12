package uyun.common.snmp.entity;

/**
 * SnmpWalk采集结果
 */
public class SnmpWalkResult<T> {
	private State state;
	private T data;

	public SnmpWalkResult(T data) {
		this(State.OK, data);
	}

	public SnmpWalkResult(State state, T data) {
		this.state = state;
		this.data = data;
	}

	/**
	 * 采集状态
	 * @return
	 */
	public State getState() {
		return state;
	}

	/**
	 * 采集到的结果
	 * @return
	 */
	public T getData() {
		return data;
	}

	/**
	 * 采集状态
	 */
	public enum State {
		/**
		 * 采集正常完成
		 */
		OK,
		/**
		 * 采集超时
		 */
		OVERTIME,
		/**
		 * walk时的数量达到了最大值
		 */
		EXCEED;
	}
}
