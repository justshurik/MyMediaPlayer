package com.example.mymediaplayer.mvvm.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.mymediaplayer.mvvm.model.database.MusicFile;

import java.util.List;

public class ViewModelDeleted extends ViewModel {

    private MutableLiveData<List<MusicFile>> deletedListLiveData;

    public ViewModelDeleted(){
        if(this.deletedListLiveData==null) this.deletedListLiveData = new MutableLiveData<>();
    }

    public MutableLiveData<List<MusicFile>> getDeletedListLiveData() {
        return deletedListLiveData;
    }
}
