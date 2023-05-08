package cheysoff.file.manager.data

import android.util.Log
import android.webkit.MimeTypeMap
import cheysoff.file.manager.FileService.FileManager
import cheysoff.file.manager.FileService.data.FileData
import cheysoff.file.manager.R
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.text.SimpleDateFormat

object FileManagerImpl : FileManager {
    override suspend fun GetFilesByPath(path: String): List<FileData> {
        val files = File(path).listFiles()
        val result = mutableListOf<FileData>()
        for (file in files) {
            val filePath: Path = Paths.get(file.absolutePath)

            // Get file name
            val fileName = filePath.fileName.toString()

            // Get file size in bytes
            val fileSize = Files.size(filePath)

            // Get file creation date
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val creationDate = dateFormat.format(Files.getLastModifiedTime(filePath).toMillis())

            // Get file extension
            var fileExtension = filePath.fileName.toString().substringAfterLast(".")

            // Check if file is a directory
            val isDirectory = Files.isDirectory(filePath)

            if(isDirectory) {
                fileExtension = ".folder"
            }

            val curFile = FileData(
                name = fileName, isDirectory = isDirectory,
                size = fileSize, creationDate = creationDate, extension = fileExtension,
                wasChanged = false
            )
            result.add(curFile)
            Log.d(
                "FileData",
                fileName + " " + fileSize + " " + creationDate + " " + fileExtension + " " + isDirectory
            )
        }
        return result
    }

    override fun getFileTypeIcon(path: String): Int {
        val file = File(path)
        val ext = MimeTypeMap.getFileExtensionFromUrl(file.name)
        Log.i("ImagesExt", "$path ----> $ext")
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
