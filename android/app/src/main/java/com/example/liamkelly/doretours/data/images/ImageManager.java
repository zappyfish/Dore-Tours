package com.example.liamkelly.doretours.data.images;

import android.graphics.Bitmap;

public class ImageManager {

    private static ImageManager sInstance;

    public static ImageManager getInstance() {
        if (sInstance == null) {
            sInstance = new ImageManager();
        }
        return sInstance;
    }

    private ImageManager() {

    }
}
