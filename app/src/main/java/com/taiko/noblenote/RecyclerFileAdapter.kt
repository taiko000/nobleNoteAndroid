package com.taiko.noblenote

import android.app.Activity
import android.databinding.ObservableArrayList
import android.graphics.Color
import android.os.Handler
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.jakewharton.rxbinding.view.clicks
import com.jakewharton.rxbinding.view.longClicks
import kotlinx.android.synthetic.main.recycler_file_item.view.*
import rx.Observable
import rx.lang.kotlin.PublishSubject
import rx.lang.kotlin.plusAssign
import rx.subjects.PublishSubject
import java.io.File
import java.io.FileFilter
import java.text.Collator
import java.util.*

/**
 * file system adapter, can be configured inside onViewCreated or onCreate
 * but you must call refresh() in onStart/onResume
 */
class RecyclerFileAdapter() : RecyclerView.Adapter<ViewHolder>() {

    private val mFiles: ObservableArrayList<FileItem>

    private val mClickSubject : PublishSubject<Int> = PublishSubject()
    private val mLongClickSubject : PublishSubject<Int> = PublishSubject()

    private val mWeakReferenceOnListChangedCallback: WeakReferenceOnListChangedCallback

    fun itemClicks(): Observable<Int> = mClickSubject.asObservable();

    fun itemLongClicks(): Observable<Int> = mLongClickSubject.asObservable();

    val selectedFiles : List<File>
        get() = mFiles.filter { it.isSelected }.map { it.file }

    var filter : FileFilter = FileFilter { true }

    var path : File = File(Pref.rootPath.value)
/*    set(value) {
        mFiles.clear()
        mHandler.postDelayed({ mFiles.addAll(FileHelper.listFilesSorted(value, filter).map { FileItem(it,false) }) },0)
        field = value
    }*/

    // reloads the file list by adding, removing files from the observable file list
    fun refresh(activity: Activity)
    {
        FileHelper.checkMountStateAndPermission(activity,
                {
            val newFileList = FileHelper.listFilesSorted(path,filter);
            // add files that arent contained in the list
            for (newFile in newFileList)
            {
                if(!mFiles.any { it.file.name == newFile.name })
                {
                    addFile(newFile)
                }
            }
            // remove files
            val iter = mFiles.listIterator();
            while (iter.hasNext())
            {
                val file = iter.next();
                if(!newFileList.any { it.name == file.file.name })
                {
                    iter.remove();
                }
            }
        }
        );

    }

    private val mHandler = Handler()

    init {
       // path = argPath
        mFiles = ObservableArrayList()
        mWeakReferenceOnListChangedCallback = WeakReferenceOnListChangedCallback(this);
        mFiles.addOnListChangedCallback(mWeakReferenceOnListChangedCallback)

        //rx.Observable.interval(3,TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe { mFiles.add(File(Pref.rootPath,"test " + it)) }
    }

    private var mSelectionColor: Int = 0

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        mSelectionColor = recyclerView.context.resources.getColor(R.color.listHighlight)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // create a new view
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_file_item, parent, false)
        // set the view's size, margins, paddings and layout parameters

        return ViewHolder(view)

    }

    fun removeSelected()
    {
        mFiles.removeAll { it.isSelected }
    }

    fun addFile(f : File)
    {
        mFiles.addSorted( FileItem(f,false), { lhs, rhs -> Collator.getInstance().compare(lhs.file.name, rhs.file.name) })
    }

    fun getItem(pos : Int) : File = mFiles[pos].file



    fun setSelected(pos : Int, isSelected: Boolean)
    {
        if( mFiles[pos].isSelected == isSelected)
            return;

        mFiles[pos].isSelected = isSelected;
        notifyItemChanged(pos)
    }

    fun isSelected(pos : Int): Boolean {
        if(pos < 0 || pos > mFiles.size -1)
            return false;

        return mFiles[pos].isSelected;
    }

    fun clearSelection()
    {
        for (i in 0.. mFiles.size-1)
        {
            mFiles[i].isSelected = false;
            notifyItemChanged(i)
        }

    }



    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val fileItem = mFiles[position]
        holder.itemView.text1.text = fileItem.file.name

        holder.mCompositeSubscription.clear() // clear subscriptions from previous bindings

        holder.mCompositeSubscription += holder.itemView.inner_layout
                .clicks()
                .doOnNext{ KLog.i("item click pos: " + position )}
                .subscribe { mClickSubject.onNext(position) }

        holder.mCompositeSubscription += holder.itemView.inner_layout
                .longClicks()
                .doOnNext{ KLog.i("item long click pos: " + position )}
                .subscribe { mLongClickSubject.onNext(position) }

        holder.itemView.outer_layout.setBackgroundColor(if(fileItem.isSelected) mSelectionColor else Color.TRANSPARENT)

    }




    override fun getItemCount(): Int {
        return mFiles.size
    }

    data class FileItem(val file : File,var isSelected: Boolean) : Comparable<FileItem> {
        override fun compareTo(other: FileItem): Int {
            return this.file.name.compareTo(other.file.name)
        }
    }

//    fun MutableList<File>.addSorted(mt : File)
//    {
//        var index = Collections.binarySearch<File>(this,mt, { lhs, rhs -> Collator.getInstance().compare(lhs.name, rhs.name) })
//        if (index < 0) index = index.inv()
//        add(index, mt)
//    }

    /**
     * based on
     * https://stackoverflow.com/questions/4903611/java-list-sorting-is-there-a-way-to-keep-a-list-permantly-sorted-automatically
     */
    fun <T> MutableList<T>.addSorted(mt : T, c : ((T, T) -> Int)) where T : Comparable<T>
    {
        var index = Collections.binarySearch<T>(this,mt, c)
        if (index < 0) index = index.inv()
        add(index, mt)
    }

}