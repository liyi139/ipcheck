package com.langnatech.ipcheck.snmp;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import com.langnatech.ipcheck.holder.DataSourceHolder;

public class CollectToDB {
	private String collectFile = "/home/liyi/Downloads/walk_log.log";
	private List<Object[]> ipList = null;
	private final static int BATCH_SIZE = 1000;

	public CollectToDB() throws Exception {
		this.createTempTable();
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
			this.insertFlush();
			this.ipList.clear();
		}
	}

	private void insertFlush() throws Exception {
		QueryRunner run = new QueryRunner(DataSourceHolder.getDatasource());
		String sql = "INSERT INTO IP_TMP_CHECK (CHECK_IP,CHECK_DEV,CHECK_CITY)VALUES(?,?,?)";
		Object[][] ary = new Object[ipList.size()][6];
		for (int i = 0; i < ipList.size(); i++) {
			ary[i] = ipList.get(i);
		}
		run.batch(sql, ary);
	}

	public void collectToDBFromFile() throws Exception {
		int bufSize = 100;
		@SuppressWarnings("resource")
		FileChannel fileChannel = new RandomAccessFile(new File(collectFile), "r").getChannel();
		ByteBuffer rBuffer = ByteBuffer.allocate(bufSize);
		readFileByLine(bufSize, fileChannel, rBuffer);

	}

	private void readFileByLine(int bufSize, FileChannel fileChannel, ByteBuffer rBuffer) throws Exception {
		String enterStr = "\n";
		byte[] bs = new byte[bufSize];
		StringBuffer strBuf = new StringBuffer("");
		BloomFilter<CharSequence> bloomFilter = BloomFilter.create(Funnels.stringFunnel(Charset.defaultCharset()),
				10000000, 0.01F);
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
				line = new String(strBuf.toString() + line);
				if (!line.startsWith("=") && !line.startsWith("-") && !line.startsWith("[")
						&& !line.startsWith("SNMP")) {
					line = line.substring(0, line.indexOf("="));
					String[] ary = line.split("\\.");
					ary = ArrayUtils.subarray(ary, ary.length - 4, ary.length);
					String ip = StringUtils.join(ary, ".");
					if (!bloomFilter.mightContain(ip)) {
						bloomFilter.put(ip);
						this.insertAddress(new Object[] { StringUtils.join(ary, ".") ,"1","997"});
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

	public void writeFileByLine(FileChannel fcout, String line) {
		try {
			fcout.write(ByteBuffer.wrap(line.getBytes()), fcout.size());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws Exception {
		CollectToDB collectToDB = new CollectToDB();
		collectToDB.collectToDBFromFile();
	}
}
