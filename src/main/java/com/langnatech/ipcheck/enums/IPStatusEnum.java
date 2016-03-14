package com.langnatech.ipcheck.enums;

public enum IPStatusEnum 
{
    AVAILABLE(1, "空闲"), USED(2, "已使用"), RESERVE(3, "已预留"), FROZEN(4, "已冻结"), ILLEGAL( -1, "");
    private Integer code;

    private String desc;

    private IPStatusEnum(Integer code, String desc)
    {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode()
    {
        return code;
    }

    public void setCode(int code)
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
