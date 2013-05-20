/**
 * Copyright 2011-2013 StackMob
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.stackmob.customcode.dev.server.sdk.data;

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
