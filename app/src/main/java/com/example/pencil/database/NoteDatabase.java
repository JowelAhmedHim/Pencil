package com.example.pencil.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.pencil.dao.NoteDao;
import com.example.pencil.entities.Note;


@Database(entities = Note.class, version = 1, exportSchema = false)
public abstract class NoteDatabase extends RoomDatabase {

    private static  NoteDatabase noteDatabase;
    public static synchronized NoteDatabase getNoteDatabase(Context context)
    {
        if (noteDatabase == null){
            noteDatabase = Room.databaseBuilder(
                    context,
                    NoteDatabase.class,
                    "note_db"
            ).build();
        }
        return  noteDatabase;
    }

    public abstract NoteDao noteDao();
}
