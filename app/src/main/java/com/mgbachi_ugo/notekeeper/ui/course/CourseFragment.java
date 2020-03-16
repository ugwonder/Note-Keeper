package com.mgbachi_ugo.notekeeper.ui.course;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.mgbachi_ugo.notekeeper.R;

public class CourseFragment extends Fragment {

    private CourseViewModel courseViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        courseViewModel =
                ViewModelProviders.of(this).get(CourseViewModel.class);
        View root = inflater.inflate(R.layout.fragment_course, container, false);
        final TextView textView = root.findViewById(R.id.text_course);
        courseViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        return root;
    }
}