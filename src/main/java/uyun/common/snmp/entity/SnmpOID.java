package uyun.common.snmp.entity;

import org.snmp4j.smi.OID;

/**
 * 封装SNMP OID对象
 */
public class SnmpOID {
	private final OID oid; // 为了减少各函数中空判断语句，不允许此字段为空，所以在建立新的构造函数请注意

	/**
	 * 从字符串构建一个oid对象
	 *
	 * @param oid 字符串可以以.号开头，也兼容不用.号开头
	 */
	public SnmpOID(String oid) {
		this.oid = new OID(oid);
	}

	/**
	 * 从snmp4j的oid开始构建
	 *
	 * @param oid
	 */
	public SnmpOID(OID oid) {
		if (oid == null)
			oid = new OID();
		this.oid = (OID) oid.clone();
	}

	/**
	 * 构建一个空的oid对象，如果toString此对象，将返回0长度字符串
	 */
	public SnmpOID() {
		this.oid = new OID();
	}

	/**
	 * 从数组中构建一个OID
	 *
	 * @param values 整形数组，逐个保存OID中的各个数字
	 */
	public SnmpOID(int[] values) {
		this.oid = new OID(values);
	}

	/**
	 * 从数组中构建一个OID
	 *
	 * @param values 整形数组，逐个保存OID中的各个数字
	 */
	public SnmpOID(long[] values) {
		this.oid = new OID(convertArray(values));
	}

	/**
	 * 合并两个oid，形成一个新的oid
	 *
	 * @param first  合并OID前部份
	 * @param second 合并OID后部份
	 * @return 返回合并出的OID值。
	 */
	public static SnmpOID join(SnmpOID first, SnmpOID second) {
		OID temp = new OID(first.oid);
		temp.append(second.oid);
		return new SnmpOID(temp);
	}

	private static int[] convertArray(long[] value) {
		int[] result = new int[value.length];
		for (int i = 0; i < value.length; i++)
			result[i] = (int) value[i];
		return result;
	}

	private static long[] convertArray(int[] value) {
		long[] result = new long[value.length];
		for (int i = 0; i < value.length; i++)
			result[i] = (value[i] & 0xffffffffl);
		return result;
	}

	/**
	 * 获得snmp4j的 oid对象
	 *
	 * @return snmp4j的oid对象
	 */
	public OID oid() {
		return oid;
	}

	/**
	 * 返回.1.2.3形式表现的OID字符串
	 *
	 * @return OID字符表现形式
	 */
	public String toString() {
		String ret = oid.toString();
		if (ret.length() <= 0)
			return ret;
		else
			return ret.charAt(0) != '.' ? ("." + ret) : ret;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		else if (obj == null)
			return false;
		else if (!(obj instanceof SnmpOID))
			return false;

		SnmpOID right = (SnmpOID) obj;
		if (this.oid == right.oid)
			return true;

		return this.oid.equals(right.oid);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return oid.hashCode();
	}

	/**
	 * 检查参数subOid是否是当前实例的子树oid
	 *
	 * @param subOid 目标oid
	 * @return 如果是，返回true，否则返回false
	 */
	public boolean isChild(SnmpOID subOid) {
		return subOid.oid.startsWith(this.oid)
				&& (subOid.oid.size() > this.oid.size());
	}

	public boolean startsWith(String oid) {
		return this.toString().startsWith(oid);
	}

	/**
	 * 返回目前oid的长度，以oid中出现的数字个数计数。如.1.2.1234则长度为3
	 *
	 * @return 当前oid的长度
	 */
	public int length() {
		return oid.size();
	}

	/**
	 * 截取从当前oid的start参数指定位置开始至结束
	 *
	 * @param start 从指定的位置开始截取
	 * @return 返回截取出来的oid
	 */
	public SnmpOID suboid(int start) {
		int[] value = oid.getValue();
		if (value == null)
			return new SnmpOID();

		int size = value.length - start;
		if (size <= 0)
			return new SnmpOID();

		int index = 0;
		int[] subValue = new int[size];
		for (int i = start; i < value.length; i++)
			subValue[index++] = value[i];

		return new SnmpOID(subValue);
	}

	/**
	 * 返回整形数组表现形式的oid
	 *
	 * @return 整形数组，每个元素都代表OID中的一个数字
	 */
	public long[] getNumbers() {
		return convertArray(oid.getValue());
	}
}
