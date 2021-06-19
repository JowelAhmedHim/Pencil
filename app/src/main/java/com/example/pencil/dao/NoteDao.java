package com.example.pencil.dao;


import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.pencil.entities.Note;

import java.util.List;

@Dao
public interface NoteDao {

    @Query("SELECT * FROM notes ORDER BY id DESC")
    List<Note> getAllNote();

    @Query("SELECT * FROM notes WHERE alarmTime>0")
    List<Note> getFavNote();

    @Query("SELECT * FROM notes WHERE noteProtected")
    List<Note> getProtectedNote();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertNote(Note note);

    @Delete
    void deleteNote(Note note);

}
