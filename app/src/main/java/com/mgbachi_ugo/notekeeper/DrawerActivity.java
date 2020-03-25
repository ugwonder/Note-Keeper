package com.mgbachi_ugo.notekeeper;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.mgbachi_ugo.notekeeper.NoteKeeperDatabaseContract.CourseInfoEntry;
import com.mgbachi_ugo.notekeeper.NoteKeeperDatabaseContract.NoteInfoEntry;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.Menu;
import android.widget.TextView;

import java.util.List;

public class DrawerActivity extends AppCompatActivity {

    private NoteRecyclerAdapter mNoteRecyclerAdapter;
    private AppBarConfiguration mAppBarConfiguration;
    private DrawerLayout mDrawer;
    private RecyclerView mRecyclerItems;
    private LinearLayoutManager mNotesLayoutManager;
    private CourseRecyclerAdapter mCourseRecyclerAdapter;
    private GridLayoutManager mCourseLayoutManager;
    private NavigationView mNavigationView;
    private NoteKeeperOpenHelper mDbOpenHelper;
    private Cursor mNoteCursor;

    @Override
    protected void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mDbOpenHelper = new NoteKeeperOpenHelper(this);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(DrawerActivity.this, MainActivity.class));
            }
        });
        PreferenceManager.setDefaultValues(this, R.xml.messages_preferences, false);
        PreferenceManager.setDefaultValues(this, R.xml.header_preferences, false);
        PreferenceManager.setDefaultValues(this, R.xml.sync_preferences, false);
        mDrawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_notes, R.id.nav_courses, R.id.nav_share, R.id.nav_send)
                .setDrawerLayout(mDrawer)
                .build();
        final NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
        initializeDisplayContent();
        navController.addOnDestinationChangedListener(new NavController.OnDestinationChangedListener() {

            @Override
            public void onDestinationChanged(@NonNull NavController controller,
                                             @NonNull NavDestination destination, @Nullable Bundle arguments) {
                if(destination.getId() == R.id.nav_notes) {
                    displayNotes();
                } else if (destination.getId() == R.id.nav_courses){
                    displayCourse();
                } else if(destination.getId() == R.id.nav_share) {
                    handleShare();
                } else if(destination.getId() == R.id.nav_send) {
                    handleSend();
                }
                mDrawer.closeDrawer(GravityCompat.START);
            }
        });
    }




    @Override
    protected void onDestroy() {
        mDbOpenHelper.close();
        super.onDestroy();
    }

    private void handleSend() {
        View view = findViewById(R.id.nav_host_fragment);
        Snackbar.make(view,
                PreferenceManager.getDefaultSharedPreferences(this).getString("user_display_name", "") +
                PreferenceManager.getDefaultSharedPreferences(this).getString("user_email_address", ""), Snackbar.LENGTH_SHORT).show();
    }

    private void handleShare() {
        View view = findViewById(R.id.nav_host_fragment);
        Snackbar.make(view, "Share to - " + PreferenceManager.getDefaultSharedPreferences(this).getString("user_favorite_social", ""), Snackbar.LENGTH_SHORT).show();
    }

    private void displayCourse() {
        mRecyclerItems.setLayoutManager(mCourseLayoutManager);
        mRecyclerItems.setAdapter(mCourseRecyclerAdapter);
        selectNavigationMenuItem(R.id.nav_courses);
    }


    @Override
    protected void onResume() {
        super.onResume();
        loadNotes();
        initializeDisplayContent();
        updateNavHeader();

    }

    private void loadNotes() {
    SQLiteDatabase db = mDbOpenHelper.getReadableDatabase();
        final String[] noteColumns = {
                NoteInfoEntry.getQName(NoteInfoEntry._ID),
                NoteInfoEntry.COLUMN_NOTE_TITLE,
                CourseInfoEntry.COLUMN_COURSE_TITLE};
        String noteOderBy = CourseInfoEntry.COLUMN_COURSE_TITLE + "," + NoteInfoEntry.COLUMN_NOTE_TITLE;
        //note_info join course_info on note_info.course_id = course_info.course_id
        String tablesWithJoin = NoteInfoEntry.TABLE_NAME + " JOIN " +
                CourseInfoEntry.TABLE_NAME + " ON " +
                NoteInfoEntry.getQName(NoteInfoEntry.COLUMN_COURSE_ID)  + " = " +
                CourseInfoEntry.getQName(CourseInfoEntry.COLUMN_COURSE_ID);
        mNoteCursor = db.query(tablesWithJoin, noteColumns,
                null, null, null, null, noteOderBy);


        mNoteRecyclerAdapter.changeCursor(mNoteCursor);

    }


    private void updateNavHeader() {
        View headerView = mNavigationView.getHeaderView(0);
        TextView textUserName = headerView.findViewById(R.id.text_user_name);
        TextView textUserEmail = headerView.findViewById(R.id.text_user_email);
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);

        String userName = pref.getString("user_display_name", "");
        String userEmail = pref.getString("user_email_address", "");
        textUserName.setText(userName);
        textUserEmail.setText(userEmail);
    }

    private void initializeDisplayContent() {
        DataManager.loadFromDatabase(mDbOpenHelper);
        mRecyclerItems = findViewById(R.id.list_items);
        mNotesLayoutManager = new LinearLayoutManager(this);
        mCourseLayoutManager = new GridLayoutManager(this,
                getResources().getInteger(R.integer.course_grid_span));

        mNoteRecyclerAdapter = new NoteRecyclerAdapter(this, mNoteCursor);

        List<CourseInfo> course = DataManager.getInstance().getCourses();
        mCourseRecyclerAdapter = new CourseRecyclerAdapter(this, course);

        displayNotes();
    }

    private void displayNotes() {
        mRecyclerItems.setLayoutManager(mNotesLayoutManager);
        mRecyclerItems.setAdapter(mNoteRecyclerAdapter);

        selectNavigationMenuItem(R.id.nav_notes);
    }

    private void selectNavigationMenuItem(int id) {
        mNavigationView = findViewById(R.id.nav_view);
        Menu menu = mNavigationView.getMenu();
        menu.findItem(id).setChecked(true);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Handles the action bar item click here. the action bar
        // will automatically handle clicks on the Home up button.
        // as long specify a parent activity in the manifest.xml.

        int id = item.getItemId();
        // noinspection simplifiable statment
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.drawer, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

}
