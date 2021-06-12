package com.example.pencil.fragment;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pencil.activity.AddNoteActivity;
import com.example.pencil.R;
import com.example.pencil.adapter.NotesAdapter;
import com.example.pencil.database.NoteDatabase;
import com.example.pencil.entities.Note;
import com.example.pencil.listeners.NotesListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;


public class NotesFragment extends Fragment implements NotesListener {

    private static final int REQUEST_ADD_NOTE = 100;
    private static final int REQUEST_UPDATE_NOTE = 200;
    private static final int REQUEST_SHOW_NOTE = 300;

    private BottomNavigationView navigationView;
    private FloatingActionButton fab;

    private RecyclerView  recyclerView;
    private List<Note> noteList;
    private NotesAdapter notesAdapter;
    private TextView emptyDesign;

    private int moteClickedPosition = -1;


    public NotesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_notes, container, false);


    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);





        recyclerView = view.findViewById(R.id.noteRecyclerView);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL));
        noteList = new ArrayList<>();
        notesAdapter = new NotesAdapter(noteList,this);
        recyclerView.setAdapter(notesAdapter);

        getNotes(REQUEST_SHOW_NOTE,false);




        fab = view.findViewById(R.id.fabButton);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), AddNoteActivity.class);
                startActivityForResult(intent,REQUEST_ADD_NOTE);
            }
        });
    }

    private void getNotes(final int requestCode,final boolean isNoteDeleted){
        class  GetNoteTask extends AsyncTask<Void,Void, List<Note>> {

            @Override
            protected List<Note> doInBackground(Void... voids) {

                return NoteDatabase.getNoteDatabase(getContext())
                        .noteDao().getAllNote();
            }

            @Override
            protected void onPostExecute(List<Note> notes) {
                super.onPostExecute(notes);
                if (requestCode == REQUEST_SHOW_NOTE){
                    noteList.addAll(notes);
                    notesAdapter.notifyDataSetChanged();
                }else if (requestCode == REQUEST_ADD_NOTE){
                    noteList.add(0,notes.get(0));
                    notesAdapter.notifyItemInserted(0);
                    recyclerView.smoothScrollToPosition(0);
                }else  if (requestCode == REQUEST_UPDATE_NOTE){
                    noteList.remove(moteClickedPosition);

                    if (isNoteDeleted){
                        notesAdapter.notifyItemRemoved(moteClickedPosition);
                    }else {

                        noteList.add(moteClickedPosition,notes.get(moteClickedPosition));
                        notesAdapter.notifyItemChanged(moteClickedPosition);

                    }
                }
            }
        }
        new GetNoteTask().execute();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == AddNoteActivity.RESULT_OK ){
            if (requestCode == REQUEST_ADD_NOTE){
                getNotes(REQUEST_ADD_NOTE,false);
            }else if (requestCode== REQUEST_UPDATE_NOTE ){
                if (data!=null){
                    getNotes(REQUEST_UPDATE_NOTE,data.getBooleanExtra("isNoteDeleted",false));
                }
            }
        }



    }

    @Override
    public void onNoteClicked(Note note, int position) {

        moteClickedPosition = position;
        Intent intent = new Intent(getActivity(),AddNoteActivity.class);
        intent.putExtra("isViewOrUpdate",true);
        intent.putExtra("note",note);
        startActivityForResult(intent,REQUEST_UPDATE_NOTE);

    }
}