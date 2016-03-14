package com.langnatech.ipcheck.enums;

public enum SubNetPlanStatusEnum 
{
    WAIT_PLAN(1, "待规划"), PLANNING(2, "规划中"), PLANNED(3, "已规划"), ILLEGAL( -1, "");
    private Integer code;

    private String desc;

    private SubNetPlanStatusEnum(Integer code, String desc)
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
