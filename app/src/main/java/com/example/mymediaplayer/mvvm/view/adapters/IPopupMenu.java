package com.example.mymediaplayer.mvvm.view.adapters;

import androidx.annotation.NonNull;

import com.example.mymediaplayer.mvvm.model.database.MusicFile;

public interface IPopupMenu {

    public void restore(@NonNull MusicFile musicFile);
    public void deleteFromDb(@NonNull MusicFile musicFile);
}
