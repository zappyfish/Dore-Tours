package com.example.liamkelly.doretours.data.location;

import com.google.firebase.database.DataSnapshot;

import java.util.LinkedList;
import java.util.List;

public class Campus {

    private static class Vertex {
        private double y, x;

        private Vertex(double latitude, double longitude) {
            this.x = latitude;
            this.y = longitude;
        }
    }

    private static final double VIEW_STEP_SIZE = 0.0003;

    private final List<List<Vertex>> mCampusCoords;
    private final List<Building> mBuildings;
    private final String mName;

    public Campus(DataSnapshot data) {
        mName = data.getKey();
        mCampusCoords = new LinkedList<>();
        for (DataSnapshot child : data.child("coordinates").getChildren()) {
            mCampusCoords.add(getCampusSection(child));
        }
        mBuildings = new LinkedList<>();
    }

    public void setBuildings(DataSnapshot data) {
        mBuildings.clear();
        for (DataSnapshot child : data.child("buildings").getChildren()) {
            mBuildings.add(new Building(child));
        }
    }

    public List<Building> getBuildings() {
        return mBuildings;
    }

    public String getName() {
        return mName;
    }

    private List<Vertex> getCampusSection(DataSnapshot data) {
        List<Vertex> section = new LinkedList<>();
        for (DataSnapshot coordPair : data.getChildren()) {
            double lat = (double) coordPair.child("lat").getValue();
            double lon = (double) coordPair.child("lon").getValue();
            section.add(new Vertex(lat, lon));
        }
        return section;
    }

    public boolean inCampus(double latitude, double longitude) {
        for (List<Vertex> campusContour : mCampusCoords) {
            if (inCampus(latitude, longitude, campusContour)) {
                return true;
            }
        }
        return false;
    }

    private boolean inCampus(double latitude, double longitude, List<Vertex> coords) {
        int i, j;
        double x = latitude, y = longitude;
        boolean inside = false;
        for(i = 0, j = coords.size() - 1; i < coords.size(); j = i++) {
            if( (coords.get(i).y > y) != (coords.get(j).y > y) &&
                    (x < (coords.get(j).x - coords.get(i).x) * (y -  coords.get(i).y) / (coords.get(j).y - coords.get(i).y) + coords.get(i).x)) {
                inside = !inside;
            }
        }
        return inside;
    }

    public Building getBuildingInView(double northMagnitude, double eastMagnitude, double lat, double lon) {
        if (inCampus(lat, lon)) {
            for (Building building : mBuildings) {
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
}
