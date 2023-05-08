package cheysoff.file.manager.presention

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cheysoff.file.manager.data.FileManagerImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ViewModel : ViewModel() {
    //    private val fileManager = FileManagerImpl
    private val _screenState = MutableStateFlow<State>(State.Start)
    val screenState = _screenState.asStateFlow()

    fun setToStart() {
        viewModelScope.launch(Dispatchers.IO) {
            _screenState.emit(State.Start)
        }
    }

    fun GetFilesByPath(path : String) {
        viewModelScope.launch(Dispatchers.IO) {
            val fileList = FileManagerImpl.GetFilesByPath(path)
            Log.d("filelist size", fileList.size.toString())
            _screenState.emit(State.HasAllData(fileList))
        }
    }
}