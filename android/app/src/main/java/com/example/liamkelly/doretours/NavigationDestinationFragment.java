package com.example.liamkelly.doretours;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.liamkelly.doretours.data.location.HardcodedBuilding;
import com.example.liamkelly.doretours.data.location.NavigationSession;

import androidx.fragment.app.Fragment;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link NavigationDestinationFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class NavigationDestinationFragment extends Fragment {


    private Button mDestinationSelectionButton;
    private TextView mDestinationText;
    private OnFragmentInteractionListener mListener;
    private NavigationSession mNavigationSession;

    public NavigationDestinationFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_navigation_destination, container, false);
        mDestinationText = (TextView) view.findViewById(R.id.destination_text);
        mDestinationSelectionButton = (Button) view.findViewById(R.id.destination_button);
        mDestinationSelectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Select a Destination");
                String[] destinations = new String[HardcodedBuilding.values().length];
                for (int i = 0; i < destinations.length; i++) {
                    destinations[i] = HardcodedBuilding.values()[i].getName();
                }
                builder.setItems(destinations, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        HardcodedBuilding selected = HardcodedBuilding.values()[which];
                        mNavigationSession = new NavigationSession(getActivity(), selected);
                        Toast.makeText(getActivity(), "Starting Navigation to: " + selected.getName(), Toast.LENGTH_SHORT).show();
                        mDestinationText.setText("Current Destination: " + selected.getName());
                    }
                });
                builder.show();
            }
        });
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public double[] getNavigationVector() {
        return mNavigationSession.getDirectionVector();
    }
    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
