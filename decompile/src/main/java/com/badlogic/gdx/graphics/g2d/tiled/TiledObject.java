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

package com.badlogic.gdx.graphics.g2d.tiled;

import java.util.HashMap;

/** Contains a Tiled map object
 * @author David Fraska */
public class TiledObject {
	public String name, type;
	public int x, y, width = 0, height = 0, gid = 0;

	/** Contains the object properties with a key of the property name. */
	public HashMap<String, String> properties = new HashMap<String, String>();
}
