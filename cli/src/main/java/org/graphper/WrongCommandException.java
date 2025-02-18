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

package org.graphper;

/**
 * Exception thrown when an invalid or unrecognized command is encountered.
 * This is typically used in command-line argument parsing to indicate incorrect usage.
 *
 * @author Jamison Jiang
 */
public class WrongCommandException extends Exception {

  private static final long serialVersionUID = 7129182882393292716L;

  public WrongCommandException() {
    super();
  }

  public WrongCommandException(String message) {
    super(message);
  }

  public WrongCommandException(Throwable cause) {
    super(cause);
  }
}
