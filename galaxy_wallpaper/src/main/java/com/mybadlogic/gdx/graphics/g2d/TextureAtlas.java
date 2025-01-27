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

package com.mybadlogic.gdx.graphics.g2d;

import com.mybadlogic.gdx.Files;
import com.mybadlogic.gdx.Gdx;
import com.mybadlogic.gdx.files.FileHandle;
import com.mybadlogic.gdx.graphics.Texture;
import com.mybadlogic.gdx.graphics.g2d.TextureAtlas.TextureAtlasData.Page;
import com.mybadlogic.gdx.graphics.g2d.TextureAtlas.TextureAtlasData.Region;
import com.mybadlogic.gdx.utils.Array;
import com.mybadlogic.gdx.utils.Disposable;
import com.mybadlogic.gdx.utils.GdxRuntimeException;
import com.mybadlogic.gdx.utils.ObjectMap;
import com.mybadlogic.gdx.graphics.Pixmap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;

/** Loads images from texture atlases created by TexturePacker.<br>
 * <br>
 * A TextureAtlas must be disposed to free up the resources consumed by the backing textures.
 * @author Nathan Sweet */
public class TextureAtlas implements Disposable {
	static final String[] tuple = new String[2];

	private final HashSet<Texture> textures = new HashSet(4);
	private final ArrayList<AtlasRegion> regions = new ArrayList<AtlasRegion>();

	public static class TextureAtlasData {
		public static class Page {
			public final FileHandle textureFile;
			public Texture texture;
			public final boolean useMipMaps;
			public final Pixmap.Format format;
			public final Texture.TextureFilter minFilter;
			public final Texture.TextureFilter magFilter;
			public final Texture.TextureWrap uWrap;
			public final Texture.TextureWrap vWrap;

			public Page (FileHandle handle, boolean useMipMaps, Pixmap.Format format, Texture.TextureFilter minFilter, Texture.TextureFilter magFilter,
                         Texture.TextureWrap uWrap, Texture.TextureWrap vWrap) {
				this.textureFile = handle;
				this.useMipMaps = useMipMaps;
				this.format = format;
				this.minFilter = minFilter;
				this.magFilter = magFilter;
				this.uWrap = uWrap;
				this.vWrap = vWrap;
			}
		}

		public static class Region {
			public Page page;
			public int index;
			public String name;
			public float offsetX;
			public float offsetY;
			public int originalWidth;
			public int originalHeight;
			public boolean rotate;
			public int left;
			public int top;
			public int width;
			public int height;
			public boolean flip;
		}

		final Array<Page> pages = new Array<Page>();
		final Array<Region> regions = new Array<Region>();

		public TextureAtlasData (FileHandle packFile, FileHandle imagesDir, boolean flip) {
			PriorityQueue<Region> sortedRegions = new PriorityQueue(16, indexComparator);

			BufferedReader reader = new BufferedReader(new InputStreamReader(packFile.read()), 64);
			try {
				Page pageImage = null;
				while (true) {
					String line = reader.readLine();
					if (line == null) break;
					if (line.trim().length() == 0)
						pageImage = null;
					else if (pageImage == null) {
						FileHandle file = imagesDir.child(line);

						Pixmap.Format format = Pixmap.Format.valueOf(readValue(reader));

						readTuple(reader);
						Texture.TextureFilter min = Texture.TextureFilter.valueOf(tuple[0]);
						Texture.TextureFilter max = Texture.TextureFilter.valueOf(tuple[1]);

						String direction = readValue(reader);
						Texture.TextureWrap repeatX = Texture.TextureWrap.ClampToEdge;
						Texture.TextureWrap repeatY = Texture.TextureWrap.ClampToEdge;
						if (direction.equals("x"))
							repeatX = Texture.TextureWrap.Repeat;
						else if (direction.equals("y"))
							repeatY = Texture.TextureWrap.Repeat;
						else if (direction.equals("xy")) {
							repeatX = Texture.TextureWrap.Repeat;
							repeatY = Texture.TextureWrap.Repeat;
						}

						pageImage = new Page(file, min.isMipMap(), format, min, max, repeatX, repeatY);
						pages.add(pageImage);
					} else {
						boolean rotate = Boolean.valueOf(readValue(reader));

						readTuple(reader);
						int left = Integer.parseInt(tuple[0]);
						int top = Integer.parseInt(tuple[1]);

						readTuple(reader);
						int width = Integer.parseInt(tuple[0]);
						int height = Integer.parseInt(tuple[1]);

						Region region = new Region();
						region.page = pageImage;
						region.left = left;
						region.top = top;
						region.width = width;
						region.height = height;
						region.name = line;
						region.rotate = rotate;

						readTuple(reader);
						region.originalWidth = Integer.parseInt(tuple[0]);
						region.originalHeight = Integer.parseInt(tuple[1]);

						readTuple(reader);
						region.offsetX = Integer.parseInt(tuple[0]);
						region.offsetY = Integer.parseInt(tuple[1]);

						region.index = Integer.parseInt(readValue(reader));

						if (flip) region.flip = true;

						sortedRegions.add(region);
					}
				}
			} catch (IOException ex) {
				throw new GdxRuntimeException("Error reading pack file: " + packFile);
			} finally {
				try {
					reader.close();
				} catch (IOException ignored) {
				}
			}

			int n = sortedRegions.size();
			for (int i = 0; i < n; i++)
				regions.add(sortedRegions.poll());
		}

		public Array<Page> getPages () {
			return pages;
		}

		public Array<Region> getRegions () {
			return regions;
		}
	}

	/** Creates an empty atlas to which regions can be added. */
	public TextureAtlas () {
	}

	/** Loads the specified pack file using {@link Files.FileType#Internal}, using the parent directory of the pack file to find the page
	 * images. */
	public TextureAtlas (String internalPackFile) {
		this(Gdx.files.internal(internalPackFile));
	}

	/** Loads the specified pack file, using the parent directory of the pack file to find the page images. */
	public TextureAtlas (FileHandle packFile) {
		this(packFile, packFile.parent());
	}

	/** @param flip If true, all regions loaded will be flipped for use with a perspective where 0,0 is the upper left corner.
	 * @see #TextureAtlas(FileHandle) */
	public TextureAtlas (FileHandle packFile, boolean flip) {
		this(packFile, packFile.parent(), flip);
	}

	public TextureAtlas (FileHandle packFile, FileHandle imagesDir) {
		this(packFile, imagesDir, false);
	}

	/** @param flip If true, all regions loaded will be flipped for use with a perspective where 0,0 is the upper left corner. */
	public TextureAtlas (FileHandle packFile, FileHandle imagesDir, boolean flip) {
		this(new TextureAtlasData(packFile, imagesDir, flip));
	}

	public TextureAtlas (TextureAtlasData data) {
		load(data);
	}

	private void load (TextureAtlasData data) {
		ObjectMap<Page, Texture> pageToTexture = new ObjectMap<Page, Texture>();
		for (Page page : data.pages) {
			Texture texture = null;
			if (page.texture == null) {
				texture = new Texture(page.textureFile, page.format, page.useMipMaps);
				texture.setFilter(page.minFilter, page.magFilter);
				texture.setWrap(page.uWrap, page.vWrap);
			} else {
				texture = page.texture;
				texture.setFilter(page.minFilter, page.magFilter);
				texture.setWrap(page.uWrap, page.vWrap);
			}
			textures.add(texture);
			pageToTexture.put(page, texture);
		}

		for (Region region : data.regions) {
			AtlasRegion atlasRegion = new AtlasRegion(pageToTexture.get(region.page), region.left, region.top, region.width,
				region.height);
			atlasRegion.index = region.index;
			atlasRegion.name = region.name;
			atlasRegion.offsetX = region.offsetX;
			atlasRegion.offsetY = region.offsetY;
			atlasRegion.originalHeight = region.originalHeight;
			atlasRegion.originalWidth = region.originalWidth;
			atlasRegion.rotate = region.rotate;
			if (region.flip) atlasRegion.flip(false, true);
			regions.add(atlasRegion);
		}
	}

	/** Adds a region to the atlas. The specified texture will be disposed when the atlas is disposed. */
	public AtlasRegion addRegion (String name, Texture texture, int x, int y, int width, int height) {
		textures.add(texture);
		AtlasRegion region = new AtlasRegion(texture, x, y, width, height);
		region.name = name;
		region.originalWidth = width;
		region.originalHeight = height;
		region.index = -1;
		regions.add(region);
		return region;
	}

	/** Adds a region to the atlas. The texture for the specified region will be disposed when the atlas is disposed. */
	public AtlasRegion addRegion (String name, TextureRegion textureRegion) {
		return addRegion(name, textureRegion.texture, textureRegion.getRegionX(), textureRegion.getRegionY(),
			textureRegion.getRegionWidth(), textureRegion.getRegionHeight());
	}

	/** Returns all regions in the atlas. */
	public List<AtlasRegion> getRegions () {
		return regions;
	}

	/** Returns the first region found with the specified name. This method uses string comparison to find the region, so the result
	 * should be cached rather than calling this method multiple times.
	 * @return The region, or null. */
	public AtlasRegion findRegion (String name) {
		for (int i = 0, n = regions.size(); i < n; i++)
			if (regions.get(i).name.equals(name)) return regions.get(i);
		return null;
	}

	/** Returns the first region found with the specified name and index. This method uses string comparison to find the region, so
	 * the result should be cached rather than calling this method multiple times.
	 * @return The region, or null. */
	public AtlasRegion findRegion (String name, int index) {
		for (int i = 0, n = regions.size(); i < n; i++) {
			AtlasRegion region = regions.get(i);
			if (!region.name.equals(name)) continue;
			if (region.index != index) continue;
			return region;
		}
		return null;
	}

	/** Returns all regions with the specified name, ordered by smallest to largest {@link AtlasRegion#index index}. This method
	 * uses string comparison to find the regions, so the result should be cached rather than calling this method multiple times. */
	public List<AtlasRegion> findRegions (String name) {
		ArrayList<AtlasRegion> matched = new ArrayList();
		for (int i = 0, n = regions.size(); i < n; i++) {
			AtlasRegion region = regions.get(i);
			if (region.name.equals(name)) matched.add(new AtlasRegion(region));
		}
		return matched;
	}

	/** Returns all regions in the atlas as sprites. This method creates a new sprite for each region, so the result should be
	 * stored rather than calling this method multiple times.
	 * @see #createSprite(String) */
	public List<Sprite> createSprites () {
		ArrayList sprites = new ArrayList(regions.size());
		for (int i = 0, n = regions.size(); i < n; i++)
			sprites.add(newSprite(regions.get(i)));
		return sprites;
	}

	/** Returns the first region found with the specified name as a sprite. If whitespace was stripped from the region when it was
	 * packed, the sprite is automatically positioned as if whitespace had not been stripped. This method uses string comparison to
	 * find the region and constructs a new sprite, so the result should be cached rather than calling this method multiple times.
	 * @return The sprite, or null. */
	public Sprite createSprite (String name) {
		for (int i = 0, n = regions.size(); i < n; i++)
			if (regions.get(i).name.equals(name)) return newSprite(regions.get(i));
		return null;
	}

	/** Returns the first region found with the specified name and index as a sprite. This method uses string comparison to find the
	 * region and constructs a new sprite, so the result should be cached rather than calling this method multiple times.
	 * @return The sprite, or null.
	 * @see #createSprite(String) */
	public Sprite createSprite (String name, int index) {
		for (int i = 0, n = regions.size(); i < n; i++) {
			AtlasRegion region = regions.get(i);
			if (!region.name.equals(name)) continue;
			if (region.index != index) continue;
			return newSprite(regions.get(i));
		}
		return null;
	}

	/** Returns all regions with the specified name as sprites, ordered by smallest to largest {@link AtlasRegion#index index}. This
	 * method uses string comparison to find the regions and constructs new sprites, so the result should be cached rather than
	 * calling this method multiple times.
	 * @see #createSprite(String) */
	public List<Sprite> createSprites (String name) {
		ArrayList<Sprite> matched = new ArrayList();
		for (int i = 0, n = regions.size(); i < n; i++) {
			AtlasRegion region = regions.get(i);
			if (region.name.equals(name)) matched.add(newSprite(region));
		}
		return matched;
	}

	private Sprite newSprite (AtlasRegion region) {
		if (region.packedWidth == region.originalWidth && region.packedHeight == region.originalHeight) {
			if (region.rotate) {
				Sprite sprite = new Sprite(region);
				sprite.setBounds(0, 0, region.getRegionHeight(), region.getRegionWidth());
				sprite.rotate90(true);
				return sprite;
			}
			return new Sprite(region);
		}
		return new AtlasSprite(region);
	}

	/** Releases all resources associated with this TextureAtlas instance. This releases all the textures backing all TextureRegions
	 * and Sprites, which should no longer be used after calling dispose. */
	public void dispose () {
		for (Texture texture : textures)
			texture.dispose();
		textures.clear();
	}

	static final Comparator<Region> indexComparator = new Comparator<Region>() {
		public int compare (Region region1, Region region2) {
			int i1 = region1.index;
			if (i1 == -1) i1 = Integer.MAX_VALUE;
			int i2 = region2.index;
			if (i2 == -1) i2 = Integer.MAX_VALUE;
			return i1 - i2;
		}
	};

	static String readValue (BufferedReader reader) throws IOException {
		String line = reader.readLine();
		int colon = line.indexOf(':');
		if (colon == -1) throw new GdxRuntimeException("Invalid line: " + line);
		return line.substring(colon + 1).trim();
	}

	static void readTuple (BufferedReader reader) throws IOException {
		String line = reader.readLine();
		int colon = line.indexOf(':');
		int comma = line.indexOf(',');
		if (colon == -1 || comma == -1 || comma < colon + 1) throw new GdxRuntimeException("Invalid line: " + line);
		tuple[0] = line.substring(colon + 1, comma).trim();
		tuple[1] = line.substring(comma + 1).trim();
	}

	/** Describes the region of a packed image and provides information about the original image before it was packed. */
	static public class AtlasRegion extends TextureRegion {
		/** The number at the end of the original image file name, or -1 if none.<br>
		 * <br>
		 * When sprites are packed, if the original file name ends with a number, it is stored as the index and is not considered as
		 * part of the sprite's name. This is useful for keeping animation frames in order.
		 * @see TextureAtlas#findRegions(String) */
		public int index;

		/** The name of the original image file, up to the first underscore. Underscores denote special instructions to the texture
		 * packer. */
		public String name;

		/** The offset from the left of the original image to the left of the packed image, after whitespace was removed for packing. */
		public float offsetX;

		/** The offset from the bottom of the original image to the bottom of the packed image, after whitespace was removed for
		 * packing. */
		public float offsetY;

		/** The width of the image, after whitespace was removed for packing. */
		public int packedWidth;

		/** The height of the image, after whitespace was removed for packing. */
		public int packedHeight;

		/** The width of the image, before whitespace was removed for packing. */
		public int originalWidth;

		/** The height of the image, before whitespace was removed for packing. */
		public int originalHeight;

		/** If true, the region has been rotated 90 degrees counter clockwise. */
		public boolean rotate;

		public AtlasRegion (Texture texture, int x, int y, int width, int height) {
			super(texture, x, y, width, height);
			packedWidth = width;
			packedHeight = height;
		}

		public AtlasRegion (AtlasRegion region) {
			setRegion(region);
			index = region.index;
			name = region.name;
			offsetX = region.offsetX;
			offsetY = region.offsetY;
			packedWidth = region.packedWidth;
			packedHeight = region.packedHeight;
			originalWidth = region.originalWidth;
			originalHeight = region.originalHeight;
			rotate = region.rotate;
		}

		/** Flips the region, adjusting the offset so the image appears to be flipped as if no whitespace has been removed for
		 * packing. */
		public void flip (boolean x, boolean y) {
			super.flip(x, y);
			if (x) offsetX = originalWidth - offsetX - packedWidth;
			if (y) offsetY = originalHeight - offsetY - packedHeight;
		}
	}

	/** A sprite that, if whitespace was stripped from the region when it was packed, is automatically positioned as if whitespace
	 * had not been stripped. */
	static public class AtlasSprite extends Sprite {
		final AtlasRegion region;
		float originalOffsetX, originalOffsetY;

		public AtlasSprite (AtlasRegion region) {
			this.region = new AtlasRegion(region);
			originalOffsetX = region.offsetX;
			originalOffsetY = region.offsetY;
			setRegion(region);
			setOrigin(region.originalWidth / 2f, region.originalHeight / 2f);
			int width = Math.abs(region.getRegionWidth());
			int height = Math.abs(region.getRegionHeight());
			if (region.rotate) {
				rotate90(true);
				super.setBounds(region.offsetX, region.offsetY, height, width);
			} else
				super.setBounds(region.offsetX, region.offsetY, width, height);
			setColor(1, 1, 1, 1);
		}

		public void setPosition (float x, float y) {
			super.setPosition(x + region.offsetX, y + region.offsetY);
		}

		public void setBounds (float x, float y, float width, float height) {
			float widthRatio = width / region.originalWidth;
			float heightRatio = height / region.originalHeight;
			region.offsetX = originalOffsetX * widthRatio;
			region.offsetY = originalOffsetY * heightRatio;
			super.setBounds(x + region.offsetX, y + region.offsetY, region.packedWidth * widthRatio, region.packedHeight
				* heightRatio);
		}

		public void setSize (float width, float height) {
			setBounds(getX(), getY(), width, height);
		}

		public void setOrigin (float originX, float originY) {
			super.setOrigin(originX - region.offsetX, originY - region.offsetY);
		}

		public void flip (boolean x, boolean y) {
			// Flip texture.
			super.flip(x, y);

			float oldOriginX = getOriginX();
			float oldOriginY = getOriginY();
			float oldOffsetX = region.offsetX;
			float oldOffsetY = region.offsetY;

			// Updates x and y offsets.
			region.flip(x, y);

			// Update position and origin with new offsets.
			translate(region.offsetX - oldOffsetX, region.offsetY - oldOffsetY);
			setOrigin(oldOriginX, oldOriginY);
		}

		public float getX () {
			return super.getX() - region.offsetX;
		}

		public float getY () {
			return super.getY() - region.offsetY;
		}

		public float getOriginX () {
			return super.getOriginX() + region.offsetX;
		}

		public float getOriginY () {
			return super.getOriginY() + region.offsetY;
		}

		public float getWidth () {
			return super.getWidth() / region.packedWidth * region.originalWidth;
		}

		public float getHeight () {
			return super.getHeight() / region.packedHeight * region.originalHeight;
		}

		public AtlasRegion getAtlasRegion () {
			return region;
		}
	}
}
