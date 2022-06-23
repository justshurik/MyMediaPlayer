package com.example.mymediaplayer.mvvm.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.mymediaplayer.mvvm.model.Singleton;
import com.example.mymediaplayer.mvvm.model.database.MusicFile;
import com.example.mymediaplayer.mvvm.model.sh_pr.UserSettings;



public class ViewModelSettings extends ViewModel {

    private MutableLiveData<UserSettings> userSettingsMutableLiveData;

    public ViewModelSettings(){
        if(this.userSettingsMutableLiveData==null) this.userSettingsMutableLiveData = new MutableLiveData<>();

    }

    public MutableLiveData<UserSettings> getLiveData() {
        return userSettingsMutableLiveData;
    }


}
