package com.example.notesapplication.UIActivities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.notesapplication.R;
import com.example.notesapplication.database.NotesDatabase;
import com.example.notesapplication.entities.Note;
import com.example.notesapplication.utilities.Utils;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class CreateNoteActivity extends AppCompatActivity {

    private EditText noteTitle,noteSubtitle,noteText;
    private TextView dateTime;
    private View subtitleIndicator;
    private ImageView[] colorStatusImageView;
    private ImageView imageNote;
    private TextView textURL;
    private LinearLayout layoutURL;


    private String selectedColor;
    private final String[] availableColors = new String[]{"#324851","#375E97","#FB6542","#DB9501","#3F681C"};
    private String selectedImagePath;

    private static final int REQUEST_STORAGE_PERMISSION_CODE = 1;
    private static final int REQUEST_SELECT_IMAGE_CODE = 2;

    private AlertDialog dialogURL,dialogDeleteNote;

    private Note alreadyAvailableNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_note);

        //region Note Properties
        colorStatusImageView = new ImageView[5];
        noteTitle = findViewById(R.id.noteTitle);
        noteSubtitle = findViewById(R.id.noteSubtitle);
        noteText = findViewById(R.id.inputNote);
        dateTime = findViewById(R.id.dateTime);
        subtitleIndicator = findViewById(R.id.viewSubtitleIndicator);
        imageNote = findViewById(R.id.imageNote);
        textURL = findViewById(R.id.textURL);
        layoutURL = findViewById(R.id.layoutURL);

        selectedColor = availableColors[0];
        selectedImagePath = "";

        //Get Current Time
        dateTime.setText(Utils.getCurrentDateTime());

        Intent intent = getIntent();
        if(intent.getBooleanExtra("isViewOrUpdate", false)){
            alreadyAvailableNote = (Note) intent.getSerializableExtra("note");
            setViewOrUpdateNote();
        }

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

        findViewById(R.id.removeWebURL).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textURL.setText(null);
                layoutURL.setVisibility(View.GONE);
            }
        });

        findViewById(R.id.removeImage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageNote.setImageBitmap(null);
                imageNote.setVisibility(View.GONE);
                findViewById(R.id.removeImage).setVisibility(View.GONE);
                selectedImagePath = "";
            }
        });
        //endregion

        if(getIntent().getBooleanExtra("isFromQuickActions",false)){
            String type = getIntent().getStringExtra("quickActionType");
            if(type!=null){
                if(type.equals("image")){
                    selectedImagePath = getIntent().getStringExtra("imagePath");
                    imageNote.setImageBitmap(BitmapFactory.decodeFile(selectedImagePath));
                    imageNote.setVisibility(View.VISIBLE);
                    findViewById(R.id.removeImage).setVisibility(View.VISIBLE);
                }
                else if(type.equals("URL")){
                    textURL.setText(getIntent().getStringExtra("URL"));
                    layoutURL.setVisibility(View.VISIBLE);
                }
            }
        }

        initOptions();
        setSubtitleIndicatorColor();//Default note color
    }

    void setViewOrUpdateNote(){
       noteTitle.setText(alreadyAvailableNote.getTitle());
       noteSubtitle.setText(alreadyAvailableNote.getSubtitle());
       noteText.setText(alreadyAvailableNote.getNoteText());
       dateTime.setText(alreadyAvailableNote.getDateTime());

       if(alreadyAvailableNote.getImagePath() != null && !alreadyAvailableNote.getImagePath().trim().isEmpty()){
           selectedImagePath = alreadyAvailableNote.getImagePath();
           imageNote.setImageBitmap(BitmapFactory.decodeFile(selectedImagePath));
           imageNote.setVisibility(View.VISIBLE);
           findViewById(R.id.removeImage).setVisibility(View.VISIBLE);
       }

       if(alreadyAvailableNote.getWebLink() != null && !alreadyAvailableNote.getWebLink().trim().isEmpty()){
           textURL.setText(alreadyAvailableNote.getWebLink());
           layoutURL.setVisibility(View.VISIBLE);
       }
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
        note.setDateTime(Utils.getCurrentDateTime());
        note.setColor(selectedColor);
        note.setImagePath(selectedImagePath);

        if(layoutURL.getVisibility() == View.VISIBLE){
            note.setWebLink(textURL.getText().toString().trim());
        }

        if(alreadyAvailableNote != null){
            note.setId(alreadyAvailableNote.getId());
        }

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

        if(alreadyAvailableNote != null && alreadyAvailableNote.getColor()!=null && !alreadyAvailableNote.getColor().trim().isEmpty()){
            String color = alreadyAvailableNote.getColor();
            int index = Arrays.asList(availableColors).indexOf(color);
            setColorChosen(index);
        }

        optionsLayout.findViewById(R.id.optionsAddImage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                if((ContextCompat.checkSelfPermission( getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE))
                        != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(
                            CreateNoteActivity.this,
                            new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},
                            REQUEST_STORAGE_PERMISSION_CODE
                    );
                }
                else {
                    selectImage();
                }
            }
        });

        optionsLayout.findViewById(R.id.optionsAddURL).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                showAddURLDialogue();
            }
        });

        if(alreadyAvailableNote!=null){
            optionsLayout.findViewById(R.id.deleteNote).setVisibility(View.VISIBLE);
            optionsLayout.findViewById(R.id.deleteNote).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    showDeleteNoteDialog();
                }
            });
        }
    }

    void setColorChosen(int index){
        if(index < 0) index = 0;
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

    void selectImage(){
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if((intent.resolveActivity(getPackageManager()) != null)){
            startActivityForResult(intent,REQUEST_SELECT_IMAGE_CODE);
        }
    }

    String getPathFromUri(Uri contentUri){
        String filePath;
        Cursor cursor = getContentResolver().query(contentUri,null,null,null,null);

        if(cursor == null){
            filePath = contentUri.getPath();
        }
        else {
            cursor.moveToFirst();
            int index = cursor.getColumnIndex("_data");
            filePath = cursor.getString(index);
            cursor.close();
        }
        return filePath;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_STORAGE_PERMISSION_CODE && grantResults.length > 0){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                selectImage();
            }
            else {
                Toast.makeText(this,"Permission denied!",Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_SELECT_IMAGE_CODE && resultCode == RESULT_OK){
            if(data!=null){
                Uri selectedImageUri = data.getData();
                if(selectedImageUri != null){
                    try {
                        InputStream is = getContentResolver().openInputStream(selectedImageUri);
                        Bitmap bmp = BitmapFactory.decodeStream(is);
                        imageNote.setImageBitmap(bmp);
                        imageNote.setVisibility(View.VISIBLE);
                        findViewById(R.id.removeImage).setVisibility(View.VISIBLE);

                        selectedImagePath = getPathFromUri(selectedImageUri);
                    }
                    catch (Exception e){
                        Toast.makeText(this,e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    void showAddURLDialogue(){
        if(dialogURL == null){
            AlertDialog.Builder builder = new AlertDialog.Builder(CreateNoteActivity.this);
            View view = LayoutInflater.from(this).inflate(
                    R.layout.layout_add_url,
                    (ViewGroup) findViewById(R.id.layoutAddUrl_parent)
            );
            builder.setView(view);

            dialogURL = builder.create();

            if(dialogURL.getWindow() !=null){
                dialogURL.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }

            final EditText inputURL = view.findViewById(R.id.inputURL);
            inputURL.requestFocus();

            view.findViewById(R.id.addURLBtn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String str = inputURL.getText().toString().trim();
                    if(str.isEmpty()){
                        Toast.makeText(CreateNoteActivity.this,"Enter URL",Toast.LENGTH_SHORT).show();
                    }
                    else if(!Patterns.WEB_URL.matcher(str).matches()){
                        Toast.makeText(CreateNoteActivity.this,"Enter valid URL",Toast.LENGTH_SHORT).show();
                    }
                    else {
                        textURL.setText(str);
                        layoutURL.setVisibility(View.VISIBLE);
                        dialogURL.dismiss();
                    }
                }
            });

            view.findViewById(R.id.cancelURLBtn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                   dialogURL.dismiss();
                }
            });
        }
        dialogURL.show();
    }

    void showDeleteNoteDialog(){
        if(dialogDeleteNote == null){
            AlertDialog.Builder builder = new AlertDialog.Builder(CreateNoteActivity.this);
            View view = LayoutInflater.from(this).inflate(
                    R.layout.delete_note,
                    (ViewGroup) findViewById(R.id.deleteNoteContainerLayout)
            );
            builder.setView(view);

            dialogDeleteNote = builder.create();

            if(dialogDeleteNote.getWindow() !=null){
                dialogDeleteNote.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }


            view.findViewById(R.id.deleteNoteYes).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    @SuppressLint("StaticFieldLeak")
                    class DeleteNoteTask extends AsyncTask<Void,Void,Void>{

                        @Override
                        protected Void doInBackground(Void... voids) {
                            NotesDatabase.getDatabase(getApplicationContext()).noteDao()
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
                }
            });

            view.findViewById(R.id.deleteNoteCancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialogDeleteNote.dismiss();
                }
            });
        }
        dialogDeleteNote.show();
    }
}