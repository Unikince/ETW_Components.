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
package com.mybadlogic.gdx.backends.android;

import android.os.PowerManager.WakeLock;

import com.mybadlogic.gdx.backends.android.surfaceview.FillResolutionStrategy;
import com.mybadlogic.gdx.backends.android.surfaceview.ResolutionStrategy;

/** Class defining the configuration of an {@link AndroidApplication}. Allows you to disable the use of the accelerometer to save
 * battery among other things.
 * @author mzechner */
public class AndroidApplicationConfiguration {
	/** whether to use OpenGL ES 2.0 or not. default: false **/
	public boolean useGL20 = false;

	/** number of bits per color channel **/
	public int r = 5, g = 6, b = 5, a = 0;

	/** number of bits for depth and stencil buffer **/
	public int depth = 16, stencil = 0;

	/** number of samples for CSAA/MSAA, 2 is a good value **/
	public int numSamples = 0;

	/** whether to use the accelerometer. default: true **/
	public boolean useAccelerometer = true;

	/** whether to use the compass. default: true **/
	public boolean useCompass = true;

	/** the time in milliseconds to sleep after each event in the touch handler, set this to 16ms to get rid of touch flooding on
	 * pre Android 2.0 devices. default: 0 **/
	public int touchSleepTime = 0;

	/** whether to use a {@link WakeLock} or not. In case this is true you have to add the permission "android.permission.WAKE_LOCK"
	 * to your manifest file. default: false */
	public boolean useWakelock = false;

	/** the {@link ResolutionStrategy}. default: {@link FillResolutionStrategy} **/
	public ResolutionStrategy resolutionStrategy = new FillResolutionStrategy();
}