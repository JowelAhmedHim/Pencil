package com.example.pencil.fragment;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.pencil.R;
import com.example.pencil.activity.AddNoteActivity;
import com.example.pencil.adapter.NotesAdapter;
import com.example.pencil.adapter.ProtectedNoteAdapter;
import com.example.pencil.database.NoteDatabase;
import com.example.pencil.entities.Note;
import com.example.pencil.listeners.NotesListener;

import java.util.ArrayList;
import java.util.List;


public class ProtectedFragment extends Fragment implements NotesListener {

    private static final int REQUEST_ADD_NOTE = 100;
    private static final int REQUEST_UPDATE_NOTE = 200;
    private static final int REQUEST_SHOW_NOTE = 300;


    private RecyclerView recyclerView;
    private List<Note> noteList;
    private ProtectedNoteAdapter notesProtectedAdapter;
    private ImageView emptyDesign;
    private EditText searchNote;

    private int moteClickedPosition = -1;


    public ProtectedFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_protected, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getActivity().setTitle("Protected Notes");

        emptyDesign = view.findViewById(R.id.empty_reminder_state);

        recyclerView = view.findViewById(R.id.noteRecyclerView);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL));
        noteList = new ArrayList<>();

        notesProtectedAdapter = new ProtectedNoteAdapter(getContext(),noteList,this);
        recyclerView.setAdapter(notesProtectedAdapter);

        getNotes(REQUEST_SHOW_NOTE,false);


        searchNote = view.findViewById(R.id.searchNote);
        searchNote.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                notesProtectedAdapter.cancelTimer();
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (noteList.size()!=0){
                    notesProtectedAdapter.searchNotes(s.toString());
                }
            }
        });
    }
    private void getNotes(final int requestCode,final boolean isNoteDeleted){
        class  GetNoteTask extends AsyncTask<Void,Void, List<Note>> {

            @Override
            protected List<Note> doInBackground(Void... voids) {

                return NoteDatabase.getNoteDatabase(getContext())
                        .noteDao().getProtectedNote();
            }

            @Override
            protected void onPostExecute(List<Note> notes) {
                super.onPostExecute(notes);

                if (requestCode == REQUEST_SHOW_NOTE){
                    if (notes.isEmpty()){
                        emptyDesign.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    }
                    noteList.addAll(notes);
                    notesProtectedAdapter.notifyDataSetChanged();
                }
                else  if (requestCode == REQUEST_UPDATE_NOTE){

                    noteList.remove(moteClickedPosition);

                    if (isNoteDeleted){
                        //as note empty show empty state
                        if (notes.isEmpty()){
                            emptyDesign.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.GONE);
                        }
                        notesProtectedAdapter.notifyItemRemoved(moteClickedPosition);
                    }else {
                        if (notes.isEmpty()){
                            emptyDesign.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.GONE);
                        }
                        noteList.add(moteClickedPosition,notes.get(moteClickedPosition));
                        notesProtectedAdapter.notifyItemChanged(moteClickedPosition);

                    }
                }
            }
        }
        new GetNoteTask().execute();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == AddNoteActivity.RESULT_OK) {
            if (requestCode == REQUEST_ADD_NOTE) {
                getNotes(REQUEST_ADD_NOTE, false);
            } else if (requestCode == REQUEST_UPDATE_NOTE) {
                if (data != null) {
                    getNotes(REQUEST_UPDATE_NOTE, data.getBooleanExtra("isNoteDeleted", false));
                }
            }
        }
    }

    @Override
    public void onNoteClicked(Note note, int position) {
        moteClickedPosition = position;
        Intent intent = new Intent(getContext(), AddNoteActivity.class);
        intent.putExtra("isViewOrUpdate",true);
        intent.putExtra("note",note);
        startActivityForResult(intent,REQUEST_UPDATE_NOTE);
    }


}