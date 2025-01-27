/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.mybadlogic.gdx.math;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.mybadlogic.gdx.graphics.OrthographicCamera;
import com.mybadlogic.gdx.graphics.PerspectiveCamera;
import com.mybadlogic.gdx.math.Plane.PlaneSide;
import com.mybadlogic.gdx.math.collision.BoundingBox;

public class Frustum {
	protected static final Vector3[] clipSpacePlanePoints = {new Vector3(-1, -1, -1), new Vector3(1, -1, -1),
		new Vector3(1, 1, -1), new Vector3(-1, 1, -1), // near clip
		new Vector3(-1, -1, 1), new Vector3(1, -1, 1), new Vector3(1, 1, 1), new Vector3(-1, 1, 1)}; // far clip
	protected static final float[] clipSpacePlanePointsArray = new float[8 * 3];

	static {
		int j = 0;
		for (Vector3 v : clipSpacePlanePoints) {
			clipSpacePlanePointsArray[j++] = v.x;
			clipSpacePlanePointsArray[j++] = v.y;
			clipSpacePlanePointsArray[j++] = v.z;
		}
	}

	/** the six clipping planes, near, far, left, right, top, bottm **/
	public final Plane[] planes = new Plane[6];

	/** eight points making up the near and far clipping "rectangles". order is counter clockwise, starting at bottom left **/
	public final Vector3[] planePoints = {new Vector3(), new Vector3(), new Vector3(), new Vector3(), new Vector3(),
		new Vector3(), new Vector3(), new Vector3()};
	protected final float[] planePointsArray = new float[8 * 3];

	public Frustum () {
		for (int i = 0; i < 6; i++) {
			planes[i] = new Plane(new Vector3(), 0);
		}
	}

	/** Updates the clipping plane's based on the given inverse combined projection and view matrix, e.g. from an
	 * {@link OrthographicCamera} or {@link PerspectiveCamera}.
	 * @param inverseProjectionView the combined projection and view matrices. */
	public void update (Matrix4 inverseProjectionView) {
		System.arraycopy(clipSpacePlanePointsArray, 0, planePointsArray, 0, clipSpacePlanePointsArray.length);
		Matrix4.prj(inverseProjectionView.val, planePointsArray, 0, 8, 3);
		for (int i = 0, j = 0; i < 8; i++) {
			Vector3 v = planePoints[i];
			v.x = planePointsArray[j++];
			v.y = planePointsArray[j++];
			v.z = planePointsArray[j++];
		}

		planes[0].set(planePoints[1], planePoints[0], planePoints[2]);
		planes[1].set(planePoints[4], planePoints[5], planePoints[7]);
		planes[2].set(planePoints[0], planePoints[4], planePoints[3]);
		planes[3].set(planePoints[5], planePoints[1], planePoints[6]);
		planes[4].set(planePoints[2], planePoints[3], planePoints[6]);
		planes[5].set(planePoints[4], planePoints[0], planePoints[1]);
	}

	/** Returns wheter the point is in the frustum.
	 * 
	 * @param point The point
	 * @return Wheter the point is in the frustum. */
	public boolean pointInFrustum (Vector3 point) {
		for (int i = 0; i < planes.length; i++) {
			PlaneSide result = planes[i].testPoint(point);
			if (result == PlaneSide.Back) return false;
		}

		return true;
	}

	/** Returns wheter the given sphere is in the frustum.
	 * 
	 * @param center The center of the sphere
	 * @param radius The radius of the sphere
	 * @return Wheter the sphere is in the frustum */
	public boolean sphereInFrustum (Vector3 center, float radius) {
		for (int i = 0; i < planes.length; i++)
			if (planes[i].distance(center) < -radius) return false;

		return true;
	}

	/** Returns wheter the given sphere is in the frustum not checking wheter it is behind the near and far clipping plane.
	 * 
	 * @param center The center of the sphere
	 * @param radius The radius of the sphere
	 * @return Wheter the sphere is in the frustum */
	public boolean sphereInFrustumWithoutNearFar (Vector3 center, float radius) {
		for (int i = 0; i < planes.length; i++)
			if (planes[i].distance(center) < -radius) return false;

		return true;
	}

	/** Returns wheter the given {@link BoundingBox} is in the frustum.
	 * 
	 * @param bounds The bounding box
	 * @return Wheter the bounding box is in the frustum */
	public boolean boundsInFrustum (BoundingBox bounds) {
		Vector3[] corners = bounds.getCorners();
		int len = corners.length;

		for (int i = 0, len2 = planes.length; i < len2; i++) {
			int out = 0;

			for (int j = 0; j < len; j++)
				if (planes[i].testPoint(corners[j]) == PlaneSide.Back) out++;

			if (out == 8) return false;
		}

		return true;
	}

// /**
// * Calculates the pick ray for the given window coordinates. Assumes the window coordinate system has it's y downwards. The
// * returned Ray is a member of this instance so don't reuse it outside this class.
// *
// * @param screen_width The window width in pixels
// * @param screen_height The window height in pixels
// * @param mouse_x The window x-coordinate
// * @param mouse_y The window y-coordinate
// * @param pos The camera position
// * @param dir The camera direction, having unit length
// * @param up The camera up vector, having unit length
// * @return the picking ray.
// */
// public Ray calculatePickRay (float screen_width, float screen_height, float mouse_x, float mouse_y, Vector3 pos, Vector3 dir,
// Vector3 up) {
// float n_x = mouse_x - screen_width / 2.0f;
// float n_y = mouse_y - screen_height / 2.0f;
// n_x /= screen_width / 2.0f;
// n_y /= screen_height / 2.0f;
//
// Z.set(dir.tmp().mul(-1)).nor();
// X.set(up.tmp().crs(Z)).nor();
// Y.set(Z.tmp().crs(X)).nor();
// near_center.set(pos.tmp3().sub(Z.tmp2().mul(near)));
// Vector3 near_point = X.tmp3().mul(near_width).mul(n_x).add(Y.tmp2().mul(near_height).mul(n_y));
// near_point.add(near_center);
//
// return ray.set(near_point.tmp(), near_point.sub(pos).nor());
// }

// public static void main(String[] argv) {
// PerspectiveCamera camera = new PerspectiveCamera(45, 2, 2);
// // camera.rotate(90, 0, 1, 0);
// camera.update();
// System.out.println(camera.direction);
// System.out.println(Arrays.toString(camera.frustum.planes));
//
// OrthographicCamera camOrtho = new OrthographicCamera(2, 2);
// camOrtho.near = 1;
// // camOrtho.rotate(90, 1, 0, 0);
// camOrtho.update();
// System.out.println(camOrtho.direction);
// System.out.println(Arrays.toString(camOrtho.frustum.planes));
// }
}
