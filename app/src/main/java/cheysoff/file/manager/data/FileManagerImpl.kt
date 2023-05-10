package cheysoff.file.manager.data

import android.os.Environment
import android.util.Log
import android.webkit.MimeTypeMap
import cheysoff.file.manager.FileService.FileManager
import cheysoff.file.manager.FileService.data.FileData
import cheysoff.file.manager.R
import cheysoff.file.manager.db.DBHelper.Companion.CHANGED_COl
import cheysoff.file.manager.presention.ViewModel
import cheysoff.file.manager.presention.ViewModel.Companion.dbManager
import cheysoff.file.manager.presention.ViewModel.Companion.SortByTypes
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

    override suspend fun getFilesByPath(
        pathPart: String,
        sortWay: Boolean,
        sortBy: SortByTypes
    ): List<FileData> {
        val path = File(
            Environment.getExternalStorageDirectory().toString() + "/" + pathPart,
        )
        val files = path.listFiles { _, name -> true }
        val result = arrayListOf<FileData>()
        if (files == null) {
            return result
        }
        for (file in files) {
            val filePath: Path = Paths.get(file.absolutePath)

            val fileName = filePath.fileName.toString()

            val fileSize = getFileSize(filePath.toFile())

            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val creationDate = dateFormat.format(withContext(Dispatchers.IO) {
                Files.getLastModifiedTime(filePath)
            }.toMillis())

            var fileExtension = filePath.fileName.toString().substringAfterLast(".")

            val isDirectory = Files.isDirectory(filePath)

            if (isDirectory) {
                fileExtension = ".folder"
            }

            Log.d("flpth", filePath.toString())

            val res = dbManager.getDbHelper().getCell(filePath.toString(), CHANGED_COl)
            Log.d("changed", res)
            val changed = (res != "same")

            val curFile = FileData(
                name = fileName,
                isDirectory = isDirectory,
                size = fileSize,
                creationDate = creationDate,
                extension = fileExtension,
                wasChanged = changed
            )
            result.add(curFile)
            Log.d(
                "FileData",
                "$fileName $fileSize $creationDate $fileExtension $isDirectory"
            )
        }

        return result.customSort(ViewModel.sortWay, ViewModel.sortBy)
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
