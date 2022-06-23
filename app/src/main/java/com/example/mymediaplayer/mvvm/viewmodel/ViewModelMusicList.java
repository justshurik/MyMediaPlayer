package com.example.mymediaplayer.mvvm.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.mymediaplayer.mvvm.model.Singleton;
import com.example.mymediaplayer.mvvm.model.database.MusicFile;
import com.example.mymediaplayer.mvvm.model.sh_pr.SortSettings;
import com.example.mymediaplayer.mvvm.model.sh_pr.UserSettings;
import com.example.mymediaplayer.mvvm.view.audio.Audio;

import java.util.ArrayList;
import java.util.List;

public class ViewModelMusicList extends ViewModel {
    private MutableLiveData<List<MusicFile>> musicListMutableLiveData;

    public ViewModelMusicList(){
        if(this.musicListMutableLiveData==null) this.musicListMutableLiveData = new MutableLiveData<>();
    }

    public MutableLiveData<List<MusicFile>> getLiveData() {
        return musicListMutableLiveData;
    }


}
