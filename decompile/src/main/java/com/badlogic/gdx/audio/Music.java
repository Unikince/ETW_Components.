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

package com.badlogic.gdx.audio;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Audio;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Disposable;

/** <p>
 * A Music instance represents a streamed audio file. The interface supports setting the play back position, pausing and resuming
 * and so on. When you are done with using the Music instance you have to dispose it via the {@link #dispose()} method.
 * </p>
 * 
 * <p>
 * Music instances are created via {@link Audio#newMusic(FileHandle)}.
 * </p>
 * 
 * <p>
 * Music instances are automatically paused and resumed when an {@link Application} is paused or resumed. See
 * {@link ApplicationListener}.
 * </p>
 * 
 * @author mzechner */
public interface Music extends Disposable {
	/** Starts the play back of the music stream. In case the stream was paused this will resume the play back. In case the music
	 * stream is finished playing this will restart the play back. */
	public void play();

	/** Pauses the play back. If the music stream has not been started yet or has finished playing a call to this method will be
	 * ignored. */
	public void pause();

	/** Stops a playing or paused Music instance. Next time play() is invoked the Music will start from the beginning. */
	public void stop();

	/** @return whether this music stream is playing */
	public boolean isPlaying();

	/** Sets whether the music stream is looping. This can be called at any time, whether the stream is playing.
	 * 
	 * @param isLooping whether to loop the stream */
	public void setLooping(boolean isLooping);

	/** @return whether the music stream is playing. */
	public boolean isLooping();

	/** Sets the volume of this music stream. The volume must be given in the range [0,1] with 0 being silent and 1 being the
	 * maximum volume.
	 * 
	 * @param volume */
	public void setVolume(float volume);

	/** Returns the playback position in milliseconds. */
	public float getPosition();

	/** Needs to be called when the Music is no longer needed. */
	public void dispose();
}
