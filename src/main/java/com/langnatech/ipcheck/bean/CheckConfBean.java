package com.langnatech.ipcheck.bean;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "DEVICE_LIST")
@XmlAccessorType(XmlAccessType.FIELD)
public class CheckConfBean extends ArrayList<DeviceConfBean> {
	private static final long serialVersionUID = -5053368910723391116L;

	@XmlElement(name = "DEVICE_CONF")
	public List<DeviceConfBean> getDeviceConfList() {
		return this;
	}
}
