package uyun.common.snmp.entity;

import org.snmp4j.smi.OID;
import org.snmp4j.smi.Variable;

/**
 * 成对的保存oid与snmp值，方便get、walk等方法返回
 */
public class SnmpVarBind {
	private SnmpOID oid;
	private SnmpValue value;

	/**
	 * 根据snmp4j的oid与variable构建一个结果
	 *
	 * @param oid      snmp4j的oid
	 * @param variable snmp4j的variable
	 */
	public SnmpVarBind(OID oid, Variable variable) {
		this.oid = new SnmpOID(oid);
		this.value = new SnmpValue(variable);
	}

	/**
	 * 通过SnmpOID构造一个结果，但值为null
	 *
	 * @param snmpOID
	 */
	public SnmpVarBind(SnmpOID snmpOID) {
		this.oid = snmpOID;
	}

	/**
	 * 返回oid
	 *
	 * @return oid
	 */
	public SnmpOID getOid() {
		return oid;
	}

	/**
	 * 设置oid
	 *
	 * @param oid oid
	 */
	public void setOid(SnmpOID oid) {
		this.oid = oid;
	}

	/**
	 * 返回snmp值
	 *
	 * @return snmp值，为null表示没有取到。具体可以参见相关get、getnext等函数
	 */
	public SnmpValue getValue() {
		return value;
	}

	/**
	 * 设置snmp值
	 *
	 * @param value snmp值，为null表示没有取到。具体可以参见相关get、getnext等函数
	 */
	public void setValue(SnmpValue value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return String.format("%s = %s", oid, value);
	}
}
