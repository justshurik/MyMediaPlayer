package com.example.mymediaplayer.mvvm.view.audio;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.annotation.NonNull;

import com.example.mymediaplayer.R;
import com.example.mymediaplayer.mvvm.model.database.MusicFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;

/**
 * класс для работы с аудиофайлами
 */
public class Audio {

    //классы рейтинга
    public static final int RATE_1=1;   //минимальный (1 звездочка)
    public static final int RATE_2=2;
    public static final int RATE_3=3;
    public static final int RATE_4=4;
    public static final int RATE_5=5;   //максимальный (5 звездочек)

    //статус
    public static final int DELETED=0;   //удаленная
    public static final int NOT_DELETED=1;  //не удаленная
    public static final int INDIVIDUAL=2;  //не удаленная

    private Context context;
    public Audio(Context context){
        this.context=context;
    }

    //берем параметры файла по URI
    public MusicFile getAudioParamsFromUri(@NonNull Uri uri){

        try{
            Audio mAudio = new Audio(context);
            MusicFile mFile = mAudio.getAudioDataByUri(uri);

            return mFile;

        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * функция поиска медиа-файлов на внешнем хранилище
     * @param minSize
     * @param maxSize
     * @return
     */
    public ArrayList<MusicFile> searchAllFiles(@NonNull float minSize,@NonNull float maxSize){

        try(Cursor cursor = context.
                getContentResolver().
                query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        new String[]{MediaStore.Audio.Media.DATA},
                        null,null,null)){

            ArrayList<MusicFile> list = new ArrayList<>();

            MusicFile mFile=null;
            while (cursor!=null && cursor.moveToNext()){

                int index=cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
                Uri uri = Uri.parse(cursor.getString(index));
                float size=sizeAudioInMb(uri);
                //отфильтровываем по размеру
                if(size>=minSize && size<=maxSize) {

                    mFile = getAudioDataByUri(uri);

                    if (mFile == null) throw new Exception("Не удалось взять файл по URI.");
                    mFile.rating= Audio.RATE_1; //устанавливаем минимальный рейтинг
                    //добавляем файл в коллекцию
                    list.add(mFile);
                }

            }

            cursor.close();

            return list;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * пытаемся взять все параметры аудиофайла по uri
     * @param uri
     * @return
     */
    public MusicFile getAudioDataByUri(@NonNull Uri uri){

        try{

            MediaMetadataRetriever metaRetriver = new MediaMetadataRetriever();
            metaRetriver.setDataSource(context,uri);
            String sArtist=null, sNameAudio=null, sPath=null;


            sArtist=metaRetriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            if(sArtist==null) sArtist=metaRetriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_AUTHOR);
            if(sArtist==null) sArtist=metaRetriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST);
            if(sArtist==null) sArtist=metaRetriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_COMPOSER);
            if(sArtist==null) sArtist=context.getResources().getString(R.string.none);

            sNameAudio=getNameOfAudio(uri,metaRetriver);
            if(sNameAudio==null) throw new Exception("Не удалось найти название файла.");

            //sNameAudio=metaRetriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);

            Long unix=getUnixTimeCreationAudio(uri); //время последнего изменения файла

            sPath=getPathAudio(uri);  //путь к файлу
            if(sPath==null) throw new Exception("Не удалось найти путь к аудиофайлу.");

            MusicFile mAudio = new MusicFile();
            mAudio.name=sNameAudio;
            mAudio.fullName=sPath;
            mAudio.unix=unix;
            mAudio.artist=sArtist;
            mAudio.rating=RATE_5;
            mAudio.status=NOT_DELETED;

            return mAudio;

        }catch (Exception e){
            e.printStackTrace();
            return null;
        }

    }

    //название файла
    //приоритетом является название файла, записанное в папке, а потом - в аттрибутах
    private String getNameOfAudio(@NonNull Uri uri,@NonNull MediaMetadataRetriever metaRetriver){
        try{
            String sName=null;

            String sPath= uri.getPath();
            String[] elements=sPath.split("\\/");
            if(elements.length>0) {
                sName = elements[elements.length-1];
                sName=sName.replaceAll("%20"," ").replaceAll(".mp3","");

            }else{
                sName=metaRetriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            }

            return sName;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    //берем размер файла
    public Float sizeAudioInMb(Uri uri){
        try{
            String path=uri.getPath();
            File fAudioFile=new File(path);

            Float size=fAudioFile.length()/1f;


            if(size==0) throw new Exception("Не верный размер аудиофайла.");

            return size/1024f/1024f;

        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    //получаем время создания композиции
    private Long getUnixTimeCreationAudio(Uri uri){

        try{
            File fAudioFile=new File(String.valueOf(uri));
            Long unix=null;
            unix= fAudioFile.lastModified();
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){

                BasicFileAttributes attrs= Files.readAttributes(fAudioFile.toPath(), BasicFileAttributes.class);
                unix=attrs.creationTime().toMillis();

            }else{

                unix= fAudioFile.lastModified();
            }

            if(unix==0) throw new Exception();

            return unix;

        }catch (Exception e){

            return System.currentTimeMillis();
        }

    }

    //получаем путь к файлу
    private String getPathAudio(@NonNull Uri uri){
        try{

            return uri.getPath();

        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }


}
