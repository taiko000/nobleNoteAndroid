package com.taiko.noblenote

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.support.v4.widget.SwipeRefreshLayout
import android.transition.Fade
import android.view.View
import com.jakewharton.rxbinding.view.clicks
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main_twopane.*
import rx.lang.kotlin.plusAssign
import rx.subscriptions.CompositeSubscription


class MainActivity : Activity()
{

    var mTwoPane: Boolean = false

    private val mCompositeSubscription = CompositeSubscription()
    lateinit var mMainToolbarController: MainToolbarController

    public companion object {
        @JvmField val ARG_TWO_PANE = "two_pane"
    }

    public fun onFolderSelected(path: String) {

            val arguments = Bundle()
            arguments.putString(NoteListFragment.ARG_FOLDER_PATH, path)
            arguments.putBoolean(MainActivity.ARG_TWO_PANE, mTwoPane)
            val fragment = NoteListFragment()
            fragment.arguments = arguments
            Pref.currentFolderPath.onNext(path);

        if (mTwoPane) {
            fragmentManager.beginTransaction().replace(R.id.item_detail_container, fragment).commit()
        } else {
            fragmentManager.beginTransaction().add(R.id.item_master_container, fragment).addToBackStack(null).commit();
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)




    // https://stackoverflow.com/questions/26600263/how-do-i-prevent-the-status-bar-and-navigation-bar-from-animating-during-an-acti
        if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.LOLLIPOP) {
            val fade = Fade()
            fade.excludeTarget(android.R.id.statusBarBackground, true)
            fade.excludeTarget(android.R.id.navigationBarBackground, true)
            window.exitTransition = fade
            window.enterTransition = fade
        }

        mTwoPane = findViewById<View>(R.id.item_detail_container) != null // two pane uses the refs.xml reference to reference activity_main_twopane.xml as activity_main.xml

//        setSupportActionBar(toolbar) // required to make styling working, activity options menu callbacks now have to be used


        // request permissions dialog



        fragmentManager.beginTransaction().add(R.id.item_master_container, FolderListFragment()).commit()

        val app = application as MainApplication
        mCompositeSubscription += app.uiCommunicator.folderSelected.subscribe { onFolderSelected(it.absolutePath) }
        mCompositeSubscription += app.uiCommunicator.fileSelected.mergeWith(app.uiCommunicator.createFileClick).subscribe { NoteListFragment.startNoteEditor(this,it, NoteEditorActivity.READ_WRITE) }



        if(mTwoPane)
        {


            mCompositeSubscription += fab_menu_note.clicks().subscribe {
                Dialogs.showNewNoteDialog(this) {app.uiCommunicator.createFileClick.onNext(it)}
                fab_menu.close(true);
            }

            mCompositeSubscription += fab_menu_folder.clicks().subscribe {
                Dialogs.showNewFolderDialog(this,{app.uiCommunicator.createFolderClick.onNext(it)})
                fab_menu.close(true);
            }
        }
        else
        {
            mCompositeSubscription += fab.clicks().subscribe {
                if(fragmentManager.backStackEntryCount > 0)
                {
                    Dialogs.showNewNoteDialog(this, {app.uiCommunicator.createFileClick.onNext(it)})
                }
                else
                {
                    Dialogs.showNewFolderDialog(this, {app.uiCommunicator.createFolderClick.onNext(it)})
                }
            }
        }

        mMainToolbarController = MainToolbarController(this)

        val handler = Handler();

        val swipeRefreshLayout = findViewById<SwipeRefreshLayout>(R.id.swipe_refresh);

        swipeRefreshLayout.setOnRefreshListener {
            handler.postDelayed({swipeRefreshLayout.isRefreshing = false},500)
            app.uiCommunicator.swipeRefresh.onNext(Unit)

        }
    }

    override fun onBackPressed() {
        // close search
        if(!mMainToolbarController.onBackPressed())
        {
            super.onBackPressed();
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mCompositeSubscription.clear()
    }

    /**
     * sets the visibility of the floating action button
     */
    fun setFabVisible(b : Boolean)
    {
        if(b)
        {
            this.fab?.visibility = View.VISIBLE;
            this.fab_menu_folder?.visibility = View.VISIBLE;
            this.fab_menu_note?.visibility = View.VISIBLE;
        }
        else
        {
            this.fab?.visibility = View.INVISIBLE;
            this.fab_menu_folder?.visibility = View.INVISIBLE;
            this.fab_menu_note?.visibility = View.INVISIBLE;
        }
    }

}