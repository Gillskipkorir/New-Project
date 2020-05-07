package com.kip.gillz.newproject.LoginPackage;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kip.gillz.newproject.R;

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




        return root;
    }
}