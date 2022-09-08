package com.yf.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
public enum RpcResponseCodeEnum {
    SUCCESS(200,"Remote call is success"),
    FAIL(500,"Remote call is failed"),

    FAIL_TOKEN_ILLEGAL(300,"service token is illegal");

    private final int code;
    private final String msg;

}
