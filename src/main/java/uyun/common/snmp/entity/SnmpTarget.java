package uyun.common.snmp.entity;

import org.snmp4j.Target;
import uyun.common.snmp.Snmp;

/**
 * 对Snmp参数提供一个统一的对象进行封装
 */
public class SnmpTarget implements Cloneable {
	/**
	 * 默认SNMP AGENT端口
	 */
	public static final int PORT_DEFAULT = 161;
	/**
	 * 默认TIMEOUT端口，单位毫秒
	 */
	public static final long TIMEOUT_DEFAULT = 2000;
	/**
	 * 默认重试次数
	 */
	public static final int RETRYTIME_DEFAULT = 1;
	/**
	 * 默认OID重试允许次数
	 */
	public static final int ALLOWREPEATTIME_DEFAULT = 3;
	/**
	 * 默认community
	 */
	public static final String COMMUNITY_DEFAULT = "public";
	/** */
	public static final boolean DISCARDERRORROW_DEFAULT = false;

	private long timeout = TIMEOUT_DEFAULT;
	private int retryTime = RETRYTIME_DEFAULT;
	private String ip;
	private String community = COMMUNITY_DEFAULT;
	private int port = PORT_DEFAULT;
	private int allowRepeatTime = ALLOWREPEATTIME_DEFAULT;
	private SnmpVersion version = SnmpVersion.V2C;
	private boolean discardErrorRow = DISCARDERRORROW_DEFAULT;
	private SecurityLevel securityLevel;
	private String securityUser;
	private AuthProtocol authProtocol = AuthProtocol.MD5;
	private String authPassword;
	private PrivProtocol privProtocol = PrivProtocol.AES128;
	private String privPassword;
	private Target target;

	/**
	 * 构建一个SnmpTarget，参数保持默认如下
	 * ip = null
	 * version = v1
	 * community = public
	 * port = 161
	 * timeout = 2000
	 * retryTime = 1
	 */
	public SnmpTarget() {
		super();
	}

	/**
	 * 构建一个SnmpTarget，仅设置community
	 * 其它默认参数见缺省构建函数
	 *
	 * @param community
	 */
	public SnmpTarget(String community) {
		this.community = community;
	}

	/**
	 * 构建一个SnmpTarget，仅设置ip与community
	 * 其它默认参数见缺省构建函数
	 *
	 * @param ip
	 * @param community
	 */
	public SnmpTarget(String ip, String community) {
		super();
		this.ip = ip;
		this.community = community;
	}

	/**
	 * 构建一个SnmpV3 Target
	 * 其它默认参数见缺省构建函数
	 *
	 * @param securityLevel
	 * @param securityUser
	 * @param authProtocol
	 * @param authPassword
	 * @param privProtocol
	 * @param privPassword
	 */
	public SnmpTarget(String ip, SecurityLevel securityLevel, String securityUser, AuthProtocol authProtocol,
					  String authPassword, PrivProtocol privProtocol, String privPassword) {
		super();
		this.ip = ip;
		this.version = SnmpVersion.V3;

		if (securityLevel == null || securityUser == null)
			throw new IllegalArgumentException("SNMPV3参数必须提供securityLevel与securityUser");

		if (securityLevel != SecurityLevel.noAuthNoPriv) {
			if (authProtocol == null || authPassword == null)
				throw new IllegalArgumentException("SNMPV3 authNoPriv验证级别必须提供authProtocol与authPassword");

			if (securityLevel != SecurityLevel.authNoPriv) {
				if (privProtocol == null || privPassword == null)
					throw new IllegalArgumentException("SNMPV3 authPriv验证级别必须提供authProtocol与authPassword");
			}
		}

		this.securityLevel = securityLevel;
		this.securityUser = securityUser;
		this.authProtocol = authProtocol;
		this.authPassword = authPassword;
		this.privProtocol = privProtocol;
		this.privPassword = privPassword;
	}

	private static String nullStr(Object value) {
		if (value == null)
			return "";
		else
			return value.toString();
	}

	public boolean isDiscardErrorRow() {
		return discardErrorRow;
	}

	public void setDiscardErrorRow(boolean discardErrorRow) {
		this.discardErrorRow = discardErrorRow;
	}

	/**
	 * 获取Snmp版本
	 *
	 * @return SNMP版本，值见SnmpConstants中提供的常量
	 */
	public SnmpVersion getVersion() {
		return version;
	}

	/**
	 * 设置Snmp版本
	 *
	 * @param version SNMP版本，值见SnmpConstants中提供的常量
	 */
	public void setVersion(SnmpVersion version) {
		this.version = version;
		this.target = null;
	}

	/**
	 * 获取目标设备IP地址
	 *
	 * @return 目标设备IP
	 */
	public String getIp() {
		return ip;
	}

	/**
	 * 设置目标设备IP地址
	 *
	 * @param 目标设备IP地址
	 */
	public void setIp(String ip) {
		this.ip = ip;
		this.target = null;
	}

	/**
	 * 返回目标设备端口
	 *
	 * @return 目标设备端口，默认为161
	 * @see PORT_DEFAULT
	 */
	public int getPort() {
		return port;
	}

	/**
	 * 设置目标设备端口
	 *
	 * @param port 目标设备端口，默认为161
	 * @see PORT_DEFAULT
	 */
	public void setPort(int port) {
		this.port = port;
		this.target = null;
	}

	/**
	 * 返回重试次数，实现尝试次数为重试次数+1
	 *
	 * @return 重试次数，默认为2。
	 */
	public int getRetryTime() {
		return retryTime;
	}

	/**
	 * 设备重试次数，实现尝试次数为重试次数+1
	 *
	 * @param retryTime 重试次数，默认为2
	 */
	public void setRetryTime(int retryTime) {
		this.retryTime = retryTime;
		this.target = null;
	}

	/**
	 * 返回超时时间
	 *
	 * @return 超时时间，单位毫秒。默认2000
	 */
	public long getTimeout() {
		return timeout;
	}

	/**
	 * 设置超时时间
	 *
	 * @param timeout 超时时间，单位毫秒。默认2000
	 */
	public void setTimeout(long timeout) {
		this.timeout = timeout;
		this.target = null;
	}

	/**
	 * 返回walk时，允许的oid重复次数，用以检查walk时的死无限循环
	 *
	 * @return 允许重复次数，默认3
	 */
	public int getAllowRepeatTime() {
		return allowRepeatTime;
	}

	/**
	 * 设置walk时，允许的oid重复次数，用以检查walk时的死无限循环
	 *
	 * @param allowRepeatTime 允许重复次数，默认3
	 */
	public void setAllowRepeatTime(int allowRepeatTime) {
		this.allowRepeatTime = allowRepeatTime;
	}

	/**
	 * 返回community
	 *
	 * @return community
	 */
	public String getCommunity() {
		return community;
	}

	/**
	 * 设备community
	 *
	 * @param community 设备community
	 */
	public void setCommunity(String community) {
		this.community = community;
		this.target = null;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(String.format("snmp[ip: %s ver: %d timeout: %d retry: %d", getIp(), getVersion(), getTimeout(),
				getRetryTime()));
		if (getVersion() == SnmpVersion.V3) {
			sb.append(String.format(" level: %s user: %s authPro: %s authPass: %s privPro: %s privPass: %s",
					getSecurityLevel(), getSecurityUser(), nullStr(getAuthProtocol()),
					nullStr(getAuthPassword()), nullStr(getPrivProtocol()), nullStr(getPrivPassword())));
		} else {
			sb.append(String.format(" community: %s]", getCommunity()));
		}
		return sb.toString();
	}

	public SecurityLevel getSecurityLevel() {
		return securityLevel;
	}

	public void setSecurityLevel(SecurityLevel securityLevel) {
		this.securityLevel = securityLevel;
		this.target = null;
	}

	public AuthProtocol getAuthProtocol() {
		return authProtocol;
	}

	public void setAuthProtocol(AuthProtocol pro) {
		this.authProtocol = pro;
		this.target = null;
	}

	public String getAuthPassword() {
		return authPassword;
	}

	public void setAuthPassword(String authPassword) {
		this.authPassword = authPassword;
		this.target = null;
	}

	public PrivProtocol getPrivProtocol() {
		return privProtocol;
	}

	public void setPrivProtocol(PrivProtocol privProtocol) {
		this.privProtocol = privProtocol;
		this.target = null;
	}

	public String getPrivPassword() {
		return privPassword;
	}

	public void setPrivPassword(String privPassword) {
		this.privPassword = privPassword;
		this.target = null;
	}

	public String getSecurityUser() {
		return securityUser;
	}

	public void setSecurityUser(String securityUser) {
		this.securityUser = securityUser;
		this.target = null;
	}

	/* 暂不启用
	public String getEngineId() {
		return engineId;
	}

	public void setEngineId(String engineId) {
		this.engineId = engineId;
		this.target = null;
	}
	*/

	/**
	 * 检查当前参数与another的snmpv3参数是否相等，包含以下参数：
	 * securityLevel, authProtocol, authPassword, privProtocol, privPassword
	 *
	 * @param another
	 * @return
	 */
	public boolean equalsV3Param(SnmpTarget another) {
		if (this.securityLevel != another.securityLevel)
			return false;

		if (this.securityLevel != SecurityLevel.noAuthNoPriv) {
			if (this.authProtocol != another.authProtocol
					|| !this.authPassword.equals(another.authPassword))
				return false;

			if (this.securityLevel != SecurityLevel.authNoPriv) {
				if (this.privProtocol != another.privProtocol
						|| !this.privPassword.equals(another.privPassword))
					return false;
			}
		}

		return true;
	}

	@Override
	public SnmpTarget clone() {
		try {
			return (SnmpTarget) super.clone();
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 判断当前是否是v3版本
	 *
	 * @return
	 */
	public boolean isV3() {
		return version == SnmpVersion.V3;
	}

	public Target getTarget() {
		if (target == null)
			target = Snmp.createTarget(this);
		return target;
	}
}
