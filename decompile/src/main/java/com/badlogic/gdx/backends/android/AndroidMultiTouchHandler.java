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

package com.badlogic.gdx.backends.android;

import android.view.MotionEvent;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidInput.TouchEvent;

/** Multitouch handler for devices running Android >= 2.0. If device is capable of (fake) multitouch this will report additional
 * pointers.
 * 
 * @author badlogicgames@gmail.com */
public class AndroidMultiTouchHandler implements AndroidTouchHandler {
	public void onTouch (MotionEvent event, AndroidInput input) {
		final int action = event.getAction() & MotionEvent.ACTION_MASK;
		int pointerIndex = (event.getAction() & MotionEvent.ACTION_POINTER_ID_MASK) >> MotionEvent.ACTION_POINTER_ID_SHIFT;
		int pointerId = event.getPointerId(pointerIndex);

		int x = 0, y = 0;
		int realPointerIndex = 0;

		long timeStamp = System.nanoTime();
//		long timeStamp = event.getEventTime() * 1000000;
		// logAction(action, pointerId);
		synchronized (input) { // FUCK
			switch (action) {
			case MotionEvent.ACTION_DOWN:
			case MotionEvent.ACTION_POINTER_DOWN:
				realPointerIndex = input.getFreePointerIndex(); // get a free pointer index as reported by Input.getX() etc.
				input.realId[realPointerIndex] = pointerId;
				x = (int)event.getX(pointerIndex);
				y = (int)event.getY(pointerIndex);
				postTouchEvent(input, TouchEvent.TOUCH_DOWN, x, y, realPointerIndex, timeStamp);
				input.touchX[realPointerIndex] = x;
				input.touchY[realPointerIndex] = y;
				input.deltaX[realPointerIndex] = 0;
				input.deltaY[realPointerIndex] = 0;
				input.touched[realPointerIndex] = true;
				break;

			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_POINTER_UP:
			case MotionEvent.ACTION_OUTSIDE:
			case MotionEvent.ACTION_CANCEL:
				realPointerIndex = input.lookUpPointerIndex(pointerId);
				if (realPointerIndex == -1) break;
				input.realId[realPointerIndex] = -1;
				x = (int)event.getX(pointerIndex);
				y = (int)event.getY(pointerIndex);
				postTouchEvent(input, TouchEvent.TOUCH_UP, x, y, realPointerIndex, timeStamp);
				input.touchX[realPointerIndex] = x;
				input.touchY[realPointerIndex] = y;
				input.deltaX[realPointerIndex] = 0;
				input.deltaY[realPointerIndex] = 0;
				input.touched[realPointerIndex] = false;
				break;

			case MotionEvent.ACTION_MOVE:
				int pointerCount = event.getPointerCount();
				for (int i = 0; i < pointerCount; i++) {
					pointerIndex = i;
					pointerId = event.getPointerId(pointerIndex);
					x = (int)event.getX(pointerIndex);
					y = (int)event.getY(pointerIndex);
					realPointerIndex = input.lookUpPointerIndex(pointerId);
					if (realPointerIndex == -1) continue;
					postTouchEvent(input, TouchEvent.TOUCH_DRAGGED, x, y, realPointerIndex, timeStamp);
					input.deltaX[realPointerIndex] = x - input.touchX[realPointerIndex];
					input.deltaY[realPointerIndex] = y - input.touchY[realPointerIndex];
					input.touchX[realPointerIndex] = x;
					input.touchY[realPointerIndex] = y;
				}
				break;
			}
		}
	}

	private void logAction (int action, int pointer) {
		String actionStr = "";
		if (action == MotionEvent.ACTION_DOWN)
			actionStr = "DOWN";
		else if (action == MotionEvent.ACTION_POINTER_DOWN)
			actionStr = "POINTER DOWN";
		else if (action == MotionEvent.ACTION_UP)
			actionStr = "UP";
		else if (action == MotionEvent.ACTION_POINTER_UP)
			actionStr = "POINTER UP";
		else if (action == MotionEvent.ACTION_OUTSIDE)
			actionStr = "OUTSIDE";
		else if (action == MotionEvent.ACTION_CANCEL)
			actionStr = "CANCEL";
		else if (action == MotionEvent.ACTION_MOVE)
			actionStr = "MOVE";
		else
			actionStr = "UNKNOWN (" + action + ")";
		Gdx.app.log("AndroidMultiTouchHandler", "action " + actionStr + ", Android pointer id: " + pointer);
	}

	private void postTouchEvent (AndroidInput input, int type, int x, int y, int pointer, long timeStamp) {
//		long timeStamp = System.nanoTime();
		TouchEvent event = input.usedTouchEvents.obtain();
		event.timeStamp = timeStamp;
		event.pointer = pointer;
		event.x = x;
		event.y = y;
		event.type = type;
		input.touchEvents.add(event);
	}

	public boolean supportsMultitouch (AndroidApplication activity) {
		return activity.getPackageManager().hasSystemFeature("android.hardware.touchscreen.multitouch");
	}
}
