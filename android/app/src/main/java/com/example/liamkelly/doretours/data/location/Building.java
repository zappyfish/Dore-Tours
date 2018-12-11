package com.example.liamkelly.doretours.data.location;

import com.google.firebase.database.DataSnapshot;

import java.util.LinkedList;
import java.util.List;

public class Building {

    private final String mName;
    private final double mLatitude;
    private final double mLongitude;
    private final List<Vertex> mVertices;

    public Building(DataSnapshot data) {
        mName = data.getKey();
        double totLat = 0;
        double totLon = 0;
        int ptCnt = 0;

        mVertices = new LinkedList<>();
        for (DataSnapshot coordPair : data.child("coordinates").getChildren()) {
            double lat = (double) coordPair.child("lat").getValue();
            double lon = (double) coordPair.child("lon").getValue();
            totLat += lat;
            totLon += lon;
            ptCnt++;
            mVertices.add(new Vertex(lat, lon));
        }

        mLatitude = totLat / ptCnt;
        mLongitude = totLon / ptCnt;
    }

    public String getName() {
        return mName;
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


    private static class Vertex {
        private double x, y;

        private Vertex(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }
}
