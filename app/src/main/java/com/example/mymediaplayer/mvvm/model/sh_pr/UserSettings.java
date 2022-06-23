package com.example.mymediaplayer.mvvm.model.sh_pr;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.mymediaplayer.mvvm.model.Singleton;

public class UserSettings {
    public static final String SORT_BY_RATING="rating";
    public static final String SORT_BY_NAME="name";
    public static final String SORT_BY_ARTIST="artist";
    public static final String SORT_BY_DATE="date";

    public static final float ZERO_SIZE=0f;
    public static final float INFINITY_SIZE=999999f;

    private Float MIN_SIZE_DEFAULT=2.0f;
    private Float MAX_SIZE_DEFAULT=5.0f;
    private String SORT_BY_DEFAULT=SORT_BY_RATING;

    private String NAME_SETTINGS="app_settings";

    private Float MIN_SIZE;
    private Float MAX_SIZE;
    private String SORT_BY;

    private String KEY_MIN_SIZE="min_size";
    private String KEY_MAX_SIZE="key_max_size";
    private String KEY_SORT_BY="sort_by";

    private SharedPreferences sh_pr;

    public UserSettings(){
        this.sh_pr= Singleton.getINSTANCE().getContext().getSharedPreferences(NAME_SETTINGS, Context.MODE_PRIVATE);
        this.MIN_SIZE=sh_pr.getFloat(KEY_MIN_SIZE,MIN_SIZE_DEFAULT);
        this.MAX_SIZE=sh_pr.getFloat(KEY_MAX_SIZE,MAX_SIZE_DEFAULT);
        this.SORT_BY=sh_pr.getString(KEY_SORT_BY,SORT_BY_DEFAULT);
    }

    public void setMinSize(Float fMinSize){
        try{
            if(fMinSize!=null)
                if(fMinSize<0) throw new Exception("Минимальный размер не может быть меньше нуля.");

            if(this.sh_pr==null) this.sh_pr= Singleton.getINSTANCE().getContext().getSharedPreferences(NAME_SETTINGS, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sh_pr.edit();
            editor.putFloat(KEY_MIN_SIZE,fMinSize);
            editor.commit();

            this.MIN_SIZE=fMinSize;


        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void setMaxSize(Float fMaxSize){
        try{
            if(fMaxSize!=null)
                if(fMaxSize<0) throw new Exception("Максимальный размер не может быть меньше нуля.");

            if(this.sh_pr==null) this.sh_pr= Singleton.getINSTANCE().getContext().getSharedPreferences(NAME_SETTINGS, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sh_pr.edit();
            editor.putFloat(KEY_MAX_SIZE,fMaxSize);
            editor.commit();

            this.MAX_SIZE=fMaxSize;


        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void setSortBy(String sSortBy){
        try{
            if(this.sh_pr==null) this.sh_pr= Singleton.getINSTANCE().getContext().getSharedPreferences(NAME_SETTINGS, Context.MODE_PRIVATE);
            if(!sSortBy.equals(SORT_BY_DATE) &&
                !sSortBy.equals(SORT_BY_NAME) &&
                ! sSortBy.equals(SORT_BY_ARTIST) &&
                !sSortBy.equals(SORT_BY_RATING)) throw new Exception("Не верно задан тег сортировки.");

            SharedPreferences.Editor editor = sh_pr.edit();
            editor.putString(KEY_SORT_BY,sSortBy);
            editor.commit();

            this.SORT_BY=sSortBy;


        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public Float getMIN_SIZE() {
        if(MIN_SIZE==null) return MIN_SIZE_DEFAULT;

        return MIN_SIZE;
    }

    public Float getMAX_SIZE() {
        if(MIN_SIZE==null) return MAX_SIZE_DEFAULT;
        return MAX_SIZE;
    }

    public String getSORT_BY() {
        if(SORT_BY==null) return SORT_BY_DEFAULT;
        return SORT_BY;
    }
}
