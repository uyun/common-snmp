package uyun.common.snmp.entity;

/**
 * 用于定义SNMPV3的检验方式（或称级别）参数
 */
public enum SecurityLevel {
	noAuthNoPriv("无验证无加密", org.snmp4j.security.SecurityLevel.NOAUTH_NOPRIV),
	authNoPriv("仅验证", org.snmp4j.security.SecurityLevel.AUTH_NOPRIV),
	authPriv("验证并加密", org.snmp4j.security.SecurityLevel.AUTH_PRIV);

	private String name;
	private int level;

	private SecurityLevel(String name, int level) {
		this.name = name;
		this.level = level;
	}

	/**
	 * 根据编码或名称获取枚举值，忽略大小写
	 *
	 * @param value
	 * @return
	 */
	public static SecurityLevel check(String value) {
		for (SecurityLevel item : values())
			if (item.name().equalsIgnoreCase(value) || item.getName().equals(value))
				return item;
		throw new IllegalArgumentException(String.format("不存在的SecurityLevel值[%s]", value));
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return this.name;
	}

	/**
	 * 获取snmp4j的安全级别
	 *
	 * @return
	 */
	public int getLevel() {
		return level;
	}
}
