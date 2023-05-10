package cheysoff.file.manager.data

import cheysoff.file.manager.FileService.data.FileData
import cheysoff.file.manager.presention.ViewModel.Companion.SortByTypes

fun ArrayList<FileData>.customSort(sortWay: Boolean, sortBy: SortByTypes) =
    if (sortWay) {
        when (sortBy) {
            SortByTypes.ByName -> this.sortBy { it.name }
            SortByTypes.BySize -> this.sortBy { it.size }
            SortByTypes.ByCreationDate -> this.sortBy { it.creationDate }
            SortByTypes.ByExtension -> this.sortBy { it.extension }
        }
        this.toCollection(ArrayList())
    } else {
        when (sortBy) {
            SortByTypes.ByName -> this.sortByDescending { it.name }
            SortByTypes.BySize -> this.sortByDescending { it.size }
            SortByTypes.ByCreationDate -> this.sortByDescending { it.creationDate }
            SortByTypes.ByExtension -> this.sortByDescending { it.extension }
        }
        this.toCollection(ArrayList())
    }