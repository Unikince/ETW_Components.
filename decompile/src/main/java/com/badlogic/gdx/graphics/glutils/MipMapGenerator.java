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
package com.badlogic.gdx.graphics.glutils;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GLCommon;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class MipMapGenerator {
	private static boolean useHWMipMap = true;

	static public void setUseHardwareMipMap (boolean useHWMipMap) {
		MipMapGenerator.useHWMipMap = useHWMipMap;
	}

	/** Sets the image data of the {@link Texture} based on the {@link Pixmap}. The texture must be bound for this to work. If
	 * <code>disposePixmap</code> is true, the pixmap will be disposed at the end of the method.
	 * @param pixmap the Pixmap
	 * @param disposePixmap whether to dispose the Pixmap after upload */
	public static void generateMipMap (Pixmap pixmap, int textureWidth, int textureHeight, boolean disposePixmap) {
		if (!useHWMipMap) {
			generateMipMapCPU(pixmap, textureWidth, textureHeight, disposePixmap);
			return;
		}

		if (Gdx.app.getType() == ApplicationType.Android) {
			if (Gdx.graphics.isGL20Available())
				generateMipMapGLES20(pixmap, disposePixmap);
			else
				generateMipMapCPU(pixmap, textureWidth, textureHeight, disposePixmap);
		} else {
			generateMipMapDesktop(pixmap, textureWidth, textureHeight, disposePixmap);
		}
	}

	private static void generateMipMapGLES20 (Pixmap pixmap, boolean disposePixmap) {
		Gdx.gl.glTexImage2D(GL20.GL_TEXTURE_2D, 0, pixmap.getGLInternalFormat(), pixmap.getWidth(), pixmap.getHeight(), 0,
			pixmap.getGLFormat(), pixmap.getGLType(), pixmap.getPixels());
		Gdx.gl20.glGenerateMipmap(GL20.GL_TEXTURE_2D);
		if (disposePixmap) pixmap.dispose();
	}

	private static void generateMipMapDesktop (Pixmap pixmap, int textureWidth, int textureHeight, boolean disposePixmap) {
		if (Gdx.graphics.isGL20Available()
			&& (Gdx.graphics.supportsExtension("GL_ARB_framebuffer_object") || Gdx.graphics
				.supportsExtension("GL_EXT_framebuffer_object"))) {
			Gdx.gl.glTexImage2D(GL20.GL_TEXTURE_2D, 0, pixmap.getGLInternalFormat(), pixmap.getWidth(), pixmap.getHeight(), 0,
				pixmap.getGLFormat(), pixmap.getGLType(), pixmap.getPixels());
			Gdx.gl20.glGenerateMipmap(GL20.GL_TEXTURE_2D);
			if (disposePixmap) pixmap.dispose();
		} else if (Gdx.graphics.supportsExtension("GL_SGIS_generate_mipmap")) {
			if ((Gdx.gl20 == null) && textureWidth != textureHeight)
				throw new GdxRuntimeException("texture width and height must be square when using mipmapping in OpenGL ES 1.x");
			Gdx.gl.glTexParameterf(GL20.GL_TEXTURE_2D, GLCommon.GL_GENERATE_MIPMAP, GL10.GL_TRUE);
			Gdx.gl.glTexImage2D(GL20.GL_TEXTURE_2D, 0, pixmap.getGLInternalFormat(), pixmap.getWidth(), pixmap.getHeight(), 0,
				pixmap.getGLFormat(), pixmap.getGLType(), pixmap.getPixels());
			if (disposePixmap) pixmap.dispose();
		} else {
			generateMipMapCPU(pixmap, textureWidth, textureHeight, disposePixmap);
		}
	}

	private static void generateMipMapCPU (Pixmap pixmap, int textureWidth, int textureHeight, boolean disposePixmap) {
		Gdx.gl.glTexImage2D(GL10.GL_TEXTURE_2D, 0, pixmap.getGLInternalFormat(), pixmap.getWidth(), pixmap.getHeight(), 0,
			pixmap.getGLFormat(), pixmap.getGLType(), pixmap.getPixels());
		if ((Gdx.gl20 == null) && textureWidth != textureHeight)
			throw new GdxRuntimeException("texture width and height must be square when using mipmapping.");
		int width = pixmap.getWidth() / 2;
		int height = pixmap.getHeight() / 2;
		int level = 1;
		while (width > 0 && height > 0) {
			Pixmap tmp = new Pixmap(width, height, pixmap.getFormat());
			tmp.drawPixmap(pixmap, 0, 0, pixmap.getWidth(), pixmap.getHeight(), 0, 0, width, height);
			if (level > 1 || disposePixmap) pixmap.dispose();
			pixmap = tmp;

			Gdx.gl.glTexImage2D(GL10.GL_TEXTURE_2D, level, pixmap.getGLInternalFormat(), pixmap.getWidth(), pixmap.getHeight(), 0,
				pixmap.getGLFormat(), pixmap.getGLType(), pixmap.getPixels());

			width = pixmap.getWidth() / 2;
			height = pixmap.getHeight() / 2;
			level++;
		}
		pixmap.dispose();
	}
}