package com.example.mymediaplayer.mvvm.model.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;


@Database(entities = {MusicFile.class}, version = 1, exportSchema = false)

public abstract class DBAudioData extends RoomDatabase {

    public static String DB_NAME="MUSIC_DATA";

    public abstract DAOMusicData AUDIO_DATA();

}
