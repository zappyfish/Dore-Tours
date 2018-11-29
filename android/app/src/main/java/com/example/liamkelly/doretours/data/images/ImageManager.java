package com.example.liamkelly.doretours.data.images;

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
