/*
 * Created on 2006-4-18
 */
package uyun.common.snmp.entity;


public class SnmpRow {
	private SnmpVarBind[] cells;
	private SnmpOID instance;

	public SnmpRow(SnmpOID instance, int size) {
		this.instance = instance;
		cells = new SnmpVarBind[size];
	}

	/**
	 * @return Returns the instance.
	 */
	public SnmpOID getInstance() {
		return instance;
	}

	public int size() {
		return cells.length;
	}

	public void set(int index, SnmpVarBind result) {
		cells[index] = result;
	}

	public SnmpVarBind get(int index) {
		return cells[index];
	}

	/**
	 * @return Returns the cells.
	 */
	public SnmpVarBind[] getCells() {
		return cells;
	}

	public SnmpVarBind get(SnmpOID column) {
		for (int i = 0; i < cells.length; i++) {
			if (cells[i] != null && column.isChild(cells[i].getOid()))
				return cells[i];
		}
		return null;
	}
}
