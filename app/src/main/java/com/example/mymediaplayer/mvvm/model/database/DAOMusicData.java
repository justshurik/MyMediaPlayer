package com.example.mymediaplayer.mvvm.model.database;

import androidx.annotation.NonNull;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.net.URI;
import java.util.List;

@Dao
public interface DAOMusicData {

    @Insert
    public void insert(MusicFile mFileData);

    @Delete
    public void delete(MusicFile musicFile);


    @Query("SELECT * FROM music_table WHERE status != :iStatus ORDER BY CASE WHEN :ask = 1 THEN name END ASC, CASE WHEN :ask=0 THEN name END DESC")
    List<MusicFile> getMusicDataByStatusOrderByName(@NonNull int iStatus, boolean ask);

    @Query("SELECT * FROM music_table WHERE status != :iStatus ORDER BY CASE WHEN :ask = 1 THEN artist END ASC, CASE WHEN :ask=0 THEN artist END DESC")
    List<MusicFile> getMusicDataByStatusOrderByArtist(@NonNull int iStatus, boolean ask);

    @Query("SELECT * FROM music_table WHERE status != :iStatus ORDER BY CASE WHEN :ask=1 THEN rating END ASC, CASE WHEN :ask=0 THEN rating END DESC")
    List<MusicFile> getMusicDataByStatusOrderByRating(@NonNull int iStatus, boolean ask);

    @Query("SELECT * FROM music_table WHERE status != :iStatus ORDER BY CASE WHEN :ask=1 THEN unix END ASC, CASE WHEN :ask=0 THEN unix END DESC")
    List<MusicFile> getMusicDataByStatusOrderByUnix(@NonNull int iStatus, boolean ask);

    @Query("SELECT * FROM music_table")
    List<MusicFile> getAllMusicData();

    @Query("SELECT * FROM music_table WHERE status = :iStatus ORDER BY name")
    List<MusicFile> getMusicListByStatus(int iStatus);

    @Query("SELECT COUNT(*) FROM music_table WHERE fullName = :sPath")
    int calcFilesByParam(String sPath);

    @Query("DELETE FROM music_table WHERE fullName = :full_name")
    public void deleteMusicDataByUri(String full_name);

    @Query("UPDATE music_table SET status = :iStatus WHERE fullName = :sPath")
    public void changeStatus(int iStatus, String sPath);

    @Query("UPDATE music_table SET rating = :iRating WHERE fullName = :sPath")
    public void changeRating(int iRating, String sPath);

    @Query("DELETE FROM music_table")
    public void clear_table();

    @Query("SELECT * FROM music_table WHERE fullName = :sPath ORDER BY name")
    List<MusicFile> getMusicFileByUri(String sPath);

    @Query("DELETE FROM music_table WHERE status = :iStatus")
    void deleteMusicFilesByStatus(int iStatus);



}
