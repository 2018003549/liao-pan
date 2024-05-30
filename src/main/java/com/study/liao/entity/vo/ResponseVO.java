package com.study.liao.entity.vo;

import lombok.Data;

@Data
public class  ResponseVO<T> {
    private String status;
    private Integer code;
    private String info;
    private T data;
    public void fail(Integer code,String msg){
        this.code=code;
        this.info=msg;
    }
}
