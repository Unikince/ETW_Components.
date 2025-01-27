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

package com.mybadlogic.gdx;

import com.mybadlogic.gdx.graphics.GL10;
import com.mybadlogic.gdx.graphics.GL11;
import com.mybadlogic.gdx.graphics.GL20;
import com.mybadlogic.gdx.graphics.GLCommon;
import com.mybadlogic.gdx.graphics.GLU;

/** <p>
 * Environment class holding references to the {@link Application}, {@link Graphics}, {@link Audio}, {@link Files} and
 * {@link Input} instances. The references are held in public static fields. Do not mess with this! This essentially allows you
 * static access to all sub systems. It is your responsiblity to keep things thread safe. Don't use Graphics in a thread that is
 * not the rendering thread or things will go crazy. Really.
 * </p>
 * 
 * <p>
 * There's also references to {@link GLCommon}, {@link GL10}, {@link GL11}, {@link GL20} and {@link GLU}. The same rules as above
 * apply. Don't mess with this or things will break!
 * </p>
 * 
 * <p>
 * This is kind of messy but better than throwing around Graphics and similar instances. I'm aware of the design faux pas.
 * </p>
 * 
 * @author mzechner */
public class Gdx {
	public static Application app;
	public static Graphics graphics;
	public static Audio audio;
	public static Input input;
	public static Files files;

	public static GLCommon gl;
	public static GL10 gl10;
	public static GL11 gl11;
	public static GL20 gl20;
	public static GLU glu;
}
