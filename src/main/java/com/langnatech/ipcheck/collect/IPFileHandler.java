package com.langnatech.ipcheck.collect;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.QueryRunner;

import com.google.common.hash.BloomFilter;
import com.langnatech.ipcheck.holder.DataSourceHolder;

public class IPFileHandler {
	private final static int BATCH_SIZE = 1000;
	private static IPFileHandler ipFileHandler = null;
	private List<Object[]> ipList = null;
	private IPFileHandler() {

	}

	public static IPFileHandler getInstance() throws Exception {
		if (ipFileHandler == null) {
			ipFileHandler = new IPFileHandler();
			ipFileHandler.createTempTable();
		}
		return ipFileHandler;
	}

	private void createTempTable() throws Exception {
		QueryRunner run = new QueryRunner(DataSourceHolder.getDatasource());
		String sql = "DROP TABLE IF EXISTS IP_TMP_CHECK";
		run.update(sql);
		sql = "CREATE TABLE IP_TMP_CHECK (CHECK_IP VARCHAR(50),CHECK_DEV VARCHAR(20),CHECK_CITY VARCHAR(20))";
		run.update(sql);
	}

	private void insertAddress(Object[] params) throws Exception {
		if (ipList == null) {
			ipList = new ArrayList<Object[]>();
		}
		ipList.add(params);
		if (ipList.size() != 0 && ipList.size() % BATCH_SIZE == 0) {
			this.flush();
			this.ipList.clear();
		}
	}

	public void flush() throws Exception {
		QueryRunner run = new QueryRunner(DataSourceHolder.getDatasource());
		String sql = "INSERT INTO IP_TMP_CHECK (CHECK_IP,CHECK_DEV,CHECK_CITY)VALUES(?,?,?)";
		Object[][] ary = new Object[ipList.size()][6];
		for (int i = 0; i < ipList.size(); i++) {
			ary[i] = ipList.get(i);
		}
		run.batch(sql, ary);
		ipList.clear();
	}

	public void readFileToDB(File file, String ip, String city, BloomFilter<CharSequence> bloomFilter)
			throws Exception {
		int bufSize = 100;
		@SuppressWarnings("resource")
		FileChannel fileChannel = new RandomAccessFile(file, "r").getChannel();
		ByteBuffer rBuffer = ByteBuffer.allocate(bufSize);
		try {
			readFileByLine(bufSize, fileChannel, rBuffer, ip, city, bloomFilter);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			fileChannel.close();
		}
	}

	private void readFileByLine(int bufSize, FileChannel fileChannel, ByteBuffer rBuffer, String checkIp,
			String checkCity, BloomFilter<CharSequence> bloomFilter) throws Exception {
		String enterStr = "\n";
		byte[] bs = new byte[bufSize];
		StringBuffer strBuf = new StringBuffer("");
		while (fileChannel.read(rBuffer) != -1) {
			int rSize = rBuffer.position();
			rBuffer.rewind();
			rBuffer.get(bs);
			rBuffer.clear();
			String tempString = new String(bs, 0, rSize);
			int fromIndex = 0;
			int endIndex = 0;
			while ((endIndex = tempString.indexOf(enterStr, fromIndex)) != -1) {
				String line = tempString.substring(fromIndex, endIndex);
				line = new String(strBuf.toString() + line).trim();
				if (!line.endsWith("127.0.0.0")) {
					if (!bloomFilter.mightContain(line)) {
						bloomFilter.put(line);
						this.insertAddress(new Object[] { line, checkIp, checkCity });
					}
				}
				strBuf.delete(0, strBuf.length());
				fromIndex = endIndex + 1;
			}
			if (rSize > tempString.length()) {
				strBuf.append(tempString.substring(fromIndex, tempString.length()));
			} else {
				strBuf.append(tempString.substring(fromIndex, rSize));
			}
		}
	}
}
