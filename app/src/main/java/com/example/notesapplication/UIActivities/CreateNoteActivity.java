package com.example.notesapplication.UIActivities;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.notesapplication.R;
import com.example.notesapplication.database.NotesDatabase;
import com.example.notesapplication.entities.Note;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CreateNoteActivity extends AppCompatActivity {

    private EditText noteTitle,noteSubtitle,noteText;
    private TextView dateTime;
    private View subtitleIndicator;
    private ImageView[] colorStatusImageView;

    private String selectedColor;
    private String[] availableColors = new String[]{"#333333","#FDBE3B","#FF4842","#3A52Fc","#FFFFFF"};

    public CreateNoteActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_note);

        //region Note Properties
        colorStatusImageView = new ImageView[5];
        selectedColor = availableColors[0];
        noteTitle = findViewById(R.id.noteTitle);
        noteSubtitle = findViewById(R.id.noteSubtitle);
        noteText = findViewById(R.id.inputNote);
        dateTime = findViewById(R.id.dateTime);
        subtitleIndicator = findViewById(R.id.viewSubtitleIndicator);

        dateTime.setText(
                new SimpleDateFormat("EEEE, dd MMMM yyyy HH:MM a", Locale.getDefault())
                        .format(new Date()));

        initOptions();
        setSubtitleIndicatorColor();//Default note color
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
        note.setColor(selectedColor);

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

    void initOptions(){
        final LinearLayout optionsLayout = findViewById(R.id.layoutOptions);
        final BottomSheetBehavior<LinearLayout> bottomSheetBehavior = BottomSheetBehavior.from(optionsLayout);

        optionsLayout.findViewById(R.id.optionsHeader).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int state = bottomSheetBehavior.getState();
                if(state != BottomSheetBehavior.STATE_EXPANDED){
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
                else {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }
        });

        colorStatusImageView[0] = optionsLayout.findViewById(R.id.imageNoteColor1);
        colorStatusImageView[1] = optionsLayout.findViewById(R.id.imageNoteColor2);
        colorStatusImageView[2] = optionsLayout.findViewById(R.id.imageNoteColor3);
        colorStatusImageView[3] = optionsLayout.findViewById(R.id.imageNoteColor4);
        colorStatusImageView[4] = optionsLayout.findViewById(R.id.imageNoteColor5);

        optionsLayout.findViewById(R.id.noteColor1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setColorChosen(0);
            }
        });
        optionsLayout.findViewById(R.id.noteColor2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setColorChosen(1);
            }
        });
        optionsLayout.findViewById(R.id.noteColor3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setColorChosen(2);
            }
        });
        optionsLayout.findViewById(R.id.noteColor4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setColorChosen(3);
            }
        });
        optionsLayout.findViewById(R.id.noteColor5).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setColorChosen(4);
            }
        });
    }

    void setColorChosen(int index){
        selectedColor = availableColors[index];
        for(int i = 0;i<colorStatusImageView.length;i++){
            if(i == index) colorStatusImageView[i].setImageResource(R.drawable.ic_done);
            else colorStatusImageView[i].setImageResource(0);
        }
        setSubtitleIndicatorColor();
    }

    void setSubtitleIndicatorColor(){
        GradientDrawable gradientDrawable = (GradientDrawable) subtitleIndicator.getBackground();
        gradientDrawable.setColor(Color.parseColor(selectedColor));
    }
}