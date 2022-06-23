package com.example.mymediaplayer.mvvm.model.repository;

import android.net.Uri;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.example.mymediaplayer.mvvm.model.Singleton;
import com.example.mymediaplayer.mvvm.model.database.MusicFile;
import com.example.mymediaplayer.mvvm.model.sh_pr.SortSettings;
import com.example.mymediaplayer.mvvm.model.sh_pr.UserSettings;
import com.example.mymediaplayer.mvvm.view.audio.Audio;
import com.example.mymediaplayer.mvvm.viewmodel.ViewModelDeleted;
import com.example.mymediaplayer.mvvm.viewmodel.ViewModelMusicList;
import com.example.mymediaplayer.mvvm.viewmodel.ViewModelSettings;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//репозиторий команд с различными LiveData

public class Repository {

    private static final int num_threads=3; //количество одновременных потоков у ExecuteService

    //константы для результата записи файла в БД
    private static byte INPUT_OK=1;
    private static byte INPUT_ERROR=0;
    private static byte IS_EXISTS=2;

    private static ExecutorService executors_service;

    private static Repository INSTANCE;
    private ViewModelSettings userSettingsMutableLiveData;
    private ViewModelMusicList musicListViewModel;
    private ViewModelDeleted deletedMusicList;

    public static Repository getInstance(){

        if(INSTANCE==null) INSTANCE=new Repository();
        if(executors_service==null) executors_service = Executors.newFixedThreadPool(num_threads);
        return INSTANCE;
    }

    public void setUserSettingsMutableLiveData(ViewModelSettings settings_live_data){
        if(this.userSettingsMutableLiveData==null) this.userSettingsMutableLiveData = settings_live_data;

    }

    public void setMusicListMutableLiveData(ViewModelMusicList musicListViewModel) {
        if(this.musicListViewModel ==null) this.musicListViewModel = musicListViewModel;

    }

    public ViewModelDeleted getDeletedMusicListLiveData() {
        return deletedMusicList;
    }

    public void setDeletedMusicListLiveData(ViewModelDeleted deletedMusicList) {
        if(this.deletedMusicList == null) this.deletedMusicList = deletedMusicList;

    }

    /**
     * функция смены статуа композиции
     *
     */
    public synchronized void changeStatusOfMusicFile(@NonNull MusicFile mFile, int STATUS){
        try{
            if(STATUS!=Audio.DELETED && STATUS!=Audio.NOT_DELETED && STATUS !=Audio.INDIVIDUAL) throw  new Exception("Не верно задан статус.");
            if(mFile.fullName==null) throw new Exception("Не удалось найти URI композиции.");

            Singleton.getINSTANCE().getAudio_database().AUDIO_DATA().changeStatus(STATUS,mFile.fullName);

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * функция проверяем, имеется ли тот или иной файл в БД. и если файл не имеется, то вносит его в БД
     * @param music_list - коллекция с данными о композициях, выгруженная из media-хранилища (т.е. все файлы с музыкой на смартфоне)
     */
    public synchronized void writeIfNotExistMusicListFilesIntoDb(ArrayList<MusicFile> music_list){
        try{

            //проверка на соответствие файлов в БД заданным пользовательским размерам (удалятся даже индивидуальные файлы из БД - их надо будет передобавлять вручнубю)
            Float sizeFile;
            Float minSize=Singleton.getINSTANCE().getUser_settings().getMIN_SIZE();
            Float maxSize=Singleton.getINSTANCE().getUser_settings().getMAX_SIZE();

            //отфильтровываем из списка композиций те, которые не проходят по размеру
            Audio audio = new Audio(Singleton.getINSTANCE().getContext());
            for(int i=0;i<music_list.size();i++){
                sizeFile=audio.sizeAudioInMb(Uri.parse(music_list.get(i).fullName));
                if(sizeFile<minSize || sizeFile>maxSize) {
                    music_list.remove(i);
                    i--;
                }
            }

            List<MusicFile> db_music_list = Singleton.getINSTANCE().getAudio_database().AUDIO_DATA().getAllMusicData();

            for(int i=0;i<db_music_list.size();i++){

                Uri uri_db=Uri.parse(db_music_list.get(i).fullName);

                sizeFile=audio.sizeAudioInMb(uri_db);

                if(sizeFile<minSize || sizeFile>maxSize) {
                    //удаляем композицию, которая не проходит по размеру из БД
                    Singleton.getINSTANCE().getAudio_database().AUDIO_DATA().deleteMusicDataByUri(db_music_list.get(i).fullName);
                    db_music_list.remove(i); //удаляем ее же из коллекции
                    i--;
                }
            }

            //вносим в БД элементы music_list, которые отсутствуют в БД
            for(MusicFile m:music_list){
                if(!contains(db_music_list,m))
                    Singleton.getINSTANCE().getAudio_database().AUDIO_DATA().insert(m);
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private boolean contains(List<MusicFile> list, MusicFile m){
        for(MusicFile el:list)
            if(el.fullName.equals(m.fullName)) return true;
        return false;
    }



    //поиск композиции в БД по uri
    private boolean is_exist_in_db(@NonNull MusicFile mFile){
        int count = Singleton.getINSTANCE().getAudio_database().AUDIO_DATA().calcFilesByParam(mFile.fullName);
        if(count==0) return false;
        return true;
    }

    //изменение рейтинга
    public void changeRatingInDB(@NonNull MusicFile mFile, int newRating){
        if(newRating<0) newRating=0;
        if(newRating>4) newRating=4;

        Singleton.getINSTANCE().getAudio_database().AUDIO_DATA().changeRating(newRating,mFile.fullName);

    }

    public void deleteMusicFileFromDB(@NonNull MusicFile mFile){
        Singleton.getINSTANCE().getAudio_database().AUDIO_DATA().deleteMusicDataByUri(mFile.fullName);
    }

    //восстанавливаем файл на основном листе
    public void restoreFileInList(@NonNull MusicFile mFile){
        Singleton.getINSTANCE().getAudio_database().AUDIO_DATA().changeStatus(Audio.NOT_DELETED,mFile.fullName);
    }

    /**
     * выгружаем из БД список удаленных композиций
     * @return
     */
    public List<MusicFile> getDeletedMusicDataFromDB(){
        try{

            return Singleton.getINSTANCE().getAudio_database().AUDIO_DATA().getMusicListByStatus(Audio.DELETED);

        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    //устанавливаем пользовательские данные
    public boolean setUserSettings(Float minSize, Float maxSize,@NonNull String sortBy){
        try{
            if(userSettingsMutableLiveData==null) throw new Exception("Нет LiveData для UserSettings.");

            if(minSize!=null && maxSize!=null)
                if(minSize>maxSize) throw new Exception("Минимальный размер превышает максимальный размер.");


            if(!sortBy.equals(UserSettings.SORT_BY_RATING) &&
                    !sortBy.equals(UserSettings.SORT_BY_ARTIST) &&
                    !sortBy.equals(UserSettings.SORT_BY_DATE) &&
                    !sortBy.equals(UserSettings.SORT_BY_NAME)) throw new Exception("Не верно задан таг сортировки: "+sortBy);

            //записываем данные в класс и ShPr
            Singleton.getINSTANCE().getUser_settings().setMinSize(minSize);
            Singleton.getINSTANCE().getUser_settings().setMaxSize(maxSize);
            Singleton.getINSTANCE().getUser_settings().setSortBy(sortBy);

            //вызываем LiveData
            userSettingsMutableLiveData.getLiveData().setValue(Singleton.getINSTANCE().getUser_settings());

            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }

    }


    public ViewModelSettings getUserSettingsMutableLiveData() {
        return userSettingsMutableLiveData;
    }

    public ExecutorService getExecutors_service() {
        return executors_service;
    }

    public ViewModelMusicList getMusicListViewModel() {
        return musicListViewModel;
    }

    /**
     * получаем просто список композиций из БД без всяких проверок, но отсортированный в соответствии с пользовательскики настройками
     * @return
     */
    public List<MusicFile> getLiteMusicListFromDb(String sort_by, int sort_type){

        List<MusicFile> db_list;

        switch (sort_by){
            case UserSettings.SORT_BY_RATING:
                if(sort_type == SortSettings.SORT_TO_DOWN) db_list=Singleton.getINSTANCE().getAudio_database().AUDIO_DATA().getMusicDataByStatusOrderByRating(Audio.DELETED,true);
                else db_list=Singleton.getINSTANCE().getAudio_database().AUDIO_DATA().getMusicDataByStatusOrderByRating(Audio.DELETED,false);
                break;
            case UserSettings.SORT_BY_ARTIST:
                if(sort_type == SortSettings.SORT_TO_DOWN) db_list=Singleton.getINSTANCE().getAudio_database().AUDIO_DATA().getMusicDataByStatusOrderByArtist(Audio.DELETED,true);
                else db_list=Singleton.getINSTANCE().getAudio_database().AUDIO_DATA().getMusicDataByStatusOrderByArtist(Audio.DELETED,false);
                break;
            case UserSettings.SORT_BY_NAME:
                if(sort_type == SortSettings.SORT_TO_DOWN) db_list=Singleton.getINSTANCE().getAudio_database().AUDIO_DATA().getMusicDataByStatusOrderByName(Audio.DELETED,true);
                else db_list=Singleton.getINSTANCE().getAudio_database().AUDIO_DATA().getMusicDataByStatusOrderByName(Audio.DELETED,false);
                break;
            case UserSettings.SORT_BY_DATE:
                if(sort_type == SortSettings.SORT_TO_DOWN) db_list=Singleton.getINSTANCE().getAudio_database().AUDIO_DATA().getMusicDataByStatusOrderByUnix(Audio.DELETED,true);
                else db_list=Singleton.getINSTANCE().getAudio_database().AUDIO_DATA().getMusicDataByStatusOrderByUnix(Audio.DELETED,false);
                break;

            default: db_list=new ArrayList<>();
        }

        return db_list;
    }


    /**
     * функция получает выгруженный с помощью Context список композиций, обрабатывает его и вызывает Observer для публикации списка
     * @param musicList
     */
    public List<MusicFile> getActualMusicListFromDb(ArrayList<MusicFile> musicList){

        //записываем ее в БД через асинхронный запрос с корректировкой на настройки пользователя
        Singleton.getINSTANCE().getRepository().writeIfNotExistMusicListFilesIntoDb(musicList);

        //настройки для сортировки
        int sort_type=Singleton.getINSTANCE().getSort_settings().getSORT_TYPE();

        String sort_by=Singleton.getINSTANCE().getUser_settings().getSORT_BY();
        List<MusicFile> db_list=getLiteMusicListFromDb(sort_by,sort_type);

        return db_list;

    }



}
