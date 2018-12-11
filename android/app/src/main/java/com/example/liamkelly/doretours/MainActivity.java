package com.example.liamkelly.doretours;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.example.liamkelly.doretours.data.location.Campus;
import com.example.liamkelly.doretours.data.location.CampusManager;

import java.util.List;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    Button trainButton;
    Button navigateButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        trainButton = (Button) findViewById(R.id.train_button);
        trainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Campus activeCampus = CampusManager.getInstance().getActiveCampus();
                if (activeCampus != null) {
                    CampusManager.getInstance().loadBuildings(activeCampus, new CampusManager.BuildingsReadyCallback() {
                        @Override
                        public void callback() {
                            startActivity(new Intent(MainActivity.this, TrainingActivity.class));
                        }
                    });
                }
            }
        });
        navigateButton = (Button) findViewById(R.id.navigate_button);
        navigateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, MainAppActivity.class));
            }
        });

        final Spinner spinner = (Spinner) findViewById(R.id.campuses);

        CampusManager.getInstance().loadCampuses(new CampusManager.CampusesReadyCallback() {
            @Override
            public void callback(final List<Campus> campuses) {
                String[] campusNames = new String[campuses.size()];
                for (int i = 0; i < campuses.size(); i++) {
                    campusNames[i] = campuses.get(i).getName();
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_item,
                        campusNames);
                spinner.setAdapter(adapter);

                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        CampusManager.getInstance().setActiveCampus(campuses.get(position));
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
            }
        });
    }
}
