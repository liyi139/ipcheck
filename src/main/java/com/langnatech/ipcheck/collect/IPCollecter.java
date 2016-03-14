package com.langnatech.ipcheck.collect;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import com.langnatech.ipcheck.bean.CheckConfBean;
import com.langnatech.ipcheck.bean.DeviceConfBean;
import com.langnatech.ipcheck.holder.PropertiesHolder;
import com.langnatech.util.IpUtils;
import com.langnatech.util.XmlConvertUtil;

public class IPCollecter {
	private static final Logger logger = Logger.getLogger(IPCollecter.class);

	private static List<DeviceConfBean> getAllCollectDevConf() {
		try {
			String file = IPCollecter.class.getClassLoader().getResource("DEVICE_CHECK_CONF.xml").getFile();
			FileReader fileReader = new FileReader(file);
			CheckConfBean checkConfBean = XmlConvertUtil.fromXml(fileReader, CheckConfBean.class);
			return checkConfBean.getDeviceConfList();
		} catch (FileNotFoundException e) {
			logger.error("Device configuration file [DEVICE_CHECK_CONF.xml] not found!");
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
		try {
			List<DeviceConfBean> list = getAllCollectDevConf();
			if (list == null)
				return;
			for (DeviceConfBean deviceConf : list) {
				executor.execute(new DeviceCollectThread(deviceConf));
			}
		} catch (Exception e) {
			executor.shutdown();
			logger.error("Execute Collect Failure!" + e.getMessage());
			e.printStackTrace();
		}
		readIPFileToDB();
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

	/**
	 * 读取所有采集文件中的IP地址,插入数据库临时表中
	 * 
	 * @throws Exception
	 */
	private static void readIPFileToDB() {
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
			logger.info("Read IP file and save to DB! IP File count:" + fileAry.length);
			for (File file : fileAry) {
				String[] nameAry = file.getName().split("@");
				String ip = "", city = "";
				if (nameAry.length >= 2) {
					ip = IpUtils.getIpByDec(Long.parseLong(nameAry[0]));
					city = nameAry[1];
				} else {
				}
				try {
					IPFileHandler.getInstance().readFileToDB(file, ip, city);
				} catch (Exception e) {
					logger.error("Read ip File [" + file.getPath() + "] to DB Failure!" + e.getMessage());
					e.printStackTrace();
				}
			}
			backupCollectFile(fileAry);
		}
	}

	private static void backupCollectFile(File[] files) {
		if (ArrayUtils.isEmpty(files))
			return;
		File firstFile = files[0];
		Date date = new Date(firstFile.lastModified());
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
		File zipFile = new File(firstFile.getParent(), dateFormat.format(date) + ".zip");
		logger.info("Backup collect files !" + zipFile.getPath());
		InputStream input = null;
		try {
			ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(zipFile));
			for (File file : files) {
				input = new FileInputStream(file);
				zipOut.putNextEntry(new ZipEntry(firstFile.getParent() + File.separator + file.getName()));
				int temp = 0;
				while ((temp = input.read()) != -1) {
					zipOut.write(temp);
				}
				input.close();
				file.delete();
			}
			zipOut.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
