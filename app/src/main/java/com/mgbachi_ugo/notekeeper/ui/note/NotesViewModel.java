package com.mgbachi_ugo.notekeeper.ui.note;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class NotesViewModel extends ViewModel {

    private MutableLiveData<String> mText;

   public NotesViewModel() {
       mText = new MutableLiveData<>();
       mText.setValue(" ");
   }



    public LiveData<String> getText() {
        return mText;
    }
}