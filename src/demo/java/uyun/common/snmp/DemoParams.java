package uyun.common.snmp;

import uyun.common.snmp.entity.SnmpTarget;

/**
 * Demo parameters.
 */
public class DemoParams {
	public static final String IP = "10.1.1.1";
	public static final String COMMUNITY = "broadapublic";

	public static final SnmpTarget TARGET = new SnmpTarget(IP, COMMUNITY);
}
