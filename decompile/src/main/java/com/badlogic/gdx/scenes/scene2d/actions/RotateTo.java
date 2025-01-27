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

package com.badlogic.gdx.scenes.scene2d.actions;

import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.AnimationAction;

public class RotateTo extends AnimationAction {

	private static final ActionResetingPool<RotateTo> pool = new ActionResetingPool<RotateTo>(4, 100) {
		@Override
		protected RotateTo newObject () {
			return new RotateTo();
		}
	};

	protected float rotation;
	protected float startRotation;;
	protected float deltaRotation;

	public static RotateTo $ (float rotation, float duration) {
		RotateTo action = pool.obtain();
		action.rotation = rotation;
		action.duration = duration;
		action.invDuration = 1 / duration;
		return action;
	}

	@Override
	public void setTarget (Actor actor) {
		this.target = actor;
		this.startRotation = target.rotation;
		this.deltaRotation = rotation - target.rotation;
		this.taken = 0;
		this.done = false;
	}

	@Override
	public void act (float delta) {
		float alpha = createInterpolatedAlpha(delta);
		if (done) {
			target.rotation = rotation;
		} else {
			target.rotation = startRotation + deltaRotation * alpha;
		}
	}

	@Override
	public void finish () {
		super.finish();
		pool.free(this);
	}

	@Override
	public Action copy () {
		RotateTo rotateTo = $(rotation, duration);
		if (interpolator != null) rotateTo.setInterpolator(interpolator.copy());
		return rotateTo;
	}
}
