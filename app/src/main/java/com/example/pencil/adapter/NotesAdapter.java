package com.example.pencil.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pencil.R;
import com.example.pencil.entities.Note;
import com.example.pencil.listeners.NotesListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NoteViewHolder> {


    private List<Note> notes;
    private NotesListener notesListener;
    private List<Note> newList;
    private Timer timer;


    public NotesAdapter(List<Note> notes,NotesListener notesListener) {
        this.notes = notes;
        this.notesListener = notesListener;
        newList = notes;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.note_rv_item,parent,false);

        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        holder.setNote(notes.get(position));
        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notesListener.onNoteClicked(notes.get(position),position);
            }
        });



    }


    @Override
    public int getItemCount() {
        return notes.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }


    class NoteViewHolder extends RecyclerView.ViewHolder {

        TextView noteTitle, noteDateTime, noteDes,alarmIcon,noteCategory;
        ImageView noteImage,audioFileIcon;
        CardView cardView;
        RelativeLayout layout,lockLayout;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);

            noteTitle = itemView.findViewById(R.id.titleNote);
            noteDateTime = itemView.findViewById(R.id.timeNote);
            noteDes = itemView.findViewById(R.id.descriptionNote);
            noteImage = itemView.findViewById(R.id.imageNote);
            cardView = itemView.findViewById(R.id.noteItem);
            layout = itemView.findViewById(R.id.item_relative_layout);
            audioFileIcon = itemView.findViewById(R.id.audioFileIcon);
            alarmIcon = itemView.findViewById(R.id.alarmIcon);
            lockLayout = itemView.findViewById(R.id.lockRelativeLayout);
            noteCategory = itemView.findViewById(R.id.note_category);
        }

        void setNote(Note note){
            noteTitle.setText(note.getTitle());
            noteDateTime.setText(note.getDateTime());
            noteDes.setText(note.getNoteText());
            noteCategory.setText(note.getNoteCategory());

            noteTitle.setTextColor(Color.parseColor(note.getFontColor()));
            noteDateTime.setTextColor(Color.parseColor(note.getFontColor()));
            noteDes.setTextColor(Color.parseColor(note.getFontColor()));


            if (note.isNoteProtected()){
                layout.setVisibility(View.GONE);
                lockLayout.setVisibility(View.VISIBLE);
            }else {
                layout.setVisibility(View.VISIBLE);
                lockLayout.setVisibility(View.GONE);
                if (note.getImagePath() == null){
                    noteImage.setVisibility(View.GONE);
                }else {
                    noteImage.setImageBitmap(BitmapFactory.decodeFile(note.getImagePath()));
                    noteImage.setVisibility(View.VISIBLE);
                }

                if (note.getNoteBgColor()!=null){
                    cardView.setCardBackgroundColor(Color.parseColor(note.getNoteBgColor()));
                }

                if (note.getAudioPath()!=null){
                    audioFileIcon.setVisibility(View.VISIBLE);
                }
                if (note.getAlarmTime()!=null){
                    alarmIcon.setVisibility(View.VISIBLE);
                    alarmIcon.setText(note.getAlarmTime());
                }
            }



        }
    }

    public void searchNotes(final  String searchKeyword){
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (searchKeyword.trim().isEmpty()){
                    notes = newList;
                }else {
                    ArrayList<Note> temp = new ArrayList<>();
                    for (Note note: newList){
                        if (note.getTitle().toLowerCase().contains(searchKeyword.toLowerCase())
                        || note.getNoteText().toLowerCase().contains(searchKeyword.toLowerCase()))
                        {
                            temp.add(note);
                        }
                    }
                    notes = temp;
                }

                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        notifyDataSetChanged();
                    }
                });

            }
        },300);
    }

    public void cancelTimer(){
        if (timer!=null){
            timer.cancel();
        }
    }
}