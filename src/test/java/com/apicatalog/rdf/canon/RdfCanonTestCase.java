/*
 * Copyright 2020 the original author or authors.
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
package com.apicatalog.rdf.canon;

import jakarta.json.JsonObject;

class RdfCanonTestCase {

    public enum Type {
        RDFC10MapTest, 
        RDFC10EvalTest
    }

    String id;
    String name;
    Type type;
    
    String input;
    String expected;

    static final RdfCanonTestCase of(JsonObject json) {

        final RdfCanonTestCase testCase = new RdfCanonTestCase();        

        testCase.id = json.getJsonString("id").getString();
        testCase.type = Type.valueOf(json.getJsonString("type").getString().substring("rdfc:".length()));
        testCase.name = json.getJsonString("name").getString();
        testCase.input = json.getJsonString("action").getString();
        testCase.expected = json.getJsonString("result").getString();
        
        return testCase;
    }

    @Override
    public String toString() {
        return id + ": " + name;
    }
}