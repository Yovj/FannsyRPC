package com.yf.common.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum SerializationTypeEnum {
    KYRO((byte) 0x01,"kyro");

    private final byte code;
    private final String name;

    public static String getName(byte code){
        for (SerializationTypeEnum value : SerializationTypeEnum.values()) {
            if (value.getCode() == code){
                return value.name;
            }
        }
        return null;
    }

}
