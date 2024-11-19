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

package org.graphper.def;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.graphper.util.Asserts;
import org.graphper.util.CollectionUtils;

/**
 * Curve correlation, especially calculation of Bessel curve correlation formula, curve fitting and
 * other operations.
 *
 * @author Jamison Jiang
 */
public final class Curves {

	/*
	 * fit curves reparameterization max times
	 */
	private static final int MAX_ITERATIONS_TIMES = 4;

	private Curves() {
	}

	/**
	 * An Algorithm for Automatically Fitting Digitized Curves by Philip J. Schneider from "Graphics
	 * Gems", Academic Press, 1990.
	 *
	 * <p>Given an array of points and a tolerance (squared error between points and  fitted curve),
	 * the algorithm will generate a piecewise cubic Bessel representation that approximates the
	 * points.The returned a piecewise cubic Bessel is orderly,ordered means that the result has the
	 * following characteristics: {@link ThirdOrderBezierCurve#getV4()} of the current Bezier segment
	 * is equals to {@link ThirdOrderBezierCurve#getV1()} of the next member.
	 *
	 * @param <F>    point type
	 * @param points the points need to be fitted
	 * @param error  fitness can tolerate maximum error
	 * @return piecewise cubic Bessel
	 * @throws IllegalArgumentException the number of points less than 2
	 * @see <a href="https://github.com/erich666/GraphicsGems/blob/master/gems/FitCurves.c">FitCurves.c</a>
	 */
	public static <F extends FlatPoint> MultiBezierCurve fitCurves(List<F> points, double error) {
		return fitCurves(points, null, null, error);
	}

	/**
	 * An Algorithm for Automatically Fitting Digitized Curves by Philip J. Schneider from "Graphics
	 * Gems", Academic Press, 1990.
	 *
	 * <p>Given an array of points and a tolerance (squared error between points and  fitted curve),
	 * the algorithm will generate a piecewise cubic Bessel representation that approximates the
	 * points.The returned a piecewise cubic Bessel is orderly,ordered means that the result has the
	 * following characteristics: {@link ThirdOrderBezierCurve#getV4()} of the current Bezier segment
	 * is equals to {@link ThirdOrderBezierCurve#getV1()} of the next member.
	 *
	 * <p>The caller can specify the endpoint tangent vector of the initial Bezier curve, which is
	 * very important for the requirement of splicing multiple fitting curves and maintaining a good
	 * "smoothness".If the caller does not specify it, the vertex adjacent to the endpoint is used as
	 * the vector direction by default.
	 *
	 * @param <F>          point type
	 * @param points       the points need to be fitted
	 * @param leftTangent  the left endpoint tangent vector
	 * @param rightTangent the right endpoint tangent vector
	 * @param error        fitness can tolerate maximum error
	 * @return piecewise cubic Bessel
	 * @throws IllegalArgumentException the number of points less than 2
	 * @see <a href="https://github.com/erich666/GraphicsGems/blob/master/gems/FitCurves.c">FitCurves.c</a>
	 */
	public static <F extends FlatPoint> MultiBezierCurve fitCurves(List<F> points,
	                                                               FlatPoint leftTangent,
	                                                               FlatPoint rightTangent,
	                                                               double error) {
		Asserts.illegalArgument(
				CollectionUtils.isEmpty(points) || points.size() < 2,
				"The number of points must be greater than 1"
		);

		// compute endpoint vector
		leftTangent = leftTangent == null ? computeLeftTangent(points) : leftTangent;
		rightTangent = rightTangent == null ? computeRightTangent(points) : rightTangent;

		return fitCurves(0, points.size() - 1, error,
		                 new MultiBezierCurve(), leftTangent, rightTangent, points);
	}

	/**
	 * Bessel Equation calculate.
	 *
	 * @param t      position ratio
	 * @param points position vector
	 * @return target vector
	 * @throws IllegalArgumentException empty vectors
	 */
	public static FlatPoint besselEquationCalc(double t, FlatPoint... points) {
		Asserts.illegalArgument(points == null || points.length < 2,
		                        "points length can not be lower than 2");
		if (t == 0) {
			return points[0];
		}

		if (t == 1) {
			return points[points.length - 1];
		}

		FlatPoint[] tmp = Arrays.copyOf(points, points.length);

		for (int i = 0; i < points.length; i++) {
			for (int j = 0; j < points.length - 1; j++) {
				Asserts.illegalArgument(tmp[j] == null, "Bessel curve contain null control point");
				Asserts.illegalArgument(tmp[j + 1] == null, "");
				tmp[j] = new FlatPoint(
						(1 - t) * tmp[j].getX() + t * tmp[j + 1].getX(),
						(1 - t) * tmp[j].getY() + t * tmp[j + 1].getY()
				);
			}
		}

		return tmp[0];
	}

	/**
	 * According to the specified time t, calculate the sub-curve before or after the time. The
	 * trajectory of the sub-curve is the same as the original curve, but the part before or after the
	 * time will be intercepted.
	 *
	 * @param t           position ratio
	 * @param isFirstHalf cut the first half
	 * @param bezierCurve original bessel curve
	 * @return The segmented curve
	 * @throws IllegalArgumentException illegal parameter range
	 */
	public static ThirdOrderBezierCurve divideThirdBesselCurve(double t, boolean isFirstHalf,
	                                                           ThirdOrderBezierCurve bezierCurve) {
		Asserts.nullArgument(bezierCurve, "bezierCurve");
		FlatPoint dividePoint = besselEquationCalc(t, bezierCurve.v1, bezierCurve.v2,
		                                           bezierCurve.v3, bezierCurve.v4);

		// Compute the point at time t on the v2 -> v3 vector
		FlatPoint intersection = Vectors.add(
				bezierCurve.v2,
				Vectors.multiple(Vectors.sub(bezierCurve.v3, bezierCurve.v2), t)
		);
		ThirdOrderBezierCurve childCurve = new ThirdOrderBezierCurve();

		if (isFirstHalf) {
			childCurve.v1 = bezierCurve.v1;
			childCurve.v2 = Vectors.add(
					bezierCurve.v1,
					Vectors.multiple(Vectors.sub(bezierCurve.v2, bezierCurve.v1), t)
			);
			childCurve.v3 = Vectors.add(
					childCurve.v2,
					Vectors.multiple(Vectors.sub(intersection, childCurve.v2), t)
			);
			childCurve.v4 = dividePoint;
		} else {
			childCurve.v1 = dividePoint;
			childCurve.v3 = Vectors.add(
					Vectors.multiple(Vectors.sub(bezierCurve.v3, bezierCurve.v4), 1 - t),
					bezierCurve.v4
			);
			childCurve.v2 = Vectors.add(
					intersection,
					Vectors.multiple(Vectors.sub(childCurve.v3, intersection), t)
			);
			childCurve.v4 = bezierCurve.v4;
		}

		return childCurve;
	}

	// ----------------------------------- private method -----------------------------------

	private static <F extends FlatPoint> MultiBezierCurve fitCurves(int first, int last, double error,
	                                                                MultiBezierCurve curve,
	                                                                FlatPoint leftTangent,
	                                                                FlatPoint rightTangent,
	                                                                List<F> points) {
		// Use heuristic if region only has two points in it
		if (last - first + 1 == 2) {
			double distance = FlatPoint.twoFlatPointDistance(points.get(first), points.get(last)) / 3;
			ThirdOrderBezierCurve thirdOrderBezierCurve = new ThirdOrderBezierCurve();
			thirdOrderBezierCurve.v1 = points.get(first);
			thirdOrderBezierCurve.v4 = points.get(last);
			thirdOrderBezierCurve.v2 = Vectors.add(
					thirdOrderBezierCurve.v1,
					Vectors.scale(leftTangent, distance)
			);
			thirdOrderBezierCurve.v3 = Vectors.add(
					thirdOrderBezierCurve.v4,
					Vectors.scale(rightTangent, distance)
			);

			curve.add(thirdOrderBezierCurve);
			return curve;
		}

		// Parameterize points, and attempt to fit curve
		double[] pointChordLens = chordLengthParameterize(points, first, last);
		ThirdOrderBezierCurve bezierCurve = generateBezier(points, first, last,
		                                                   pointChordLens, leftTangent, rightTangent);

		int[] splitIndex = new int[]{0};
		// Find max deviation of points to fitted curve
		double maxError = computeMaxError(points, first, last, bezierCurve, pointChordLens, splitIndex);
		// If error not too large, try some reparameterization and iteration
		if (maxError < error) {
			curve.add(bezierCurve);
			return curve;
		}

		/*
		 * If error not too large, try some reparameterization and iteration
		 */
		double iterationError = error * 4;
		if (maxError < iterationError) {
			for (int i = 0; i < MAX_ITERATIONS_TIMES; i++) {
				double[] uPrime = reparameterize(points, first, last, pointChordLens, bezierCurve);
				bezierCurve = generateBezier(points, first, last, uPrime, leftTangent, rightTangent);
				maxError = computeMaxError(points, first, last, bezierCurve, uPrime, splitIndex);
				if (maxError < error) {
					curve.add(bezierCurve);
					return curve;
				}

				pointChordLens = uPrime;
			}
		}

		FlatPoint centerVector = computeCenterTangent(points, splitIndex[0]);
		fitCurves(first, splitIndex[0], error, curve, leftTangent, centerVector, points);
		fitCurves(splitIndex[0], last, error, curve,
		          centerVector.reserve(), rightTangent, points);
		return curve;
	}

	/*
	 * Use least-squares method to find Bezier control points for region.
	 */
	private static <F extends FlatPoint> ThirdOrderBezierCurve generateBezier(List<F> points,
	                                                                          int first, int last,
	                                                                          double[] pointChordLens,
	                                                                          FlatPoint leftVector,
	                                                                          FlatPoint rightVector) {
		int size = last - first + 1;
		ThirdOrderBezierCurve bezierCurve = new ThirdOrderBezierCurve();

		FlatPoint[][] A = new FlatPoint[size][];
		// compute all point's left and right vector
		for (int i = 0; i < size; i++) {
			A[i] = new FlatPoint[2];
			A[i][0] = Vectors.scale(leftVector, B1(pointChordLens[i]));
			A[i][1] = Vectors.scale(rightVector, B2(pointChordLens[i]));
		}

		FlatPoint firstPoint = points.get(first);
		FlatPoint lastPoint = points.get(last);
		double[][] C = new double[][]{
				{0, 0},
				{0, 0}
		};
		double[] X = new double[]{0, 0};

		for (int i = 0; i < size; i++) {
			C[0][0] += Vectors.mul(A[i][0], A[i][0]);
			C[0][1] += Vectors.mul(A[i][0], A[i][1]);

			C[1][0] = C[0][1];
			C[1][1] += Vectors.mul(A[i][1], A[i][1]);

			FlatPoint tmp = Vectors.sub(
					points.get(first + i),
					Vectors.add(
							Vectors.multiple(firstPoint, B0(pointChordLens[i])),
							Vectors.add(
									Vectors.multiple(firstPoint, B1(pointChordLens[i])),
									Vectors.add(
											Vectors.multiple(lastPoint, B2(pointChordLens[i])),
											Vectors.multiple(lastPoint, B3(pointChordLens[i]))
									)
							)
					)
			);

			X[0] += Vectors.mul(A[i][0], tmp);
			X[1] += Vectors.mul(A[i][1], tmp);
		}

		// Compute the determinants of C and X
		double detC0C1 = C[0][0] * C[1][1] - C[1][0] * C[0][1];
		double detC0X = C[0][0] * X[1] - C[1][0] * X[0];
		double detXC1 = X[0] * C[1][1] - X[1] * C[0][1];

		// Finally, derive alpha values
		double alphaL;
		double alphaR;
		if (detC0C1 == 0) {
			alphaL = 0;
			alphaR = 0;
		} else {
			alphaL = detXC1 / detC0C1;
			alphaR = detC0X / detC0C1;
		}

		/*
		 * If alpha negative, use the Wu/Barsky heuristic (see text)
		 * (if alpha is 0, you get coincident control points that lead to
		 * divide by zero in any subsequent NewtonRaphsonRootFind() call.
		 */
		double segLength = FlatPoint.twoFlatPointDistance(firstPoint, lastPoint);
		double epsilon = 1.0e-6 * segLength;

		bezierCurve.v1 = firstPoint;
		bezierCurve.v4 = lastPoint;
		if (alphaL < epsilon || alphaR < epsilon) {
			// fall back on standard (probably inaccurate) formula, and subdivide further if needed.
			double dist = segLength / 3;
			bezierCurve.v2 = Vectors.add(bezierCurve.v1, Vectors.scale(leftVector, dist));
			bezierCurve.v3 = Vectors.add(bezierCurve.v4, Vectors.scale(rightVector, dist));
		} else {
			/*
			 * First and last control points of the Bezier curve are
			 * positioned exactly at the first and last data points
			 * Control points 1 and 2 are positioned an alpha distance out
			 * on the tangent vectors, left and right, respectively
			 */
			bezierCurve.v2 = Vectors.add(bezierCurve.v1, Vectors.scale(leftVector, alphaL));
			bezierCurve.v3 = Vectors.add(bezierCurve.v4, Vectors.scale(rightVector, alphaR));
		}

		return bezierCurve;
	}

	/*
	 * Use least-squares method to find Bezier control points for region.
	 */
	private static <F extends FlatPoint> double computeMaxError(List<F> points, int first, int last,
	                                                            ThirdOrderBezierCurve bezierCurve,
	                                                            double[] pointChordLens,
	                                                            int[] splitIndex) {
		splitIndex[0] = (last - first + 1) / 2;
		double maxDist = -Double.MAX_VALUE;
		for (int i = first + 1; i < last; i++) {
			FlatPoint p = besselEquationCalc(
					pointChordLens[i - first],
					bezierCurve.v1,
					bezierCurve.v2,
					bezierCurve.v3,
					bezierCurve.v4
			);
			FlatPoint v = Vectors.sub(p, points.get(i));
			double dist = Vectors.squaredLen(v.getX(), v.getY());
			if (dist >= maxDist) {
				maxDist = dist;
				splitIndex[0] = i;
			}
		}
		return maxDist;
	}

	/*
	 * Given set of points and their parameterization, try to find a better parameterization.
	 */
	private static <F extends FlatPoint> double[] reparameterize(List<F> points, int first,
	                                                             int last, double[] pointChordLens,
	                                                             ThirdOrderBezierCurve bezierCurve) {
		double[] repl = new double[last - first + 1];
		for (int i = first; i <= last; i++) {
			repl[i - first] = newtonRaphsonRootFind(
					bezierCurve,
					points.get(i),
					pointChordLens[i - first]
			);
		}

		return repl;
	}

	/*
	 * Use Newton-Raphson iteration to find better root.
	 */
	private static double newtonRaphsonRootFind(ThirdOrderBezierCurve curve, FlatPoint p, double u) {
		// Compute Q(u)
		FlatPoint qu = besselEquationCalc(u, curve.v1, curve.v2, curve.v3, curve.v4);

		FlatPoint[] q1 = new FlatPoint[3];
		FlatPoint[] q2 = new FlatPoint[2];

		// Generate control vertices for Q'
		for (int i = 0; i <= 2; i++) {
			q1[i] = new FlatPoint(
					(curve.getByIndex(i + 1).getX() - curve.getByIndex(i).getX()) * 3,
					(curve.getByIndex(i + 1).getY() - curve.getByIndex(i).getY()) * 3
			);
		}

		// Generate control vertices for Q''
		for (int i = 0; i <= 1; i++) {
			q2[i] = new FlatPoint(
					(q1[i + 1].getX() - q1[i].getX()) * 2,
					(q1[i + 1].getY() - q1[i].getY()) * 2
			);
		}

		// Compute Q'(u) and Q''(u)
		FlatPoint q1u = besselEquationCalc(u, q1);
		FlatPoint q2u = besselEquationCalc(u, q2);

		double numerator = (qu.getX() - p.getX()) * q1u.getX() + (qu.getY() - p.getY()) * q1u.getY();
		double denominator =
				q1u.getX() * q1u.getX() + q1u.getY() * q1u.getY() + (qu.getX() - p.getX()) * q2u.getX()
						+ (qu.getY() - p.getY()) * q2u.getY();

		if (denominator == 0) {
			return u;
		}

		return u - (numerator / denominator);
	}

	// calculate the ratio of the arc length of each vertex to all arc lengths
	private static <F extends FlatPoint> double[] chordLengthParameterize(List<F> points, int first,
	                                                                      int last) {
		double[] pointChordLens = new double[last - first + 1];
		// stack of distances all the way from the first vertex
		for (int i = 1; i < pointChordLens.length; i++) {
			pointChordLens[i] = pointChordLens[i - 1]
					+ FlatPoint.twoFlatPointDistance(points.get(first + i), points.get(first + i - 1));
		}

		// ratio of arc length to total arc length
		double lastPoint = pointChordLens[pointChordLens.length - 1];
		for (int i = 1; i < pointChordLens.length; i++) {
			pointChordLens[i] = pointChordLens[i] / lastPoint;
		}

		return pointChordLens;
	}

	// left point vector, determined by next point
	private static <F extends FlatPoint> FlatPoint computeLeftTangent(List<F> points) {
		return Vectors.unit(points.get(1), points.get(0));
	}

	// right point vector, determined by pre point
	private static <F extends FlatPoint> FlatPoint computeRightTangent(List<F> points) {
		return Vectors.unit(points.get(points.size() - 2), points.get(points.size() - 1));
	}

	// center point vector, determined by pre and next point
	private static <F extends FlatPoint> FlatPoint computeCenterTangent(List<F> points,
	                                                                    int centerIndex) {
		FlatPoint v1 = Vectors.sub(points.get(centerIndex - 1), points.get(centerIndex));
		FlatPoint v2 = Vectors.sub(points.get(centerIndex), points.get(centerIndex + 1));
		return Vectors.unit((v1.getX() + v2.getX()) / 2, (v1.getY() + v2.getY()) / 2);
	}

	// ---------------------------------- Bezier multipliers ----------------------------------

	private static double B0(double t) {
		double tmp = 1 - t;
		return tmp * tmp * tmp;
	}

	private static double B1(double t) {
		double tmp = 1 - t;
		return 3 * t * tmp * tmp;
	}

	private static double B2(double t) {
		double tmp = 1 - t;
		return 3 * t * t * tmp;
	}

	private static double B3(double t) {
		return t * t * t;
	}

	// --------------------------- MultiBezierCurve and ThirdOrderBezierCurve ---------------------------

	/**
	 * Multi Bezier Curve Segment
	 */
	public static class MultiBezierCurve extends ArrayList<ThirdOrderBezierCurve> {

		private static final long serialVersionUID = -7991103325506043318L;

		public MultiBezierCurve() {
		}

		public MultiBezierCurve(int initialCapacity) {
			super(initialCapacity);
		}

		@Override
		public boolean equals(Object o) {
			return super.equals(o) && o instanceof MultiBezierCurve;
		}

		@Override
		public int hashCode() {
			return super.hashCode() + MultiBezierCurve.class.hashCode();
		}
	}

	/**
	 * Third Order BezierCurve
	 */
	public static class ThirdOrderBezierCurve implements Serializable {

		private static final long serialVersionUID = 2766088821979206982L;

		/**
		 * Bezier curve control points
		 */
		private FlatPoint v1;
		private FlatPoint v2;
		private FlatPoint v3;
		private FlatPoint v4;

		private ThirdOrderBezierCurve() {
		}

		public ThirdOrderBezierCurve(
				FlatPoint v1, FlatPoint v2,
				FlatPoint v3, FlatPoint v4
		) {
			Asserts.nullArgument(v1, "v1");
			Asserts.nullArgument(v2, "v2");
			Asserts.nullArgument(v3, "v3");
			Asserts.nullArgument(v4, "v4");
			this.v1 = v1;
			this.v2 = v2;
			this.v3 = v3;
			this.v4 = v4;
		}

		public ThirdOrderBezierCurve(ThirdOrderBezierCurve curve) {
			this(curve.v1, curve.v2, curve.v3, curve.v4);
		}

		public FlatPoint getV1() {
			return v1;
		}

		public FlatPoint getV2() {
			return v2;
		}

		public FlatPoint getV3() {
			return v3;
		}

		public FlatPoint getV4() {
			return v4;
		}

		/**
		 * Adjust the curve by moving the position of v2 and v3 on the tangent line, and the parameter
		 * represents the change of the original tangent line length.
		 *
		 * @param v2AdjustRatio tangent adjustment ratio in v2 direction
		 * @param v3AdjustRatio tangent adjustment ratio in v3 direction
		 */
		public void adjust(double v2AdjustRatio, double v3AdjustRatio) {
			this.v2 = Vectors.add(v1, Vectors.scale(Vectors.sub(v2, v1),
			                                        FlatPoint.twoFlatPointDistance(v2, v1)
					                                                    * v2AdjustRatio));
			this.v3 = Vectors.add(v4, Vectors.scale(Vectors.sub(v3, v4),
			                                        FlatPoint.twoFlatPointDistance(v3, v4)
					                                                    * v3AdjustRatio));
		}

		private FlatPoint getByIndex(int index) {
			if (index == 0) {
				return v1;
			}
			if (index == 1) {
				return v2;
			}
			if (index == 2) {
				return v3;
			}
			if (index == 3) {
				return v4;
			}
			throw new IndexOutOfBoundsException("index must between 0 and 3");
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}
			ThirdOrderBezierCurve that = (ThirdOrderBezierCurve) o;
			return Objects.equals(v1, that.v1)
					&& Objects.equals(v2, that.v2) && Objects.equals(v3, that.v3)
					&& Objects.equals(v4, that.v4);
		}

		@Override
		public int hashCode() {
			return Objects.hash(v1, v2, v3, v4);
		}

		@Override
		public String toString() {
			return "ThirdOrderBezierCurve{" +
					"v1=" + v1 +
					", v2=" + v2 +
					", v3=" + v3 +
					", v4=" + v4 +
					'}';
		}
	}
}
