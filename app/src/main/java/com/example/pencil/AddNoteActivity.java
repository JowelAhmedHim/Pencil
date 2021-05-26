package com.example.pencil;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import android.Manifest;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
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

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorSelectedListener;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AddNoteActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener, View.OnClickListener,TimePickerDialog.OnTimeSetListener {

    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int STORAGE_REQUEST_CODE = 300;
    private static final int IMAGE_PICK_GALLERY_CODE = 400;
    private static final int IMAGE_PICK_CAMERA_CODE = 500;
    private static final int RECORD_AUDIO_REQUEST_CODE =600;



    private String[] cameraPermission;
    private String[] storagePermission;
    private String[] recordingPermission;


    private RelativeLayout relativeLayout;
    private ImageView noteImage,notePaint;
    private ImageButton noteBackBtn,noteSaveBtn,noteAlertBtn;
    private TextView noteTime,alertTime;
    private EditText noteTitle,noteDescription;
    

    private BottomNavigationView navigationView;
    private BottomSheetDialog bottomSheetDialog;

    
    private Spinner spinner;
    private ProgressDialog progressDialog;


  

    private Uri imageUri;
    private String selectedImagePath;

    private MediaRecorder mediaRecorder;
    private boolean isRecording = false;

    private int colorBackground;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);
        
        //initially add note background color white
        colorBackground = Color.WHITE;
        

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
        noteTime.setOnClickListener(this);
        
        
        //getPaintImageFromIntent
        getPaintImage();
      




        selectedImagePath = "";


        //bottom navigation view setup
        navigationView = findViewById(R.id.bottomNavigation);
        navigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId())
                {
                    case R.id.addImage:
                        showImagePickerDialog();
                        return true;
                    case R.id.addPaint:
                        Intent intent = new Intent(AddNoteActivity.this,PaintActivity.class);
                        startActivity(intent);
                        return true;

                    case R.id.addVoice:
                        showBottomSheetDialog();
                        return true;
                    case R.id.addColor:
                        showBackgroundColorPicker();
                        return true;
                }
                return false;
            }
        });

    }

    private void getPaintImage() {
        if (getIntent().hasExtra("image")){
            Bitmap b = BitmapFactory.decodeByteArray(
                    getIntent().getByteArrayExtra("image"),0,getIntent()
                            .getByteArrayExtra("image").length);
            Toast.makeText(this, "Successfully get intent", Toast.LENGTH_SHORT).show();
            notePaint.setImageBitmap(b);
        }
    }

    private void init() {
        relativeLayout = findViewById(R.id.addNoteBackground);
        noteBackBtn = findViewById(R.id.noteBack_btn);
        noteAlertBtn = findViewById(R.id.noteAlert_btn);
        noteSaveBtn = findViewById(R.id.noteSave_btn);
        alertTime = findViewById(R.id.alertTimer);
        noteTime = findViewById(R.id.noteTime);
        noteTitle = findViewById(R.id.noteTitle);
        noteDescription = findViewById(R.id.noteDescription);
        noteImage = findViewById(R.id.noteImage);
        notePaint = findViewById(R.id.notePaint);
        spinner = findViewById(R.id.spinner);
        List<String> categories = new ArrayList<String>();
        categories.add("Home");
        categories.add("Work");
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,categories);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(arrayAdapter);

    }
    
    private void initPermission() {
        cameraPermission = new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        recordingPermission = new String[]{Manifest.permission.RECORD_AUDIO};
    }

    private void showBackgroundColorPicker(){

        int color = colorBackground;
        ColorPickerDialogBuilder
                .with(this)
                .setTitle("Choose color")
                .initialColor(color)
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .density(12)
                .setOnColorSelectedListener(new OnColorSelectedListener() {
                    @Override
                    public void onColorSelected(int selectedColor) {
                        Toast.makeText(AddNoteActivity.this, "onColorSelected: 0x"+ Integer.toHexString(selectedColor), Toast.LENGTH_SHORT).show();
                    }
                })
                .setPositiveButton("ok", new ColorPickerClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {

                            colorBackground = selectedColor;
                            relativeLayout.setBackgroundColor(colorBackground);

                    }
                })
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .build()
                .show();

    }

    //function of image taking note
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

    //function of voice recording note
    private void showBottomSheetDialog() {

        bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(R.layout.add_note_bottom_sheet_dialog);
        bottomSheetDialog.setCancelable(false);

        ImageButton playBtn,cancelBtn,okBtn,pauseBtn;
        Chronometer timer;
        timer = bottomSheetDialog.findViewById(R.id.chronometers);
        playBtn=bottomSheetDialog.findViewById(R.id.play_btn);
        pauseBtn = bottomSheetDialog.findViewById(R.id.pause_btn);
        cancelBtn = bottomSheetDialog.findViewById(R.id.cancel_btn);
        okBtn = bottomSheetDialog.findViewById(R.id.ok_btn);
        bottomSheetDialog.show();

        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    if (checkAudioRecordPermission())
                    {
                        startRecording();
                        isRecording = true;
                        timer.setBase(SystemClock.elapsedRealtime());
                        timer.start();
                    }else {
                        requestAudioRecordPermission();
                    }

            }
        });

        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timer.stop();
                isRecording = false;
                stopRecording();
            }
        });
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isRecording){
                    stopRecording();

                }else {
                    bottomSheetDialog.dismiss();
                }
            }
        });

    }

    private void startRecording() {

        String getFilePath = this.getExternalFilesDir("/").getAbsolutePath();

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yy_MM_dd_hh_mm_ss", Locale.getDefault());
        Date date = new Date();
        String recordFile =  "Recording_"+simpleDateFormat.format(date)+".3gp";

        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mediaRecorder.setOutputFile(getFilePath+"/"+recordFile);

        try {
            mediaRecorder.prepare();
        }catch (Exception e){
            e.printStackTrace();
        }
        mediaRecorder.start();
    }

    private void stopRecording() {

        mediaRecorder.stop();
        mediaRecorder.release();
        mediaRecorder = null;
        bottomSheetDialog.dismiss();
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
        boolean result1 = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)==(PackageManager.PERMISSION_GRANTED);
        return result1;
    }
    private void requestStoragePermission(){
        ActivityCompat.requestPermissions(this,storagePermission,CAMERA_REQUEST_CODE);
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
        if (resultCode == RESULT_OK){
            if (requestCode == IMAGE_PICK_GALLERY_CODE){

               if (data != null){
                   Uri selectedImage = data.getData();
                   if (selectedImage != null){
                       try {
                           InputStream inputStream = getContentResolver().openInputStream(selectedImage);
                           Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                           noteImage.setImageBitmap(bitmap);
                           selectedImagePath =getPathFromUri(selectedImage);

                       } catch (FileNotFoundException e) {
                           e.printStackTrace();
                       }
                   }
               }

            }else if (requestCode== IMAGE_PICK_CAMERA_CODE){
                Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                if (bitmap != null){
                    noteImage.setImageBitmap(bitmap);
                }

            }
        }
        super.onActivityResult(requestCode, resultCode, data);
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


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String item = parent.getItemAtPosition(position).toString();
        // Showing selected spinner item
        Toast.makeText(parent.getContext(), "Selected: " + item, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.noteBack_btn:
                onBackPressed();
                break; 
            case R.id.noteAlert_btn:
                showTimePicker();
                break;
        }
        
    }

    private void showTimePicker() {


        DialogFragment timePicker = new TimePickerFragment();
        timePicker.show(getSupportFragmentManager(),"time picker");


      /*  Calendar currentTime = Calendar.getInstance();
        int hour = currentTime.get(Calendar.HOUR_OF_DAY);
        int minute = currentTime.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(AddNoteActivity.this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                alertTime.setText(hourOfDay+":"+ minute);
            }
        },hour,minute,true);
        timePickerDialog.show();*/
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
        String timeSet = DateFormat.getTimeInstance(DateFormat.SHORT).format(c.getTime());
        alertTime.setText(timeSet);
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
}