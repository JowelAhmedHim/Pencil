package com.example.pencil;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AddNoteActivity extends AppCompatActivity {


    private RelativeLayout relativeLayout;
    private ImageView noteImageIv;

    private Toolbar toolbar;
    private BottomNavigationView navigationView;
    private BottomSheetDialog bottomSheetDialog;

    private ProgressDialog progressDialog;

    private String[] cameraPermission;
    private String[] storagePermission;
    private String[] recordingPermission;

    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int STORAGE_REQUEST_CODE = 300;
    private static final int IMAGE_PICK_GALLERY_CODE = 400;
    private static final int IMAGE_PICK_CAMERA_CODE = 500;
    private static final int RECORD_AUDIO_REQUEST_CODE =600;

    private Uri imageUri;
    private String selectedImagePath;

    private MediaRecorder mediaRecorder;
    private boolean isRecording = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);

        //toolbar setup
        toolbar  = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Add Note");

        //progress dialog setup
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        init();
        initPermission();

        if (getIntent().hasExtra("image")){
            Bitmap b = BitmapFactory.decodeByteArray(
                    getIntent().getByteArrayExtra("image"),0,getIntent()
                            .getByteArrayExtra("image").length);
            Toast.makeText(this, "Successfully get intent", Toast.LENGTH_SHORT).show();
            noteImageIv.setImageBitmap(b);
        }




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
                }
                return false;
            }
        });

    }
    private void init() {
        relativeLayout = findViewById(R.id.addNoteBackground);
        noteImageIv = findViewById(R.id.noteImage);
    }


    private void initPermission() {
        cameraPermission = new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        recordingPermission = new String[]{Manifest.permission.RECORD_AUDIO};
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
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.addnote_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
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
                           noteImageIv.setImageBitmap(bitmap);
                           selectedImagePath =getPathFromUri(selectedImage);

                       } catch (FileNotFoundException e) {
                           e.printStackTrace();
                       }
                   }
               }

            }else if (requestCode== IMAGE_PICK_CAMERA_CODE){
                Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                if (bitmap != null){
                    noteImageIv.setImageBitmap(bitmap);
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




}