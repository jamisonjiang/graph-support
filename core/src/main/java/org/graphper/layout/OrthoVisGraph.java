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

package org.graphper.layout;

import java.util.Objects;
import java.util.TreeSet;
import org.graphper.api.attributes.Splines;
import org.graphper.def.FlatPoint;
import org.graphper.util.Asserts;
import org.graphper.api.ext.Box;

/**
 * Orthogonal visibility graph for {@link Splines#ORTHO} router.
 *
 * @author Jamison Jiang
 */
public class OrthoVisGraph {

	private final TreeSet<GridVertex> nodes;

	public OrthoVisGraph() {
			this.nodes = new TreeSet<>();
	}

	public Iterable<GridVertex> nodes() {
		return nodes;
	}

	public int nodeNum() {
		return nodes.size();
	}

	public void add(GridVertex vertex) {
		Asserts.nullArgument(vertex, "vertex");
		nodes.add(vertex);
	}

	public void addLeft(GridVertex vertex, GridVertex left) {
		Asserts.nullArgument(left, "left");
		Asserts.nullArgument(vertex, "vertex");
		Asserts.illegalArgument(vertex == left, "The left node cannot be equal to the current node");
		Asserts.illegalArgument(vertex.right == left || left.left == vertex, "Circular reference");
		add(left);
		add(vertex);
		left.right = vertex;
		vertex.left = left;
	}

	public void addRight(GridVertex vertex, GridVertex right) {
		Asserts.nullArgument(right, "right");
		Asserts.nullArgument(vertex, "vertex");
		Asserts.illegalArgument(vertex == right, "The right node cannot be equal to the current node");
		addLeft(right, vertex);
	}

	public void addTop(GridVertex vertex, GridVertex top) {
		Asserts.nullArgument(top, "top");
		Asserts.nullArgument(vertex, "vertex");
		Asserts.illegalArgument(vertex == top, "The top node cannot be equal to the current node");
		Asserts.illegalArgument(vertex.bottom == top || top.top == vertex, "Circular reference");
		add(top);
		add(vertex);
		top.bottom = vertex;
		vertex.top = top;
	}

	public void addBottom(GridVertex vertex, GridVertex bottom) {
		Asserts.nullArgument(bottom, "bottom");
		Asserts.nullArgument(vertex, "vertex");
		Asserts
				.illegalArgument(vertex == bottom, "The bottom node cannot be equal to the current node");
		addTop(bottom, vertex);
	}

	public static class GridVertex implements Box, Comparable<GridVertex> {

		private boolean nodeInternal;

		private GridVertex left;

		private GridVertex right;

		private GridVertex top;

		private GridVertex bottom;

		private final FlatPoint leftUp;

		private final FlatPoint rightDown;

//		public int leftNo;
//		public int topNo;

		public GridVertex(FlatPoint leftUp, FlatPoint rightDown) {
			Asserts.nullArgument(leftUp, "leftUp");
			Asserts.nullArgument(rightDown, "rightDown");
			this.leftUp = leftUp;
			this.rightDown = rightDown;
		}

		@Override
		public double getLeftBorder() {
			return leftUp.getX();
		}

		@Override
		public double getRightBorder() {
			return rightDown.getX();
		}

		@Override
		public double getUpBorder() {
			return leftUp.getY();
		}

		@Override
		public double getDownBorder() {
			return rightDown.getY();
		}

		@Override
		public double getX() {
			return (leftUp.getX() + rightDown.getX()) / 2;
		}

		@Override
		public double getY() {
			return (leftUp.getY() + rightDown.getY()) / 2;
		}

		@Override
		public FlatPoint getLeftUp() {
			return leftUp;
		}

		@Override
		public FlatPoint getRightDown() {
			return rightDown;
		}

		public boolean isNodeInternal() {
			return nodeInternal;
		}

		public void markInternalNode() {
			this.nodeInternal = true;
		}

		public GridVertex getLeft() {
			return left;
		}

		public GridVertex getRight() {
			return right;
		}

		public GridVertex getTop() {
			return top;
		}

		public GridVertex getBottom() {
			return bottom;
		}

		@Override
		public int compareTo(GridVertex o) {
			if (o == null) {
				return 1;
			}
			int r = Double.compare(getX(), o.getX());
			if (r != 0) {
				return r;
			}
			return Double.compare(getY(), o.getY());
		}
	}

	public static class Segment {

		private FlatPoint start;
		private FlatPoint end;

		public FlatPoint getStart() {
			return start;
		}

		public FlatPoint getEnd() {
			return end;
		}

		public void setStart(FlatPoint start) {
			Asserts.nullArgument(start, "start");
			Asserts.illegalArgument(Objects.equals(start, end), "start equals to end");
			this.start = start;
		}

		public void setEnd(FlatPoint end) {
			Asserts.nullArgument(end, "end");
			Asserts.illegalArgument(Objects.equals(start, end), "end equals to start");
			this.end = end;
		}
	}
}
