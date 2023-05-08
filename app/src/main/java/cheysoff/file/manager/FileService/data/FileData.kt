package cheysoff.file.manager.FileService.data

class FileData(
    val name: String, val isDirectory: Boolean, val size: Long,
    val creationDate: String, val extension: String, val wasChanged: Boolean
)