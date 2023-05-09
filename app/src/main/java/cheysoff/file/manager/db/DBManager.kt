package cheysoff.file.manager.db

import android.util.Log
import cheysoff.file.manager.MainActivity.Companion.db
import java.io.File
import java.math.BigInteger
import java.security.MessageDigest

class DBManager() {
    fun updateDB(fileString: String): Boolean {
        val file = File(fileString)
        if (!file.exists()) return false

        if (file.isDirectory) {
            var shouldChange = false
            val listFiles = file.listFiles()
            if (listFiles == null || listFiles.size == 0) {
                return false
            }
            for (child in listFiles) {
                val childString = child.toString()
                if(updateDB(childString)) {
                    shouldChange = true
                }
            }
            updateDBRowDir(file.toString(), shouldChange)
            return shouldChange
        }
        else {
            return updateDBRow(file.toString())
        }

    }

    fun updateDBRowDir(childString: String, shouldChange: Boolean) {
        val changed = if (shouldChange) {
            "changed"
        } else {
            "same"
        }
        db.updateOrInsertRow(childString, "", changed)
    }

    fun updateDBRow(childString: String): Boolean {
        val md = MessageDigest.getInstance("MD5")
        val hash =
            BigInteger(1, md.digest(childString.toByteArray())).toString(16)
                .padStart(32, '0')
        Log.d("hash", hash)
        return db.updateHash(childString, hash)
    }

}