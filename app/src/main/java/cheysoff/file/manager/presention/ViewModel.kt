package cheysoff.file.manager.presention

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cheysoff.file.manager.data.FileManagerImpl
import cheysoff.file.manager.db.DBManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ViewModel : ViewModel() {
    private val _screenState = MutableStateFlow<State>(State.Start)
    val screenState = _screenState.asStateFlow()

    fun setToStart() {
        viewModelScope.launch(Dispatchers.IO) {
            _screenState.emit(State.Start)
        }
    }

    fun getFilesByPath(
        path: String,
        sortWay: Boolean,
        sortBy: SortByTypes
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val fileList = FileManagerImpl.getFilesByPath(path, sortWay, sortBy)
            Log.d("filelist size", fileList.size.toString())
            _screenState.emit(State.HasAllData(fileList))
        }
    }

    companion object {

        lateinit var dbManager: DBManager

        enum class SortByTypes {
            ByName,
            BySize,
            ByCreationDate,
            ByExtension
        }


        var sortBy = SortByTypes.ByName
        var sortWay = true

        var currentDirectory = ""
    }
}