package uyun.common.snmp.entity;

import org.snmp4j.mp.SnmpConstants;

/**
 * snmp版本
 */
public enum SnmpVersion {
	V1(SnmpConstants.version1), V2C(SnmpConstants.version2c), V3(SnmpConstants.version3);

	private int id;

	private SnmpVersion(int id) {
		this.id = id;
	}

	public static SnmpVersion checkByName(String version) {
		for (SnmpVersion item : values())
			if (item.name().equalsIgnoreCase(version))
				return item;
		throw new IllegalArgumentException("未知的Snmp版本：" + version);
	}

	public int getId() {
		return id;
	}

}
