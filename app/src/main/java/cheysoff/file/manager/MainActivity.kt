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
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.viewModelScope
import cheysoff.file.manager.FileService.data.FileData
import cheysoff.file.manager.data.FileManagerImpl
import cheysoff.file.manager.db.DBHelper
import cheysoff.file.manager.db.DBManager
import cheysoff.file.manager.presention.State
import cheysoff.file.manager.presention.ViewModel
import cheysoff.file.manager.ui.theme.Beuge
import cheysoff.file.manager.ui.theme.Beugelight
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

        db = DBHelper(this, null)
        dbManager = DBManager()


        viewModel.viewModelScope.launch {
            withContext(Dispatchers.IO) {
                dbManager.updateDB(environmentDirectory)
            }
            viewModel.screenState
                .flowWithLifecycle(lifecycle, Lifecycle.State.RESUMED)
                .buffer()
                .collect { state ->
                    when (state) {
                        is State.Start -> {
                            Log.d("Start", "Start")

                            viewModel.GetFilesByPath(currentDirectory, sortWay, sortBy)
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
                DisplayTopBar()
                DisplayFiles(filesList)
            }
        }
    }

    @Composable
    fun DisplayTopBar() {
        val mainTextSize = 20.sp
        Card(
            colors = CardDefaults.cardColors(
                containerColor = Beuge,
            ),
            shape = RoundedCornerShape(0.dp),
        )
        {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 3.dp, bottom = 3.dp, start = 10.dp),

                ) {

                Row(horizontalArrangement = Arrangement.spacedBy(15.dp)) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.1f),
                        colors = CardDefaults.cardColors(
                            containerColor = Transparent,
                        ),
                        shape = RoundedCornerShape(5.dp),
                    ) {
                        Text(
                            text = "Sort by",
                            textAlign = TextAlign.Center,
                            fontSize = mainTextSize
                        )
                    }

                    ColorWidthCardOnClick({
                        Text(
                            text = "Name",
                            textAlign = TextAlign.Center,
                            fontSize = mainTextSize
                        )
                    }, 0.18f, {
                        changeSort(sortByTypes.ByName)
                    },
                        Beugelight
                    )

                    ColorWidthCardOnClick({
                        Text(
                            text = "Size",
                            textAlign = TextAlign.Center,
                            fontSize = mainTextSize
                        )
                    }, 0.17f, {
                        changeSort(sortByTypes.BySize)
                    },
                        Beugelight
                    )

                    ColorWidthCardOnClick({
                        Text(
                            text = "Creation Date",
                            textAlign = TextAlign.Center,
                            fontSize = mainTextSize
                        )
                    }, 0.4f, {
                        changeSort(sortByTypes.ByCreationDate)
                    },
                        Beugelight
                    )

                    ColorWidthCardOnClick({
                        Text(
                            text = "Extension",
                            textAlign = TextAlign.Center,
                            fontSize = mainTextSize
                        )
                    }, 1f, {
                        changeSort(sortByTypes.ByExtension)
                    },
                        Beugelight
                    )
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun DisplayFiles(filesList: List<FileData>) {
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
            val mainTextSize = 20.sp
            val subTextSize = 15.sp
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Beuge,
                ),
                shape = RoundedCornerShape(15.dp),
                onClick = {
                    if (file.isDirectory) {
                        currentDirectory += "/" + file.name

                        sortBy = sortByTypes.ByName
                        sortWay = true

                        viewModel.setToStart()

                        Log.d("currentDirectory", currentDirectory)
                    }
                },


                ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, start = 16.dp),

                    ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(15.dp)) {

                        TransparentWidthCard({
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
                            )
                        }, 0.1f)

                        TransparentWidthCard({
                            Column() {
                                Text(
                                    text = file.name,
                                    fontSize = mainTextSize,
                                    color = Color.Black
                                )
                                Text(
                                    text = file.size.toString() + " bytes",
                                    fontSize = subTextSize,
                                    color = Gray,
                                )
                            }
                        }, 0.35f)

                        TransparentWidthCard({
                            Text(
                                text = file.creationDate,
                                fontSize = subTextSize,
                                color = Color.Black
                            )
                        }, 0.5f)

                        TransparentWidthCard({
                            if (file.wasChanged) {
                                Image(
                                    painter = painterResource(
                                        id = R.drawable.changed
                                    ),
                                    contentDescription = "changed",
                                    modifier = Modifier
                                        .size(40.dp),
                                    contentScale = ContentScale.Fit
                                )
                            }
                        }, 1f)


                    }


                }
            }
        }
    }

    fun changeSort(changeType: sortByTypes) {
        if (sortBy == changeType) {
            sortWay = !sortWay
        } else {
            sortBy = changeType
            sortWay = true
        }
        viewModel.setToStart()
        Log.d("Sort", changeType.toString() + " " + sortWay.toString())
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun TransparentWidthCard(
        content: @Composable ColumnScope.() -> Unit,
        percentage: Float
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(percentage)
                .fillMaxHeight(),
            colors = CardDefaults.cardColors(
                containerColor = Transparent,
            ),
            content = content
        )
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ColorWidthCardOnClick(
        content: @Composable ColumnScope.() -> Unit,
        percentage: Float,
        onClick: () -> Unit,
        color: Color
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(percentage)
                .fillMaxHeight(),
            colors = CardDefaults.cardColors(
                containerColor = color,
            ),
            content = content,
            onClick = onClick
        )
    }

    companion object {

        lateinit var db: DBHelper
        lateinit var dbManager: DBManager

        enum class sortByTypes {
            ByName,
            BySize,
            ByCreationDate,
            ByExtension
        }


        var sortBy = sortByTypes.ByName
        var sortWay = true

        var currentDirectory = ""
        val environmentDirectory = getExternalStorageDirectory().absolutePath

        private val STORAGE_PERMISSION_CODE = 100
    }

}

