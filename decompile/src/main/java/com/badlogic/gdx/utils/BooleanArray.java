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

package com.badlogic.gdx.utils;

import java.util.BitSet;

import com.badlogic.gdx.math.MathUtils;

/** A resizable, ordered or unordered boolean array. Avoids the boxing that occurs with ArrayList<Boolean>. It is less memory
 * efficient than {@link BitSet}, except for very small sizes. It more CPU efficient than {@link BitSet}, except for very large
 * sizes or if BitSet functionality such as and, or, xor, etc are needed. If unordered, this class avoids a memory copy when
 * removing elements (the last element is moved to the removed element's position).
 * @author Nathan Sweet */
public class BooleanArray {
	public boolean[] items;
	public int size;
	public boolean ordered;

	/** Creates an ordered array with a capacity of 16. */
	public BooleanArray () {
		this(true, 16);
	}

	/** Creates an ordered array with the specified capacity. */
	public BooleanArray (int capacity) {
		this(true, capacity);
	}

	/** @param ordered If false, methods that remove elements may change the order of other elements in the array, which avoids a
	 *           memory copy.
	 * @param capacity Any elements added beyond this will cause the backing array to be grown. */
	public BooleanArray (boolean ordered, int capacity) {
		this.ordered = ordered;
		items = new boolean[capacity];
	}

	/** Creates a new array containing the elements in the specific array. The new array will be ordered if the specific array is
	 * ordered. The capacity is set to the number of elements, so any subsequent elements added will cause the backing array to be
	 * grown. */
	public BooleanArray (BooleanArray array) {
		this.ordered = array.ordered;
		size = array.size;
		items = new boolean[size];
		System.arraycopy(array.items, 0, items, 0, size);
	}

	/** Creates a new ordered array containing the elements in the specified array. The capacity is set to the number of elements,
	 * so any subsequent elements added will cause the backing array to be grown. */
	public BooleanArray (boolean[] array) {
		this(true, array);
	}

	/** Creates a new array containing the elements in the specified array. The capacity is set to the number of elements, so any
	 * subsequent elements added will cause the backing array to be grown.
	 * @param ordered If false, methods that remove elements may change the order of other elements in the array, which avoids a
	 *           memory copy. */
	public BooleanArray (boolean ordered, boolean[] array) {
		this(ordered, array.length);
		size = array.length;
		System.arraycopy(array, 0, items, 0, size);
	}

	public void add (boolean value) {
		boolean[] items = this.items;
		if (size == items.length) items = resize(Math.max(8, (int)(size * 1.75f)));
		items[size++] = value;
	}

	public void addAll (BooleanArray array) {
		addAll(array, 0, array.size);
	}

	public void addAll (BooleanArray array, int offset, int length) {
		if (offset + length > array.size)
			throw new IllegalArgumentException("offset + length must be <= size: " + offset + " + " + length + " <= " + array.size);
		addAll(array.items, offset, length);
	}

	public void addAll (boolean[] array) {
		addAll(array, 0, array.length);
	}

	public void addAll (boolean[] array, int offset, int length) {
		boolean[] items = this.items;
		int sizeNeeded = size + length - offset;
		if (sizeNeeded >= items.length) items = resize(Math.max(8, (int)(sizeNeeded * 1.75f)));
		System.arraycopy(array, offset, items, size, length);
		size += length;
	}

	public boolean get (int index) {
		if (index >= size) throw new IndexOutOfBoundsException(String.valueOf(index));
		return items[index];
	}

	public void set (int index, boolean value) {
		if (index >= size) throw new IndexOutOfBoundsException(String.valueOf(index));
		items[index] = value;
	}

	public void insert (int index, boolean value) {
		boolean[] items = this.items;
		if (size == items.length) items = resize(Math.max(8, (int)(size * 1.75f)));
		if (ordered)
			System.arraycopy(items, index, items, index + 1, size - index);
		else
			items[size] = items[index];
		size++;
		items[index] = value;
	}

	/** Removes and returns the item at the specified index. */
	public boolean removeIndex (int index) {
		if (index >= size) throw new IndexOutOfBoundsException(String.valueOf(index));
		boolean[] items = this.items;
		boolean value = items[index];
		size--;
		if (ordered)
			System.arraycopy(items, index + 1, items, index, size - index);
		else
			items[index] = items[size];
		return value;
	}

	/** Removes and returns the last item. */
	public boolean pop () {
		return items[--size];
	}

	/** Returns the last item. */
	public boolean peek () {
		return items[size - 1];
	}

	public void clear () {
		size = 0;
	}

	/** Reduces the size of the backing array to the size of the actual items. This is useful to release memory when many items have
	 * been removed, or if it is known that more items will not be added. */
	public void shrink () {
		resize(size);
	}

	/** Increases the size of the backing array to acommodate the specified number of additional items. Useful before adding many
	 * items to avoid multiple backing array resizes.
	 * @return {@link #items} */
	public boolean[] ensureCapacity (int additionalCapacity) {
		int sizeNeeded = size + additionalCapacity;
		if (sizeNeeded >= items.length) resize(Math.max(8, sizeNeeded));
		return items;
	}

	protected boolean[] resize (int newSize) {
		boolean[] newItems = new boolean[newSize];
		boolean[] items = this.items;
		System.arraycopy(items, 0, newItems, 0, Math.min(items.length, newItems.length));
		this.items = newItems;
		return newItems;
	}

	public void reverse () {
		for (int i = 0, lastIndex = size - 1, n = size / 2; i < n; i++) {
			int ii = lastIndex - i;
			boolean temp = items[i];
			items[i] = items[ii];
			items[ii] = temp;
		}
	}

	public void shuffle () {
		for (int i = size - 1; i >= 0; i--) {
			int ii = MathUtils.random(i);
			boolean temp = items[i];
			items[i] = items[ii];
			items[ii] = temp;
		}
	}

	public String toString () {
		if (size == 0) return "[]";
		boolean[] items = this.items;
		StringBuilder buffer = new StringBuilder(32);
		buffer.append('[');
		buffer.append(items[0]);
		for (int i = 1; i < size; i++) {
			buffer.append(", ");
			buffer.append(items[i]);
		}
		buffer.append(']');
		return buffer.toString();
	}
}
