package uyun.common.snmp.entity;

import org.snmp4j.security.PrivAES128;
import org.snmp4j.security.PrivAES192;
import org.snmp4j.security.PrivAES256;
import org.snmp4j.security.PrivDES;
import org.snmp4j.smi.OID;

/**
 * 用于定义SNMPV3的加密协议参数
 */
public enum PrivProtocol {
	DES(PrivDES.ID), AES128(PrivAES128.ID), AES192(PrivAES192.ID), AES256(PrivAES256.ID);
	private OID oid;

	private PrivProtocol(OID oid) {
		this.oid = oid;
	}

	/**
	 * 根据编码获取枚举值，忽略大小写
	 *
	 * @param code
	 * @return
	 */
	public static PrivProtocol check(String code) {
		for (PrivProtocol item : values())
			if (item.name().equalsIgnoreCase(code))
				return item;
		throw new IllegalArgumentException(String.format("不存在的PrivProtocol值[%s]", code));
	}

	public OID getOID() {
		return this.oid;
	}
}