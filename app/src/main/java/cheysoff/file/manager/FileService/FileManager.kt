package cheysoff.file.manager.FileService

import android.util.Log
import cheysoff.file.manager.FileService.data.FileData
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.text.SimpleDateFormat


interface FileManager {
    fun GetFilesByPath(path: String): List<FileData>
    fun getFileTypeIcon(path: String): Int
}