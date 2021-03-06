package com.taiko.noblenote.filesystem

import android.net.Uri
import com.taiko.noblenote.filesystem.document.IDocumentFile
import com.taiko.noblenote.util.loggerFor
import org.apache.commons.text.StringEscapeUtils
import rx.Observable
import rx.lang.kotlin.toObservable
import rx.lang.kotlin.toSingletonObservable
import rx.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList
import kotlin.collections.HashSet


data class FindResult(val file : SFile, val line : CharSequence) : Comparable<FindResult>
{
    override fun compareTo(other: FindResult): Int {
        return this.file.name.compareTo(other.file.name) // this must also work for search results where the path is not the same
    }
}

data class FindResultList(val list : List<FindResult> = emptyList(), val nothingFound: Boolean = false)

/**
 * full text search inside files and subdirs
 */
object FindInFilesEngine {

    private val log = loggerFor();



    @JvmStatic
    fun findInFiles(file: SFile, queryTextObservable : Observable<out CharSequence>): Observable<FindResultList> {
        val obs: Observable<FindResultList> = queryTextObservable
                .observeOn(Schedulers.io())
                .switchMap { queryText ->

                    if(queryText.isBlank())
                    {
                        return@switchMap Observable.just(FindResultList());
                    }

                    val searchRes = recursiveFullTextSearch(file, queryText)
                            .delaySubscription(400,TimeUnit.MILLISECONDS)
                            .map {
                                FindResultList(list = it)
                                }
                            .defaultIfEmpty(FindResultList(nothingFound = true))

                    return@switchMap searchRes.startWith(FindResultList())
                }

        return obs;

    }


    private fun <T> Observable<Observable<T>>.concat(): Observable<T> = Observable.concat(this)

    private val regex = Regex("<[^>]*>");

    private const val whiteSpacePreWrap = "p, li { white-space: pre-wrap; }"

    // assumes the regular folder structure without subfolders except for root for performance
    @JvmStatic
    fun recursiveFullTextSearch(directoryToSearch : SFile, rawQueryText: CharSequence) : Observable<out List<FindResult>> {

        val queryText = StringEscapeUtils.escapeHtml4(rawQueryText.toString())
        // work on DocumentFile directly to increase performance
        return directoryToSearch.doc.listFiles().toObservable()

                .flatMap( {
            it.listFiles()
                    .filter { it.name?.startsWith(".") == false }
                    .toObservable()
                    .subscribeOn(Schedulers.io())
                    .map { note ->
                        if (note.name.orEmpty().contains(rawQueryText, true)) {
                            FindResult(SFile(note), "").toSingletonObservable() // file name contains the queryText, return the filePath
                        } else {

                            try {
                                val text = note.openInputStream().bufferedReader().use { it.readText() }
                                //TextConverter.removeHtmlTagsFast(text)
                                text
                                        .replace(regex,"")
                                        .lineSequence()
                                        .filter { line ->
                                            val found = line.contains(queryText, true)
                                            return@filter found && line != whiteSpacePreWrap

                                        }
                                        .take(1)
                                        .map { line -> FindResult(SFile(note), StringEscapeUtils.unescapeHtml4(line)) }
                                        .toObservable()
                            } catch (e: Exception) {
                                // capture attempts to read directory as files (isDirectory check is very slow)
                                log.w("failed to read: ${note.uri}",e);
                                return@map Observable.never<FindResult>();
                            }
                        }
                    }
                    .concat()

        }, 8)

                .scan(ArrayList<FindResult>(), { list, newVal ->
            list.add(newVal)
            list
        } )
        .filter { it.isNotEmpty() }



    }

    // assumes the regular folder structure without subfolders except for root for performance
    @JvmStatic
    @Deprecated("Use recursiveFullTextSearch")
    fun recursiveFullTextSearchSynchronous(directoryToSearch : SFile, queryText: CharSequence) : Observable<List<FindResult>> {


        return Observable.create<List<FindResult>> {

            log.v("Entering Observable.create")

            val dirs = directoryToSearch.doc.listFiles()

            val results = ArrayList<FindResult>();

            for (dir in dirs)
            {
                val notes = dir.listFiles();

                    for (note in notes) {
                        if (it.isUnsubscribed) {
                            log.v("Observable.create unsubscribed")
                            return@create
                        }

                        if (note.name.orEmpty().contains(queryText, true)) {
                            results.add(FindResult(SFile(note), ""))
                        } else {

                            val first = note.openInputStream().bufferedReader().use { it.readText() }
                                    .replace(regex, "")
                                    .lineSequence()
                                    .firstOrNull { line ->

                                        val found = line.contains(StringEscapeUtils.escapeHtml4(queryText.toString()), true)
                                        return@firstOrNull (found && line != whiteSpacePreWrap)

                                    }


                            if (first != null) {

                                results.add(FindResult(SFile(note), StringEscapeUtils.unescapeHtml4(first)));
                            }
                        }

                    }
            } // end outer loop
            it.onNext(results);
            log.v("Observable.create onNext(results) " + results.size)

        }

    }


    /**
     * queries usually start by typing Mon..,Monta..,Montana. If a lot of files do not contain Mon we do not need to search all these files again for Montana
     */
    private class FullTextSearchCache
    {
        fun returnDocumentIfItContains(doc : IDocumentFile, queryText: CharSequence) : Observable<IDocumentFile>
        {
            if(documentsWithoutQueryText.any { it == doc.uri })
            {
                return Observable.empty();
            }

            // FIXME can using replaced by useLines() ?

            return Observable.using(// open the file and try to find the queryText inside
                    { doc.openInputStream().bufferedReader() },
                    { it.lineSequence().toObservable() },
                    { it.close() })
                    .exists { it.contains(queryText, true) }
                    .doOnNext {
                        if(!it)
                        {
                            documentsWithoutQueryText.add(doc.uri);
                        }
                    }
                    .filter { it == true } // found
                    .map { doc }
        }

        fun clearCache()
        {
            documentsWithoutQueryText.clear();
        }

        private val documentsWithoutQueryText : HashSet<Uri> = HashSet();
    }
}