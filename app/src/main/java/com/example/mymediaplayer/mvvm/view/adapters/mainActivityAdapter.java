package com.example.mymediaplayer.mvvm.view.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.MediaPlayer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mymediaplayer.R;
import com.example.mymediaplayer.mvvm.model.database.MusicFile;
import com.example.mymediaplayer.mvvm.view.audio.Audio;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class mainActivityAdapter extends RecyclerView.Adapter<mainActivityAdapter.AudioData> {

    public static final int NO_SELECTED=-1;

    private List<MusicFile> compositions;  //данные из БД
    private Context context;
    private LayoutInflater inflater;

    private ISetRating ratingListener;
    private IDeleteFromList deleteFromListListener;
    private IAudioPlayerData playAudio;
    private int currentComposition=NO_SELECTED;

    public mainActivityAdapter(Context context, List<MusicFile> compositions){
        this.context = context;
        this.compositions=compositions;
        inflater=LayoutInflater.from(context);
    }

    //убираем композицию из adapter по uri
    public void removeComposition(MusicFile file){
        for(int i=0;i<compositions.size();i++){
            if(compositions.get(i).fullName.equals(file.fullName)){
                compositions.remove(i);
                notifyItemRemoved(i);
            }
        }
    }

    public void setCurrentComposition(int currentComposition) {
        this.currentComposition = currentComposition;
    }

    //добавляем композицию
    public void setCompositions(List<MusicFile> list){
        this.compositions = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AudioData onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = inflater.inflate(R.layout.items,parent,false);
        return new AudioData(v);
    }

    @Override
    public void onBindViewHolder(@NonNull AudioData holder, @SuppressLint("RecyclerView") int position) {
        MusicFile file = compositions.get(position);
        String sName= file.name;
        String sAuthor = file.artist;

        holder.tvNameComposition.setText(sName);
        holder.tvArtist.setText(sAuthor);
        //количество звыезд

        setRating(holder.lRatingPanel,file.rating,file,position);

        //удаление композиции
        holder.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                deleteFromListListener.deleteFromList(compositions,position);
            }
        });

        holder.cvItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //начинаем проигрывать композиции
                try {
                    playAudio.play_audio_from_adapter(compositions,position);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        if(currentComposition!=NO_SELECTED){
            if(position==currentComposition){
                holder.item_layout.setBackground(context.getDrawable(R.color.selected_current_composition_color));
            }else{
                holder.item_layout.setBackground(context.getDrawable(R.color.white));
            }
        }else{
            holder.item_layout.setBackground(context.getDrawable(R.color.white));
        }

    }

    //устанавливаем рейтинги и боработчики событий
    private void setRating(LinearLayout ratingPanel, int iRating, MusicFile mFile, int position){
        ImageView star1=ratingPanel.findViewById(R.id.star1);
        ImageView star2=ratingPanel.findViewById(R.id.star2);
        ImageView star3=ratingPanel.findViewById(R.id.star3);
        ImageView star4=ratingPanel.findViewById(R.id.star4);
        ImageView star5=ratingPanel.findViewById(R.id.star5);

        ImageView[] stars = new ImageView[]{star1,star2, star3, star4, star5};
        if(iRating<0) iRating=0;
        if(iRating>4) iRating=4;

        for(int i=0;i<5;i++){
            if(i<=iRating) stars[i].setBackground(context.getDrawable(R.drawable.star_yellow));
            else stars[i].setBackground(context.getDrawable(R.drawable.star_border));

            //устанавливаем слушателя
            int iCurrentRating = i;

            stars[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    compositions.get(position).rating=iCurrentRating;
                    ratingListener.setRating(mFile, iCurrentRating,stars);
                    notifyDataSetChanged();
                    //currentComposition=NO_SELECTED;
                }
            });
        }
   }


    @Override
    public int getItemCount() {
        return compositions.size();
    }

    public class AudioData extends RecyclerView.ViewHolder {

        TextView tvNameComposition;
        TextView tvArtist;
        LinearLayout lRatingPanel;
        ImageView btnDelete;
        CardView cvItem;
        ConstraintLayout item_layout;

        public AudioData(@NonNull View v) {
            super(v);
            tvNameComposition=v.findViewById(R.id.name_audio);
            tvArtist=v.findViewById(R.id.creator_audio);
            lRatingPanel=v.findViewById(R.id.rating_panel);
            btnDelete=v.findViewById(R.id.delete);
            cvItem=v.findViewById(R.id.audio_item);
            item_layout=v.findViewById(R.id.item_constrained_layout);

        }
    }

    public void setSetRatingListener(ISetRating setRatingListener) {
        this.ratingListener = setRatingListener;
    }

    public void setDeleteFromListListener(IDeleteFromList deleteFromListListener) {
        this.deleteFromListListener = deleteFromListListener;
    }

    public void setPlayAudio(IAudioPlayerData playAudio) {
        this.playAudio = playAudio;
    }
}
