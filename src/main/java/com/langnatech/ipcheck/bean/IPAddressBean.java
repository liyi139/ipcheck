package com.langnatech.ipcheck.bean;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class IPAddressBean {

	private String addressIp;

	private String subnetId;
	
	private String poolId;
	
	private String cityId;
	
	public String getCityId() {
		return cityId;
	}


	public void setCityId(String cityId) {
		this.cityId = cityId;
	}

	private Integer addressType;

	public String getPoolId() {
		return poolId;
	}


	public void setPoolId(String poolId) {
		this.poolId = poolId;
	}


	public Integer getAddressType() {
		return addressType;
	}


	public void setAddressType(Integer addressType) {
		this.addressType = addressType;
	}

	private Integer addressStatus;


	public IPAddressBean() {
		super();
	}


	public String getAddressIp() {
		return addressIp;
	}

	public void setAddressIp(String addressIp) {
		this.addressIp = addressIp == null ? null : addressIp.trim();
	}

	public String getSubnetId() {
		return subnetId;
	}

	public void setSubnetId(String subnetId) {
		this.subnetId = subnetId == null ? null : subnetId.trim();
	}

	public Integer getAddressStatus() {
		return addressStatus;
	}

	public void setAddressStatus(Integer addressStatus) {
		this.addressStatus = addressStatus;
	}

	public String toString() {
		return ToStringBuilder.reflectionToString(this,ToStringStyle.SIMPLE_STYLE);
	}
	

}