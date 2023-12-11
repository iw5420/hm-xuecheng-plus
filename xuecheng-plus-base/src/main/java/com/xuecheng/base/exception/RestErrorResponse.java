package com.xuecheng.base.exception;

/**
 * @description: 錯誤響應參數包裝
 * @author: Ian Wang
 * @date: 2023/11/20 下午 04:26
 * @version: 1.0
 */
public class RestErrorResponse {
    private String errMessage;

    public RestErrorResponse(String errMessage){
        this.errMessage= errMessage;
    }

    public String getErrMessage() {
        return errMessage;
    }
}
