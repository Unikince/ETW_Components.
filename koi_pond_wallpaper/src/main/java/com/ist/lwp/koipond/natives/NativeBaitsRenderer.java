package com.ist.lwp.koipond.natives;

public class NativeBaitsRenderer {
    public NativeBaitsRenderer() {
        NativeLibraryMethods.baitsrenderer_init();
    }

    public void addBait(int screenX, int screenY, int pointer) {
        NativeLibraryMethods.baitsrenderer_addBait(screenX, screenY, pointer);
    }
}
