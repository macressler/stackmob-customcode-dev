package com.stackmob.customcode.dev.test.server.sdk.data;

import com.stackmob.sdkapi.SMBoolean;
import com.stackmob.sdkapi.SMList;
import com.stackmob.sdkapi.SMValue;

public class SMListTestUtils {
    public static SMList createNested(Integer depth, SMList<? extends SMValue> base) {
        for(int i = 0; i < depth; i++) {
            base = new SMList<SMValue<?>>(base);
        }
        return base;
    }
}
