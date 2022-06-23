package com.example.mymediaplayer.mvvm.view.adapters;

import com.example.mymediaplayer.mvvm.model.database.MusicFile;

import java.io.IOException;
import java.util.List;

public interface IAudioPlayerData {

    public void play_audio_from_adapter(List<MusicFile> list, int number_of_element) throws IOException;
}
