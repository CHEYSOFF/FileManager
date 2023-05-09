package cheysoff.file.manager.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class DBHelper(context: Context, factory: SQLiteDatabase.CursorFactory?) :
    SQLiteOpenHelper(context, DATABASE_NAME, factory, DATABASE_VERSION) {

    // below is the method for creating a database by a sqlite query
    override fun onCreate(db: SQLiteDatabase) {
        // below is a sqlite query, where column names
        // along with their data types is given
        val query = ("CREATE TABLE " + TABLE_NAME + " ("
                + FILE_PATH_COL + " TEXT PRIMARY KEY," +
                HASH_COl + " TEXT," +
                CHANGED_COl + " TEXT" + ")")

        // we are calling sqlite
        // method for executing our query
        db.execSQL(query)
    }

    override fun onUpgrade(db: SQLiteDatabase, p1: Int, p2: Int) {
        // this method is to check if table already exists
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME)
        onCreate(db)

    }

    fun updateHash(path: String, newHash: String): Boolean {
        val oldHash = getCell(path, HASH_COl)
        Log.d("old", oldHash)
        Log.d("new", newHash)
        val changed = if (oldHash == newHash) {
            "same"
        } else {
            "changed"
        }
        Log.d("changes?", changed)
        updateOrInsertRow(path, newHash, changed)
        return oldHash != newHash
    }

    fun updateOrInsertRow(filePath: String, newHash: String, newChanged: String) {
        val db = writableDatabase
        Log.d("wa", filePath)
        val values = ContentValues().apply {
            put(FILE_PATH_COL, filePath)
            put(HASH_COl, newHash)
            put(CHANGED_COl, newChanged)
        }
        val selection = "$FILE_PATH_COL = ?"
        val selectionArgs = arrayOf(filePath)
        val rowsAffected = db.update(TABLE_NAME, values, selection, selectionArgs)

        if (rowsAffected == 0) {
            Log.d("hey", "way")
            db.insertWithOnConflict(TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE)
        }
    }


    fun getCell(path: String, column: String): String {
        val db = readableDatabase
        val selection = "$FILE_PATH_COL = ?"
        val selectionArgs = arrayOf(path)
        val cursor = db.query(TABLE_NAME, arrayOf(column), selection, selectionArgs, null, null, null)
        var result: String? = null
        Log.d("col", column)
        if (cursor.moveToFirst()) {
            Log.d("tru", "e")
            val columnIndex = cursor.getColumnIndex(column)
            if (columnIndex >= 0) {
                result = cursor.getString(columnIndex)
            }
        }
        cursor.close()
        return result.orEmpty()
    }


    companion object {
        private val DATABASE_NAME = "MD5_hash_codes"

        private val DATABASE_VERSION = 1

        val TABLE_NAME = "files_hashes"

        val FILE_PATH_COL = "file_path"

        val HASH_COl = "hash"

        val CHANGED_COl = "changed"

    }
}