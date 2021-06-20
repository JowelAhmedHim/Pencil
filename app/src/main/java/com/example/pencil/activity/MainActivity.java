package com.example.pencil.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pencil.database.NoteDatabase;
import com.example.pencil.entities.Note;
import com.example.pencil.fragment.NotesFragment;
import com.example.pencil.R;
import com.example.pencil.fragment.ProtectedFragment;
import com.example.pencil.fragment.ReminderFragment;
import com.google.android.material.navigation.NavigationView;

import java.util.List;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle toggle;
    Fragment fragment = null;

    private AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //FIND VIEW
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navView);

        navigationView.setNavigationItemSelectedListener(this);

        getSupportFragmentManager().beginTransaction().replace(R.id.frame,new NotesFragment()).commit();


        //setup toggle to display hamburger icon with nice animation
        toggle = new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.open,R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        toggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.white));



        if (savedInstanceState == null){
            getSupportFragmentManager().beginTransaction().replace(R.id.frame,new NotesFragment()).commit();
            navigationView.setCheckedItem(R.id.notes);

        }

    }


    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        }else {
            super.onBackPressed();
        }

    }



    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {


        switch (item.getItemId())
        {
            case R.id.notes:
                fragment = new NotesFragment();
                loadFragment(fragment);
                break;
            case R.id.reminders:
                fragment = new ReminderFragment();
                loadFragment(fragment);
                break;
            case R.id.privateNote:
                showAlertDialog();
                break;
            case R.id.rateUs:
                rateFunction();
                break;
            case R.id.share:
                shareFunction();
                break;
            case R.id.setting:
                startActivity(new Intent(MainActivity.this,SettingActivity.class));
                drawerLayout.closeDrawer(GravityCompat.START);
                break;
            case R.id.About:
                startActivity(new Intent(MainActivity.this,About.class));
                drawerLayout.closeDrawer(GravityCompat.START);
                break;

        }
        return true;
    }

    private void rateFunction() {

        Uri uri = Uri.parse("http://play.google.com/store/apps/details?id="+getApplicationContext().getPackageName());
        Intent intent = new Intent(Intent.ACTION_VIEW,uri);
        try {
            startActivity(intent);
        }catch (Exception e){
            Toast.makeText(this, "Unable to open\n"+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void shareFunction() {
        try {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            String body = "Download This App";
            String sub = "http://play.google.com";
            intent.putExtra(Intent.EXTRA_SUBJECT,body);
            intent.putExtra(Intent.EXTRA_TEXT,sub);
            startActivity(Intent.createChooser(intent,"Share using"));
            drawerLayout.closeDrawer(GravityCompat.START);

        }catch (Exception e){
            Toast.makeText(this, "Unable yo share this app", Toast.LENGTH_SHORT).show();
        }
    }


    String real = "1234";

    private void showAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.note_password,(ViewGroup)findViewById(R.id.note_password_layout));
        EditText edPassword = view.findViewById(R.id.password_field);
        Button submit = view.findViewById(R.id.password_submit);
        builder.setView(view);
        alertDialog = builder.create();
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String password = edPassword.getText().toString();
                if (password.equals(real)){
                    fragment = new ProtectedFragment();
                    loadFragment(fragment);
                    alertDialog.dismiss();
                }else {
                    Toast.makeText(MainActivity.this, "Wrong Password", Toast.LENGTH_SHORT).show();
                }
            }
        });
        alertDialog.show();
        drawerLayout.closeDrawer(GravityCompat.START);
    }

    private void loadFragment(Fragment fragment) {

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame, fragment).commit();
        drawerLayout.closeDrawer(GravityCompat.START);
    }

}