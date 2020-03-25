package com.mgbachi_ugo.notekeeper;

import android.annotation.SuppressLint;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;

import static com.mgbachi_ugo.notekeeper.NoteKeeperDatabaseContract.*;

public class MainActivity extends AppCompatActivity {
    public final static String NOTE_ID = "com.mgbachi_ugo.notekeeper.NOTE_ID";
    public static final String ORIGINAL_NOTE_COURSE_ID = "com.mgbachi_ugo.notekeeper.ORIGINAL_NOTE_COURSE_ID";
    public static final String ORIGINAL_NOTE_TITLE = "com.mgbachi_ugo.notekeeper.ORIGINAL_NOTE_TITLE";
    public static final String ORIGINAL_NOTE_TEXT = "com.mgbachi_ugo.notekeeper.ORIGINAL_NOTE_TEXT";

    public static final int ID_NOT_SET = -1;
    public static final int LOADER_NOTES = 0;
    private NoteInfo mNote;
    private int mNotePosition;
    private boolean mIsCancelling;
    private String mOriginalCourseId;
    private String mOriginalNoteTitle;
    private String mOriginalNoteText;
    private boolean mIsNewNote;
    private Spinner mSpinnerCourses;
    private EditText mTextNoteTitle;
    private EditText mTextNoteText;
    private NoteKeeperOpenHelper mDbOpenHelper;
    private Cursor mNoteCursor;
    private int mCourseIdPos;
    private int mNoteTitle;
    private int mNoteText;
    private int mNoteId;
    private SimpleCursorAdapter mAdapterCourses;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mDbOpenHelper = new NoteKeeperOpenHelper(this);

        mSpinnerCourses = findViewById(R.id.spinner_courses);


        mAdapterCourses = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, null,
               new String[] {CourseInfoEntry.COLUMN_COURSE_TITLE},
                new int[] {android.R.id.text1}, 0);
        mAdapterCourses.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerCourses.setAdapter(mAdapterCourses);
        loadCourseData();
        readDisplayValues();
        if (savedInstanceState == null) {
            saveOriginalNoteValues();
        } else {
            restoreOriginalNoteValues(savedInstanceState);
        }
        mTextNoteTitle = findViewById(R.id.text_note_title);
        mTextNoteText = findViewById(R.id.text_note_text);
        if (!mIsNewNote)

            loadNoteData();
    }

    private void loadCourseData() {
        SQLiteDatabase db = mDbOpenHelper.getReadableDatabase();
        String[] courseColumns = {
                CourseInfoEntry.COLUMN_COURSE_TITLE,
                CourseInfoEntry.COLUMN_COURSE_ID,
                CourseInfoEntry._ID
        };
        Cursor cursor = db.query(CourseInfoEntry.TABLE_NAME, courseColumns, null,
                null, null, null, CourseInfoEntry.COLUMN_COURSE_TITLE);
        mAdapterCourses.changeCursor(cursor);
    }

    @Override
    protected void onDestroy() {
        mDbOpenHelper.close();
        super.onDestroy();
    }

    private void loadNoteData() {
        SQLiteDatabase db = mDbOpenHelper.getReadableDatabase();
        String courseId = "android_intents";
        String titleStart = "dynamic";

        String selection = NoteInfoEntry._ID + " = ?";

        String[] selectionArgs = {Integer.toString(mNoteId)};

        String[] noteColumns = {
                NoteInfoEntry.COLUMN_COURSE_ID,
                NoteInfoEntry.COLUMN_NOTE_TITLE,
                NoteInfoEntry.COLUMN_NOTE_TEXT
        };
        mNoteCursor = db.query(NoteInfoEntry.TABLE_NAME, noteColumns,
                selection, selectionArgs, null, null, null);
        mCourseIdPos = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_COURSE_ID);
        mNoteTitle = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TITLE);
        mNoteText = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TEXT);
        mNoteCursor.moveToNext();
        displayNote();

    }

    private void restoreOriginalNoteValues(Bundle savedInstanceState) {
        mOriginalCourseId = savedInstanceState.getString(ORIGINAL_NOTE_COURSE_ID);
        mOriginalNoteTitle = savedInstanceState.getString(ORIGINAL_NOTE_TITLE);
        mOriginalNoteText = savedInstanceState.getString(ORIGINAL_NOTE_TEXT);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mIsCancelling) {
            if (mIsNewNote) {
              deleteNoteFromDatabase();
            } else {
                storePreviousValues();
            }
        } else {
            saveNote();
        }
    }

    private void deleteNoteFromDatabase() {
        final String selection = NoteInfoEntry._ID + " = ?";
        final String[] selectionArgs = {Integer.toString(mNoteId)};

        AsyncTask task = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                SQLiteDatabase db = mDbOpenHelper.getWritableDatabase();
                db.delete(NoteInfoEntry.TABLE_NAME, selection, selectionArgs);
                return null;
            }
        };
        task.execute();
    }

    private void storePreviousValues() {
        CourseInfo course = DataManager.getInstance().getCourse(mOriginalCourseId);
        mNote.setCourse(course);
        mNote.setTitle(mOriginalNoteTitle);
        mNote.setText(mOriginalNoteText);

    }

    private void saveNote() {
        String courseId = selectedCourseId();
       String noteTitle = mTextNoteTitle.getText().toString();
       String noteText = mTextNoteText.getText().toString();
       saveNoteToDatabase(courseId, noteTitle, noteText);
    }

    private String selectedCourseId() {
        int selectedPosition = mSpinnerCourses.getSelectedItemPosition();
        Cursor cursor = mAdapterCourses.getCursor();
        cursor.moveToPosition(selectedPosition);
        int courseIdPos = cursor.getColumnIndex(CourseInfoEntry.COLUMN_COURSE_ID);
        String courseId = cursor.getString(courseIdPos);
        return courseId;
    }

    private void saveNoteToDatabase(String courseId, String noteTitle, String noteText){
        String selection = NoteInfoEntry._ID + " = ?";
        String [] selectionArgs = {Integer.toString(mNoteId)};

        ContentValues values = new ContentValues();
        values.put(NoteInfoEntry.COLUMN_COURSE_ID, courseId);
        values.put(NoteInfoEntry.COLUMN_NOTE_TITLE, noteTitle);
        values.put(NoteInfoEntry.COLUMN_NOTE_TEXT, noteText);

        SQLiteDatabase db = mDbOpenHelper.getWritableDatabase();
        db.update(NoteInfoEntry.TABLE_NAME, values, selection, selectionArgs);

    }


    private void saveOriginalNoteValues() {
        if (mIsNewNote)
            return;
        mOriginalCourseId = mNote.getCourse().getCourseId();
        mOriginalNoteTitle = mNote.getTitle();
        mOriginalNoteText = mNote.getText();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(ORIGINAL_NOTE_COURSE_ID, mOriginalCourseId);
        outState.putString(ORIGINAL_NOTE_TITLE, mOriginalNoteTitle);
        outState.putString(ORIGINAL_NOTE_TEXT, mOriginalNoteText);
    }

    private void displayNote() {
        String courseId = mNoteCursor.getString(mCourseIdPos);
        String noteTitle = mNoteCursor.getString(mNoteTitle);
        String noteText = mNoteCursor.getString(mNoteText);


        int courseIndex = getIndexOfCourseId(courseId);
        mSpinnerCourses.setSelection(courseIndex);
        mTextNoteText.setText(noteText);
        mTextNoteTitle.setText(noteTitle);
    }

    private int getIndexOfCourseId(String courseId) {
        Cursor cursor = mAdapterCourses.getCursor();
        int courseIdPos = cursor.getColumnIndex(CourseInfoEntry.COLUMN_COURSE_ID);
        int courseRowIndex = 0;
        boolean more = cursor.moveToFirst();
        while (more) {
            String cursorCourseId = cursor.getString(courseIdPos);
            if (courseId.equals(cursorCourseId))
                break;
            courseRowIndex++;
            more = cursor.moveToNext();
        }
        return courseRowIndex;
    }


    private void readDisplayValues() {
        Intent intent = getIntent();
        mNoteId = intent.getIntExtra(NOTE_ID, ID_NOT_SET);
        mIsNewNote = mNoteId == ID_NOT_SET;
        if (mIsNewNote){
            createNewNote();
        } else {
            mNote = DataManager.getInstance().getNotes().get(mNoteId);
        }
    }

    private void createNewNote() {
        ContentValues values = new ContentValues();
        values.put(NoteInfoEntry.COLUMN_COURSE_ID, "");
        values.put(NoteInfoEntry.COLUMN_NOTE_TITLE, "");
        values.put(NoteInfoEntry.COLUMN_NOTE_TEXT, "");
        SQLiteDatabase db = mDbOpenHelper.getWritableDatabase();
        mNoteId = (int) db.insert(NoteInfoEntry.TABLE_NAME, null, values);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @SuppressLint("RestrictedApi")
    @Deprecated
    protected boolean onPrepareOptionsPanel(View view, @NonNull Menu menu) {
        MenuItem item = menu.findItem(R.id.action_next);
        int lastNoteIndex = DataManager.getInstance().getNotes().size() -1;
        item.setEnabled(mNotePosition < lastNoteIndex);
        return super.onPrepareOptionsPanel(view, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_send_mail) {
            sendEmail();
            return true;
        } else if (id == R.id.action_cancel) {
            mIsCancelling = true;
            finish();
        } else if (id == R.id.action_next) {
            moveNext();
            invalidateOptionsMenu();
        }

        return super.onOptionsItemSelected(item);
    }

    private void moveNext() {
        saveNote();
        ++mNotePosition;
        mNote = DataManager.getInstance().getNotes().get(mNotePosition);
        saveOriginalNoteValues();
        displayNote();
    }

    private void sendEmail() {
        CourseInfo course = (CourseInfo) mSpinnerCourses.getSelectedItem();
        String Subject = mTextNoteTitle.getText().toString();
        String text = "I thought you would love to know that i just learnt \"" + course.getTitle() + "\"\n" + mTextNoteText.getText().toString();
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc2822");
        intent.putExtra(Intent.EXTRA_SUBJECT, Subject);
        intent.putExtra(Intent.EXTRA_TEXT, text);
        startActivity(intent);
    }


}
