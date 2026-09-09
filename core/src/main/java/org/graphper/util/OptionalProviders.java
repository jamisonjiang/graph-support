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

package org.graphper.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.TreeMap;
import org.graphper.layout.EnvStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Discovers optional platform providers without making their dependencies core requirements. */
public final class OptionalProviders {

  private static final Logger log = LoggerFactory.getLogger(OptionalProviders.class);

  private OptionalProviders() {}

  /** Returns supported providers in priority order, skipping unavailable providers. */
  public static <T extends EnvStrategy> List<T> load(Class<T> service) {
    Map<Integer, List<T>> ordered = new TreeMap<>();
    try {
      Iterator<T> providers = ServiceLoader.load(service).iterator();
      // Some ServiceLoader implementations do not advance after a failed hasNext/next.
      // Bound total failures rather than retrying a broken descriptor indefinitely.
      int failures = 0;
      while (failures < 32) {
        try {
          if (!providers.hasNext()) {
            break;
          }
          T provider = providers.next();
          if (!provider.envSupport()) {
            continue;
          }
          int order = provider.order();
          List<T> group = ordered.get(order);
          if (group == null) {
            group = new ArrayList<>();
            ordered.put(order, group);
          }
          group.add(provider);
        } catch (ServiceConfigurationError | LinkageError | RuntimeException e) {
          failures++;
          log.warn("Skipping unavailable provider for " + service.getName(), e);
        }
      }
    } catch (ServiceConfigurationError | LinkageError | RuntimeException e) {
      log.warn("Cannot discover optional providers for " + service.getName(), e);
    }
    List<T> result = new ArrayList<>();
    for (List<T> group : ordered.values()) {
      result.addAll(group);
    }
    return Collections.unmodifiableList(result);
  }
}
