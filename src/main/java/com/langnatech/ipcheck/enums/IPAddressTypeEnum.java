package com.langnatech.ipcheck.enums;

public enum IPAddressTypeEnum 
{
    NET (1, "网络地址"), HOST(2, "主机地址"), BROADCAST(3, "广播地址");
    private int code;

    private String desc;

    private IPAddressTypeEnum(Integer code, String desc)
    {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode()
    {
        return code;
    }

    public void setCode(Integer code)
    {
        this.code = code;
    }

    public String getDesc()
    {
        return desc;
    }

    public void setDesc(String desc)
    {
        this.desc = desc;
    }

}
