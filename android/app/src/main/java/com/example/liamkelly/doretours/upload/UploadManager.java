package com.example.liamkelly.doretours.upload;

import android.content.Context;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.UUID;

import androidx.annotation.NonNull;

public class UploadManager {

    private static UploadManager sInstance;
    private final StorageReference mStorageRef;

    private UploadManager() {
        mStorageRef = FirebaseStorage.getInstance().getReference();
    }

    public static synchronized UploadManager getInstance() {
        if (sInstance == null) {
            sInstance= new UploadManager();
        }
        return sInstance;
    }

    public void uploadDataPoint(LabeledData data, final Context context) {
        String id = UUID.randomUUID() + ".jpg";
        UploadTask uploadTask = mStorageRef.child(data.getName() + "/" + id).putBytes(data.getImage());
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "Upload Failed: " + e.getCause().getMessage(), Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(context, "Upload Succesful", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
