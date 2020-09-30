package com.example.notesapplication.UIActivities;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.notesapplication.R;
import com.example.notesapplication.database.NotesDatabase;
import com.example.notesapplication.entities.Note;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CreateNoteActivity extends AppCompatActivity {

    private EditText noteTitle,noteSubtitle,noteText;
    private TextView dateTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_note);

        //region Note Properties
        noteTitle = findViewById(R.id.noteTitle);
        noteSubtitle = findViewById(R.id.noteSubtitle);
        noteText = findViewById(R.id.inputNote);
        dateTime = findViewById(R.id.dateTime);

        dateTime.setText(
                new SimpleDateFormat("EEEE, dd MMMM yyyy HH:MM a", Locale.getDefault())
                        .format(new Date()));
        //endregion

        //region Button Functionality
        //Go back to the dashboard
        ImageView backBtn = findViewById(R.id.back);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        //Save the data written within a note
        ImageView done = findViewById(R.id.done);
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveNote();
            }
        });
        //endregion
    }

    private void saveNote(){
        if(noteTitle.getText().toString().trim().isEmpty()){
            Toast.makeText(this,"Note title cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }else if(noteSubtitle.getText().toString().trim().isEmpty() &&
                noteText.getText().toString().trim().isEmpty()){
            Toast.makeText(this,"Note cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        final Note note = new Note();
        note.setTitle(noteTitle.getText().toString());
        note.setSubtitle(noteSubtitle.getText().toString());
        note.setNoteText(noteText.getText().toString());
        note.setDateTime(dateTime.getText().toString());

        //Using Async Task to save the note into database
        @SuppressLint("StaticFieldLeak")
        class SaveNoteTask extends AsyncTask<Void , Void, Void> {

            @Override
            protected Void doInBackground(Void... voids) {
                NotesDatabase.getDatabase(getApplicationContext()).noteDao().insertNote(note);
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
}