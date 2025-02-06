/*
 * Copyright 2022 The graph-support project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.graphper.parser;

/**
 * A custom unchecked exception indicating a parsing error in DOT or HTML-like inputs.
 *
 * @author johannes
 */
public class ParseException extends RuntimeException {

    private static final long serialVersionUID = 6494880405240898272L;

    public ParseException(String message) {

        super(message);
    }

    public ParseException(String message, Throwable cause) {

        super(message, cause);
    }

    public ParseException(Throwable cause) {

        super(cause);
    }
}
