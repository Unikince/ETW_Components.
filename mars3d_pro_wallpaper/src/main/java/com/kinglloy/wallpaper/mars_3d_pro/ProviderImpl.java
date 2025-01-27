package com.kinglloy.wallpaper.mars_3d_pro;

import android.content.Context;
import android.service.wallpaper.WallpaperService;

import com.codekonditor.mars.Wallpaper;
import com.yalin.style.engine.IProvider;

/**
 * @author jinyalin
 * @since 2017/7/28.
 */

public class ProviderImpl implements IProvider {
    @Override
    public WallpaperService provideProxy(Context host) {
        return new Wallpaper(host);
    }
}
