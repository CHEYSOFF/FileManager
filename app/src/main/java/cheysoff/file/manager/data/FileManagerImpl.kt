package cheysoff.file.manager.data

import android.os.Environment
import android.util.Log
import android.webkit.MimeTypeMap
import cheysoff.file.manager.FileService.FileManager
import cheysoff.file.manager.FileService.data.FileData
import cheysoff.file.manager.MainActivity
import cheysoff.file.manager.MainActivity.Companion.db
import cheysoff.file.manager.R
import cheysoff.file.manager.db.DBHelper.Companion.CHANGED_COl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.text.SimpleDateFormat
import java.util.LinkedList


object FileManagerImpl : FileManager {

    fun getFileSize(file: File?): Long {
        if (file == null || !file.exists()) return 0
        if (!file.isDirectory) return file.length()
        val dirs: MutableList<File> = LinkedList()
        dirs.add(file)
        var result: Long = 0
        while (!dirs.isEmpty()) {
            val dir = dirs.removeAt(0)
            if (!dir.exists()) continue
            val listFiles = dir.listFiles()
            if (listFiles == null || listFiles.size == 0) continue
            for (child in listFiles) {

                if (child.isDirectory) {
                    dirs.add(child)
                } else {
                    result += child.length()
                }
            }
        }
        return result
    }

    override suspend fun GetFilesByPath(
        pathPart: String,
        sortWay: Boolean,
        sortBy: MainActivity.Companion.sortByTypes
    ): List<FileData> {
        val path = File(
            Environment.getExternalStorageDirectory().toString() + "/" + pathPart,
        )
        val files = path.listFiles { _, name -> true }
        val result = mutableListOf<FileData>()
        if (files == null) {
            return result
        }
        for (file in files) {
            val filePath: Path = Paths.get(file.absolutePath)

            // Get file name
            val fileName = filePath.fileName.toString()

            // Get file size in bytes
            val fileSize = getFileSize(filePath.toFile())

            // Get file creation date
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val creationDate = dateFormat.format(withContext(Dispatchers.IO) {
                Files.getLastModifiedTime(filePath)
            }.toMillis())

            // Get file extension
            var fileExtension = filePath.fileName.toString().substringAfterLast(".")

            // Check if file is a directory
            val isDirectory = Files.isDirectory(filePath)

            if (isDirectory) {
                fileExtension = ".folder"
            }

            Log.d("flpth", filePath.toString())


            val res = db.getCell(filePath.toString(), CHANGED_COl)
            Log.d("changed", res)
            val changed = (res != "same")

            val curFile = FileData(
                name = fileName, isDirectory = isDirectory,
                size = fileSize, creationDate = creationDate, extension = fileExtension,
                wasChanged = changed
            )
            result.add(curFile)
            Log.d(
                "FileData",
                fileName + " " + fileSize + " " + creationDate + " " + fileExtension + " " + isDirectory
            )
        }

        if(sortWay) {
            when (sortBy) {
                MainActivity.Companion.sortByTypes.ByName -> result.sortBy { it.name }
                MainActivity.Companion.sortByTypes.BySize -> result.sortBy { it.size }
                MainActivity.Companion.sortByTypes.ByCreationDate -> result.sortBy { it.creationDate }
                MainActivity.Companion.sortByTypes.ByExtension -> result.sortBy { it.extension }
            }
        }
        else {
            when (sortBy) {
                MainActivity.Companion.sortByTypes.ByName -> result.sortByDescending { it.name }
                MainActivity.Companion.sortByTypes.BySize -> result.sortByDescending  { it.size }
                MainActivity.Companion.sortByTypes.ByCreationDate -> result.sortByDescending  { it.creationDate }
                MainActivity.Companion.sortByTypes.ByExtension -> result.sortByDescending  { it.extension }
            }
        }

        return result
    }

    override fun getFileTypeIcon(path: String): Int {
        val file = File(path)
        val ext = MimeTypeMap.getFileExtensionFromUrl(file.name)
        return when (".$ext") {
            ".folder" -> R.drawable.folder
            ".png", ".jpg", ".jpeg", ".gif", ".bmp" -> R.drawable.image
            ".mp3", ".wav", ".ogg", "midi" -> R.drawable.audio
            ".mp4", ".rmvb", ".avi", ".flv", ".3gp" -> R.drawable.video
            ".jsp", ".html", ".htm", ".js", ".php" -> R.drawable.web
            ".txt", ".c", ".cpp", ".xml", ".py", ".json", ".log",
            ".xls", ".xlsx", ".doc", ".docx",
            ".ppt", ".pptx" -> R.drawable.file
            ".pdf" -> R.drawable.pdf
            ".jar", ".zip", ".rar", ".gz" -> R.drawable.zip
            ".apk" -> R.drawable.apk
            else -> R.drawable.file
        }
    }
}
