package com.longjunwang.moni.entity;

import lombok.Data;

@Data
public class Expense {
    /**
     * 记录号
     */
    private String id;
    /**
     * 记录日期
     */
    private String date;
    /**
     * 记录金额
     */
    private String amount;
    /**
     * 记录类型
     */
    private String type;
    /**
     * 备注
     */
    private String remark;
    /**
     * 子备注
     */
    private String subRemark;

    private String createdAt;

    private String updatedAt;
}
