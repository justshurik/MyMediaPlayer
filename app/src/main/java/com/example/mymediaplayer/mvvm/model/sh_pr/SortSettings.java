package com.example.mymediaplayer.mvvm.model.sh_pr;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.mymediaplayer.mvvm.model.Singleton;

import java.util.concurrent.Executor;

public class SortSettings {
    private String KET_SORT="user_sort";

    private String NAME_SETTINGS="sort_settings";
    public static Integer SORT_TO_UP=0;
    public static Integer SORT_TO_DOWN=1;

    private Integer SORT_DEFAULT=SORT_TO_UP;

    private Integer SORT_TYPE;

    private SharedPreferences sh_pr;

    public SortSettings(){
        this.sh_pr= Singleton.getINSTANCE().getContext().getSharedPreferences(NAME_SETTINGS, Context.MODE_PRIVATE);
        this.SORT_TYPE=sh_pr.getInt(KET_SORT,SORT_DEFAULT);
    }

    public void setSortType(Integer SORT_TYPE){
        try{
            if(this.sh_pr==null) this.sh_pr= Singleton.getINSTANCE().getContext().getSharedPreferences(NAME_SETTINGS, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sh_pr.edit();
            editor.putInt(KET_SORT,SORT_TYPE);
            editor.commit();

            this.SORT_TYPE=SORT_TYPE;

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public Integer getSORT_TYPE() {
        if(this.SORT_TYPE==null) this.SORT_TYPE=SORT_DEFAULT;
        return SORT_TYPE;
    }


}
