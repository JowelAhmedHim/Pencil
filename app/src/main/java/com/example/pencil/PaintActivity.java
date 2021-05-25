package com.example.pencil;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSeekBar;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Path;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorSelectedListener;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.ByteArrayOutputStream;

public class PaintActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    private static final int REQUEST_PERMISSION = 100;
    private boolean isEraser;
    private boolean isBackground;

    public PaintView paintView;
    int colorBackground,colorBrush;
    int brushSize,eraserSize;

    private BottomNavigationView bottomNavigationView;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paint);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initDraw();
        bottomNavigationView = findViewById(R.id.paintBottomNavigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);

    }
    private void initDraw() {

        colorBackground =Color.WHITE;
        colorBrush = Color.BLACK;
        eraserSize = brushSize = 12;
        paintView = findViewById(R.id.paintView);

    }
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.brushSize:
                paintView.disableEraser();
                showDialogBrushSize(false);
                return true;
            case R.id.eraserSize:
                paintView.enableEraser();
                showDialogBrushSize(true);
                return true;
            case R.id.brushColor:
                updateColor(false);
                return true;
            case R.id.backgroundColor:
                updateColor(true);
                return true;
        }
        return false;
    }

    private void showDialogBrushSize(boolean isEraser){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.layout_brush_size_dialog,null,false);
        TextView sizeValue= view.findViewById(R.id.sizeValue);
        TextView name = view.findViewById(R.id.brushSize);
        SeekBar seekBar = view.findViewById(R.id.seekBar);
        seekBar.setMax(99);

        if (isEraser){
            name.setText("Eraser");
            sizeValue.setText("Size: "+eraserSize);
        }else {
            name.setText("Brush");
            sizeValue.setText("Size: "+brushSize);
        }

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (isEraser){
                    eraserSize = progress+1;
                    sizeValue.setText("Size: "+eraserSize);
                    paintView.setSizeEraser(eraserSize);

                }else {
                    brushSize= progress+1;
                    sizeValue.setText("Size: "+brushSize);
                    paintView.setSizeBrush(brushSize);

                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setView(view);
        builder.show();

    }
    private void updateColor(final boolean isBackground){
        int color;
        if (isBackground){
            color = colorBackground;
        }else {
            color = colorBrush;
        }

        ColorPickerDialogBuilder
                .with(this)
                .setTitle("Choose color")
                .initialColor(color)
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .density(12)
                .setOnColorSelectedListener(new OnColorSelectedListener() {
                    @Override
                    public void onColorSelected(int selectedColor) {
                        Toast.makeText(PaintActivity.this, "onColorSelected: 0x"+ Integer.toHexString(selectedColor), Toast.LENGTH_SHORT).show();
                    }
                })
                .setPositiveButton("ok", new ColorPickerClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {
                        if (isBackground){
                            colorBackground = selectedColor;
                            paintView.setColourBackground(colorBackground);
                        }else {
                            colorBrush = selectedColor;
                            paintView.setBrushColour(colorBrush);
                        }
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





    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.paint_top_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.undoAction:
                paintView.returnLastAction();
                break;
            case R.id.savePaintFile:
                saveFile();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    public void saveFile(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED)
        {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},REQUEST_PERMISSION);
        }else {
            saveBitmap();
        }
    }

    public void saveBitmap(){

        Bitmap bitmap = paintView.getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100,stream);
        byte[] byteArray = stream.toByteArray();

        Intent intent = new Intent(this,AddNoteActivity.class);
        intent.putExtra("image",byteArray);
        startActivity(intent);
        finish();

    }
}