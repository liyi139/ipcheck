package com.langnatech.ipcheck.bean;

public class SubNetBean {
	private String subnetId;
	private String beginIp;
	private Long beginIpDecimal;
	private String endIp;
	private Long endIpDecimal;
	private Short maskBits;
	private String netmask;
	private Integer ipCount;
	private String subnetPid;
	private Short isIpv6;
	private Integer planStatus;
	private Integer useStatus;
	private String poolId;
	private String cityId;

	public SubNetBean() {

	}

	public String getSubnetId() {
		return subnetId;
	}

	public void setSubnetId(String subnetId) {
		this.subnetId = subnetId == null ? null : subnetId.trim();
	}

	public String getBeginIp() {
		return beginIp;
	}

	public void setBeginIp(String beginIp) {
		this.beginIp = beginIp == null ? null : beginIp.trim();
	}

	public Long getBeginIpDecimal() {
		return beginIpDecimal;
	}

	public void setBeginIpDecimal(Long beginIpDecimal) {
		this.beginIpDecimal = beginIpDecimal;
	}

	public String getEndIp() {
		return endIp;
	}

	public void setEndIp(String endIp) {
		this.endIp = endIp == null ? null : endIp.trim();
	}

	public Long getEndIpDecimal() {
		return endIpDecimal;
	}

	public void setEndIpDecimal(Long endIpDecimal) {
		this.endIpDecimal = endIpDecimal;
	}

	public Short getMaskBits() {
		return maskBits;
	}

	public void setMaskBits(Short maskBits) {
		this.maskBits = maskBits;
	}

	public String getNetmask() {
		return netmask;
	}

	public void setNetmask(String netmask) {
		this.netmask = netmask == null ? null : netmask.trim();
	}

	public Integer getIpCount() {
		return ipCount;
	}

	public void setIpCount(Integer ipCount) {
		this.ipCount = ipCount;
	}

	public String getSubnetPid() {
		return subnetPid;
	}

	public void setSubnetPid(String subnetPid) {
		this.subnetPid = subnetPid == null ? null : subnetPid.trim();
	}

	public Short getIsIpv6() {
		return isIpv6;
	}

	public void setIsIpv6(Short isIpv6) {
		this.isIpv6 = isIpv6;
	}

	public Integer getPlanStatus() {
		return planStatus;
	}

	public void setPlanStatus(Integer planStatus) {
		this.planStatus = planStatus;
	}

	public Integer getUseStatus() {
		return useStatus;
	}

	public void setUseStatus(Integer useStatus) {
		this.useStatus = useStatus;
	}

	public String getPoolId() {
		return poolId;
	}

	public void setPoolId(String poolId) {
		this.poolId = poolId == null ? null : poolId.trim();
	}

	public String getCityId() {
		return cityId;
	}

	public void setCityId(String cityId) {
		this.cityId = cityId == null ? null : cityId.trim();
	}

}