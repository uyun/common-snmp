package uyun.common.snmp.entity;

/**
 * Snmp interface entity
 */
public class Interface {
	private long index;
	private String descr;
	private int type;
	private int mtu;
	private long speed;
	private String physAddress;
	private int adminStatus;
	private int operStatus;
	private long lastChange;
	private long inOctets;
	private long inUCastPkts;
	private long inNUcastPkts;
	private long inDiscards;
	private long inErrors;
	private long inUnknownProtos;
	private long outOctets;
	private long outUcastPkts;
	private long outNUcastPkts;
	private long outDiscards;
	private long outErrors;
	private long outQLen;
	private String outSpecific;

	public long getIndex() {
		return index;
	}

	public void setIndex(long index) {
		this.index = index;
	}

	public String getDescr() {
		return descr;
	}

	public void setDescr(String descr) {
		this.descr = descr;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getMtu() {
		return mtu;
	}

	public void setMtu(int mtu) {
		this.mtu = mtu;
	}

	public long getSpeed() {
		return speed;
	}

	public void setSpeed(long speed) {
		this.speed = speed;
	}

	public String getPhysAddress() {
		return physAddress;
	}

	public void setPhysAddress(String physAddress) {
		this.physAddress = physAddress;
	}

	public int getAdminStatus() {
		return adminStatus;
	}

	public void setAdminStatus(int adminStatus) {
		this.adminStatus = adminStatus;
	}

	public int getOperStatus() {
		return operStatus;
	}

	public void setOperStatus(int operStatus) {
		this.operStatus = operStatus;
	}

	public long getLastChange() {
		return lastChange;
	}

	public void setLastChange(long lastChange) {
		this.lastChange = lastChange;
	}

	public long getInOctets() {
		return inOctets;
	}

	public void setInOctets(long inOctets) {
		this.inOctets = inOctets;
	}

	public long getInUCastPkts() {
		return inUCastPkts;
	}

	public void setInUCastPkts(long inUCastPkts) {
		this.inUCastPkts = inUCastPkts;
	}

	public long getInNUcastPkts() {
		return inNUcastPkts;
	}

	public void setInNUcastPkts(long inNUcastPkts) {
		this.inNUcastPkts = inNUcastPkts;
	}

	public long getInDiscards() {
		return inDiscards;
	}

	public void setInDiscards(long inDiscards) {
		this.inDiscards = inDiscards;
	}

	public long getInErrors() {
		return inErrors;
	}

	public void setInErrors(long inErrors) {
		this.inErrors = inErrors;
	}

	public long getInUnknownProtos() {
		return inUnknownProtos;
	}

	public void setInUnknownProtos(long inUnknownProtos) {
		this.inUnknownProtos = inUnknownProtos;
	}

	public long getOutOctets() {
		return outOctets;
	}

	public void setOutOctets(long outOctets) {
		this.outOctets = outOctets;
	}

	public long getOutUcastPkts() {
		return outUcastPkts;
	}

	public void setOutUcastPkts(long outUcastPkts) {
		this.outUcastPkts = outUcastPkts;
	}

	public long getOutNUcastPkts() {
		return outNUcastPkts;
	}

	public void setOutNUcastPkts(long outNUcastPkts) {
		this.outNUcastPkts = outNUcastPkts;
	}

	public long getOutDiscards() {
		return outDiscards;
	}

	public void setOutDiscards(long outDiscards) {
		this.outDiscards = outDiscards;
	}

	public long getOutErrors() {
		return outErrors;
	}

	public void setOutErrors(long outErrors) {
		this.outErrors = outErrors;
	}

	public long getOutQLen() {
		return outQLen;
	}

	public void setOutQLen(long outQLen) {
		this.outQLen = outQLen;
	}

	public String getOutSpecific() {
		return outSpecific;
	}

	public void setOutSpecific(String outSpecific) {
		this.outSpecific = outSpecific;
	}

	@Override
	public String toString() {
		return "Interface{" +
				"index=" + index +
				", descr='" + descr + '\'' +
				", speed=" + speed +
				", physAddress='" + physAddress + '\'' +
				'}';
	}
}
