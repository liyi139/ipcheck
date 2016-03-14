insert into ip_check_log
select 
    IFNULL(T.ADDRESS_IP, T.CHECK_IP) AS IP_ADDRESS,
    T.SUBNET_ID,
    T.CITY_ID,
    T.POOL_ID,
    T.ADDRESS_STATUS ASIP_STATUS,
	case when (T.ADDRESS_STATUS = 2 and T.CHECK_IP IS null) then 2
		 when (T.CHECK_IP IS NOT NULL and T.ADDRESS_IP IS NULL) then 3
		 when (T.CITY_ID != T.CHECK_CITY AND T.CITY_ID IS NOT NULL AND T.CHECK_CITY IS NOT NULL) then 1
	else -1 end as WARN_TYPE,
    T.CHECK_CITY,
    T.CHECK_DEV,
    sysdate()
from
    (select * from ip_tmp_full_address T1
    left outer join ip_tmp_check T2 ON T1.ADDRESS_IP = T2.CHECK_IP 
	union 
	select * from ip_tmp_full_address T1
    right outer join ip_tmp_check T2 ON T1.ADDRESS_IP = T2.CHECK_IP) T
where
    T.ADDRESS_TYPE = 2 and (
		(T.ADDRESS_STATUS = 2 and T.CHECK_IP IS null)
        or (T.CHECK_IP IS NOT NULL and T.ADDRESS_IP IS NULL)
        or (T.CITY_ID != T.CHECK_CITY AND T.CITY_ID IS NOT NULL AND T.CHECK_CITY IS NOT NULL)
	)
