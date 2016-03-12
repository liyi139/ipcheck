package com.langnatech.device;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.FIELD)
public class DeviceConfBean implements Serializable {
	private static final long serialVersionUID = 3708413968918400605L;
	@XmlAttribute(name = "isCheck")
	private boolean isCheck = false;
	@XmlAttribute(name = "brand")
	private String brand;
	@XmlAttribute(name = "deviceType")
	private String deviceType;
	@XmlAttribute(name = "model")
	private String model;
	@XmlAttribute(name = "city")
	private String city;
	@XmlAttribute(name = "cityname")
	private String cityname;
	@XmlAttribute(name = "ip")
	private String ip;
	@XmlAttribute(name = "port")
	private Integer port;
	@XmlAttribute(name = "community")
	private String community;
	@XmlAttribute(name = "oid")
	private String oid;
	public boolean isCheck() {
		return isCheck;
	}
	public void setCheck(boolean isCheck) {
		this.isCheck = isCheck;
	}
	public String getBrand() {
		return brand;
	}
	public void setBrand(String brand) {
		this.brand = brand;
	}
	public String getDeviceType() {
		return deviceType;
	}
	public void setDeviceType(String deviceType) {
		this.deviceType = deviceType;
	}
	public String getModel() {
		return model;
	}
	public void setModel(String model) {
		this.model = model;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public String getCityname() {
		return cityname;
	}
	public void setCityname(String cityname) {
		this.cityname = cityname;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public Integer getPort() {
		return port;
	}
	public void setPort(Integer port) {
		this.port = port;
	}
	public String getCommunity() {
		return community;
	}
	public void setCommunity(String community) {
		this.community = community;
	}
	public String getOid() {
		return oid;
	}
	public void setOid(String oid) {
		this.oid = oid;
	}
	
}
