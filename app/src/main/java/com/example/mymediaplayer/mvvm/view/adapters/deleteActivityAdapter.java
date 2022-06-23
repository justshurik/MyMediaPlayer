package com.example.mymediaplayer.mvvm.view.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mymediaplayer.R;
import com.example.mymediaplayer.mvvm.model.database.MusicFile;

import java.util.List;

public class deleteActivityAdapter extends RecyclerView.Adapter<deleteActivityAdapter.DeletedItem> {

    private final int ID_RESTORE=1001;
    private final int ID_DELETE=1002;

    private Context context;
    private List<MusicFile> deletedCompositions;
    private LayoutInflater inflater;
    private IPopupMenu popupListener;

    public deleteActivityAdapter(Context context, List<MusicFile> list){
        this.context=context;
        this.inflater=LayoutInflater.from(context);
        this.deletedCompositions=list;
    }

    public void setDeletedCompositions(List<MusicFile> list){
        this.deletedCompositions=list;
        notifyDataSetChanged();
    }

    public void setPopupListener(IPopupMenu popupListener) {
        this.popupListener = popupListener;
    }

    @NonNull
    @Override
    public DeletedItem onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = inflater.inflate(R.layout.deleted_items,parent,false);
        return new DeletedItem(v);

    }

    @Override
    public void onBindViewHolder(@NonNull DeletedItem holder, int position) {
        MusicFile musicFile = deletedCompositions.get(position);

        holder.tvName.setText(musicFile.name);
        holder.tvArtist.setText(musicFile.artist);

        holder.btnPopup.setTag(musicFile);  //запоминаем элемент

        holder.btnPopup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupMenu(v);
            }
        });

    }


    //показываем popup menu
    private void showPopupMenu(View v){
        PopupMenu popup_menu = new PopupMenu(v.getContext(),v);

        popup_menu.getMenu().add(0,ID_RESTORE, Menu.NONE,context.getResources().getStringArray(R.array.popup)[0]);
        popup_menu.getMenu().add(0,ID_DELETE,Menu.NONE,context.getResources().getStringArray(R.array.popup)[1]);
        MusicFile musicFile = (MusicFile) v.getTag();

        popup_menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){
                    case ID_RESTORE: popupListener.restore(musicFile); break;
                    case ID_DELETE: popupListener.deleteFromDb(musicFile); break;
                }
                return true;
            }
        });
        popup_menu.show();

    }



    @Override
    public int getItemCount() {
        return deletedCompositions.size();
    }



    public class DeletedItem extends RecyclerView.ViewHolder{
        TextView tvName;
        TextView tvArtist;
        ImageView btnPopup;

        public DeletedItem(@NonNull View itemView) {
            super(itemView);
            tvName=itemView.findViewById(R.id.name_audio);
            tvArtist=itemView.findViewById(R.id.creator_audio);

            btnPopup=itemView.findViewById(R.id.popup);
        }
    }

}
