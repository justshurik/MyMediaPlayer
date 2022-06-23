package com.example.mymediaplayer.mvvm.view.adapters;

import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.example.mymediaplayer.mvvm.model.database.MusicFile;

/**
 * интерфейс для обработки изменения рейтинга
 */
public interface ISetRating {

    public void setRating(@NonNull MusicFile mFile, int iRate, ImageView[] stars);
}


