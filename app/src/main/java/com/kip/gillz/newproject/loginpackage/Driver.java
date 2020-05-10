package com.kip.gillz.newproject.loginpackage;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.kip.gillz.newproject.driverpackage.Driver_Route;
import com.kip.gillz.newproject.R;

public class Driver extends Fragment {
    public Driver() {
    }

    public static Fragment newInstance() {
        Driver fragment = new Driver();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_driver, container, false);


        FloatingActionButton floatingActionButton = root.findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(),Driver_Route.class);
                startActivity(intent);
            }
        });

        return root;
    }
}