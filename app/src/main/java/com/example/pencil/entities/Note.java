package com.example.pencil.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "notes")
public class Note implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private int id;
    @ColumnInfo(name = "title")
    private String title;
    @ColumnInfo(name = "date_time")
    private String dateTime;
    @ColumnInfo(name = "note_text")
    private String noteText;
    @ColumnInfo(name = "image_path")
    private String imagePath;
    @ColumnInfo(name = "audioPath")
    private String audioPath;
    @ColumnInfo(name = "fontColor")
    private String fontColor;
    @ColumnInfo(name = "fontState")
    private String fontState;
    @ColumnInfo(name = "fontFamily")
    private String fontFamily;
    @ColumnInfo(name = "alarmTime")
    private String alarmTime;
    @ColumnInfo(name = "noteBgColor")
    private String noteBgColor;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getNoteText() {
        return noteText;
    }

    public void setNoteText(String noteText) {
        this.noteText = noteText;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getFontState() {
        return fontState;
    }

    public void setFontState(String fontState) {
        this.fontState = fontState;
    }

    public String getFontFamily() {
        return fontFamily;
    }

    public void setFontFamily(String fontFamily) {
        this.fontFamily = fontFamily;
    }

    public String getAlarmTime() {
        return alarmTime;
    }

    public void setAlarmTime(String alarmTime) {
        this.alarmTime = alarmTime;
    }


    public String getAudioPath() {
        return audioPath;
    }

    public void setAudioPath(String audioPath) {
        this.audioPath = audioPath;
    }

    public String getNoteBgColor() {
        return noteBgColor;
    }

    public void setNoteBgColor(String noteBgColor) {
        this.noteBgColor = noteBgColor;
    }

    public String getFontColor() {
        return fontColor;
    }

    public void setFontColor(String fontColor) {
        this.fontColor = fontColor;
    }

    @NonNull
    @Override
    public String toString() {
        return title + ":" +dateTime;
    }
}
