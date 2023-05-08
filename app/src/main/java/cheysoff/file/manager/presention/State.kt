package cheysoff.file.manager.presention

import cheysoff.file.manager.FileService.data.FileData

sealed class State {
    object Start : State()
//    class GetFileList(val path: String) : State()
    class HasAllData(val fileList: List<FileData>) : State()
    class Error(val errorText: String) : State()
}