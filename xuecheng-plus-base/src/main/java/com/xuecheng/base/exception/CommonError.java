package com.xuecheng.base.exception;

/**
 * @description: 通用錯誤信息
 * @author: Ian Wang
 * @date: 2023/11/20 下午 04:31
 * @version: 1.0
 */
public enum CommonError {
    UNKOWN_ERROR("執行過程異常，請重試。"),
    PARAMS_ERROR("非法參數"),
    OBJECT_NULL("對象為空"),
    QUERY_NULL("查詢結果為空"),
    REQUEST_NULL("請求參數為空");

    private String errMessage;

    public String getErrMessage() {
        return errMessage;
    }

    private CommonError( String errMessage) {
        this.errMessage = errMessage;
    }
}
