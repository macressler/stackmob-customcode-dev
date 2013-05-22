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

package com.stackmob.customcode.dev.javaexamples;

import com.stackmob.core.customcode.CustomCodeMethod;
import com.stackmob.core.rest.ProcessedAPIRequest;
import com.stackmob.core.rest.ResponseToProcess;
import com.stackmob.sdkapi.SDKServiceProvider;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class HelloWorldMethod implements CustomCodeMethod {
    @Override
    public String getMethodName() {
        return "hello_world";
    }

    @Override
    public List<String> getParams() {
        return new ArrayList<String>();
    }

    @Override
    public ResponseToProcess execute(ProcessedAPIRequest request, SDKServiceProvider serviceProvider) {
        Map<String, String> resp = new HashMap<String, String>();
        resp.put("msg", "Hello, world!");
        return new ResponseToProcess(HttpURLConnection.HTTP_OK, resp);
    }

}
