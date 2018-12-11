package com.example.liamkelly.doretours.data.location;

import java.util.LinkedList;
import java.util.List;

import androidx.annotation.Nullable;

public enum HardcodedBuilding {

    FEATHERINGILL("Featheringill", 36.144721, -86.803184,
            36.144511, -86.802817,
            36.144647, -86.803421,
            36.144925, -86.803382,
            36.144885, -86.802828),
    STEVENSON("Stevenson", 36.144803,-86.801813,
            36.145388, -86.801787,
            36.145230, -86.801818,
            36.145163, -86.801809,
            36.145098, -86.801904,
            36.144831, -86.801928,
            36.144823, -86.801701,
            36.144734, -86.801709,
            36.144779, -86.802351,
            36.144617, -86.802364,
            36.144600, -86.802196,
            36.144556, -86.802215,
            36.144562, -86.802282,
            36.144189, -86.802325,
            36.144172, -86.801974,
            36.144497, -86.801923,
            36.144497, -86.801561,
            36.144637, -86.801451,
            36.145174, -86.801368,
            36.145196, -86.801237,
            36.145352, -86.801213),
    BUTTRICK("Buttrick", 36.145796, -86.802585,
            36.146126, -86.802733,
            36.145470, -86.802813,
            36.145470, -86.802692,
            36.145559, -86.802561,
            36.145648, -86.802558,
            36.145644, -86.802448,
            36.145678, -86.802385,
            36.146178, -86.802329,
            36.146186, -86.802353,
            36.146224, -86.802348,
            36.146237, -86.802469),
    WILSON("Wilson", 36.148967, -86.800677,
            36.149203, -86.801075,
            36.148523, -86.800576,
            36.148740, -86.800243,
            36.149372, -86.800718),
    KISSAM("Kissam", 36.149448, -86.801680,
            36.149363, -86.802778,
            36.148735, -86.802279,
            36.149435, -86.800785,
            36.150083, -86.801268),
    RAND("Rand", 36.146430, -86.80311,
            36.146037, -86.803061,
            36.146268, -86.803260,
            36.146053, -86.803921,
            36.146185, -86.804015,
            36.146321, -86.803792,
            36.146726, -86.804012,
            36.146778, -86.803752,
            36.146668, -86.803647,
            36.146705, -86.803529,
            36.146664, -86.803465,
            36.146813, -86.803119,
            36.146255, -86.802697,
            36.146184, -86.802732),
    BRANSCOMB("Branscomb", 36.144979, -86.805527,
            36.144958, -86.805980,
            36.144635, -86.805768,
            36.144778, -86.805446,
            36.144897, -86.805524,
            36.144945, -86.805430,
            36.144302, -86.804934,
            36.144640, -86.804258,
            36.145476, -86.804907),
    CENTRAL_LIBRARY("Central Library", 36.145762, -86.800395,
            36.145998, -86.800549,
            36.145485, -86.800624,
            36.145457, -86.800125,
            36.145545, -86.800092,
            36.145538, -86.799867,
            36.145878, -86.799813,
            36.145904, -86.800028,
            36.145965, -86.800025),
    TOWERS("Carmichael Towers", 36.147884, -86.805769,
            36.147050, -86.806986,
            36.147901, -86.805261,
            36.148135, -86.805446,
            36.147324, -86.807150);
    // include towers probably lol

    private static final double DISTANCE_THRESHOLD = 0.0002;
    private static final double VIEW_STEP_SIZE = 0.0003;

    private final String mName;
    private final double mLatitude;
    private final double mLongitude;
    private final List<Vertex> mVertices;

    HardcodedBuilding(String name, double lat, double longitude, double... coords) {
        mName = name;
        mLatitude = lat;
        mLongitude = longitude;
        mVertices = new LinkedList<>();
        for (int i = 0; i < coords.length; i+=2) {
            mVertices.add(new Vertex(coords[i], coords[i + 1]));
        }
    }

    public boolean inBuilding(double latitude, double longitude) {
        int i, j;
        double x = latitude, y = longitude;
        boolean inside = false;
        for(i = 0, j = mVertices.size() - 1; i < mVertices.size(); j = i++) {
            if( (mVertices.get(i).y > y) != (mVertices.get(j).y > y) &&
                    (x < (mVertices.get(j).x - mVertices.get(i).x) * (y - mVertices.get(i).y) / (mVertices.get(j).y - mVertices.get(i).y) + mVertices.get(i).x)) {
                inside = !inside;
            }
        }
        return inside;
    }

    public String getName() {
        return mName;
    }

    public double getLatitude() {
        return mLatitude;
    }

    public double getLongitude() {
        return mLongitude;
    }

    @Nullable
    public static HardcodedBuilding getBuildingInView(double northMagnitude, double eastMagnitude, double lat, double lon) {
        if (Vanderbilt.inVanderbilt(lat, lon)) {
            for (HardcodedBuilding building : HardcodedBuilding.values()) {
                if (building.inBuilding(lat, lon)) {
                    return building;
                }
            }
            lat += northMagnitude * VIEW_STEP_SIZE;
            lon += eastMagnitude * VIEW_STEP_SIZE;
            return getBuildingInView(northMagnitude, eastMagnitude, lat, lon);
        } else {
            return null;
        }
    }

    private static class Vertex {
        private double x, y;

        private Vertex(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }
}
