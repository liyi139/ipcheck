package com.langnatech.ipcheck.pool;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.langnatech.ipcheck.bean.SubNetBean;
import com.langnatech.ipcheck.enums.IPAddressTypeEnum;
import com.langnatech.ipcheck.enums.IPStatusEnum;
import com.langnatech.ipcheck.enums.SubNetUseStatusEnum;
import com.langnatech.ipcheck.holder.DataSourceHolder;
import com.langnatech.util.IpUtils;

public class IpPoolService {
	private Logger logger = Logger.getLogger(IpPoolService.class);
	private List<Object[]> ipList = null;
	private final static int BATCH_SIZE = 1000;

	/**
	 * 初始化全量IP地址临时表
	 */
	public void initFullAddressTable() throws Exception {
		logger.info("Generate the full amount of address temporary table");
		logger.info("Initialization temporary table");
		this.createTempTableForFullIP();
		List<SubNetBean> subnetList = this.getAllLeafSubnet();
		Map<String, Object> poolMap = this.getIPPoolMap();
		logger.info("Insert address data into temporary table");
		int ipCount = 0;
		for (SubNetBean subNetBean : subnetList) {
			String poolId = subNetBean.getPoolId();
			if (StringUtils.isNotEmpty(poolId)) {
				Integer assignType = (Integer) poolMap.get(poolId);
				if (assignType != null && assignType == 1) {
					ipCount += this.insertAddressFromAddressRes(subNetBean);
				} else {
					ipCount += this.insertAddressFromSubnet(subNetBean);
				}
			}
		}
		this.insertFlush();
		logger.info("Insert success! Number of addresses:" + ipCount);
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
		String sql = "INSERT INTO IP_TMP_FULL_ADDRESS(ADDRESS_IP,SUBNET_ID,POOL_ID,CITY_ID,ADDRESS_STATUS,ADDRESS_TYPE)VALUES(?,?,?,?,?,?)";
		Object[][] ary = new Object[ipList.size()][6];
		for (int i = 0; i < ipList.size(); i++) {
			ary[i] = ipList.get(i);
		}
		run.batch(sql, ary);
	}

	/**
	 * 获取IP地址池中所有的叶子节点的地址段
	 * 
	 * @return
	 * @throws Exception
	 */
	public List<SubNetBean> getAllLeafSubnet() throws Exception {
		QueryRunner queryRunner = new QueryRunner(DataSourceHolder.getDatasource());
		String sql = "SELECT A.SUBNET_ID subnetId,A.CITY_ID cityId,A.BEGIN_IP beginIp,A.END_IP endIp,"
				+ "A.NETMASK netmask,A.PLAN_STATUS planStatus,A.USE_STATUS useStatus,A.POOL_ID poolId "
				+ "FROM  IP_SUBNET_RES A LEFT OUTER JOIN IP_SUBNET_RES B ON A.SUBNET_ID = B.SUBNET_PID "
				+ " WHERE B.SUBNET_ID IS NULL";
		List<SubNetBean> subnetList = queryRunner.query(sql, new BeanListHandler<SubNetBean>(SubNetBean.class));
		logger.debug("get subnet count:" + (subnetList == null ? 0 : subnetList.size()));
		return subnetList;
	}

	/**
	 * 创建全量IP地址临时表
	 * 
	 * @throws Exception
	 */
	public void createTempTableForFullIP() throws Exception {
		QueryRunner run = new QueryRunner(DataSourceHolder.getDatasource());
		String sql = "DROP TABLE IF EXISTS IP_TMP_FULL_ADDRESS";
		run.update(sql);
		sql = "CREATE TABLE IP_TMP_FULL_ADDRESS (" + "ADDRESS_IP VARCHAR(50)," + "SUBNET_ID VARCHAR(20),"
				+ "POOL_ID VARCHAR(20)," + "CITY_ID VARCHAR(20)," + "ADDRESS_STATUS SMALLINT,"
				+ "ADDRESS_TYPE SMALLINT);";
		run.update(sql);
	}

	/**
	 * 根据网段对应的IP地址表,读取IP列表插入临时表
	 */
	private int insertAddressFromAddressRes(SubNetBean subNetBean) throws Exception {
		final QueryRunner run = new QueryRunner(DataSourceHolder.getDatasource());
		String sql = "SELECT A.ADDRESS_IP,A.SUBNET_ID,B.POOL_ID,B.CITY_ID,A.ADDRESS_STATUS,A.ADDRESS_TYPE FROM IP_ADDRESS_RES A,IP_SUBNET_RES B WHERE A.SUBNET_ID=B.SUBNET_ID AND A.SUBNET_ID=?";
		final IpPoolService self = this;
		return run.query(sql, new ResultSetHandler<Integer>() {
			public Integer handle(ResultSet rs) throws SQLException {
				int count = 0;
				while(rs.next()){
					Object[] ary = new Object[6];
					for (int i = 0; i < 6; i++) {
						ary[i] = rs.getObject(i+1);
					}
					try {
						self.insertAddress(ary);
					} catch (Exception e) {
						throw new SQLException(e.getMessage());
					}
					count++;
				}
				return count;
			}
		}, subNetBean.getSubnetId());
	}

	/**
	 * 根据子网段信息,生成IP地址列表插入临时表
	 */
	private int insertAddressFromSubnet(SubNetBean subNetBean) throws Exception {
		long begin = IpUtils.getDecByIp(subNetBean.getBeginIp()) - 1;
		long end = IpUtils.getDecByIp(subNetBean.getEndIp()) + 1;
		for (int i = 0; i <= (end - begin); i++) {
			int status = IPStatusEnum.AVAILABLE.getCode();
			int type = IPAddressTypeEnum.HOST.getCode();
			if (subNetBean.getUseStatus() == SubNetUseStatusEnum.USED.getCode()) {
				status = IPStatusEnum.USED.getCode();
			}
			if (subNetBean.getUseStatus() == SubNetUseStatusEnum.RESERVE.getCode()) {
				status = IPStatusEnum.RESERVE.getCode();
			}
			if (subNetBean.getUseStatus() == SubNetUseStatusEnum.FROZEN.getCode()) {
				status = IPStatusEnum.FROZEN.getCode();
			}
			if (i == 0) {
				type = IPAddressTypeEnum.NET.getCode();
			}
			if (i == (end - begin)) {
				type = IPAddressTypeEnum.BROADCAST.getCode();
			}
			Object[] ary = new Object[6];
			ary[0] = IpUtils.getIpByDec(begin + i);
			ary[1] = subNetBean.getSubnetId();
			ary[2] = subNetBean.getPoolId();
			ary[3] = subNetBean.getCityId();
			ary[4] = status;
			ary[5] = type;
			this.insertAddress(ary);
		}
		return Long.valueOf(end - begin + 1).intValue();
	}

	/**
	 * 查询所有的地址池信息
	 * 
	 * @throws SQLException
	 */
	private Map<String, Object> getIPPoolMap() throws Exception {
		QueryRunner run = new QueryRunner(DataSourceHolder.getDatasource());
		String sql = "SELECT POOL_ID,ASSIGN_TYPE FROM IP_POOL_CONF";
		return run.query(sql, new ResultSetHandler<Map<String, Object>>() {
			public Map<String, Object> handle(ResultSet rs) throws SQLException {
				Map<String, Object> map = new HashMap<String, Object>();
				while(rs.next()){
					map.put(rs.getString("POOL_ID"), rs.getInt("ASSIGN_TYPE"));
				}
				return map;
			}

		});
	}

}
