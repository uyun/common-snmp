package uyun.common.snmp.entity;

import org.snmp4j.security.AuthMD5;
import org.snmp4j.security.AuthSHA;
import org.snmp4j.smi.OID;

/**
 * 用于定义SNMPV3的验证协议参数
 */
public enum AuthProtocol {
	MD5(AuthMD5.ID), SHA(AuthSHA.ID);
	private OID oid;

	private AuthProtocol(OID oid) {
		this.oid = oid;
	}

	/**
	 * 根据编码获取枚举值，忽略大小写
	 *
	 * @param code
	 * @return
	 */
	public static AuthProtocol check(String code) {
		for (AuthProtocol item : values())
			if (item.name().equalsIgnoreCase(code))
				return item;
		throw new IllegalArgumentException(String.format("不存在的AuthProtocol值[%s]", code));
	}

	public OID getOID() {
		return this.oid;
	}
}