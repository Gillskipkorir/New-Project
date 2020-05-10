package com.kip.gillz.newproject.loginpackage;

import android.content.Intent;
import android.os.Bundle;
//import android.support.design.widget.FloatingActionButton;
//import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.kip.gillz.newproject.R;
import com.kip.gillz.newproject.userpackage.Home;

public class User extends Fragment {

    public User() {
    }

    public static User newInstance() {
        User fragment = new User();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_user, container, false);
        FloatingActionButton floatingActionButton = root.findViewById(R.id.fabu);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(),Home.class);
                startActivity(intent);
            }
        });



        return root;
    }
}