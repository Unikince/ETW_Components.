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

package com.badlogic.gdx.scenes.scene2d.ui.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GLCommon;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

/** A scissor stack is a stack of {@link Rectangle} to be used for clipping via {@link GLCommon#glScissor(int, int, int, int)}.
 * When a new Rectangle is pushed onto the stack, it will be merged with the current top of stack. The minimum area of overlap is
 * then set as the real top of the stack.
 * @author mzechner */
public class ScissorStack {
	private static Array<Rectangle> scissors = new Array<Rectangle>();

	/** Pushes a new scissor {@link Rectangle} onto the stack, merging it with the current top of the stack. The minimal area of
	 * overlap between the TOS rectangle and the provided rectangle is pushed onto the stack. This will invoke
	 * {@link GLCommon#glScissor(int, int, int, int)} with the final TOS rectangle. In case no scissor is yet on the stack this
	 * will also enable {@link GL10#GL_SCISSOR_TEST} automatically.
	 * @param scissor the scissor Rectangle */
	public static void pushScissors (Rectangle scissor) {
		fix(scissor);

		if (scissors.size == 0) {
			Gdx.gl.glEnable(GL10.GL_SCISSOR_TEST);
		} else {
			// merge scissors
			Rectangle parent = scissors.get(scissors.size - 1);
			float minX = Math.max(parent.x, scissor.x);
			float maxX = Math.min(parent.x + parent.width, scissor.x + scissor.width);
			scissor.x = minX;
			scissor.width = Math.max(1, maxX - minX);

			float minY = Math.max(parent.y, scissor.y);
			float maxY = Math.min(parent.y + parent.height, scissor.y + scissor.height);
			scissor.y = minY;
			scissor.height = Math.max(1, maxY - minY);
		}
		scissors.add(scissor);
		Gdx.gl.glScissor((int)scissor.x, (int)scissor.y, (int)scissor.width, (int)scissor.height);
	}

	/** Pops the current scissor rectangle from the stack and sets the new scissor area to the new TOS rectangle. In case no more
	 * rectangles are on the stack, {@link GL10#GL_SCISSOR_TEST} is disabled. */
	public static void popScissors () {
		scissors.pop();
		if (scissors.size == 0)
			Gdx.gl.glDisable(GL10.GL_SCISSOR_TEST);
		else {
			Rectangle scissor = scissors.peek();
			Gdx.gl.glScissor((int)scissor.x, (int)scissor.y, (int)scissor.width, (int)scissor.height);
		}
	}

	private static void fix (Rectangle rect) {
		if (rect.width < 0) {
			rect.width = -rect.width;
			rect.x -= rect.width;
		}
		if (rect.height < 0) {
			rect.height = -rect.height;
			rect.y -= rect.height;
		}
	}

	static Vector3 tmp = new Vector3();

	/** Calculates a scissor rectangle in OpenGL ES window coordinates from a {@link Camera}, a transformation {@link Matrix4} and
	 * an axis aligned {@link Rectangle}. The rectangle will get transformed by the camera and transform matrices and is then
	 * projected to screen coordinates. Note that only axis aligned rectangles will work with this method. If either the Camera or
	 * the Matrix4 have rotational components, the output of this method will not be suitable for
	 * {@link GLCommon#glScissor(int, int, int, int)}.
	 * @param camera the {@link Camera}
	 * @param batchTransform the transformation {@link Matrix4}
	 * @param area the {@link Rectangle} to transform to window coordinates
	 * @param scissor the Rectangle to store the result in */
	public static void calculateScissors (Camera camera, Matrix4 batchTransform, Rectangle area, Rectangle scissor) {
		tmp.set(area.x, area.y, 0);
		tmp.mul(batchTransform);
		camera.project(tmp);
		scissor.x = tmp.x;
		scissor.y = tmp.y;

		tmp.set(area.x + area.width, area.y + area.height, 0);
		tmp.mul(batchTransform);
		camera.project(tmp);
		scissor.width = tmp.x - scissor.x;
		scissor.height = tmp.y - scissor.y;
	}

	static final Rectangle viewport = new Rectangle();

	/** @return the current viewport in OpenGL ES window coordinates based on the currently applied scissor */
	public static Rectangle getViewport () {
		if (scissors.size == 0) {
			viewport.set(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
			return viewport;
		} else {
			Rectangle scissor = scissors.peek();
			viewport.set(scissor);
			return viewport;
		}
	}

	/** Transforms a point to real window coordinates (as oposed to OpenGL ES window coordinates), where the origin is in the top
	 * left and the the y-axis is pointing downwards
	 * @param camera the {@link Camera}
	 * @param transformMatrix the transformation {@link Matrix4}
	 * @param point the point to be transformed. */
	public static void toWindowCoordinates (Camera camera, Matrix4 transformMatrix, Vector2 point) {
		tmp.set(point.x, point.y, 0);
		tmp.mul(transformMatrix);
		camera.project(tmp);
		tmp.y = Gdx.graphics.getHeight() - tmp.y;
		point.x = tmp.x;
		point.y = tmp.y;
	}
}
