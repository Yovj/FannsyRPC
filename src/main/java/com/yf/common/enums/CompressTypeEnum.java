package com.yf.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum CompressTypeEnum {
    GZIP((byte)0X01,"gzip");

    private final byte code;
    private final String name;

    public static String getName(byte code){
        for (CompressTypeEnum value : CompressTypeEnum.values()) {
            if (value.getCode() == code){
                return value.name;
            }
        }
        return null;
    }
}
