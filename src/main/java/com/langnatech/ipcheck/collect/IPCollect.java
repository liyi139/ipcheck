package com.langnatech.ipcheck.collect;

import java.io.FileReader;
import java.util.List;

import com.langnatech.ipcheck.bean.CheckConfBean;
import com.langnatech.ipcheck.bean.DeviceConfBean;
import com.langnatech.ipcheck.snmp.SnmpWalk;
import com.langnatech.util.XmlConvertUtil;

public class IPCollect {
	public List<DeviceConfBean> getAllCollectDevConf() throws Exception {
		String file = IPCollect.class.getClassLoader().getResource("DEVICE_CHECK_CONF.xml").getFile();
		FileReader fileReader = new FileReader(file);
		CheckConfBean checkConfBean = XmlConvertUtil.fromXml(fileReader, CheckConfBean.class);
		return checkConfBean.getDeviceConfList();
	}

	public void collectToDB() throws Exception{
		List<DeviceConfBean> list=this.getAllCollectDevConf();
		for (DeviceConfBean deviceConf: list) {
			SnmpWalk.snmpWalk(deviceConf.getIp(), deviceConf.getCommunity(), deviceConf.getOid());
		}
	}
}
