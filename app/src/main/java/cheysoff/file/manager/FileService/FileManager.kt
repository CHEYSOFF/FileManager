package cheysoff.file.manager.FileService

import cheysoff.file.manager.FileService.data.FileData
import cheysoff.file.manager.presention.ViewModel


interface FileManager {
    suspend fun getFilesByPath(
        path: String,
        sortWay: Boolean,
        sortBy: ViewModel.Companion.SortByTypes
    ): List<FileData>

    fun getFileTypeIcon(path: String): Int
}