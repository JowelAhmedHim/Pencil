package com.example.pencil.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pencil.R;
import com.example.pencil.entities.Note;

import java.util.List;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NoteViewHolder> {


    private List<Note> notes;

    public NotesAdapter(List<Note> notes) {
        this.notes = notes;
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

        TextView noteTitle, noteDateTime, noteDes;
        ImageView noteImage;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);

            noteTitle = itemView.findViewById(R.id.titleNote);
            noteDateTime = itemView.findViewById(R.id.timeNote);
            noteDes = itemView.findViewById(R.id.descriptionNote);
            noteImage = itemView.findViewById(R.id.imageNote);



        }

        void setNote(Note note){
            noteTitle.setText(note.getTitle());
            noteDateTime.setText(note.getDateTime());
            noteDes.setText(note.getNoteText());
            if (note.getImagePath() == null){
                noteImage.setVisibility(View.GONE);
            }else {

                noteImage.setImageBitmap(BitmapFactory.decodeFile(note.getImagePath()));
                noteImage.setVisibility(View.VISIBLE);
            }


        }
    }
}
