package com.sample.system.vault.service.domain.response.base;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.logging.log4j.ThreadContext;

import java.util.Date;

@ToString
@Getter
@Setter
public class BaseResponse<T> {
    private static  final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    private Boolean success;
    private T data;
    private  String trackingId;
//    private String doTime;
    private Long doTimeStamp;
    private ErrorDetail errorDetail;

    public BaseResponse() {
    }

    public BaseResponse(Boolean success) {
        this.success = success;
        this.trackingId = ThreadContext.get("uuid");
        Date date = new Date();
        this.doTimeStamp = date.getTime();
    }

    public BaseResponse(Boolean success, T data) {
        this.success = success;
        this.data = data;
        this.trackingId = ThreadContext.get("uuid");
        Date date = new Date();
        this.doTimeStamp = date.getTime();
    }

    public BaseResponse(Boolean success, ErrorDetail errorDetail){
        this.success = success;
        this.data = data;
        this.trackingId = ThreadContext.get("uuid");
        Date date = new Date();
        this.doTimeStamp = date.getTime();
        this.errorDetail = errorDetail;
    }
}
