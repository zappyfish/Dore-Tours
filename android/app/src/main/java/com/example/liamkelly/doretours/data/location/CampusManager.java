package com.example.liamkelly.doretours.data.location;

import android.content.Context;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.LinkedList;
import java.util.List;

public class CampusManager {


    private static CampusManager sInstance;
    private Campus mActiveCampus;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mReference;
    private CampusesReadyCallback mCampusesReadyCallback;
    private final List<Campus> mCampuses;

    public static synchronized CampusManager getInstance() {
        if (sInstance == null) {
            sInstance = new CampusManager();
        }
        return sInstance;
    }

    public void loadCampuses(CampusesReadyCallback callback) {
        mCampusesReadyCallback = callback;
        mReference.child("campuses").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mCampuses.clear();
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    mCampuses.add(new Campus(child));
                }

                if (mCampusesReadyCallback != null) {
                    mCampusesReadyCallback.callback(mCampuses);
                    // mCampusesReadyCallback = null; // One time load
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void loadBuildings(final Campus campus, final BuildingsReadyCallback callback) {
        mReference.child("campuses").child(campus.getName()).addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        campus.setBuildings(dataSnapshot);
                        callback.callback();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                }
        );
    }

    public void setActiveCampus(Campus campus) {
        mActiveCampus = campus;
    }

    private CampusManager() {
        mDatabase = FirebaseDatabase.getInstance();
        mReference = mDatabase.getReference();
        mCampuses = new LinkedList<>();
    }

    public Campus getActiveCampus() {
        return mActiveCampus;
    }

    private List<Campus> getCampuses() {
        return mCampuses;
    }

    private void getCampusCoords() {

    }

    private void getBuildings() {

    }

    private void getBuildingCoords() {

    }

    public interface CampusesReadyCallback {
        void callback(List<Campus> campuses);
    }

    public interface BuildingsReadyCallback {
        void callback();
    }
}
