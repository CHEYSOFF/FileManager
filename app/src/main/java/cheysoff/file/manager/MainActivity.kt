package cheysoff.file.manager

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Environment.getExternalStorageDirectory
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.viewModelScope
import cheysoff.file.manager.FileService.data.FileData
import cheysoff.file.manager.data.FileManagerImpl
import cheysoff.file.manager.presention.State
import cheysoff.file.manager.presention.ViewModel
import cheysoff.file.manager.ui.theme.Beuge
import cheysoff.file.manager.ui.theme.DarkBeuge
import cheysoff.file.manager.ui.theme.Gray
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MainActivity : ComponentActivity() {
    private val viewModel: ViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (checkPermission()) {
            Log.d(TAG, "onCreate: Permission already granted")
        } else {
            Log.d(TAG, "onCreate: Permission was not granted, request")
            requestPermission()
        }

        viewModel.viewModelScope.launch {
            viewModel.screenState
                .flowWithLifecycle(lifecycle, Lifecycle.State.RESUMED)
                .buffer()
                .collect { state ->
                    when (state) {
                        is State.Start -> {
                            Log.d("Start", "Start")

                            viewModel.GetFilesByPath(currentDirectory)
                        }

                        is State.HasAllData -> {
                            Log.d("HasAllData", "HasAllData")

                            withContext(Dispatchers.Main) {
                                setContent {
                                    ShowFiles(state.fileList)
                                }
                            }
                        }

                        is State.Error -> {


                        }
                    }
                }
        }
    }


    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            //Android is 11(R) or above
            try {
                Log.d(TAG, "requestPermission: try")
                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
                val uri = Uri.fromParts("package", this.packageName, null)
                intent.data = uri
                storageActivityResultLauncher.launch(intent)
            } catch (e: Exception) {
                Log.e(TAG, "requestPermission: ", e)
                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                storageActivityResultLauncher.launch(intent)
            }
        } else {
            //Android is below 11(R)
            ActivityCompat.requestPermissions(
                this,
                arrayOf(WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE),
                STORAGE_PERMISSION_CODE
            )
        }
    }

    private val storageActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            Log.d(TAG, "storageActivityResultLauncher: ")
            //here we will handle the result of our intent
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                //Android is 11(R) or above
                if (Environment.isExternalStorageManager()) {
                    //Manage External Storage Permission is granted
                    Log.d(
                        TAG,
                        "storageActivityResultLauncher: Manage External Storage Permission is granted"
                    )
                } else {
                    //Manage External Storage Permission is denied....
                    Log.d(
                        TAG,
                        "storageActivityResultLauncher: Manage External Storage Permission is denied...."
                    )
                    toast("Manage External Storage Permission is denied....")
                }
            } else {
                //Android is below 11(R)
            }
        }

    private fun checkPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            //Android is 11(R) or above
            Environment.isExternalStorageManager()
        } else {
            //Android is below 11(R)
            val write = ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE)
            val read = ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE)
            write == PackageManager.PERMISSION_GRANTED && read == PackageManager.PERMISSION_GRANTED
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty()) {
                //check each permission if granted or not
                val write = grantResults[0] == PackageManager.PERMISSION_GRANTED
                val read = grantResults[1] == PackageManager.PERMISSION_GRANTED
                if (write && read) {
                    //External Storage Permission granted
                    Log.d(TAG, "onRequestPermissionsResult: External Storage Permission granted")
                } else {
                    //External Storage Permission denied...
                    Log.d(TAG, "onRequestPermissionsResult: External Storage Permission denied...")
                    toast("External Storage Permission denied...")
                }
            }
        }
    }


    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }


    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ShowFiles(filesList: List<FileData>) {
        Card(
            modifier = Modifier.fillMaxSize(),
            colors = CardDefaults.cardColors(
                containerColor = DarkBeuge,
            ),
            shape = RoundedCornerShape(0.dp)
        ) {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
//                verticalArrangement = Arrangement.Center,
                verticalArrangement = Arrangement.spacedBy(3.dp)

            ) {
                if (currentDirectory != "") {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Beuge,
                        ),
                        shape = RoundedCornerShape(15.dp),
                        onClick = {
                            currentDirectory = currentDirectory.substringBeforeLast("/")
                            viewModel.setToStart()
                            Log.d("currentDirectory", currentDirectory)
                        },


                        ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxWidth()

                        ) {
                            Box(
//                            contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp, start = 16.dp),

                                ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(15.dp)) {
                                    Image(
                                        painter = painterResource(
                                            id = (R.drawable.folder)
                                        ),
                                        contentDescription = "file type image",
                                        modifier = Modifier
                                            .size(40.dp),
                                        contentScale = ContentScale.Fit
                                    )
                                    Text(
                                        text = "..",
                                        fontSize = 20.sp,
                                        color = Color.Black
                                    )
                                }
                            }
                        }
                    }
                }
                for (file in filesList) {
//                    val fullName = file.name + (if (file.isDirectory == false) "." + file.extension else "")
//                    val fullName = file.name + (file.isDirectory == false) ? file.extension
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Beuge,
                        ),
                        shape = RoundedCornerShape(15.dp),
                        onClick = {
                            if (file.isDirectory) {
                                currentDirectory += "/" + file.name
                                viewModel.setToStart()

                                Log.d("currentDirectory", currentDirectory)
                            }
                        },


                        ) {
                        Box(
//                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp, start = 16.dp),

                            ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(15.dp)) {
                                Image(
                                    painter = painterResource(
                                        id = (FileManagerImpl::getFileTypeIcon)(
                                            file.extension
                                        )
                                    ),
                                    contentDescription = "file type image",
                                    modifier = Modifier
                                        .size(40.dp),
                                    contentScale = ContentScale.Fit
//                                    modifier = Modifier
//                                        .fillMaxSize()
//                                        .alpha(0.7f),
//                                    contentScale = ContentScale.FillBounds,
                                )
                                Column() {
                                    Text(
                                        text = file.name + (if (!file.isDirectory) "." + file.extension else ""),
                                        fontSize = 20.sp,
                                        color = Color.Black
                                    )
                                    Text(
                                        text = file.size.toString() + " bytes",
                                        fontSize = 15.sp,
                                        color = Gray,
                                    )
                                }
                            }


                        }
                    }
                }
            }
        }
    }

    companion object {
        var currentDirectory = ""
        val environmentDirectory = getExternalStorageDirectory().absolutePath

        private val STORAGE_PERMISSION_CODE = 100
    }

}

