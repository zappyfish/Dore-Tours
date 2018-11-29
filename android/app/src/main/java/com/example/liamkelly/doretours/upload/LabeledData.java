package com.example.liamkelly.doretours.upload;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.ByteArrayOutputStream;

public class LabeledData {

    private static final int MAX_DIMENSION = 300;

    private final String mBuildingName;
    private final byte[] mImage;

    public LabeledData(String name, byte[] img) {
        mBuildingName = name;
        mImage = scale(img);
    }

    private double getScaleFactor(int dim) {
        return MAX_DIMENSION / ((double)dim);
    }

    private double determineScaleFactor(Bitmap original) {
        double widthScale = getScaleFactor(original.getWidth());
        double heightScale = getScaleFactor(original.getHeight());
        // Choose the smaller scale factor to ensure BOTH are under MAX_DIMENSION
        return widthScale < heightScale ? widthScale : heightScale;
    }

    private byte[] scale(byte[] data) {
        Bitmap asBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
        double scaleFactor = determineScaleFactor(asBitmap);
        int width = (int)(scaleFactor * asBitmap.getWidth());
        int height = (int)(scaleFactor * asBitmap.getHeight());
        Bitmap resized = Bitmap.createScaledBitmap(asBitmap, width, height, true);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        resized.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        return stream.toByteArray();
    }

    public String getName() {
        return mBuildingName;
    }

    public byte[] getImage() {
        return mImage;
    }
}
