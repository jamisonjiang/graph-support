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

package org.graphper.layout.dot;

import java.util.Comparator;
import java.util.List;
import org.graphper.api.GraphContainer;

interface CrossRank {

  int getRankIndex(DNode node);

  Integer safeGetRankIndex(DNode node);

  List<DNode> getNodes(int rank);

  DNode getNode(int rank, int rankIdx);

  void addNode(DNode node);

  int rankSize(int rank);

  int minRank();

  int maxRank();

  void exchange(DNode v, DNode w, boolean needSyncRankIdx);

  void sort(Comparator<DNode> comparator, boolean needSyncRankIdx);

  void sort(int rank, Comparator<DNode> comparator, boolean needSyncRankIdx);

  GraphContainer container();
}
