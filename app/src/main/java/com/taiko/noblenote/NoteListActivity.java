package com.taiko.noblenote;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;


public class NoteListActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_list);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {
            Bundle arguments = new Bundle();
            arguments.putString(NoteListFragment.ARG_FOLDER_PATH,
                    getIntent().getStringExtra(NoteListFragment.ARG_FOLDER_PATH));
            arguments.putBoolean(FolderListActivity.ARG_TWO_PANE, 
            		getIntent().getBooleanExtra(FolderListActivity.ARG_TWO_PANE,false));
            NoteListFragment fragment = new NoteListFragment();
            fragment.setArguments(arguments);
            getFragmentManager().beginTransaction()
                    .add(R.id.item_detail_container, fragment)
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            NavUtils.navigateUpTo(this, new Intent(this, FolderListActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}