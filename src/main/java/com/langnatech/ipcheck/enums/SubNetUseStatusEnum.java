package com.langnatech.ipcheck.enums;

public enum SubNetUseStatusEnum {
	AVAILABLE(1, "空闲"), USED(2, "已使用"), RESERVE(3, "已预留"), FROZEN(4, "已冻结"), ILLEGAL(-1, "");
	private int code;

	private String desc;

	private SubNetUseStatusEnum(Integer code, String desc) {
		this.code = code;
		this.desc = desc;
	}

	public Integer getCode() {
		return code;
	}

	public void setCode(Integer code) {
		this.code = code;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public static String getNameByValue(Integer code) {
		SubNetUseStatusEnum[] statusEnums = SubNetUseStatusEnum.values();
		for (SubNetUseStatusEnum o : statusEnums) {
			if (o.code == code) {
				return o.desc;
			}
		}
		return null;
	}
}
