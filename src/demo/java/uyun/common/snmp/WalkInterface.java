package uyun.common.snmp;

import uyun.common.snmp.entity.Interface;
import uyun.common.snmp.entity.SnmpOID;
import uyun.common.snmp.entity.SnmpRow;
import uyun.common.snmp.entity.SnmpTable;
import uyun.common.snmp.error.SnmpException;

import java.util.ArrayList;
import java.util.List;

/**
 * Collect device interface table
 */
public class WalkInterface {
	public static void main(String[] args) throws SnmpException {
		String ifEntry = "1.3.6.1.2.1.2.2.1";
		SnmpOID[] ifTableColumns = new SnmpOID[22];
		for (int i = 0; i < ifTableColumns.length; i++)
			ifTableColumns[i] = new SnmpOID(ifEntry + "." + (i + 1));

		List<Interface> ifs = new ArrayList<Interface>();
		SnmpTable table = Snmp.walkTable(DemoParams.TARGET, ifTableColumns);
		for (SnmpRow row : table.getRows()) {
			Interface ife = new Interface();
			ife.setIndex(row.get(0).getValue().toLong());
			ife.setDescr(row.get(1).getValue().toText());
			ife.setType(row.get(2).getValue().toInteger());
			ife.setMtu(row.get(3).getValue().toInteger());
			ife.setSpeed(row.get(4).getValue().toLong());
			ife.setPhysAddress(row.get(5).getValue().toMac());
			ife.setAdminStatus(row.get(6).getValue().toInteger());
			ife.setOperStatus(row.get(7).getValue().toInteger());
			ife.setLastChange(row.get(8).getValue().toLong());
			ife.setInOctets(row.get(9).getValue().toLong());
			ife.setInUCastPkts(row.get(10).getValue().toLong());
			ife.setInNUcastPkts(row.get(11).getValue().toLong());
			ife.setInDiscards(row.get(12).getValue().toLong());
			ife.setInErrors(row.get(13).getValue().toLong());
			ife.setInUnknownProtos(row.get(14).getValue().toLong());
			ife.setOutOctets(row.get(15).getValue().toLong());
			ife.setOutUcastPkts(row.get(16).getValue().toLong());
			ife.setOutNUcastPkts(row.get(17).getValue().toLong());
			ife.setOutDiscards(row.get(18).getValue().toLong());
			ife.setOutErrors(row.get(19).getValue().toLong());
			ife.setOutQLen(row.get(20).getValue().toLong());
			ife.setOutSpecific(row.get(21).getValue().toText());
			ifs.add(ife);
		}

		for (Interface ife : ifs)
			System.out.println(ife);
	}
}
