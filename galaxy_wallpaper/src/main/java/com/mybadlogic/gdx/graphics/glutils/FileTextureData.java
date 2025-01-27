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

package com.mybadlogic.gdx.graphics.glutils;

import com.mybadlogic.gdx.files.FileHandle;
import com.mybadlogic.gdx.graphics.Pixmap;
import com.mybadlogic.gdx.graphics.TextureData;
import com.mybadlogic.gdx.utils.GdxRuntimeException;

public class FileTextureData implements TextureData {
	final FileHandle file;
	int width = 0;
	int height = 0;
	Pixmap.Format format;
	Pixmap pixmap;
	boolean useMipMaps;
	boolean isPrepared = false;

	public FileTextureData (FileHandle file, Pixmap preloadedPixmap, Pixmap.Format format, boolean useMipMaps) {
		this.file = file;
		this.pixmap = preloadedPixmap;
		this.format = format;
		this.useMipMaps = useMipMaps;
		if (pixmap != null) {
			width = pixmap.getWidth();
			height = pixmap.getHeight();
			if (format == null) this.format = pixmap.getFormat();
		}
	}

	@Override
	public boolean isPrepared () {
		return isPrepared;
	}

	@Override
	public void prepare () {
		if (isPrepared) throw new GdxRuntimeException("Already prepared");
		if (pixmap == null) {
			pixmap = new Pixmap(file);
			width = pixmap.getWidth();
			height = pixmap.getHeight();
			if (format == null) format = pixmap.getFormat();
		}
		isPrepared = true;
	}

	@Override
	public Pixmap consumePixmap () {
		if (!isPrepared) throw new GdxRuntimeException("Call prepare() before calling getPixmap()");
		isPrepared = false;
		Pixmap pixmap = this.pixmap;
		this.pixmap = null;
		return pixmap;
	}

	@Override
	public boolean disposePixmap () {
		return true;
	}

	@Override
	public int getWidth () {
		return width;
	}

	@Override
	public int getHeight () {
		return height;
	}

	@Override
	public Pixmap.Format getFormat () {
		return format;
	}

	@Override
	public boolean useMipMaps () {
		return useMipMaps;
	}

	@Override
	public boolean isManaged () {
		return true;
	}

	public FileHandle getFileHandle () {
		return file;
	}

	@Override
	public TextureDataType getType () {
		return TextureDataType.Pixmap;
	}

	@Override
	public void consumeCompressedData () {
		throw new GdxRuntimeException("This TextureData implementation does not upload data itself");
	}
}
