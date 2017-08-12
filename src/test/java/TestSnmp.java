import org.junit.Test;
import uyun.common.snmp.Snmp;
import uyun.common.snmp.entity.*;
import uyun.common.snmp.error.SnmpException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestSnmp {
	private SnmpTarget createTarget() {
		return new SnmpTarget("10.1.1.1", "public");
	}

	@Test
	public void testSnmpWalkExceed() throws SnmpException {
		SnmpTarget target = createTarget();

		// 检查walk的目标树在正常个数内
		int maxLength = 100;
		SnmpWalkRequest request = new SnmpWalkRequest(maxLength, 0);
		SnmpWalkResult<SnmpVarBind[]> result = Snmp.walk(target, request, new SnmpOID(".1.3.6.1.2.1.1"));
		assertEquals(SnmpWalkResult.State.OK, result.getState());

		// 检查walk的目标树已超出要求
		result = Snmp.walk(target, request, new SnmpOID(".1"));
		assertEquals(SnmpWalkResult.State.EXCEED, result.getState());
		assertEquals(maxLength, result.getData().length);

		// 检查walk的目标表在正常个数内
		SnmpWalkResult<SnmpTable> resultTable = Snmp.walkTable(target, request, new SnmpOID[]{
				new SnmpOID("1.3.6.1.2.1.2.2.1.2"),
				new SnmpOID("1.3.6.1.2.1.2.2.1.3"),
		});
		assertEquals(SnmpWalkResult.State.OK, resultTable.getState());

		// 检查walk的目标表已超出要求
		resultTable = Snmp.walkTable(target, request, new SnmpOID[]{
				new SnmpOID("1.3.6.1.2.1.17.4.3.1.1"),
				new SnmpOID("1.3.6.1.2.1.17.4.3.1.2"),
		});
		assertEquals(SnmpWalkResult.State.EXCEED, resultTable.getState());
		assertEquals(maxLength, resultTable.getData().getRows().size());
	}

	@Test
	public void testSnmpWalkTimeout() throws SnmpException {
		SnmpTarget target = createTarget();

		// 检查walk的耗时在正常范围内
		int maxTimeout = 5000;
		long start = System.currentTimeMillis();
		SnmpWalkRequest request = new SnmpWalkRequest(0, maxTimeout);
		SnmpWalkResult<SnmpVarBind[]> result = Snmp.walk(target, request, new SnmpOID(".1.3.6.1.2.1.1"));
		assertEquals(SnmpWalkResult.State.OK, result.getState());
		long time = System.currentTimeMillis() - start;
		assertTrue(time < maxTimeout);

		// 检查walk的耗时已超出范围
		start = System.currentTimeMillis();
		result = Snmp.walk(target, request, new SnmpOID(".1"));
		assertEquals(SnmpWalkResult.State.OVERTIME, result.getState());
		time = System.currentTimeMillis() - start;
		assertTrue(time > maxTimeout && time < maxTimeout + 1000);

		// 检查walk的耗时在正常范围内
		start = System.currentTimeMillis();
		SnmpWalkResult<SnmpTable> resultTable = Snmp.walkTable(target, request, new SnmpOID[]{
				new SnmpOID("1.3.6.1.2.1.2.2.1.2"),
				new SnmpOID("1.3.6.1.2.1.2.2.1.3"),
		});
		assertEquals(SnmpWalkResult.State.OK, resultTable.getState());
		time = System.currentTimeMillis() - start;
		assertTrue(time < maxTimeout);

		// 检查walk的耗时已超出范围
		start = System.currentTimeMillis();
		resultTable = Snmp.walkTable(target, request, new SnmpOID[]{
				new SnmpOID("1.3.6.1.2.1.17.4.3.1.1"),
				new SnmpOID("1.3.6.1.2.1.17.4.3.1.2"),
		});
		assertEquals(SnmpWalkResult.State.OVERTIME, resultTable.getState());
		time = System.currentTimeMillis() - start;
		assertTrue(time > maxTimeout && time < maxTimeout + 1000);
	}

	public static void main(String[] args) throws SnmpException {
//		SnmpTarget target = new SnmpTarget("10.1.1.50", "broadapublic");
//		target.setAllowRepeatTime(2);
//		SnmpOID[] table = new SnmpOID[]{
//				new SnmpOID(".1.3.6.1.2.1.17.4.3.1.2"),
//				new SnmpOID(".1.3.6.1.2.1.17.4.3.1.3")
//		};
//		System.out.println(Snmp.walkTable(target, table).getRows().size());

//		SnmpTarget target = new SnmpTarget("10.1.1.50", SecurityLevel.authPriv, "uyunuser", AuthProtocol.SHA, "uyun1234", PrivProtocol.DES, "uyundes1");
//		System.out.println(Arrays.toString(Snmp.walk(target, new SnmpOID(".1.3.6.1.2.1.1"))));
	}
}
