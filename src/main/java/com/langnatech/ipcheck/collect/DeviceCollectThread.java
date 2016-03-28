package com.langnatech.ipcheck.collect;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.Callable;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import com.langnatech.ipcheck.bean.DeviceConfBean;
import com.langnatech.ipcheck.holder.PropertiesHolder;
import com.langnatech.ipcheck.snmp.SnmpWalk;
import com.langnatech.ipcheck.snmp.SnmpWalkCallback;

public class DeviceCollectThread implements Callable<Boolean> {
	private DeviceConfBean deviceConf;
	private static final Logger logger = Logger.getLogger(DeviceCollectThread.class);

	public DeviceCollectThread(DeviceConfBean deviceConf) {
		this.deviceConf = deviceConf;
	}

	public Boolean call() {
		logger.info(deviceConf.getIp() + " Collect ip start! City is: " + deviceConf.getCityname() + ", DeviceModel:"
				+ deviceConf.getModel());
		final BloomFilter<CharSequence> bloomFilter = BloomFilter.create(Funnels.stringFunnel(Charset.defaultCharset()),
				2000000, 0.01F);
		String saveDir = PropertiesHolder.getConfig().getString("collect.saveDir");
		String fileName = deviceConf.getIp().replaceAll("\\.", "-") + "@" + deviceConf.getCity() + ".tmp";
		File saveFile = new File(saveDir, fileName);
		logger.info(deviceConf.getIp() + " Save file to " + saveFile.getPath());
		FileWriter fileWriter = null;
		try {
			fileWriter = new FileWriter(saveFile);
			final FileWriter fileWriter2 = fileWriter;
			SnmpWalk.snmpWalk(deviceConf.getIp(), deviceConf.getCommunity(), deviceConf.getOid(),
					new SnmpWalkCallback() {
						public void handleIP(String oid, String variable) throws Exception {
							String[] ary = oid.split("\\.");
							ary = ArrayUtils.subarray(ary, ary.length - 4, ary.length);
							String ip = StringUtils.join(ary, ".");
							if (!bloomFilter.mightContain(ip)) {
								bloomFilter.put(ip);
								fileWriter2.write(ip);
								fileWriter2.write("\n");
							}
						}
					});
			fileWriter.flush();
			fileWriter.close();
			saveFile.renameTo(new File(saveFile.getParent(), saveFile.getName().replaceAll("\\.tmp", ".ip")));
			return true;
		} catch (IOException e) {
			logger.error(deviceConf.getIp() + " Collect error! " + e.getMessage());
			e.printStackTrace();
			return false;
		} finally {
			logger.info(deviceConf.getIp() + " Collect ip end!  City is: " + deviceConf.getCityname() + ", DeviceModel:"
					+ deviceConf.getModel());
			try {
				fileWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}