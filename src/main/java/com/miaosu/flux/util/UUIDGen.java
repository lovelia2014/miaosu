package com.miaosu.flux.util;

import java.util.UUID;

/**
 * UUID
 * Created by angus on 15/6/15.
 */
public final class UUIDGen {
    public static String systemUuid(){
        return UUID.randomUUID().toString();
    }
}