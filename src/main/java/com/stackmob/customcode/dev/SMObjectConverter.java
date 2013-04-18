package com.stackmob.customcode.sdk;

import java.util.Map;
import java.util.HashMap;
import com.stackmob.sdkapi.*;

public class SMObjectConverter {
    /**
     * create an adjusted map from the SMObject. the SMObject's contained map is <pre>Map<String, SMValue></pre>,
     * but the Scala compiler knows that SMValue takes a type param, so it's much more convenient to use
     * <pre>Map<String, SMValue<?>></pre>
     * @param obj the SMObject to convert
     * @return the new Map
     */
    public static Map<String, SMValue<?>> getMap(SMObject obj) {
        Map<String, SMValue> paramFreeMap = obj.getValue();
        Map<String, SMValue<?>> paramMap = new HashMap<String, SMValue<?>>();
        for(String key : paramFreeMap.keySet()) {
            SMValue<?> value = paramFreeMap.get(key);
            paramMap.put(key, value);
        }
        return paramMap;
    }
}
