package cheysoff.file.manager.presention

import cheysoff.file.manager.FileService.data.FileData

sealed class State {
    object Start : State()
    class HasAllData(val fileList: List<FileData>) : State()
}