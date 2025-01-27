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
package com.mybadlogic.gdx.utils;

import com.mybadlogic.gdx.Application;
import com.mybadlogic.gdx.Gdx;

/** Simple logger that uses the {@link Application} logging facilities to output messages.
 * @author mzechner */
public class Logger {
	private final String tag;
	private boolean enabled = true;

	public Logger (String tag) {
		this.tag = tag;
	}

	public void log (String message) {
		if (enabled) {
			Gdx.app.log(tag, message);
		}
	}

	public void setEnabled (boolean enabled) {
		this.enabled = enabled;
	}
}