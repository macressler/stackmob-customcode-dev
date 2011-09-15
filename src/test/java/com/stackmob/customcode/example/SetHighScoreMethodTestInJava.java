/**
 * Copyright 2011 StackMob
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.stackmob.customcode.example;

import com.stackmob.core.jar.JarEntryObject;
import com.stackmob.core.rest.ResponseToProcess;
import org.junit.Test;
import org.junit.Assert;
import com.stackmob.customcode.localrunner.*;
import com.stackmob.core.MethodVerb;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class SetHighScoreMethodTestInJava {

    private String method = new SetHighScoreMethod().getMethodName();
    private Map<String, String> params = new HashMap<String, String>();
    private JarEntryObject entryPoint = new EntryPointExtender();
    private List<String> initialModels = new ArrayList<String>();

    private CustomCodeMethodRunnerJavaAdapter runner;

    public SetHighScoreMethodTestInJava() {
        params.put("username", "aaron");
        params.put("score", "22");
        initialModels.add("users");
        runner = CustomCodeMethodRunnerFactory.getForJava(entryPoint, initialModels);
    }

    @Test
    public void newHighScore() {
        ResponseToProcess res = runner.run(MethodVerb.GET, method, params);
        Map<String, ?> response = res.getResponseMap();
        Assert.assertTrue((Boolean) response.get("updated"));
        Assert.assertTrue((Boolean) response.get("newUser"));
        Assert.assertTrue("aaron".equals(response.get("username")));
        Assert.assertEquals(new Integer(22), response.get("newScore"));
    }

}
