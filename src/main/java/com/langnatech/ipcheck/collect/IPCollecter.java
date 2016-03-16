package com.langnatech.ipcheck.collect;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import com.langnatech.ipcheck.bean.CheckConfBean;
import com.langnatech.ipcheck.bean.DeviceConfBean;
import com.langnatech.ipcheck.holder.PropertiesHolder;
import com.langnatech.util.XmlConvertUtil;

public class IPCollecter {
	private static final Logger logger = Logger.getLogger(IPCollecter.class);

	private static List<DeviceConfBean> getAllCollectDevConf() {
		try {
			CheckConfBean checkConfBean = XmlConvertUtil
					.fromXml(ClassLoader.getSystemResourceAsStream("DEVICE_CHECK_CONF.xml"), CheckConfBean.class);
			return checkConfBean.getDeviceConfList();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Parse configuration file [DEVICE_CHECK_CONF.xml] exception!" + e.getMessage());
		}
		return null;
	}

	public static void collect() throws Exception {
		clearFile();
		ExecutorService executor = Executors
				.newFixedThreadPool(PropertiesHolder.getConfig().getInt("collect.maxThreads"));
		// 采集所有设备路由表中的IP地址,保存IP地址至文件
		List<Callable<Boolean>> callList = new ArrayList<Callable<Boolean>>();
		try {
			List<DeviceConfBean> list = getAllCollectDevConf();
			if (list == null)
				return;
			for (DeviceConfBean deviceConf : list) {
				callList.add(new DeviceCollectThread(deviceConf));
			}
			executor.invokeAll(callList);
			executor.shutdown();
		} catch (Exception e) {
			logger.error("Execute Collect Failure!" + e.getMessage());
			e.printStackTrace();
		}
	}

	private static void clearFile() {
		logger.info("Clear history files!");
		String saveDir = PropertiesHolder.getConfig().getString("collect.saveDir");
		File saveFile = new File(saveDir);
		if (saveFile.isDirectory()) {
			File[] fileAry = saveFile.listFiles((new FilenameFilter() {
				public boolean accept(File dir, String name) {
					if (name.endsWith(".ip")) {
						return true;
					} else {
						return false;
					}
				}
			}));
			for (File file : fileAry) {
				file.delete();
			}
		}
	}
}
