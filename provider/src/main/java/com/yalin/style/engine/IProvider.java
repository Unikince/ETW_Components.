package com.yalin.style.engine;

import android.content.Context;
import android.service.wallpaper.WallpaperService;

/**
 * @author jinyalin
 * @since 2018/4/28.
 */

public interface IProvider {
    WallpaperService provideProxy(Context host);
}