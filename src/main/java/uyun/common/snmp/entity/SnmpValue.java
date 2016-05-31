package uyun.common.snmp.entity;

import org.snmp4j.smi.Counter32;
import org.snmp4j.smi.Counter64;
import org.snmp4j.smi.Gauge32;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.IpAddress;
import org.snmp4j.smi.Null;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.Opaque;
import org.snmp4j.smi.TimeTicks;
import org.snmp4j.smi.UnsignedInteger32;
import org.snmp4j.smi.Variable;

/**
 * 封装snmp值对象，避免直接处理各种snmp数据类型
 */
public class SnmpValue {
	public static final SnmpValue NULL = new SnmpValue();
	public static final int TYPE_UNKNOWN = 0;
	public static final int TYPE_INTEGER32 = 1;
	public static final int TYPE_UNSIGNEDINTEGER32 = 2;
	public static final int TYPE_OCTETSTRING = 3;
	public static final int TYPE_NULL = 4;
	public static final int TYPE_COUNTER64 = 5;
	public static final int TYPE_COUNTER32 = 6;
	public static final int TYPE_GAUGE32 = 7;
	public static final int TYPE_GENERICADDRESS = 8;
	public static final int TYPE_IPADDRESS = 9;
	public static final int TYPE_TIMETICKS = 10;
	public static final int TYPE_OPAQUE = 11;
	public static final int TYPE_OID = 12;

	private Variable value;

	/**
	 * 从snmp4j的variable构造一个值对象
	 *
	 * @param variable
	 */
	public SnmpValue(Variable variable) {
		this.value = variable;
	}

	public SnmpValue() {
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		if (value == null)
			return "";
		else if (value instanceof OID)
			return "." + value.toString();
		else
			return value.toString();
	}

	public SnmpOID toOid() {
		if (value instanceof OID)
			return new SnmpOID((OID) value);
		else
			throw new IllegalArgumentException("值不是一个OID类型：" + value.toString());
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof SnmpValue))
			return false;

		SnmpValue another = (SnmpValue) obj;
		if (this.value == another.value)
			return true;

		if (this.value == null)
			return false;

		return this.value.equals(another.value);
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	/**
	 * 如果SnmpValue为OctetString，可以返回其文本表示。
	 * <p>
	 * 由于asn使用OctetString类型来表示很多类型，如文本、MAC地址、IP地址等，
	 * 这就导致了OctetString.toString会有两种不同的行为如下：
	 * 1.如果数据都是ascii编码，即每个字符都是小于128的话，则转为文本字符串输出
	 * 2.如不是，则转为Hex字符串输出，如01:3c:2d:4d等
	 * 这就导致了这种情况，如果一个mib信息，如端口名称，
	 * 包括了中文或是127位以上的字符编码，就无法正常输出文本，而输出Hex字符串这种不可阅读的文本。
	 * 因此，必须使用本方法toText来进行调用，或toIP来进行调用。
	 * <p>
	 * 使用规则如下：
	 * 1.如果你的oid值为文本，就一定使用toText
	 * 2.如果你的oid值为mac地址，就一定使用toString
	 * 3.如果你的oid值为ip地址，就一定使用toIP
	 * 4.如果你的oid值为其它数据，视情况，可使用toString或getBytes
	 *
	 * @return 返回转换后的文本
	 * @throws IllegalArgumentException 如果值非OctetString类型，则弹出此异常
	 */
	public String toText() throws IllegalArgumentException {
		if (value instanceof OctetString) {
			byte[] datas = ((OctetString) value).getValue();
			if (datas.length == 0)
				return new String("");

			int len = datas.length;
			if (datas[datas.length - 1] == 0)
				len = len - 1;
			return new String(datas, 0, len);
		} else
			return value.toString();
	}

	public String toMac() {
		return value.toString();
	}

	/**
	 * 如果SnmpValue为OctetString，可以返回其字节数组。
	 *
	 * @return
	 * @throws IllegalArgumentException
	 */
	public byte[] toBytes() throws IllegalArgumentException {
		if (value instanceof OctetString)
			return ((OctetString) value).getValue();
		throw new IllegalArgumentException("值[" + value + "]非字符串类型.");
	}

	/**
	 * 如果SnmpValue为OctetString并表示IP，则可以转为IPV4表示方式
	 *
	 * @return 返回转换后的IP，以192.168.0.0的形式表示
	 * @throws IllegalArgumentException 如果值非OctetString类型且长度不为4，则弹出此异常
	 */
	public String toIp() throws IllegalArgumentException {
		if (value instanceof OctetString) {
			byte[] datas = ((OctetString) value).getValue();
			if (datas.length == 4)
				return (datas[0] & 0xff) + "." + (datas[1] & 0xff) + "."
						+ (datas[2] & 0xff) + "." + (datas[3] & 0xff);
		} else if (value instanceof IpAddress)
			return value.toString();
		throw new IllegalArgumentException("值[" + value + "]非IP类型.");
	}

	/**
	 * 如果SnmpValue为Couter64、Integer64、Unsi8gnedInteger32等类型时，可以转换为long类型输出
	 *
	 * @return 数值
	 * @throws NumberFormatException 如果snmp值类型非数字类型，不可转换，则弹出此异常
	 */
	public long toLong() throws NumberFormatException {
		if (value == null)
			throw new NumberFormatException("采集结果值为空.");

		if (value instanceof Counter64)
			return ((Counter64) value).getValue();
		else if (value instanceof Integer32)
			return ((Integer32) value).getValue();
		else if (value instanceof UnsignedInteger32)
			return ((UnsignedInteger32) value).getValue();
		else {
			//先做转换
			try {
				return Long.parseLong(value.toString());
			} catch (NumberFormatException e) {
				throw new NumberFormatException("值[" + value + "]非数字类型.");
			}
		}
	}

	public int toInteger() throws NumberFormatException {
		if (value == null)
			throw new NumberFormatException("采集结果值为空.");

		if (value instanceof Counter64)
			return (int) (((Counter64) value).getValue());
		else if (value instanceof Integer32)
			return ((Integer32) value).getValue();
		else if (value instanceof UnsignedInteger32)
			return (int) (((UnsignedInteger32) value).getValue());
		else {
			//先做转换
			try {
				return Integer.parseInt(value.toString());
			} catch (NumberFormatException e) {
				throw new NumberFormatException("值[" + value + "]非数字类型.");
			}
		}
	}

	/**
	 * 测试当前snmp值对象是否为空值
	 *
	 * @return 检查当前snmp值对象是否为空值
	 */
	public boolean isNull() {
		return value instanceof Null || value == null || "noSuchInstance".equals(value.toString());
	}

	public int getType() {
		if (isNull())
			return TYPE_NULL;
		else if (value instanceof Counter64)
			return TYPE_COUNTER64;
		else if (value instanceof Counter32)
			return TYPE_COUNTER32;
		else if (value instanceof Integer32)
			return TYPE_INTEGER32;
		else if (value instanceof UnsignedInteger32)
			return TYPE_UNSIGNEDINTEGER32;
		else if (value instanceof OctetString)
			return TYPE_OCTETSTRING;
		else if (value instanceof OID)
			return TYPE_OID;
		else if (value instanceof Gauge32)
			return TYPE_GAUGE32;
		else if (value instanceof GenericAddress)
			return TYPE_GENERICADDRESS;
		else if (value instanceof IpAddress)
			return TYPE_IPADDRESS;
		else if (value instanceof TimeTicks)
			return TYPE_TIMETICKS;
		else if (value instanceof Opaque)
			return TYPE_OPAQUE;
		else
			return TYPE_UNKNOWN;
	}
}
