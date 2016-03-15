package com.langnatech.ipcheck.collect;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import com.langnatech.ipcheck.holder.PropertiesHolder;

public class CollectFileToDB {
	private static final Logger logger = Logger.getLogger(CollectFileToDB.class);

	/**
	 * 读取所有采集文件中的IP地址,插入数据库临时表中
	 * 
	 * @throws Exception
	 */
	public static void readFileToDB() {
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
			BloomFilter<CharSequence> bloomFilter = BloomFilter.create(Funnels.stringFunnel(Charset.defaultCharset()),
					10000000, 0.001F);
			for (File file : fileAry) {
				String[] nameAry = file.getName().split("@");
				String ip = "", city = "";
				if (nameAry.length >= 2) {
					ip = nameAry[0].replaceAll("-", ".");
					city = nameAry[1];
				} else {
				}
				try {
					IPFileHandler.getInstance().readFileToDB(file, ip, city, bloomFilter);
					IPFileHandler.getInstance().flush();
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
