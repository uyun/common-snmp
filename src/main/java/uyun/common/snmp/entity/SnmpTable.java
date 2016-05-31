/*
 * Created on 2006-4-18
 */
package uyun.common.snmp.entity;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

public class SnmpTable implements Iterable<SnmpRow> {
	private SnmpOID[] columns;
	private Collection<SnmpRow> rows = new LinkedList<SnmpRow>();

	public SnmpTable(SnmpOID[] columns) {
		this.columns = columns;
	}

	/**
	 * @return Returns the rows.
	 */
	public Collection<SnmpRow> getRows() {
		return rows;
	}

	public void addRow(SnmpRow row) {
		rows.add(row);
	}

	/**
	 * @return Returns the columns.
	 */
	public SnmpOID[] getColumns() {
		return columns;
	}

	public SnmpRow getRow(SnmpOID instanceOID) {
		for (Iterator<SnmpRow> iter = rows.iterator(); iter.hasNext(); ) {
			SnmpRow row = (SnmpRow) iter.next();
			if (row.getInstance().equals(instanceOID))
				return row;
		}
		return null;
	}

	@Override
	public Iterator<SnmpRow> iterator() {
		return rows.iterator();
	}
}
