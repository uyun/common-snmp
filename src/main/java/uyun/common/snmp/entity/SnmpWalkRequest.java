package uyun.common.snmp.entity;

/**
 * SnmpWalk采集请求
 */
public class SnmpWalkRequest {
	public static final SnmpWalkRequest DEFAULT = new SnmpWalkRequest();

	private int maxLength;
	private int timeout;

	public SnmpWalkRequest() {
		this(0, 0);
	}

	public SnmpWalkRequest(int maxLength, int timeout) {
		this.maxLength = maxLength;
		this.timeout = timeout;
	}

	/**
	 * walk时返回的结果最大数量，如果<=0则表示无限制
	 * @return
	 */
	public int getMaxLength() {
		return maxLength;
	}

	/**
	 * walk时的整体耗时，单位ms，如果<=0则表示不限制
	 * @return
	 */
	public int getTimeout() {
		return timeout;
	}
}
