package com.mgbachi_ugo.notekeeper.ui.course;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class CourseViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public CourseViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue(" ");
    }

    public LiveData<String> getText() {
        return mText;
    }
}