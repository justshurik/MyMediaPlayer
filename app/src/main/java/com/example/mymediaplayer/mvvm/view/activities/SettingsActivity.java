package com.example.mymediaplayer.mvvm.view.activities;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;


import android.app.Activity;
import android.app.ProgressDialog;

import android.content.Intent;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;


import android.os.FileUtils;
import android.os.Handler;
import android.os.Looper;

import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import android.widget.RadioButton;
import android.widget.Toast;

import com.example.mymediaplayer.R;
import com.example.mymediaplayer.mvvm.model.Singleton;
import com.example.mymediaplayer.mvvm.model.database.MusicFile;
import com.example.mymediaplayer.mvvm.model.sh_pr.UserSettings;
import com.example.mymediaplayer.mvvm.view.audio.Audio;

import com.example.mymediaplayer.mvvm.viewmodel.ViewModelSettings;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;



public class SettingsActivity extends AppCompatActivity {

    private int TAG_ON_ACTIVITY_RESULT=1;


    EditText etMinSize;
    EditText etMaxSize;
    Button btnSave, btnAddFile;
    RadioButton rbSortByRate, rbSortByName, rbSortByArtist, rbSortByDate;
    BottomNavigationView bottom_menu;
    ProgressDialog progressDialog;
    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //Livedata настроек
        Singleton.getINSTANCE().
                getRepository().
                setUserSettingsMutableLiveData(new ViewModelProvider(this).get(ViewModelSettings.class));
        //инициализация элементов
        initElements();
        //handler
        this.handler = new Handler(Looper.getMainLooper());

    }

    //инициализируем ряд элементов и их поведение
    private void initElements(){
        etMinSize=findViewById(R.id.min_size);
        etMaxSize=findViewById(R.id.max_size);
        bottom_menu=findViewById(R.id.navigation_menu_main_activity);
        rbSortByRate=findViewById(R.id.sort_by_rate);
        rbSortByDate=findViewById(R.id.sort_by_date);
        rbSortByName=findViewById(R.id.sort_by_name);
        rbSortByArtist=findViewById(R.id.sort_by_artist);
        btnSave=findViewById(R.id.save_search_data);
        btnAddFile=findViewById(R.id.add_file_from_file);
        //выделяем элемент
        bottom_menu.setSelectedItemId(R.id.settings);

        //свойства нижней панели
        setBottomPanelListeners(bottom_menu);

        //устанавливаем начальные значения в поля настроек
        setStartValues();

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {

                                showProgressDialog();

                                //сохраняем настройки по сортировке и минимальный/максимальный размер
                                saveValues();

                                //тут перезаписываем весь список композиций
                                findAndRewriteAllMusic();

                                //выводим сообщение
                                Toast.makeText(getBaseContext(), "Данные сохранены.", Toast.LENGTH_SHORT).show();

                                closeProgressDialog();

                            }
                        });
                    }
                }).start();

            }
        });

        //место срабатывания  observer от ViewModel с настройками
        Singleton.getINSTANCE().getRepository().getUserSettingsMutableLiveData().getLiveData().observe(this, new Observer<UserSettings>() {
            @Override
            public void onChanged(UserSettings userSettings) {

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        float minSize=userSettings.getMIN_SIZE();
                        if(minSize>UserSettings.ZERO_SIZE)
                            etMinSize.setText(String.valueOf(userSettings.getMIN_SIZE()));
                        else
                            etMinSize.setText("");

                        float maxSize=userSettings.getMAX_SIZE();
                        if(maxSize<UserSettings.INFINITY_SIZE)
                            etMaxSize.setText(String.valueOf(userSettings.getMAX_SIZE()));
                        else
                            etMaxSize.setText("");

                        setRaioButtonValues(userSettings.getSORT_BY());
                    }
                });

            }
        });

        //сохранение настроек при клике на RadioButton
        rbSortByName.setOnClickListener(rbClickListener);
        rbSortByDate.setOnClickListener(rbClickListener);
        rbSortByArtist.setOnClickListener(rbClickListener);
        rbSortByRate.setOnClickListener(rbClickListener);

//        btnAddFile.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                openFileChooser();
//            }
//        });

    }


    //функция перезаписи всего списка компнзиций
    public void findAndRewriteAllMusic(){

        //ищем всю музыку, которая удовлетворяет заданным критериям
        ArrayList<MusicFile> listMusic = (new Audio(getBaseContext())).searchAllFiles(
                Singleton.getINSTANCE().getUser_settings().getMIN_SIZE(),
                Singleton.getINSTANCE().getUser_settings().getMAX_SIZE());

        //записываем ее в БД через асинхронный запрос с применением настроек пользователя
        Singleton.getINSTANCE().getRepository().writeIfNotExistMusicListFilesIntoDb(listMusic);

        //передаем данные в ViewModel MainActivity для дальнейшей обработки
        //Singleton.getINSTANCE().getRepository().getMusicListViewModel().getActualMusicListFromDb(listMusic);

    }

//    ActivityResultLauncher<Intent> startFileChooser = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
//            new ActivityResultCallback<ActivityResult>() {
//                @Override
//                public void onActivityResult(ActivityResult result) {
//                    if(result==null) return;
//                    if(result.getResultCode()== Activity.RESULT_OK){
//                        Intent data = result.getData();
//                        Uri uri = data.getData();
//
//                        try {
//
//                            InputStream is = getBaseContext().getContentResolver().openInputStream( uri );
//
//                            uri = Uri.parse(is.toString()).normalizeScheme();
//
//                        } catch (FileNotFoundException e) {
//                            e.printStackTrace();
//                        }
//
//
//
//                        boolean is_ex=(new File(String.valueOf(uri))).exists();
//                        if(is_ex==false)    //вот тут все время будет false
//                            return;
//
//                        //какие-то действия
//                        //...
//                    }
//                }
//            });
//
//    private void openFileChooser(){
//        Intent data= new Intent(Intent.ACTION_OPEN_DOCUMENT);
//        data.addCategory(Intent.CATEGORY_OPENABLE);
//        data.setType("*/*");
//        data = Intent.createChooser(data,"Выберите файл");
//        startFileChooser.launch(data);
//
////        Intent openFileForResult=new Intent(Intent.ACTION_GET_CONTENT);
////        openFileForResult.setType("*/*");
////        startActivityForResult(openFileForResult,TAG_ON_ACTIVITY_RESULT);
//    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if(data==null) return;;
//
//        if(requestCode==TAG_ON_ACTIVITY_RESULT && resultCode==RESULT_OK){
//
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//
//                    Uri uri = data.getData().normalizeScheme();
//                    if(!isAudio(uri)) {
//                        Toast.makeText(getBaseContext(),"Файл не является аудио файлом.",Toast.LENGTH_SHORT).show();
//                        return;
//                    }
//
//
//                    Audio audio = new Audio(getBaseContext());
//                    MusicFile mFile = audio.getAudioParamsFromUri(uri);
//                    mFile.status=Audio.INDIVIDUAL;  //устанавливаем тег, что этот файл был добавлен индивидуально пользователем
//                    Singleton.getINSTANCE().getRepository().inputMusicFileInToDB(mFile);
//
//                    Toast.makeText(getBaseContext(),"Аудиофайл добавлен.", Toast.LENGTH_SHORT).show();
//                }
//            }).start();
//
//
//
//        }
//
//    }

    //получаем uri путь к файлу в нужном формате нам
    private String getPathFromURI(Uri uri){
        String sPath=null;
        String scheme=uri.getScheme();
        if(scheme!=null && scheme.contains("content")) {
            Cursor cursor = getContentResolver().
                    query(uri,
                            new String[]{MediaStore.Audio.Media.DATA},
                            null,
                            null,
                            null);
            Integer indexCursor = null;
            if (cursor.moveToFirst()) {
                indexCursor = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
            }
            sPath = cursor.getString(indexCursor);

            cursor.close();
        }else{
            sPath=uri.getPath();
        }

        //проверяем путь к файлу

        return sPath;
    }

    //проверка, является ли путь к ресурсу правильным
    private boolean isCorrectPath(String sPath){
        try{

            File f = new File(sPath);
            if(f==null) return false;

            boolean is_exist = f.exists();

            Long size=f.length();
            if(size==0) return false;

            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    //проверка является ли файл audio
    private boolean isAudio(Uri uri){
        try{

            String mime = getContentResolver().getType(uri);

            if(!mime.contains("audio")) return false;

            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }


    //слушатель нажатия на кнопку сохранить настройки и
    View.OnClickListener rbClickListener = new View.OnClickListener() {
       @Override
       public void onClick(View v) {
           saveValues();
       }
   };

    //сохранить данные
    public void saveValues(){

        Float minSize=UserSettings.ZERO_SIZE;
        if(etMinSize.getText().toString().length()>0)
            minSize=Float.valueOf(etMinSize.getText().toString());

        Float maxSize=UserSettings.INFINITY_SIZE;
        if(etMaxSize.getText().toString().length()>0)
            maxSize=Float.valueOf(etMaxSize.getText().toString());

        String sortBy;
        if(rbSortByName.isChecked())
            sortBy=UserSettings.SORT_BY_NAME;
        else if(rbSortByArtist.isChecked())
            sortBy=UserSettings.SORT_BY_ARTIST;
        else if(rbSortByDate.isChecked())
            sortBy=UserSettings.SORT_BY_DATE;
        else
            sortBy = UserSettings.SORT_BY_RATING;

        //сохраняем данные
        Singleton.getINSTANCE().getRepository().setUserSettings(minSize,maxSize,sortBy);


    }

    //стартовые значения
    private void setStartValues(){
        UserSettings sett = Singleton.getINSTANCE().getUser_settings();

        float fMinSize=sett.getMIN_SIZE();

        if(fMinSize==UserSettings.ZERO_SIZE) etMinSize.setText("");

        else etMinSize.setText(String.valueOf(sett.getMIN_SIZE()));

        float fMaxSize=sett.getMAX_SIZE();

        if(fMaxSize==UserSettings.INFINITY_SIZE)  etMaxSize.setText("");

        else etMaxSize.setText(String.valueOf(sett.getMAX_SIZE()));

        setRaioButtonValues(sett.getSORT_BY());

    }

    private void setRaioButtonValues(String sSortBy){
        switch (sSortBy){
            case UserSettings.SORT_BY_RATING:
                rbSortByRate.setChecked(true);
                break;
            case UserSettings.SORT_BY_ARTIST:
                rbSortByArtist.setChecked(true);
                break;
            case UserSettings.SORT_BY_NAME:
                rbSortByName.setChecked(true);
                break;
            case UserSettings.SORT_BY_DATE:
                rbSortByDate.setChecked(true);
                break;
            default: break;
        }
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
                            startActivity(new Intent(SettingsActivity.this, MainActivity.class)
                                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            );
                            break;
                        case R.id.settings:

                            break;
                        case R.id.deleted_music:
                            startActivity(new Intent(SettingsActivity.this, DeletedActivity.class)
                                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            );

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

    //показываем диалог
    private void showProgressDialog(){
        this.progressDialog = new ProgressDialog(SettingsActivity.this,ProgressDialog.STYLE_SPINNER);
        this.progressDialog.setTitle(getString(R.string.loading));
        this.progressDialog.show();
    }

    //закрываем диалог
    private void closeProgressDialog(){
        if(progressDialog!=null) progressDialog.dismiss();
    }

    @Override
    protected void onResume() {
        super.onResume();
        bottom_menu.setSelectedItemId(R.id.settings);
    }


}