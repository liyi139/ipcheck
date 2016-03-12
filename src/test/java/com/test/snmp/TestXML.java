package com.test.snmp;

import java.io.FileReader;

import org.junit.Test;

import com.langnatech.ipcheck.bean.CheckConfBean;
import com.langnatech.util.XmlConvertUtil;

public class TestXML {
  @Test
  public void testSetPDU() throws Exception {
	  String file=this.getClass().getClassLoader().getResource("DEVICE_CHECK_CONF.xml").getFile();
	  FileReader fileReader=new FileReader(file);
	  XmlConvertUtil.fromXml(fileReader, CheckConfBean.class);
  }
}
