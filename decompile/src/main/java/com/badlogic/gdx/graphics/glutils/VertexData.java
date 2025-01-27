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

import java.nio.FloatBuffer;

import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.utils.Disposable;

/** A VertexData instance holds vertices for rendering with OpenGL. It is implemented as either a {@link VertexArray} or a
 * {@link VertexBufferObject}. Only the later supports OpenGL ES 2.0.
 * 
 * @author mzechner */
public interface VertexData extends Disposable {
	/** @return the number of vertices this VertexData stores */
	public int getNumVertices();

	/** @return the number of vertices this VertedData can store */
	public int getNumMaxVertices();

	/** @return the {@link VertexAttributes} as specified during construction. */
	public VertexAttributes getAttributes();

	/** <p>
	 * Sets the vertices of this VertexData, discarding the old vertex data. The count must equal the number of floats per vertex
	 * times the number of vertices to be copied to this VertexData. The order of the vertex attributes must be the same as
	 * specified at construction time via {@link VertexAttributes}.
	 * </p>
	 * 
	 * <p>
	 * This can be called in between calls to {@link #bind()} and {@link #unbind()}. The vertex data will be updated instantly.
	 * </p>
	 * 
	 * @param vertices the vertex data
	 * @param offset the offset to start copying the data from
	 * @param count the number of floats to copy */
	public void setVertices(float[] vertices, int offset, int count);

	/** <p>
	 * Returns the underlying FloatBuffer. If you modify the buffer contents they will be uploaded on the next call to
	 * {@link #bind()}. If you need immediate uploading use {@link #setVertices(float[], int, int)};
	 * </p>
	 * 
	 * @return the underlying FloatBuffer holding the vertex data. */
	public FloatBuffer getBuffer();

	/** Binds this VertexData for rendering via glDrawArrays or glDrawElements. */
	public void bind();

	/** Unbinds this VertexData. */
	public void unbind();

	/** Disposes this VertexData and all its associated OpenGL resources. */
	public void dispose();
}
