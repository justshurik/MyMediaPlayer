package com.example.mymediaplayer.mvvm.view.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.example.mymediaplayer.R;
import com.example.mymediaplayer.mvvm.model.Singleton;
import com.example.mymediaplayer.mvvm.model.database.MusicFile;
import com.example.mymediaplayer.mvvm.view.adapters.IPopupMenu;
import com.example.mymediaplayer.mvvm.view.adapters.deleteActivityAdapter;
import com.example.mymediaplayer.mvvm.view.audio.Audio;
import com.example.mymediaplayer.mvvm.viewmodel.ViewModelDeleted;
import com.example.mymediaplayer.mvvm.viewmodel.ViewModelSettings;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

public class DeletedActivity extends AppCompatActivity implements IPopupMenu {

    BottomNavigationView bottomPanel;
    deleteActivityAdapter adapter;
    RecyclerView deleteRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deleted);

        //ViewModel
        Singleton.getINSTANCE().
                getRepository().
                setDeletedMusicListLiveData(new ViewModelProvider(this).get(ViewModelDeleted.class));

        bottomPanel=findViewById(R.id.navigation_menu_deleted_activity);
        setBottomPanelListeners(bottomPanel);

        adapter = new deleteActivityAdapter(getBaseContext(),null);
        adapter.setPopupListener(this);
        deleteRecyclerView=findViewById(R.id.recycler_view_deleted);
        deleteRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        deleteRecyclerView.setAdapter(adapter);
        setDeletedMusicListToRecyclerView();    //выгружаем данные из БД и записываем в adapter

        //observer LiveData
        Singleton.getINSTANCE().getRepository().getDeletedMusicListLiveData().getDeletedListLiveData().observe(this, new Observer<List<MusicFile>>() {
            @Override
            public void onChanged(List<MusicFile> musicFiles) {
                adapter.setDeletedCompositions(musicFiles);
                adapter.notifyDataSetChanged();
            }
        });


    }
    //выгржаем удаленные композиции из БД и направляем их в LiveData
    private void setDeletedMusicListToRecyclerView(){
        List<MusicFile> list = Singleton.getINSTANCE().getRepository().getDeletedMusicDataFromDB();
        Singleton.getINSTANCE().getRepository().getDeletedMusicListLiveData().getDeletedListLiveData().setValue(list);
    }




    //настройки слушателей нижней панели
    private void setBottomPanelListeners(BottomNavigationView panel){
        try{
            if (panel==null) throw new Exception("Нижняя панель не определена.");
            //слушатель
            panel.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                    switch (item.getItemId()){
                        case R.id.playlist:
                            startActivity(new Intent(DeletedActivity.this, MainActivity.class)
                                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            );

                            break;
                        case R.id.settings:
                            startActivity(new Intent(DeletedActivity.this, SettingsActivity.class)
                                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            );
                            //bottomPanel.setSelectedItemId(R.id.settings);
                            break;
                        case R.id.deleted_music:


                            break;
                        default: break;

                    }

                    return true;
                }
            });

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        bottomPanel.setSelectedItemId(R.id.deleted_music);
    }

    @Override
    public void restore(@NonNull MusicFile musicFile) {
        //меняем статус файла на НЕ УДАЛЕННЫЙ
        Singleton.getINSTANCE().getRepository().restoreFileInList(musicFile);
        setDeletedMusicListToRecyclerView();

    }

    @Override
    public void deleteFromDb(@NonNull MusicFile musicFile) {
        //удаляем из БД файл
        Singleton.getINSTANCE().getRepository().deleteMusicFileFromDB(musicFile);
        setDeletedMusicListToRecyclerView();
    }
}