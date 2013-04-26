package com.stackmob.customcode.dev.test.server.sdk.data;

import com.stackmob.sdkapi.SMBoolean;
import com.stackmob.sdkapi.SMList;

public class SMListTestUtils {
    public static SMList createNested(Integer depth) {
        SMList starting = new SMList<SMBoolean>(new SMBoolean(true));
        for(int i = 0; i < depth; i++) {
            starting = new SMList<SMList<SMBoolean>>(starting);
        }
        return starting;
    }
}
