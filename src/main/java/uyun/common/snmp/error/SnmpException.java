package uyun.common.snmp.error;

/**
 * 包装SNMP API所发出的异常
 *
 * @author Victor
 */
public class SnmpException extends Exception {
	private static final long serialVersionUID = -793146770473323916L;

	/**
	 * 未知错误
	 */
	public static final int ERR_UNKNOWN = 0;
	/**
	 * 操作超时
	 */
	public static final int ERR_TIMEOUT = 1;
	/**
	 * 操作错误，多为目标设备可以访问，但具体OID返回异常
	 */
	public static final int ERR_SNMPOPER = 2;
	/**
	 * IO通信错误，可能是套接字被占用等操作失败
	 */
	public static final int ERR_IOERROR = 3;
	/**
	 * SNMP V3相关的参数错误
	 */
	public static final int ERR_V3_PARAM_ERROR = 4;
	/**
	 * SNMP采集到MIB结尾时的错误消息
	 */
	public static final int ERR_ENDOFMIB = 130;

	private int errorCode;

	/**
	 * 构建一个SnmpExcpetion
	 *
	 * @param errorCode 错误代码。见本类错误代码常量
	 * @param errorMsg  错误消息
	 */
	public SnmpException(int errorCode, String errorMsg) {
		super(errorMsg);
		this.errorCode = errorCode;
	}

	/**
	 * 构建一个SnmpExcpetion
	 *
	 * @param errorCode 错误代码。见本类错误代码常量
	 * @param cause     异常原因
	 */
	public SnmpException(int errorCode, Exception cause) {
		super(cause);
		this.errorCode = errorCode;
	}

	/**
	 * 构建一个SnmpExcpetion
	 *
	 * @param errorCode 错误代码。见本类错误代码常量
	 * @param errorMsg  错误消息
	 * @param cause     异常原因
	 */
	public SnmpException(int errorCode, String errorMsg, Exception cause) {
		super(errorMsg, cause);
		this.errorCode = errorCode;
	}

	/**
	 * 返回错误代码
	 *
	 * @return 错误代码，见本类错误代码常量
	 */
	public int getErrorCode() {
		return errorCode;
	}

	/**
	 * 设置错误代码
	 *
	 * @param errorCode 错误代码，见本类错误代码常量
	 */
	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}
}
