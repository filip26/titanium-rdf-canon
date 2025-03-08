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
import jakarta.json.JsonValue.ValueType;

class RdfCanonTestCase {

    public enum Type {
        RDFC10MapTest,
        RDFC10EvalTest,
        RDFC10NegativeEvalTest
    }

    String id;
    Type type;

    String name;
    String comment;

    String input;
    String expected;

    static final RdfCanonTestCase of(JsonObject json) {

        final RdfCanonTestCase testCase = new RdfCanonTestCase();

        testCase.id = json.getString("id");
        testCase.type = Type.valueOf(json.getString("type").substring("rdfc:".length()));
        testCase.name = json.getString("name");
        testCase.comment = json.containsKey("comment") && ValueType.STRING.equals(json.get("comment").getValueType())
                ? json.getString("comment")
                : null;
        testCase.input = json.getString("action");
        testCase.expected = json.containsKey("result")
                ? json.getString("result")
                : null;

        return testCase;
    }

    @Override
    public String toString() {
        return id + ": " + name;
    }
}