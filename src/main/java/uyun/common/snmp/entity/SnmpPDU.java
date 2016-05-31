package uyun.common.snmp.entity;

/**
 * <p>
 * Title: BCSnmpPDU
 * </p>
 * <p>
 * Description: 继承AdventNet的SnmpPDU,添加addVarBind等方法便于操作
 * </p>
 * <p>
 * Copyright: Copyright (c) 2002
 * </p>
 * <p>
 * Company: 杭州广通软件
 * </p>
 *
 * @author 庞辉富
 * @version 1.0
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.PDU;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.Null;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.VariableBinding;

public class SnmpPDU extends PDU {
	private static final long serialVersionUID = 1L;

	private static final Logger logger = LoggerFactory.getLogger(SnmpPDU.class);

	/*是否打印错误信息*/
	private boolean debug = false;

	private int version;

	public SnmpPDU() {
		super();
	}

	public void addIntegerVarBind(String oid, String val) {
		VariableBinding varbind = new VariableBinding(new OID(oid), new Integer32(Integer.parseInt(val)));
		add(varbind);
	}

	/**
	 * 添加一个字符串类型的VarBind,可以指定字符值的类型
	 * @param oid
	 * @param val
	 * @param charsetName
	 */
	public void addStringVarBind(String oid, String val, String charsetName) {
		if (logger.isDebugEnabled()) {
			logger.debug("Set VarBind{" + oid + ":" + val + "}");
		}
		VariableBinding varbind = new VariableBinding(new OID(oid), new OctetString(val));
		add(varbind);
	}

	/**
	 * 添加一个字符串类型的VarBind,字符类型为BGK
	 * @param oid
	 * @param val
	 */
	public void addStringVarBind(String oid, String val) {
		addStringVarBind(oid, val, "GBK");
	}

	/**
	 * 添加一个空的varbind
	 * @param oid
	 */
	public void addNull(String oid) {
		VariableBinding varbind = new VariableBinding(new OID(oid), new Null());
		add(varbind);
	}

	/**
	 * 是否打印错误
	 * @return
	 */
	public boolean isDebug() {
		return debug;
	}

	/**
	 * 设置是否打印错误
	 * @param debug
	 */
	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public void setCommand(int command) {
		this.setType(command);
	}

}