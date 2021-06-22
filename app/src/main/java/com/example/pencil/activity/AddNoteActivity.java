package com.example.pencil.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.DialogFragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.pencil.broadcastReciever.AlertReceiver;
import com.example.pencil.R;
import com.example.pencil.database.NoteDatabase;
import com.example.pencil.entities.Note;
import com.example.pencil.fragment.TimePickerFragment;
import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorSelectedListener;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.visualizer.amplitude.AudioRecordView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.Time;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class AddNoteActivity extends AppCompatActivity implements View.OnClickListener,TimePickerDialog.OnTimeSetListener {

    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int STORAGE_REQUEST_CODE = 300;
    private static final int IMAGE_PICK_GALLERY_CODE = 400;
    private static final int IMAGE_PICK_CAMERA_CODE = 500;
    private static final int RECORD_AUDIO_REQUEST_CODE = 600;
    private static final int REQUEST_CODE_SPEECH_TITLE_INPUT = 700;
    private static final int REQUEST_CODE_SPEECH_DES_INPUT = 800;

    private String[] cameraPermission;
    private String[] storagePermission;
    private String[] recordingPermission;

    private RelativeLayout noteBackgroundColor,noteAudioFileLayout,alertTimerLayout;
    private ImageView noteImage,deleteImage,deleteNote,audioFileDelete,audioIcon,notePrivacy;
    private ImageButton noteBackBtn,noteSaveBtn,noteAlertBtn,micTitle,micDes,cancelAlert;
    private TextView noteTime,alertTime,audioFileTitle;
    private EditText noteTitle,noteDescription;

    private BottomNavigationView navigationView;
    private BottomSheetDialog bottomSheetDialog;
    private AlertDialog alertDialog;

    private Spinner spinner;
    private ProgressDialog progressDialog;

    private String selectedFontColor;
    private String selectedFontFamily ;
    private String selectedImagePath ;
    private String selectedNoteColor;
    private String selectedNoteCategory;
    private String selectedAlarmTime;
    private String audioFilePath;
    private String recordFile;

    private  String currentDateAndTime;

    private MediaRecorder mediaRecorder;
    private boolean isRecording = false;
    private boolean isPaused = false;
    private boolean noteProtected = false;
    private long pauseOffset;
    private Note alreadyAvailableNote;

    private MediaPlayer mediaPlayer;
    private boolean isAudioPlaying ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);
        
        //initially set note attribute
        selectedNoteColor = "#ffffff";
        selectedFontColor = null;
        selectedImagePath = null;
        selectedAlarmTime = null;
        selectedFontFamily = null;
        audioFilePath = null;

        //progress dialog setup
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        //initiate view and permission
        init();
        initPermission();

        //add listener
        noteBackBtn.setOnClickListener(this);
        noteSaveBtn.setOnClickListener(this);
        noteAlertBtn.setOnClickListener(this);
        deleteImage.setOnClickListener(this);
        noteAudioFileLayout.setOnClickListener(this);
        audioFileDelete.setOnClickListener(this);
        notePrivacy.setOnClickListener(this);
        micTitle.setOnClickListener(this);
        micDes.setOnClickListener(this);
        cancelAlert.setOnClickListener(this);

         //getPresentTime
         getPresentTime();
         //getNoteCategory
         getCategory();

        if (getIntent().getBooleanExtra("isViewOrUpdate",false)){
            alreadyAvailableNote = (Note) getIntent().getSerializableExtra("note");
            setViewOrUpdate();
        }

        //bottom navigation view setup
        navigationView = findViewById(R.id.bottomNavigation);
        navigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId())
                {
                    case R.id.addText:
                         showBottomSheetFontDialog();
                        return true;
                    case R.id.addImage:
                        showImagePickerDialog();
                        return true;

                    case R.id.addVoice:
                        if (checkAudioRecordPermission()){
                            showBottomSheetVoiceDialog();
                        }else {
                            requestAudioRecordPermission();
                        }
                        return true;
                    case R.id.addColor:
                        showBottomSheetBackgroundColor();
                        return true;
                }
                return false;
            }
        });

    }

    //all view initialization
    private void init() {

        noteBackgroundColor = findViewById(R.id.relativeLayoutLayout);
        noteBackBtn = findViewById(R.id.noteBack_btn);
        noteAlertBtn = findViewById(R.id.noteAlert_btn);
        noteSaveBtn = findViewById(R.id.noteSave_btn);
        alertTime = findViewById(R.id.alertTimer);
        noteTime = findViewById(R.id.noteTime);
        noteTitle = findViewById(R.id.noteTitle);
        noteDescription = findViewById(R.id.noteDescription);
        noteImage = findViewById(R.id.noteImage);
        spinner = findViewById(R.id.spinner);
        deleteImage = findViewById(R.id.deleteImage);
        deleteNote = findViewById(R.id.noteDelete);
        noteAudioFileLayout = findViewById(R.id.audioFileLayout);
        audioFileDelete = findViewById(R.id.audioDelete);
        audioFileTitle = findViewById(R.id.fileName);
        audioIcon = findViewById(R.id.audioIcon);
        notePrivacy = findViewById(R.id.notePrivacy);
        micTitle = findViewById(R.id.micTitle);
        micDes = findViewById(R.id.micDes);
        alertTimerLayout = findViewById(R.id.alertTimerLayout);
        cancelAlert = findViewById(R.id.deleteAlertTime);

    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){

            case R.id.micTitle:
                String s = "title";
                micFunction(s);
                break;
            case R.id.micDes:
                String s1 = "des";
                micFunction(s1);
                break;
            case R.id.noteBack_btn:
                onBackPressed();
                break;
            case R.id.noteAlert_btn:
                showTimePicker();
                break;
            case R.id.noteSave_btn:
                saveData();
                break;
            case R.id.audioFileLayout:
                playAudioFile(audioFilePath);
                break;
            case R.id.notePrivacy:
                protectedFunction();
                break;
            case R.id.audioDelete:
                noteAudioDeleted();
                break;
            case R.id.deleteAlertTime:
                selectedAlarmTime = null;
                alertTimerLayout.setVisibility(View.GONE);
                break;
            case R.id.deleteImage:
                noteImage.setImageBitmap(null);
                noteImage.setVisibility(View.GONE);
                deleteImage.setVisibility(View.GONE);
                selectedImagePath = null;
                break;
        }
    }

    //all permission initialization
    private void initPermission() {
        cameraPermission = new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        recordingPermission = new String[]{Manifest.permission.RECORD_AUDIO};
    }

    //save data on room database
    private void saveData(){
        if (noteTitle.getText().toString().trim().isEmpty()){
            Toast.makeText(this, "Note title can't be empty", Toast.LENGTH_SHORT).show();
            return;
        }
        final Note note = new Note();

        note.setTitle(noteTitle.getText().toString());
        note.setNoteText(noteDescription.getText().toString());
        note.setDateTime(noteTime.getText().toString());
        note.setImagePath(selectedImagePath);
        note.setFontColor(selectedFontColor);
        note.setFontFamily(selectedFontFamily);
        note.setAudioPath(audioFilePath);
        note.setNoteBgColor(selectedNoteColor);
        note.setAlarmTime(selectedAlarmTime);
        note.setNoteProtected(noteProtected);
        note.setNoteCategory(selectedNoteCategory);

        if (alreadyAvailableNote!=null){
            note.setId(alreadyAvailableNote.getId());
        }


        @SuppressLint("StaticFieldLeak")
        class SaveNoteTask extends AsyncTask<Void,Void,Void>{

            @Override
            protected Void doInBackground(Void... voids) {
                NoteDatabase.getNoteDatabase(getApplicationContext()).noteDao().insertNote(note);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);

                Intent intent = new Intent();
                setResult(RESULT_OK,intent);
                finish();
            }
        }

        new SaveNoteTask().execute();
    }

    //view note and update notes
    private void setViewOrUpdate() {

        noteTitle.setText(alreadyAvailableNote.getTitle());
        noteDescription.setText(alreadyAvailableNote.getNoteText());
        noteTime.setText(alreadyAvailableNote.getDateTime());

        if(alreadyAvailableNote.getImagePath() != null && !alreadyAvailableNote.getImagePath().trim().isEmpty()){
            noteImage.setImageBitmap(BitmapFactory.decodeFile(alreadyAvailableNote.getImagePath()));
            noteImage.setVisibility(View.VISIBLE);
            deleteImage.setVisibility(View.VISIBLE);
            selectedImagePath = alreadyAvailableNote.getImagePath();
        }

        if(alreadyAvailableNote!=null){
            deleteNote.setVisibility(View.VISIBLE);
            deleteNote.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                        showDeleteNoteAlertDialog();
                }
            });
        }


        if (alreadyAvailableNote.getAudioPath()!=null && !alreadyAvailableNote.getAudioPath().trim().isEmpty())
        {
            noteAudioFileLayout.setVisibility(View.VISIBLE);
            audioFilePath =  alreadyAvailableNote.getAudioPath();
            noteAudioFileLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    playAudioFile(audioFilePath);
                }
            });
        }

        if (alreadyAvailableNote.getNoteBgColor() != null){
            noteBackgroundColor.setBackgroundColor(Color.parseColor(alreadyAvailableNote.getNoteBgColor()));
            selectedNoteColor = alreadyAvailableNote.getNoteBgColor();
        }

        if(alreadyAvailableNote.getFontColor() != null){
            noteTitle.setTextColor(Color.parseColor(alreadyAvailableNote.getFontColor()));
            noteTime.setTextColor(Color.parseColor(alreadyAvailableNote.getFontColor()));
            noteDescription.setTextColor(Color.parseColor(alreadyAvailableNote.getFontColor()));
            selectedFontColor = alreadyAvailableNote.getFontColor();
        }

        if (alreadyAvailableNote.getAlarmTime()!=null){
            alertTimerLayout.setVisibility(View.VISIBLE);
            alertTime.setText(alreadyAvailableNote.getAlarmTime());
            selectedAlarmTime= alreadyAvailableNote.getAlarmTime();

        }

        if (alreadyAvailableNote.getFontFamily()!=null){
            if (alreadyAvailableNote.getFontFamily().equals("amatic"))
            {
                noteTitle.setTypeface(ResourcesCompat.getFont(getApplicationContext(),R.font.amaticbold));
                noteDescription.setTypeface(ResourcesCompat.getFont(getApplicationContext(),R.font.amaticbold));
                noteTime.setTypeface(ResourcesCompat.getFont(getApplicationContext(),R.font.amaticbold));
                selectedFontFamily = alreadyAvailableNote.getFontFamily();
            }else  if (alreadyAvailableNote.getFontFamily().equals("pacific")){
                noteTitle.setTypeface(ResourcesCompat.getFont(getApplicationContext(),R.font.pacifico));
                noteDescription.setTypeface(ResourcesCompat.getFont(getApplicationContext(),R.font.pacifico));
                noteTime.setTypeface(ResourcesCompat.getFont(getApplicationContext(),R.font.pacifico));
                selectedFontFamily = alreadyAvailableNote.getFontFamily();
            }else if (alreadyAvailableNote.getFontFamily().equals("roboto")){
                noteTitle.setTypeface(ResourcesCompat.getFont(getApplicationContext(),R.font.robotoblack));
                noteDescription.setTypeface(ResourcesCompat.getFont(getApplicationContext(),R.font.robotoblack));
                noteTime.setTypeface(ResourcesCompat.getFont(getApplicationContext(),R.font.robotoblack));
                selectedFontFamily = alreadyAvailableNote.getFontFamily();
            }
        }

        if (alreadyAvailableNote.getNoteCategory()!=null){
            selectedNoteCategory = alreadyAvailableNote.getNoteCategory();
            selectedNoteCategory = alreadyAvailableNote.getNoteCategory();
        }
        if (alreadyAvailableNote.isNoteProtected()){
            protectedFunction();
        }
    }

    //deleting note from database
    private void showDeleteNoteAlertDialog() {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            View view = LayoutInflater.from(this).inflate(
                    R.layout.delete_note_dialog,
                    (ViewGroup)findViewById(R.id.deleteNoteContainer)
            );
            builder.setView(view);
            builder.setCancelable(false);
            alertDialog = builder.create();
            if (alertDialog.getWindow()!=null){
                alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }
            view.findViewById(R.id.dialog_delete_btn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    class DeleteNoteTask extends AsyncTask<Void, Void,Void>{

                        @Override
                        protected Void doInBackground(Void... voids) {
                            NoteDatabase.getNoteDatabase(getApplicationContext()).noteDao()
                                    .deleteNote(alreadyAvailableNote);
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void aVoid) {
                            super.onPostExecute(aVoid);
                            Intent intent = new Intent();
                            intent.putExtra("isNoteDeleted",true);
                            setResult(RESULT_OK,intent);
                            finish();
                        }
                    }
                    new DeleteNoteTask().execute();
                    alertDialog.dismiss();
                }
            });

            view.findViewById(R.id.dialog_cancel_btn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialog.dismiss();
                }
            });
            alertDialog.show();

    }

    //background color change
    private void showBottomSheetBackgroundColor() {

        bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(R.layout.note_background_color);
        final ImageView imageColor1 = bottomSheetDialog.findViewById(R.id.imageColor1);
        final ImageView imageColor2 = bottomSheetDialog.findViewById(R.id.imageColor2);
        final ImageView imageColor3 = bottomSheetDialog.findViewById(R.id.imageColor3);
        final ImageView imageColor4 = bottomSheetDialog.findViewById(R.id.imageColor4);
        final ImageView imageColor5 = bottomSheetDialog.findViewById(R.id.imageColor5);

        bottomSheetDialog.findViewById(R.id.viewColor1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedNoteColor = "#ffffff";
                noteBackgroundColor.setBackgroundColor(Color.parseColor(selectedNoteColor));
                imageColor1.setImageResource(R.drawable.ic_baseline_done_24);
                imageColor2.setImageResource(0);
                imageColor3.setImageResource(0);
                imageColor4.setImageResource(0);
                imageColor5.setImageResource(0);
                //setNoteBackGroundColor();
            }
        });
        bottomSheetDialog.findViewById(R.id.viewColor2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedNoteColor = "#9FE2BF";
                noteBackgroundColor.setBackgroundColor(Color.parseColor(selectedNoteColor));
                imageColor1.setImageResource(0);
                imageColor2.setImageResource(R.drawable.ic_baseline_done_24);
                imageColor3.setImageResource(0);
                imageColor4.setImageResource(0);
                imageColor5.setImageResource(0);
                //setNoteBackGroundColor();
            }
        });
        bottomSheetDialog.findViewById(R.id.viewColor3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedNoteColor = "#40E0D0";
                noteBackgroundColor.setBackgroundColor(Color.parseColor(selectedNoteColor));
                imageColor1.setImageResource(0);
                imageColor2.setImageResource(0);
                imageColor3.setImageResource(R.drawable.ic_baseline_done_24);
                imageColor4.setImageResource(0);
                imageColor5.setImageResource(0);
               // setNoteBackGroundColor();
            }
        });
        bottomSheetDialog.findViewById(R.id.viewColor4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedNoteColor = "#CCCCFF";
                noteBackgroundColor.setBackgroundColor(Color.parseColor(selectedNoteColor));
                imageColor1.setImageResource(0);
                imageColor2.setImageResource(0);
                imageColor3.setImageResource(0);
                imageColor4.setImageResource(R.drawable.ic_baseline_done_24);
                imageColor5.setImageResource(0);
               // setNoteBackGroundColor();
            }
        });
        bottomSheetDialog.findViewById(R.id.viewColor5).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedNoteColor = "#FF7F50";
                noteBackgroundColor.setBackgroundColor(Color.parseColor(selectedNoteColor));
                imageColor1.setImageResource(0);
                imageColor2.setImageResource(0);
                imageColor3.setImageResource(0);
                imageColor4.setImageResource(0);
                imageColor5.setImageResource(R.drawable.ic_baseline_done_24);
                //setNoteBackGroundColor();
            }
        });

        if (alreadyAvailableNote != null && alreadyAvailableNote.getNoteBgColor() != null && alreadyAvailableNote.getNoteBgColor().trim().isEmpty()){
            switch (alreadyAvailableNote.getNoteBgColor()){
                case "#fffff":
                    bottomSheetDialog.findViewById(R.id.viewColor1).performClick();
                    break;
                case "#9FE2BF":
                    bottomSheetDialog.findViewById(R.id.viewColor2).performClick();
                    break;
                case "#FFC107":
                    bottomSheetDialog.findViewById(R.id.viewColor3).performClick();
                    break;
                case "#8C8989":
                    bottomSheetDialog.findViewById(R.id.viewColor4).performClick();
                    break;
                case "#FF018786":
                    bottomSheetDialog.findViewById(R.id.viewColor5).performClick();
                    break;

            }
        }
        bottomSheetDialog.create();
        bottomSheetDialog.show();

    }

    //get note category
    private void getCategory() {
        List<String> categories = new ArrayList<String>();
        categories.add("Default");
        categories.add("Personal");
        categories.add("Business");
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,categories);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(arrayAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedNoteCategory = (String) parent.getItemAtPosition(position);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    //get note save time
    private void getPresentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd/MM/yyyy ,HH:mm a", Locale.getDefault());
        currentDateAndTime = sdf.format(new Date());
        noteTime.setText(currentDateAndTime);
    }


    private void micFunction(String s) {

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,
                Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                "Speck to text"
                );

        try {
            if (s == "title")
            {
                startActivityForResult(intent,REQUEST_CODE_SPEECH_TITLE_INPUT);
            }else {
                startActivityForResult(intent,REQUEST_CODE_SPEECH_DES_INPUT);
            }

        }catch (Exception e){
            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }


    }

    private void protectedFunction() {
        if (noteProtected == false){
            noteProtected = true;
            Drawable img = getApplicationContext().getDrawable(R.drawable.ic_baseline_lock_24);
            notePrivacy.setImageDrawable(img);
        }else {
            noteProtected = false;
            Drawable img = getApplicationContext().getDrawable(R.drawable.ic_baseline_lock_open_24);
            notePrivacy.setImageDrawable(img);
        }
    }

    private void noteAudioDeleted() {
        if (isAudioPlaying)
        {
            stopPlayingAudio();
        }
        recordFile = "";
        noteAudioFileLayout.setVisibility(View.GONE);


    }

    private void playAudioFile(String audioFilePath) {
        String file = audioFilePath;
        if (!isAudioPlaying){
            audioIcon.setImageResource(R.drawable.ic_baseline_stop_circle_24);
            mediaPlayer = new MediaPlayer();
            try {
                mediaPlayer.setDataSource(file);
                mediaPlayer.prepare();
                mediaPlayer.start();

            }catch (Exception e)
            {
                e.printStackTrace();
            }

            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    stopPlayingAudio();
                }
            });
            isAudioPlaying = true;
        }else {
            stopPlayingAudio();
        }


    }
    private void stopPlayingAudio() {
        isAudioPlaying = false;
        audioIcon.setImageResource(R.drawable.ic_baseline_play_circle_24);
        mediaPlayer.stop();
    }


    //font functionality
    private void showBottomSheetFontDialog() {
        bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(R.layout.add_note_bottomsheet_font_dialog);
        bottomSheetDialog.setCancelable(false);

        TextView amatic,pacific,roboto;
        View black,yellow,orange;
        ImageView fontSave;

        fontSave = bottomSheetDialog.findViewById(R.id.fontSave);

        black = bottomSheetDialog.findViewById(R.id.blackColor);
        yellow = bottomSheetDialog.findViewById(R.id.yellowColor);
        orange = bottomSheetDialog.findViewById(R.id.redColor);
        black.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                noteTitle.setTextColor(Color.BLACK);
                noteDescription.setTextColor(Color.BLACK);
                noteTime.setTextColor(Color.BLACK);
                selectedFontColor = "#000000";

            }
        });
        orange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                noteTitle.setTextColor(Color.RED);
                noteDescription.setTextColor(Color.RED);
                noteTime.setTextColor(Color.RED);
                selectedFontColor = "#F44336";

            }
        });
        yellow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                noteTitle.setTextColor(Color.YELLOW);
                noteDescription.setTextColor(Color.YELLOW);
                noteTime.setTextColor(Color.YELLOW);
                selectedFontColor = "#FFC107";
            }
        });

        fontSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog.dismiss();
            }
        });
        amatic = bottomSheetDialog.findViewById(R.id.amatic);
        pacific = bottomSheetDialog.findViewById(R.id.pacific);
        roboto = bottomSheetDialog.findViewById(R.id.roboto);
        amatic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                amatic.setTextColor(Color.BLUE);
                pacific.setTextColor(Color.BLACK);
                roboto.setTextColor(Color.BLACK);
                noteTitle.setTypeface(ResourcesCompat.getFont(getApplicationContext(),R.font.amaticbold));
                noteDescription.setTypeface(ResourcesCompat.getFont(getApplicationContext(),R.font.amaticbold));
                selectedFontFamily = "amatic";

            }
        });
        pacific.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                amatic.setTextColor(Color.BLACK);
                pacific.setTextColor(Color.BLUE);
                roboto.setTextColor(Color.BLACK);
                noteTitle.setTypeface(ResourcesCompat.getFont(getApplicationContext(),R.font.pacifico));
                noteDescription.setTypeface(ResourcesCompat.getFont(getApplicationContext(),R.font.pacifico));
                selectedFontFamily = "pacific";

            }
        });
        roboto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                amatic.setTextColor(Color.BLACK);
                pacific.setTextColor(Color.BLACK);
                roboto.setTextColor(Color.BLUE);
                noteTitle.setTypeface(ResourcesCompat.getFont(getApplicationContext(),R.font.robotoblack));
                noteDescription.setTypeface(ResourcesCompat.getFont(getApplicationContext(),R.font.robotoblack));
                selectedFontFamily = "roboto";
            }
        });

        bottomSheetDialog.create();
        bottomSheetDialog.show();
    }

    //function of image taking of note
    private void showImagePickerDialog() {
        String[] option = {"Camera","Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick Image")
                .setItems(option, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which==0){
                            //camera clicked
                            if (checkCameraPermission())
                            {
                                //camera permission allowed
                                pickFromCamera();

                            }else {
                                //camera permission not allowed
                                requestCameraPermission();
                            }
                        }else {
                            //Gallery clicked
                            if (checkStoragePermission()){
                                //permission allowed
                                pickFromGallery();
                            }else {
                                //permission not allowed
                                requestStoragePermission();
                            }
                        }
                    }
                }).show();
    }
    private void pickFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent,IMAGE_PICK_GALLERY_CODE);
    }
    private void pickFromCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent,IMAGE_PICK_CAMERA_CODE);
    }


    Chronometer timer;
    //function of voice recording note
    private void showBottomSheetVoiceDialog() {

        bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(R.layout.add_note_bottom_sheet_dialog);
        bottomSheetDialog.setCancelable(false);

        ImageButton playBtn,cancelBtn,okBtn,stopbtn;


        timer = bottomSheetDialog.findViewById(R.id.chronometers);
        playBtn=bottomSheetDialog.findViewById(R.id.play_btn);
        cancelBtn = bottomSheetDialog.findViewById(R.id.cancel_btn);
        okBtn = bottomSheetDialog.findViewById(R.id.ok_btn);
        bottomSheetDialog.show();

        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isPaused) {
                    timer.setBase(SystemClock.elapsedRealtime()-pauseOffset);
                    timer.start();
                    resumedRecording();
                    playBtn.setImageResource(R.drawable.ic_baseline_pause_circle_24);
                }
                else if (isRecording) {
                    timer.stop();
                    pauseOffset = SystemClock.elapsedRealtime() - timer.getBase();
                    pauseRecording();
                    playBtn.setImageResource(R.drawable.ic_baseline_play_circle_24);
                }else {
                    timer.setBase(SystemClock.elapsedRealtime()-pauseOffset);
                    timer.start();
                    startRecording();
                    playBtn.setImageResource(R.drawable.ic_baseline_pause_circle_24);
                }
            }
        });

        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isRecording || isPaused){
                    timer.stop();
                    timer.setBase(SystemClock.elapsedRealtime());
                    pauseOffset = 0;
                    stopRecording();
                   // Toast.makeText(AddNoteActivity.this, ""+audioFilePath, Toast.LENGTH_SHORT).show();
                    noteAudioFileLayout.setVisibility(View.VISIBLE);
                    audioFileTitle.setText(recordFile);
                }else {
                    bottomSheetDialog.dismiss();
                }

            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    File file = new File(getFilePath+"/"+recordFile);
                    file.delete();
                    bottomSheetDialog.dismiss();
                }
        });


    }
    private void resumedRecording() {
        mediaRecorder.resume();
        isPaused = false;
        isRecording = true;
    }
    private void pauseRecording() {
        mediaRecorder.pause();
        isPaused = true;
        isRecording = false;

    }

    String getFilePath;
    private void startRecording() {

        if (!checkAudioRecordPermission())
        {
            requestAudioRecordPermission();
            return;
        }
        //start recording
        getFilePath = this.getExternalFilesDir("/").getAbsolutePath();

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yy_MM_dd_hh_mm_ss", Locale.getDefault());
        Date date = new Date();
        recordFile =  simpleDateFormat.format(date)+".3gp";

        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mediaRecorder.setOutputFile(getFilePath+"/"+recordFile);
        mediaRecorder.setAudioSamplingRate(48000);
        mediaRecorder.setAudioEncodingBitRate(48000);

        audioFilePath = getFilePath+"/"+recordFile;
        try {
            mediaRecorder.prepare();
        }catch (Exception e){
            e.printStackTrace();
        }
        mediaRecorder.start();
        isPaused = false;
        isRecording = true;
        startDrawing();
    }

    private void startDrawing() {


    }

    private void stopRecording() {
        isRecording = false;
        isPaused = false;
        mediaRecorder.stop();
        mediaRecorder.release();
        mediaRecorder = null;
        bottomSheetDialog.dismiss();
        stopDrawing();
    }

    private void stopDrawing() {

    }


    //for alarm timePicker and notification via pending intent
    private void showTimePicker() {

        DialogFragment timePicker = new TimePickerFragment();
        timePicker.show(getSupportFragmentManager(),"time picker");


    }
    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, hourOfDay);
        c.set(Calendar.MINUTE, minute);
        c.set(Calendar.SECOND, 0);
        updateTimeText(c);
        startAlarm(c);

    }
    private void updateTimeText(Calendar c) {
        selectedAlarmTime = DateFormat.getTimeInstance(DateFormat.SHORT).format(c.getTime());
        alertTimerLayout.setVisibility(View.VISIBLE);
        alertTime.setText(selectedAlarmTime);

    }
    private void startAlarm(Calendar c) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlertReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this,1,intent,0);
        if (c.before(Calendar.getInstance())){
            c.add(Calendar.DATE, 1);

        }
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(),pendingIntent);
    }


    //checking all permission & request permission
    private boolean checkCameraPermission(){
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)==(PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)==(PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }
    private void requestCameraPermission(){
        ActivityCompat.requestPermissions(this,cameraPermission,CAMERA_REQUEST_CODE);
    }
    private boolean checkStoragePermission(){
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)==(PackageManager.PERMISSION_GRANTED);
        return result;
    }
    private void requestStoragePermission(){
        ActivityCompat.requestPermissions(this,storagePermission,STORAGE_REQUEST_CODE);
    }
    private boolean checkAudioRecordPermission() {

        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)==(PackageManager.PERMISSION_GRANTED);
        return result;
    }
    private void requestAudioRecordPermission(){
        ActivityCompat.requestPermissions(this,recordingPermission,RECORD_AUDIO_REQUEST_CODE);
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case CAMERA_REQUEST_CODE:{
                if (grantResults.length>0){
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && storageAccepted){
                        pickFromCamera();
                    }else {
                        Toast.makeText(this, "Camera permission Necessary..", Toast.LENGTH_SHORT).show();
                    }
                }

            }
            case STORAGE_REQUEST_CODE:{
                if (grantResults.length>0){
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (storageAccepted){
                        //permission granted
                        pickFromGallery();
                    }else {
                        //permission denied
                        Toast.makeText(this,"Storage permission necessary..",Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
            case RECORD_AUDIO_REQUEST_CODE:{
                if (grantResults.length>0){
                    boolean audioRecordAccepted= grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (audioRecordAccepted){

                        //permission granted

                        showBottomSheetVoiceDialog();


                    }else {
                        //permission denied
                        Toast.makeText(this,"Audio permission necessary..",Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;


        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_PICK_GALLERY_CODE){
            if (resultCode == RESULT_OK && data!=null){
                Uri selectedImage = data.getData();
                if (selectedImage != null){
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(selectedImage);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        noteImage.setImageBitmap(bitmap);
                        noteImage.setVisibility(View.VISIBLE);
                        deleteImage.setVisibility(View.VISIBLE);
                        selectedImagePath = getPathFromUri(selectedImage);
                    }catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
            else {
                selectedImagePath = null;
            }
        }
        if (requestCode == IMAGE_PICK_CAMERA_CODE) {
            if (resultCode == RESULT_OK && data!=null){
                Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                if (bitmap != null){
                    noteImage.setImageBitmap(bitmap);
                    noteImage.setVisibility(View.VISIBLE);
                    Uri uri = getImageUri(getApplicationContext(),bitmap);
                    selectedImagePath = getPathFromUri(uri);
                }
            }
            else {
                selectedImagePath = null;
            }

        }
        if (requestCode == REQUEST_CODE_SPEECH_TITLE_INPUT){
            if (resultCode == RESULT_OK && data!=null ){
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                noteTitle.setText(Objects.requireNonNull(result).get(0));
            }
        }
        if (requestCode == REQUEST_CODE_SPEECH_DES_INPUT){
            if (resultCode == RESULT_OK && data!=null ){
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                noteDescription.setText(Objects.requireNonNull(result).get(0));

            }
        }

    }

    private Uri getImageUri(Context applicationContext, Bitmap bitmap) {

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,byteArrayOutputStream);
        String path = MediaStore.Images.Media.insertImage(applicationContext.getContentResolver(),bitmap,"Title",null);
        return Uri.parse(path);
    }

    private String getPathFromUri (Uri contentUrl){
        String filePath;
        Cursor cursor = getContentResolver().query(contentUrl,null,null,null,null);
        if (cursor == null){
            filePath =  contentUrl.getPath();
        }else {
            cursor.moveToFirst();
            int index = cursor.getColumnIndex("_data");
            filePath = cursor.getString(index);
            cursor.close();
        }
        return filePath;
    }

}