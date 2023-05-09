package cheysoff.file.manager.FileService

import cheysoff.file.manager.FileService.data.FileData
import cheysoff.file.manager.MainActivity


interface FileManager {
    suspend fun GetFilesByPath(
        path: String,
        sortWay: Boolean,
        sortBy: MainActivity.Companion.sortByTypes
    ): List<FileData>

    fun getFileTypeIcon(path: String): Int
}