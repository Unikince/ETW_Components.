/*
 * Copyright 2010 Mario Zechner (contact@badlogicgames.com), Nathan Sweet (admin@esotericsoftware.com), Dave Clayton (contact@redskyforge.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package com.mybadlogic.gdx.backends.android.surfaceview;

/** This {@link ResolutionStrategy} will place the GLSurfaceView with the given height and width in the center the screen.
 * 
 * @author christoph widulle */
public class FixedResolutionStrategy implements ResolutionStrategy {

	private final int width;
	private final int height;

	public FixedResolutionStrategy (int width, int height) {
		this.width = width;
		this.height = height;
	}

	@Override
	public MeasuredDimension calcMeasures (int widthMeasureSpec, int heightMeasureSpec) {
		return new MeasuredDimension(width, height);
	}
}
