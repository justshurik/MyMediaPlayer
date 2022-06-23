package com.example.mymediaplayer.mvvm.model.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.net.URI;

@Entity(tableName = "music_table")
public class MusicFile {

    @PrimaryKey(autoGenerate = true) long id;

    @ColumnInfo public String name;
    @ColumnInfo public String artist;
    @ColumnInfo public String fullName;
    @ColumnInfo public int rating;
    @ColumnInfo public long unix;
    @ColumnInfo public int status;

}
