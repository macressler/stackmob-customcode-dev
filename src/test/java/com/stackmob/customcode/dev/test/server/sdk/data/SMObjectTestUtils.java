package com.stackmob.customcode.dev.test.server.sdk.data;

import com.stackmob.sdkapi.SMObject;
import com.stackmob.sdkapi.SMValue;
import java.util.HashMap;
import java.util.Map;

public class SMObjectTestUtils {
    public static SMObject createNested(Integer depth) {
        SMObject start = new SMObject(new HashMap<String, SMValue>());
        for(int i = 0; i < depth; i++) {
            Map<String, SMValue> newMap = new HashMap<String, SMValue>();
            newMap.put(String.format("nested-%d", i), start);
            start = new SMObject(newMap);
        }
        return start;
    }
}
