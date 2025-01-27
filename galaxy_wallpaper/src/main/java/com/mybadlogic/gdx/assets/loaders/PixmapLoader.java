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

package com.mybadlogic.gdx.assets.loaders;

import com.mybadlogic.gdx.assets.AssetDescriptor;
import com.mybadlogic.gdx.assets.AssetLoaderParameters;
import com.mybadlogic.gdx.assets.AssetManager;
import com.mybadlogic.gdx.graphics.Pixmap;
import com.mybadlogic.gdx.utils.Array;

public class PixmapLoader extends AsynchronousAssetLoader<Pixmap, PixmapLoader.PixmapParameter> {
	public PixmapLoader (FileHandleResolver resolver) {
		super(resolver);
	}

	Pixmap pixmap;

	@Override
	public void loadAsync (AssetManager manager, String fileName, PixmapParameter parameter) {
		pixmap = null;
		pixmap = new Pixmap(resolve(fileName));
	}

	@Override
	public Pixmap loadSync (AssetManager manager, String fileName, PixmapParameter parameter) {
		return pixmap;
	}

	@Override
	public Array<AssetDescriptor> getDependencies (String fileName, PixmapParameter parameter) {
		return null;
	}

	static public class PixmapParameter extends AssetLoaderParameters<Pixmap> {
	}
}
