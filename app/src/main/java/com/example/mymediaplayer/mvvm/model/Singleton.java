package com.example.mymediaplayer.mvvm.model;

import android.app.Application;
import android.content.Context;

import androidx.room.Room;

import com.example.mymediaplayer.mvvm.model.database.DBAudioData;
import com.example.mymediaplayer.mvvm.model.repository.Repository;
import com.example.mymediaplayer.mvvm.model.sh_pr.SortSettings;
import com.example.mymediaplayer.mvvm.model.sh_pr.UserSettings;

public class Singleton extends Application {
    private Context context;
    private static Singleton INSTANCE;
    public Singleton(){}

    private DBAudioData audio_database;
    private UserSettings user_settings;
    private Repository repository;
    private SortSettings sort_settings;

    @Override
    public void onCreate() {
        super.onCreate();

        this.context=this;
        if(INSTANCE==null) INSTANCE=this;

        //инициализация обьектов, доступ к которым нужен всегда
        this.audio_database= Room
                                .databaseBuilder(context,DBAudioData.class,DBAudioData.DB_NAME)
                                .allowMainThreadQueries()
                                .build();

        this.user_settings = new UserSettings();
        this.sort_settings=new SortSettings();

        this.repository = Repository.getInstance();
       }



    public SortSettings getSort_settings() {
        return sort_settings;
    }

    public static Singleton getINSTANCE() {
        if(INSTANCE==null){
            INSTANCE=new Singleton();
        }
        return INSTANCE;
    }

    public Context getContext() {
        return context;
    }

    public DBAudioData getAudio_database() {
        return audio_database;
    }

    public UserSettings getUser_settings() {
        return user_settings;
    }

    public Repository getRepository() {
        return repository;
    }
}
