package uyun.common.snmp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.*;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.event.UsmUserEvent;
import org.snmp4j.event.UsmUserListener;
import org.snmp4j.mp.MPv3;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.security.SecurityLevel;
import org.snmp4j.security.*;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import uyun.common.snmp.entity.*;
import uyun.common.snmp.error.ErrorUtil;
import uyun.common.snmp.error.SnmpException;
import uyun.common.snmp.util.SpeedController;

import java.io.IOException;
import java.util.*;

/**
 * 对Snmp基本操作行为进行封装 本类中，所有方法行为都是一致的，比如返回均为SnmpResult，返回null均表示超时，弹出异常则为相应的异常
 */
public class Snmp {
	private static final Logger logger = LoggerFactory.getLogger(Snmp.class);
	/**
	 * snmp4j通信端口
	 */
	private static DefaultUdpTransportMapping transport;
	/**
	 * snmp4j操作对象
	 */
	private static org.snmp4j.Snmp snmp;
	/**
	 * Snmp发送包控制类
	 */
	private static SpeedController controller;
	private static RemoveableUSM usm;

	/**
	 * 调用此对象时，进行初始化。如果初始化失败，需要弹出RuntimeException异常。主程序捕捉到此异常的话，应该退出处理
	 */
	static {
		usm = new RemoveableUSM(SecurityProtocols.getInstance(), new OctetString(MPv3.createLocalEngineID()), 0);
		SecurityModels.getInstance().addSecurityModel(usm);
		open();
	}

	public static void open() {
		synchronized (Snmp.class) {
			try {
				transport = new DefaultUdpTransportMapping();
				snmp = new org.snmp4j.Snmp(transport);
				controller = SpeedController.getInstance();
				transport.listen();
			} catch (IOException e) {
				throw new RuntimeException("SNMP类库初始化失败", new SnmpException(
						SnmpException.ERR_IOERROR, "通信错误", e));
			}
		}
		logger.debug("SNMP4J 协议栈已启动");
	}

	/**
	 * 根据指定参数发送一个pdu对象，并同步等待目标对象的返回
	 *
	 * @param param snmp参数
	 * @param pdu   要发送的pdu
	 * @return 返回目标设备返回的pdu，如果超时未返回，则返回null
	 * @throws SnmpException 如果在操作时发送通信异常，则弹出此异常
	 */
	public static PDU send(SnmpTarget param, PDU pdu) throws SnmpException {
		controller.synTime(param.getIp());

		for (int i = 0; i < 3; i++) {
			try {
				ResponseEvent event = snmp.send(pdu, param.getTarget());
				if (event != null && event.getResponse() != null)
					return event.getResponse();
				else if (pdu.getType() == PDU.TRAP)
					return null;
				else
					throw new SnmpException(SnmpException.ERR_TIMEOUT, "SNMP操作超时，请确认目标IP[" + param.getIp() + "]可以访问且Snmp相关配置正确");
			} catch (Exception err) {
				if (err instanceof IllegalStateException ||
						(err instanceof MessageException && err.getMessage().contains("Socket is closed"))) {
					// 如果是协议栈错误
					logger.warn("SNMP4J 协议栈错误次数：" + i, err);
					synchronized (snmp) {
						try {
							close();
							open();
						} catch (Exception e) {
							logger.warn("SNMP4J 协议栈恢复失败", e);
							break;
						}
					}
				} else if (err instanceof SnmpException)    // 超时错误
					throw (SnmpException) err;
				else if (err instanceof IOException)            // IO错误
					throw new SnmpException(SnmpException.ERR_IOERROR, "SNMP IO错误：" + err.getMessage(), err);
				else
					throw new SnmpException(SnmpException.ERR_UNKNOWN, ErrorUtil.createMessage("未知错误", err), err);
			}
		}
		logger.error("由于SNMP4J 协议栈错误，进程无法继续工作");
		System.exit(1);
		return null;
	}

	public static SnmpVarBind set(SnmpTarget param, SnmpPDU pdu) throws SnmpException {
		pdu.setType(PDU.SET);
		PDU ret = send(param, pdu);
		//ret可能为null
		if (ret == null) {
			return null;
		}
		if (ret.getErrorStatus() != PDU.noError) {
			throw new SnmpException(SnmpException.ERR_SNMPOPER, "SNMP SET失败：" + ret.getErrorStatusText());
		}

		VariableBinding vb = (VariableBinding) ret.getVariableBindings().iterator().next();
		return new SnmpVarBind(vb.getOid(), vb.getVariable());
	}

	/**
	 * 根据指定参数，对指定的oid做get操作，并返回get的结果
	 *
	 * @param param   访问参数
	 * @param request 目标oid
	 * @return 如果成功则返回指定的结果。如果操作超时，则返回null
	 * @throws SnmpException 如果在操作时发送通信异常，则弹出此异常
	 */
	public static SnmpVarBind get(SnmpTarget param, SnmpOID request)
			throws SnmpException {
		SnmpVarBind[] results = get(param, new SnmpOID[]{request});
		// results可能为null
		if (results == null) {
			return null;
		}
		if (results.length > 1)
			throw new SnmpException(SnmpException.ERR_SNMPOPER, "get不应返回多个结果。");

		return results[0];
	}

	/**
	 * 根据指定参数，对指定的oid做getnext操作，并返回getnext的结果
	 *
	 * @param param   访问参数
	 * @param request 目标oid
	 * @return 如果成功则返回指定的结果，如果操作超时，则返回null
	 * @throws SnmpException 如果在操作时发送通信异常，则弹出此异常
	 */
	public static SnmpVarBind getNext(SnmpTarget param, SnmpOID request)
			throws SnmpException {
		SnmpVarBind[] results = getNext(param, new SnmpOID[]{request});

		if (results.length > 1)
			throw new SnmpException(SnmpException.ERR_SNMPOPER,
					"getNext不应返回多个结果。");

		return results[0];
	}

	/**
	 * 内部根据类型，批量snmp操作方法，但其是通过逐个获取的方式
	 */
	private static SnmpVarBind[] sendBatchStepByStep(int pduType,
													 SnmpTarget param, SnmpOID[] requests) throws SnmpException {
		List<SnmpVarBind> results = new ArrayList<SnmpVarBind>();
		for (int i = 0; i < requests.length; i++) {
			PDU pdu = createPDU(param.getVersion());
			pdu.setType(pduType);
			pdu.add(new VariableBinding(requests[i].oid()));

			PDU ret = send(param, pdu);
			if (ret.getErrorStatus() != 0) {
				if (ret.getErrorStatus() == 2) {
					results.add(new SnmpVarBind(requests[i]));
					continue;
				} else
					throw new SnmpException(SnmpException.ERR_SNMPOPER,
							"SNMP请求获取失败：" + ret.getErrorStatusText());
			}

			VariableBinding vb = (VariableBinding) ret.getVariableBindings()
					.iterator().next();
			results.add(new SnmpVarBind(vb.getOid(), vb.getVariable()));
		}
		return results.toArray(new SnmpVarBind[0]);
	}

	private static PDU createPDU(SnmpVersion version) {
		switch (version) {
			case V1:
				return new PDUv1();
			case V2C:
				return new PDU();
			default:
				return new ScopedPDU();
		}
	}

	/**
	 * 内部根据类型，批量snmp操作方法，通过一个PDU包含多个OID请求的方式
	 */
	private static SnmpVarBind[] sendBatch(int pduType, SnmpTarget param,
										   SnmpOID[] requests) throws SnmpException {
		/*监测pdu对应的snmp版本*/
		PDU pdu = createPDU(param.getVersion());
		pdu.setType(pduType);
		
		/*检查配置是否允许批处理，如果不运行就一步一步获取*/
		if (!controller.isBatch(param.getIp())) {
			return sendBatchStepByStep(pduType, param, requests);
		}

		for (int i = 0; i < requests.length; i++)
			pdu.add(new VariableBinding(requests[i].oid()));
		//send方法有可能返回null
		PDU ret = send(param, pdu);
		if (ret == null) {
			return null;
		}
		if (ret.getErrorStatus() != 0) {
			if (ret.getErrorStatus() == 2)
				return sendBatchStepByStep(pduType, param, requests);
			else
				throw new SnmpException(SnmpException.ERR_SNMPOPER,
						"SNMP请求获取失败：" + ret.getErrorStatusText());
		}

		if (ret.getVariableBindings().size() != requests.length) // 如果通过批量获取，无法获取到与请求相同数量的oid，则应该重新逐个获取
			return sendBatchStepByStep(pduType, param, requests);

		int index = 0;
		SnmpVarBind[] results = new SnmpVarBind[ret.getVariableBindings().size()];
		for (Iterator<?> iter = ret.getVariableBindings().iterator(); iter.hasNext(); ) {
			VariableBinding vb = (VariableBinding) iter.next();
			results[index++] = new SnmpVarBind(vb.getOid(), vb.getVariable());
		}

		return results;
	}

	/**
	 * 逐个通过get请求，获取requests数组中的各个oid值。多用于目标设备不支持一个get操作中，进行批量的oid请求
	 *
	 * @param param    访问参数
	 * @param requests 目标oid数组
	 * @return 返回成功得到值的结果数组。结果数组与请求数组相对应。
	 * @throws SnmpException 当发现下列情况时弹出：Snmp操作错误
	 */
	public static SnmpVarBind[] getStepByStep(SnmpTarget param, SnmpOID[] requests)
			throws SnmpException {
		return sendBatchStepByStep(PDU.GET, param, requests);
	}

	/**
	 * 通过get请求，获取requests数组中的各个oid值。其实现是尝试使用一个get操作，完成对所有oid的查询。
	 * 如果目标结果设备不支持一次请求批量，则会自动通过getStepByStep处理
	 *
	 * @param param    访问参数
	 * @param requests 目标oid数组
	 * @return 返回成功得到值的结果数组。结果数组与请求数组相对应。
	 * @throws SnmpException 当发现下列情况时弹出：Snmp操作错误
	 */
	public static SnmpVarBind[] get(SnmpTarget param, SnmpOID[] requests)
			throws SnmpException {
		return sendBatch(PDU.GET, param, requests);
	}

	/**
	 * 逐个通过getNext请求，获取requests数组中的各个oid值。多用于目标设备不支持一个getNext操作中，进行批量的oid请求
	 *
	 * @param param    访问参数
	 * @param requests 目标oid数组
	 * @return 返回成功得到值的结果数组。结果数组与请求数组相对应。
	 * @throws SnmpException 当发现下列情况时弹出：Snmp操作错误
	 */
	public static SnmpVarBind[] getNextStepByStep(SnmpTarget param,
												  SnmpOID[] requests) throws SnmpException {
		return sendBatchStepByStep(PDU.GETNEXT, param, requests);
	}

	/**
	 * 通过getNext请求，获取requests数组中的各个oid值。其实现是尝试使用一个getNext操作，完成对所有oid的查询。
	 * 如果目标结果设备不支持一次请求批量，则会自动通过getStepByStep处理
	 *
	 * @param param    访问参数
	 * @param requests 目标oid数组
	 * @return 返回成功得到值的结果数组。结果数组与请求数组相对应。
	 * @throws SnmpException 当发现下列情况时弹出：Snmp操作错误
	 */
	public static SnmpVarBind[] getNext(SnmpTarget param, SnmpOID[] requests)
			throws SnmpException {
		return sendBatch(PDU.GETNEXT, param, requests);
	}

	/**
	 * 对指定的request oid进行walk子树操作
	 *
	 * @param param   访问参数
	 * @param request walk目标oid
	 * @return 返回walk成功的整个数据，按walk顺序排列。超时则返回null
	 * @throws SnmpException 当发现下列情况时弹出：Snmp操作错误
	 */
	public static SnmpVarBind[] walk(SnmpTarget param, SnmpOID request)
			throws SnmpException {
		SnmpOID[] requests = new SnmpOID[1];
		ArrayList<SnmpVarBind> results = new ArrayList<SnmpVarBind>();
		SnmpVarBind[] ret;
		int repeatCount = 0;

		//如果oid最后一位是0，直接使用get获得结果: NCC-1143
		if (request.oid().last() == 0) {
			SnmpVarBind r = get(param, request);
			if (r.getValue() == null)
				return new SnmpVarBind[]{};
			else
				return new SnmpVarBind[]{r};
		}

		requests[0] = request;
		while (true) {
			try {
				ret = getNext(param, requests);
			} catch (SnmpException e) {
				//如果超时并且尚未获得的数据，或者继续循环
				if (e.getErrorCode() == SnmpException.ERR_TIMEOUT && results.size() > 0) {
					break;
				} else {
					throw e;
				}
			}
			if (ret.length > 1)
				throw new SnmpException(SnmpException.ERR_SNMPOPER,
						"getNext不应返回多个结果。");

			if (request.isChild(ret[0].getOid())) {
				// 如果OID重复超出次数
				if (ret[0].getOid().equals(requests[0])) {
					repeatCount++;
					if (repeatCount >= param.getAllowRepeatTime())
						break;
				} else
					repeatCount = 0;

				results.add(ret[0]);
				//重置request[0],requests用于比较，节省了一个元素的空间开销，并无其他意思。
				requests[0] = ret[0].getOid();
			} else
				break;
		}

		return (SnmpVarBind[]) results.toArray(new SnmpVarBind[0]);
	}

	/**
	 * 获取指定的列集的完整表格
	 *
	 * @param param
	 * @param columns
	 * @return
	 * @throws SnmpException
	 */
	public static SnmpTable walkTable(SnmpTarget param, SnmpOID[] columns)
			throws SnmpException {
		SnmpTable table = new SnmpTable(columns);
		SnmpOID[] requests = new SnmpOID[columns.length];
		SnmpVarBind[] ret;
		int j;

		// 遍历所有实例
		SnmpOID currInstance = null;
		while (true) {
			// 设置当前实例与所需要请求的列OID
			j = 0;

			for (; j < columns.length; j++) {
				if (currInstance == null)
					requests[j] = columns[j];
				else
					requests[j] = SnmpOID.join(columns[j], currInstance);
			}

			// GET请求
			ret = getNext(param, requests);

			// 分析建立行
			if (ret.length == 0)
				break;
			for (j = 0; j < requests.length; j++) {
				if (requests[j].equals(ret[j].getOid()))
					continue;
				if (columns[j].isChild(ret[j].getOid()))
					break;
			}
			if (j >= requests.length)    // 当前获取的表格数据，没有一个是当前需要的
				break;
			currInstance = ret[j].getOid().suboid(columns[j].length());
			for (j = 0; j < requests.length; j++)
				requests[j] = SnmpOID.join(columns[j], currInstance);

			SnmpRow row = new SnmpRow(currInstance, columns.length);
			for (j = 0; j < requests.length; j++) {

				// 从结果中查找，当前请求cell是否存在
				int m = 0;
				for (; m < ret.length; m++) {
					if (ret[m].getOid().equals(requests[j])) {
						row.set(j, ret[m]);
						break;
					}
				}

				// 如果不存在
				if (m >= ret.length) {
					try {
						SnmpVarBind tryRet = get(param, requests[j]);
						if (!tryRet.getValue().isNull()) {
							row.set(j, tryRet);
							continue;
						}
					} catch (Throwable e) {
						logger.warn(String.format("尝试重试walkTable缺失oid时失败[oid: %s]。错误：%s", requests[j], e));
						logger.debug("堆栈：", e);
					}

					if (param.isDiscardErrorRow()) // 如果参数设置为抛弃不完整行，则直接退出当前行分析
						break;
					else
						// 否则建立一个没有SnmpValue的Cell
						row.set(j, new SnmpVarBind(requests[j]));
				}
			}

			// 如果行顺利分析完成，包括有不完整行，但param允许保留不完整行
			if (j >= requests.length)
				table.addRow(row);
		}

		return table;
	}

	/**
	 * 指据指定的instances，获取这些实例相关的列数据行
	 *
	 * @param param
	 * @param columns
	 * @param instances
	 * @return
	 * @throws SnmpException
	 */
	public static SnmpTable walkTable(SnmpTarget param, SnmpOID[] columns,
									  SnmpOID[] instances) throws SnmpException {
		SnmpTable table = new SnmpTable(columns);
		SnmpOID[] requests = new SnmpOID[columns.length];
		SnmpVarBind[] ret;
		int j;

		// 遍历所有实例
		for (int i = 0; i < instances.length; i++) {
			// 设置当前实例与所需要请求的列OID
			j = 0;
			for (; j < columns.length; j++)
				requests[j] = SnmpOID.join(columns[j], instances[i]);

			// GET请求
			ret = get(param, requests);

			// 分析建立行
			SnmpRow row = new SnmpRow(instances[i], columns.length);
			j = 0;
			for (; j < requests.length; j++) {

				// 从结果中查找，当前请求cell是否存在
				int m = 0;
				for (; m < ret.length; m++) {
					if (ret[m].getOid().equals(requests[j])) {
						row.set(j, ret[m]);
						break;
					}
				}

				// 如果不存在
				if (m >= ret.length) {
					try {
						SnmpVarBind tryRet = get(param, requests[j]);
						if (!tryRet.getValue().isNull()) {
							row.set(j, tryRet);
							continue;
						}
					} catch (Throwable e) {
						if (logger.isDebugEnabled())
							logger.debug(String.format("尝试重试walkTable缺失oid时失败[oid: %s]。错误：", requests[j]), e);
					}

					if (param.isDiscardErrorRow()) // 如果参数设置为抛弃不完整行，则直接退出当前行分析
						break;
					else
						// 否则建立一个没有SnmpValue的Cell
						row.set(j, new SnmpVarBind(requests[j]));
				}
			}

			// 如果行顺利分析完成，包括有不完整行，但param允许保留不完整行
			if (j >= requests.length)
				table.addRow(row);
		}

		return table;
	}

	/**
	 * 从一个SnmpTarget建出snmp4j Target
	 *
	 * @param param
	 * @return
	 */
	public static Target createTarget(SnmpTarget param) {
		Address address = GenericAddress.parse("udp:" + param.getIp() + "/"
				+ param.getPort());
		if (param.isV3()) {
			OctetString user = new OctetString(param.getSecurityUser());

			UserTarget target = new UserTarget();
			target.setVersion(SnmpConstants.version3);
			target.setSecurityLevel(param.getSecurityLevel().getLevel());
			target.setSecurityName(user);
			target.setAddress(address);
			target.setRetries(param.getRetryTime());
			target.setTimeout(param.getTimeout());
			target.setVersion(param.getVersion().getId());

			usm.setUser(param, user);

			return target;
		} else {
			CommunityTarget target = new CommunityTarget();
			target.setCommunity(new OctetString(param.getCommunity()));
			target.setAddress(address);
			target.setRetries(param.getRetryTime());
			target.setTimeout(param.getTimeout());
			target.setVersion(param.getVersion().getId());
			return target;
		}
	}

	/**
	 * 关闭释放当前Snmp资源
	 *
	 * @throws SnmpException 如果发生通信错误，则弹出此异常
	 */
	public static void close() throws SnmpException {
		try {
			usm.removeAllUsers();
			snmp.close();
			transport.close();
		} catch (IOException err) {
			throw new SnmpException(SnmpException.ERR_IOERROR, "通信错误", err);
		}
	}

	private static class RemoveableUSM extends USM {
		private static Map<String, Entry> entries = new HashMap<String, Entry>();

		public RemoveableUSM(SecurityProtocols securityProtocols, OctetString localEngineID, int engineBoots) {
			super(securityProtocols, localEngineID, engineBoots);

			addUsmUserListener(new UsmUserListener() {
				public void usmUserChange(UsmUserEvent event) {
					if (event.getType() == UsmUserEvent.USER_ADDED) {
						String user = event.getUser().getUserName().toString();

						synchronized (entries) {
							Entry entry = entries.get(user);
							if (entry == null) {
								logger.warn("存在错误SNMPV3 USER添加");
								return;
							}
							entry.entries.add(event.getUser());
						}
					}
				}
			});
		}

		private void setUser(SnmpTarget param, OctetString user) {
			if (!param.isV3())
				return;

			Entry entry = entries.get(param.getSecurityUser());
			if (entry != null && entry.target.equalsV3Param(param))
				return;

			synchronized (entries) {
				if (entry == null) {
					entry = new Entry(param.clone());
					entries.put(param.getSecurityUser(), entry);
				} else {
					entry.target = param.clone();
					for (UsmUserEntry uue : entry.entries) {
						System.out.println("remove: " + this.removeUser(uue.getEngineID(), uue.getUserName()));
					}
					entry.entries.clear();
				}
			}

			if (param.getSecurityLevel().equals(SecurityLevel.authNoPriv)) {
				snmp.getUSM().addUser(user,
						new UsmUser(user, param.getAuthProtocol().getOID(), new OctetString(param.getAuthPassword()), null, null));
			} else if (param.getSecurityLevel().equals(SecurityLevel.authPriv)) {
				snmp.getUSM().addUser(
						user,
						new UsmUser(user, param.getAuthProtocol().getOID(), new OctetString(param.getAuthPassword()), param
								.getPrivProtocol().getOID(), new OctetString(param.getPrivPassword())));
			} else if (param.getSecurityLevel().equals(SecurityLevel.noAuthNoPriv)) {
				snmp.getUSM().addUser(
						user,
						new UsmUser(user, null, null, null, null));
			}
		}

		@Override
		public void removeAllUsers() {
			super.removeAllUsers();
			entries.clear();
		}

		private class Entry {
			SnmpTarget target;
			List<UsmUserEntry> entries = new ArrayList<UsmUserEntry>(2);

			public Entry(SnmpTarget target) {
				super();
				this.target = target;
			}
		}
	}
}
