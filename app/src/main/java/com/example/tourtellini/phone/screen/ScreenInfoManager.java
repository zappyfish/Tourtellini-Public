package com.example.tourtellini.phone.screen;

// TODO: Implement
public class ScreenInfoManager {

    private static ScreenInfoManager sInstance;

    private ScreenInfoManager() {}

    public static ScreenInfoManager getInstance() {
        if (sInstance == null) {
            sInstance = new ScreenInfoManager();
        }

        return sInstance;
    }

    public ScreenInfo getScreenInfo() {
        return new ScreenInfo();
    }
}
