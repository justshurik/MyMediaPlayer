package com.example.mymediaplayer.mvvm.view.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.Intent;

import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mymediaplayer.R;
import com.example.mymediaplayer.mvvm.model.Singleton;
import com.example.mymediaplayer.mvvm.model.database.MusicFile;
import com.example.mymediaplayer.mvvm.model.sh_pr.SortSettings;
import com.example.mymediaplayer.mvvm.view.adapters.IAudioPlayerData;
import com.example.mymediaplayer.mvvm.view.adapters.IDeleteFromList;
import com.example.mymediaplayer.mvvm.view.adapters.ISetRating;
import com.example.mymediaplayer.mvvm.view.adapters.mainActivityAdapter;
import com.example.mymediaplayer.mvvm.view.audio.Audio;
import com.example.mymediaplayer.mvvm.viewmodel.ViewModelMusicList;
import com.google.android.material.bottomnavigation.BottomNavigationView;


import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements ISetRating, IDeleteFromList, IAudioPlayerData {
    private long curr_time;
    private int REQUEST_PERMISSION;
    Handler handler;
    boolean looping;
    LinearLayout play_panel;
    ImageView btnSkipPrevious;
    ImageView btnStop;
    ImageView btnPlay;
    ImageView btnPause;
    ImageView bthSkipNext;
    ImageView btnRepeate;
    SeekBar progressBar;
    RecyclerView audio_list;
    BottomNavigationView bottomPanel;
    TextView bthBluetoth;
    ImageView btnSortBy;
    TextView tvCurrentTime;
    TextView tvMaxTime;
    TextView tvTitleComposition;
    MediaPlayer mediaPlayer;
    mainActivityAdapter adapter;
    Integer number_compositions=-1;
    List<MusicFile> list_music;
    boolean is_pause;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ckeckUserPermissions();

        this.handler = new Handler(Looper.getMainLooper()); //Handler для вывода графияеской информации

        //определеяем все элементы
        definiteElements();

        //устанавливаем ViewModel
       Singleton.getINSTANCE().
                getRepository().
                setMusicListMutableLiveData(new ViewModelProvider(this).get(ViewModelMusicList.class));

        //настройки нижней панели
        setBottomPanelListeners(bottomPanel);

        audio_list.setLayoutManager(new LinearLayoutManager(this));
        adapter = new mainActivityAdapter(getBaseContext(),null);
        adapter.setSetRatingListener(this); //слушатель для обработки события по нажатию на установку рейтинга
        adapter.setDeleteFromListListener(this);    //слушатель нажатия на кнопку удаления
        adapter.setPlayAudio(this); //слушатель для проигрывания композиции, которую выбрал пользователь
        audio_list.setAdapter(adapter);


        //код, исполняющийся при вызове observer
        Singleton.getINSTANCE().getRepository().getMusicListViewModel().getLiveData().observe(this, new Observer<List<MusicFile>>() {
            @Override
            public void onChanged(List<MusicFile> musicFiles) {
                setColorSelectedCompositionInAdapter(mainActivityAdapter.NO_SELECTED);
                adapter.setCompositions(musicFiles);
                adapter.notifyDataSetChanged();
                releasePlayer();

            }
        });

        //кнопка сортировки
        btnSortBy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                sortButtonAction();
                //setColorSelectedCompositionInAdapter(mainActivityAdapter.NO_SELECTED);
            }
        });

    }

    public void ckeckUserPermissions(){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_PERMISSION);
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION) {
            if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                Toast.makeText(this,"Доступ к хранилищу не разрешен.", Toast.LENGTH_SHORT).show();

            }else{
                Toast.makeText(this,"Проверьте настройки.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //поведение кнопки сортировки
    private void sortButtonAction(){
        //запрашиваем весь список композиций c учетом всех настроек пользователя
        int sort_type=Singleton.getINSTANCE().getSort_settings().getSORT_TYPE();
        String sort_by=Singleton.getINSTANCE().getUser_settings().getSORT_BY();

        if(sort_type==SortSettings.SORT_TO_DOWN){
            Singleton.getINSTANCE().getSort_settings().setSortType(SortSettings.SORT_TO_UP);
            sort_type=SortSettings.SORT_TO_UP;
            //меняем картинку
            btnSortBy.setBackground(getDrawable(R.drawable.sort_to_up));

        }else{
            Singleton.getINSTANCE().getSort_settings().setSortType(SortSettings.SORT_TO_DOWN);
            sort_type=SortSettings.SORT_TO_DOWN;
            //меняем картинку
            btnSortBy.setBackground(getDrawable(R.drawable.sort_to_down));

        }

        List<MusicFile> list= Singleton.getINSTANCE().getRepository().getLiteMusicListFromDb(sort_by,sort_type);
        //вызываем LiveData observer через Repository
        Singleton.getINSTANCE().getRepository().getMusicListViewModel().getLiveData().setValue(list);
    }

    @Override
    protected void onStart() {
        super.onStart();
        //запрашиваем весь список композиций c учетом всех настроек пользователя
        getAllMusicData();

    }

    //получаем все актуальные композиции
    public void getAllMusicData(){
        ArrayList<MusicFile> listMusic = (new Audio(getBaseContext())).searchAllFiles(
                Singleton.getINSTANCE().getUser_settings().getMIN_SIZE(),
                Singleton.getINSTANCE().getUser_settings().getMAX_SIZE());

        //передаем данные в ViewModel для дальнейшей обработки
        List<MusicFile> list= Singleton.getINSTANCE().getRepository().getActualMusicListFromDb(listMusic);

        //вызываем LiveData observer через Repository
        Singleton.getINSTANCE().getRepository().getMusicListViewModel().getLiveData().setValue(list);
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
                      case R.id.playlist: break;
                      case R.id.settings:
                          startActivity(new Intent(MainActivity.this, SettingsActivity.class)
                                      //.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                          );
                          //bottomPanel.setSelectedItemId(R.id.settings);
                          break;
                      case R.id.deleted_music:
                          startActivity(new Intent(MainActivity.this, DeletedActivity.class)
                                  //.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                          );
                          //bottomPanel.setSelectedItemId(R.id.deleted_music);
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

    //запуск проигрывателя с элемента, который выбрал пользователь в адаптере
    @Override
    public void play_audio_from_adapter(List<MusicFile> list, int number_of_element) throws IOException {
        number_compositions=number_of_element;
        list_music=list;

        btnRepeate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(looping ==true){
                    looping = false;
                }else {
                    looping = true;
                }
                if(mediaPlayer!=null) mediaPlayer.setLooping(looping);
                setBackgroudLoppingButton(looping);
            }
        });

        btnPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mediaPlayer.isPlaying()){
                    mediaPlayer.pause();
                    btnPause.setVisibility(View.GONE);
                    btnPlay.setVisibility(View.VISIBLE);
                    is_pause=true;
                }
            }
        });

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(is_pause==true){
                    mediaPlayer.start();
                    is_pause=false;
                    btnPause.setVisibility(View.VISIBLE);
                    btnPlay.setVisibility(View.GONE);
                }
            }
        });

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mediaPlayer.isPlaying()){
                    mediaPlayer.seekTo(0);
                    mediaPlayer.pause();
                    btnPause.setVisibility(View.GONE);
                    btnPlay.setVisibility(View.VISIBLE);
                    is_pause=true;
                }
            }
        });

        btnSkipPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    number_compositions--;
                    if(number_compositions<0){
                        number_compositions=0;
                    }
                    Uri uri = Uri.parse(list.get(number_compositions).fullName);
                    String sName=list.get(number_compositions).name;
                    setColorSelectedCompositionInAdapter(number_compositions);
                    //проигрываем аудио
                    play_audio(uri,getApplicationContext(),sName);

                    //работа с progressbar
                    progressBarStatus();

                    setColorSelectedCompositionInAdapter(number_compositions);

                } catch (IOException e) {
                    e.printStackTrace();
                }


            }


        });



        bthSkipNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    number_compositions++;
                    if(number_compositions>=list_music.size()){
                        number_compositions=list_music.size()-1;
                    }

                    Uri uri = Uri.parse(list.get(number_compositions).fullName);
                    String sName=list.get(number_compositions).name;
                    setColorSelectedCompositionInAdapter(number_compositions);
                    //проигрываем аудио
                    play_audio(uri,getApplicationContext(),sName);

                    //работа с progressbar
                    progressBarStatus();



                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
        });

        Uri uri = Uri.parse(list.get(number_compositions).fullName);
        String sName=list.get(number_compositions).name;
        setColorSelectedCompositionInAdapter(number_compositions);
        //проигрываем аудио
        play_audio(uri,getApplicationContext(),sName);

        //работа с progressbar
        progressBarStatus();


    }

    private void setColorSelectedCompositionInAdapter(int number_compositions){
        adapter.setCurrentComposition(number_compositions);

        adapter.notifyDataSetChanged();
    }

    //слушатель окончания проигрывания композиции
    MediaPlayer.OnCompletionListener onEndCompositionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            try {
                if(number_compositions>=list_music.size()){
                    number_compositions=0;
                }else{
                    number_compositions++;
                }

                Uri uri = Uri.parse(list_music.get(number_compositions).fullName);
                String sName=list_music.get(number_compositions).name;
                setColorSelectedCompositionInAdapter(number_compositions);

                //проигрываем аудио
                play_audio(uri,getApplicationContext(),sName);

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    };

    //проигрывание композиции
    private void play_audio(@NonNull Uri uri, Context context, String sTitleComposition) throws IOException {

        if(mediaPlayer!=null){
            if(mediaPlayer.isPlaying()) mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.release();
        }

        mediaPlayer = MediaPlayer.create(context, uri);
        mediaPlayer.setLooping(looping);
        mediaPlayer.setOnCompletionListener(onEndCompositionListener);

        //показываем панель управления композицией
        if(play_panel.getVisibility()!=View.VISIBLE){
            play_panel.setVisibility(View.VISIBLE);
        }

        if(btnPlay.getVisibility()==View.VISIBLE) {
            btnPlay.setVisibility(View.GONE);
            btnPause.setVisibility(View.VISIBLE);
        }
        tvTitleComposition.setText(sTitleComposition);

        mediaPlayer.seekTo(0);

        //продолжительность композиции
        int duration = mediaPlayer.getDuration();
        String sDur = stringDurationAudio(duration);    //продолжительность в формате mm:ss
        progressBar.setMax(duration);
        tvMaxTime.setText(sDur);

        mediaPlayer.start();
        is_pause=false;
        //текущий прогресс
        handler.post(current_progress);


    }

    Runnable current_progress = new Runnable() {
        @Override
        public void run() {
            int position=mediaPlayer.getCurrentPosition();
            progressBar.setProgress(position);
            tvCurrentTime.setText(stringDurationAudio(position));
            handler.post(this);
        }
    };

    private void progressBarStatus(){
        progressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int curr_progress;
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                curr_progress=progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mediaPlayer.seekTo(curr_progress);
            }
        });
    }

    //цвет залицвки кнопки повторения композиции
    private void setBackgroudLoppingButton(boolean isLoop){
        if(isLoop ==true){
            btnRepeate.setBackground(getDrawable(R.color.gray));
        }else {
            btnRepeate.setBackground(getDrawable(R.color.white));
        }
    }

    //зкарываем панель управления плеером
    private void releasePlayer(){
        if(mediaPlayer!=null) {
            mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.release();
            handler.removeCallbacks(current_progress);
            mediaPlayer=null;
        }

        btnPause.setVisibility(View.GONE);
        btnPlay.setVisibility(View.VISIBLE);
        play_panel.setVisibility(View.GONE);
    }

    //преобразовываем миллисекунды длительности композиции в формат mm:ss
    private String stringDurationAudio(int duration){
        return String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(duration),
                TimeUnit.MILLISECONDS.toSeconds(duration)- TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration)));
    }


    //внутрення функция определения всех элементов
    private void definiteElements(){
        play_panel=findViewById(R.id.play_panel);
        btnSkipPrevious=findViewById(R.id.btn_skip_previous);
        btnStop=findViewById(R.id.btn_stop);
        btnPlay = findViewById(R.id.btn_play);
        btnPause=findViewById(R.id.btn_pause);
        bthSkipNext=findViewById(R.id.btn_skip_next);
        btnRepeate=findViewById(R.id.btn_repeate);
        progressBar=findViewById(R.id.progress_bar);
        audio_list=findViewById(R.id.recycler_view);
        bottomPanel=findViewById(R.id.navigation_menu_main_activity);
        bthBluetoth=findViewById(R.id.title_main);
        btnSortBy=findViewById(R.id.sort);
        tvCurrentTime=findViewById(R.id.current_time);
        tvMaxTime=findViewById(R.id.final_time);
        tvTitleComposition=findViewById(R.id.player_title_composition);

        //устанавливаем начальные значения
        btnPause.setVisibility(View.GONE);
        play_panel.setVisibility(View.GONE);
        tvCurrentTime.setText("00:00");
        tvMaxTime.setText("00:00");
        tvTitleComposition.setText("");
        looping=false;
        setBackgroudLoppingButton(looping);

        //тип сортировки
        if(Singleton.getINSTANCE().getSort_settings().getSORT_TYPE()== SortSettings.SORT_TO_DOWN)
            btnSortBy.setBackground(getDrawable(R.drawable.sort_to_down));
        else
            btnSortBy.setBackground(getDrawable(R.drawable.sort_to_up));


    }

    @Override
    protected void onResume() {
        super.onResume();
        bottomPanel.setSelectedItemId(R.id.playlist);
        btnPlay.setVisibility(View.VISIBLE);
        btnPause.setVisibility(View.GONE);
        adapter.setCurrentComposition(mainActivityAdapter.NO_SELECTED);
    }


    @Override
    protected void onPause() {
        super.onPause();
        releasePlayer();
    }

    @Override
    public void onBackPressed() {
        if (curr_time + 2000 > System.currentTimeMillis()) {
            System.exit(0);
        }else{
            Toast.makeText(getBaseContext(),"Нажмите еще раз для выхода.",Toast.LENGTH_SHORT).show();
        }
        curr_time=System.currentTimeMillis();
    }

    //обработка нажатия на звездочку в композициях для установки рейтинга
    @Override
    public void setRating(@NonNull MusicFile mFile, int iRate, ImageView[] stars) {

        //вносим новый рейтинг в БД
        Singleton.getINSTANCE().getRepository().changeRatingInDB(mFile,iRate);
        //adapter.notifyDataSetChanged();
        //updateListMusicByViewModeFromDB();

    }

    //удаляем композицию из списка
    @Override
    public void deleteFromList(@NonNull List<MusicFile> mList, int number_of_element) {
        //меняем статус элемента на удаленный
        Singleton.getINSTANCE().getRepository().changeStatusOfMusicFile(mList.get(number_of_element),Audio.DELETED);

        updateListMusicByViewModeFromDB();

    }

    //обновление даннх о компнзицию из ViewModel
    private void updateListMusicByViewModeFromDB(){

        //запрашиваем пользовательские настройки
        int sort_type=Singleton.getINSTANCE().getSort_settings().getSORT_TYPE();
        String sort_by=Singleton.getINSTANCE().getUser_settings().getSORT_BY();

        //вызываем ViewModel observer
        List<MusicFile> fileList = Singleton.getINSTANCE().getRepository().getLiteMusicListFromDb(sort_by,sort_type);
        Singleton.getINSTANCE().getRepository().getMusicListViewModel().getLiveData().setValue(fileList);
    }



}