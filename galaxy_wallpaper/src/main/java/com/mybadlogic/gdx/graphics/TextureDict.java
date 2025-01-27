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

package com.mybadlogic.gdx.graphics;

import com.mybadlogic.gdx.Files;
import com.mybadlogic.gdx.Gdx;
import com.mybadlogic.gdx.files.FileHandle;
import com.mybadlogic.gdx.utils.ObjectMap;

/** <p>
 * A texture dictionary is used to track the usage of your textures and supply a single point of access for texture resources. It
 * stores {@link TextureRef}s by their path. If you need to load the same texture in different parts of your application it will
 * only be loaded into memory once by the dictionary.
 * </p>
 * 
 * @author Dave Clayton <contact@redskyforge.com> */
public class TextureDict {

	private static ObjectMap<String, TextureRef> sDictionary = new ObjectMap<String, TextureRef>();

	/** Loads a new texture into the dictionary as a reference counted {@link TextureRef}.
	 * @param path the path to the texture image.
	 * @return the {@TextureRef} representing the texture. */
	public static TextureRef loadTexture (String path) {
		return loadTexture(path, Texture.TextureFilter.MipMap, Texture.TextureFilter.Linear, Texture.TextureWrap.ClampToEdge, Texture.TextureWrap.ClampToEdge);
	}

	/** Loads a new texture into the dictionary as a reference counted {@link TextureRef}.
	 * @param path the path to the texture image.
	 * @param minFilter minFilter {@link Texture.TextureFilter}.
	 * @param magFilter magFilter {@link Texture.TextureFilter}.
	 * @param uwrap u-wrapping.
	 * @param vwrap v-wrapping.
	 * @return the {@TextureRef} representing the texture. */
	public static TextureRef loadTexture (String path, Texture.TextureFilter minFilter, Texture.TextureFilter magFilter, Texture.TextureWrap uwrap,
                                          Texture.TextureWrap vwrap) {
		if (sDictionary.containsKey(path)) {
			TextureRef ref = sDictionary.get(path);
			ref.addRef();
			return ref;
		}
		// load new texture
		FileHandle texFile = Gdx.app.getFiles().getFileHandle(path, Files.FileType.Internal);
		Texture newTex = new Texture(texFile, minFilter.isMipMap() || magFilter.isMipMap() ? true : false);
		newTex.setFilter(minFilter, magFilter);
		newTex.setWrap(uwrap, vwrap);
		TextureRef ref = new TextureRef(path, newTex);
		sDictionary.put(path, ref);
		return ref;
	}

	/** Removes a texture from the dictionary. In general you should probably not use this - use {@link TextureRef#unload()}
	 * instead.
	 * @param path to the texture. */
	public static void removeTexture (String path) {
		sDictionary.remove(path);
	}

	/** Unloads all of the currently managed textures. */
	public static void unloadAll () {
		for (TextureRef tex : sDictionary.values()) {
			tex.dispose();
		}
		sDictionary.clear();
	}
}
