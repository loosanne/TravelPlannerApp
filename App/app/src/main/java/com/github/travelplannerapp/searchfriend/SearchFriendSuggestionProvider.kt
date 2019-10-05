package com.github.travelplannerapp.searchfriend

import android.app.SearchManager
import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import com.github.travelplannerapp.communication.CommunicationService

class SearchFriendSuggestionProvider : ContentProvider() {

    override fun query(query: Uri, p1: Array<out String>?, p2: String?, p3: Array<out String>?, p4: String?): Cursor? {
        val searchString = query.lastPathSegment.toString()
        return createCursor(searchString)
    }

    private fun createCursor(searchString: String): MatrixCursor {
        val cursor = MatrixCursor(arrayOf("_id", SearchManager.SUGGEST_COLUMN_TEXT_1, SearchManager.SUGGEST_COLUMN_TEXT_2))

        var list = CommunicationService.serverApi.findUsersEmails(searchString)

        for ((index, u) in list.blockingGet().data.orEmpty().withIndex()) {
            cursor.addRow(arrayOf(index, u.email, u.id))
        }
        return cursor
    }

    override fun onCreate(): Boolean {
        return true
    }

    override fun update(p0: Uri, p1: ContentValues?, p2: String?, p3: Array<out String>?): Int {
        return 0
    }

    override fun delete(p0: Uri, p1: String?, p2: Array<out String>?): Int {
        return 0
    }

    override fun getType(p0: Uri): String? {
        return "null"
    }

    override fun insert(p0: Uri, p1: ContentValues?): Uri? {
        return null
    }
}
