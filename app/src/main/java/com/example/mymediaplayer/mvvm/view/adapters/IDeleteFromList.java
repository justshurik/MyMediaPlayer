package com.example.mymediaplayer.mvvm.view.adapters;

import androidx.annotation.NonNull;

import com.example.mymediaplayer.mvvm.model.database.MusicFile;

import java.util.List;

public interface IDeleteFromList {

    public void deleteFromList(@NonNull List<MusicFile> mFile, int nummber_of_element);
}
