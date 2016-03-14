package com.langnatech.ipcheck.collect;

import org.apache.commons.dbutils.QueryRunner;

import com.langnatech.ipcheck.holder.DataSourceHolder;

public class IPCheck {
	public static void main(String[] args) {
		try {
			CollectIP.collectToDB();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public int checkIP() throws Exception {
		QueryRunner run = new QueryRunner(DataSourceHolder.getDatasource());
		String sql = "TRUNCATE TABLE IP_CHECK_LOG";
		run.update(sql);
		sql = "INSERT INTO IP_CHECK_LOG  SELECT  " + "     IFNULL(T.ADDRESS_IP, T.CHECK_IP) AS IP_ADDRESS, "
				+ "     T.SUBNET_ID, " + "     T.CITY_ID, " + "     T.POOL_ID, "
				+ "     T.ADDRESS_STATUS AS IP_STATUS, "
				+ " 	CASE WHEN (T.ADDRESS_STATUS = 2 AND T.CHECK_IP IS NULL) THEN 2 "
				+ " 		 WHEN (T.CHECK_IP IS NOT NULL AND T.ADDRESS_IP IS NULL) THEN 3 "
				+ " 		 WHEN (T.CITY_ID != T.CHECK_CITY AND T.CITY_ID IS NOT NULL AND T.CHECK_CITY IS NOT NULL) THEN 1 "
				+ " 	ELSE -1 END AS WARN_TYPE, " + "     T.CHECK_CITY, " + "     T.CHECK_DEV, " + "     SYSDATE() "
				+ " FROM " + "     (SELECT * FROM IP_TMP_FULL_ADDRESS T1 "
				+ "     LEFT OUTER JOIN IP_TMP_CHECK T2 ON T1.ADDRESS_IP = T2.CHECK_IP  " + " 	UNION  "
				+ " 	SELECT * FROM ip_tmp_full_address T1 "
				+ "     RIGHT OUTER JOIN IP_TMP_CHECK T2 ON T1.ADDRESS_IP = T2.CHECK_IP) T " + " WHERE "
				+ " (T.ADDRESS_TYPE iS NULL OR T.ADDRESS_TYPE=2) and  " + " ( "
				+ " 		(T.ADDRESS_STATUS = 2 AND T.CHECK_IP IS NULL) "
				+ "         OR (T.CHECK_IP IS NOT NULL AND T.ADDRESS_IP IS NULL) "
				+ "         OR (T.CITY_ID != T.CHECK_CITY AND T.CITY_ID IS NOT NULL AND T.CHECK_CITY IS NOT NULL) "
				+ " 	) ";
		return run.update(sql);
	}
}
