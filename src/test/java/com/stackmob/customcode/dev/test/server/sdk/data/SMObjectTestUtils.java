package com.stackmob.customcode.dev.test.server.sdk.data;

import com.stackmob.sdkapi.SMObject;
import com.stackmob.sdkapi.SMValue;
import java.util.HashMap;
import java.util.Map;

public class SMObjectTestUtils {
    private static SMObject nest(SMObject cur, String key) {
        Map<String, SMValue> map = new HashMap<String, SMValue>();
        map.put(key, cur);
        return new SMObject(map);
    }
    public static SMObject createNested(Integer depth, String baseKey, SMObject baseObj) {
        for(int i = 0; i < depth; i++) {
            baseObj = nest(baseObj, baseKey);
        }
        return baseObj;
    }
}
