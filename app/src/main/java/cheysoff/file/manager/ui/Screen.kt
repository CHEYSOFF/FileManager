package cheysoff.file.manager.ui

import android.util.Log
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cheysoff.file.manager.FileService.data.FileData
import cheysoff.file.manager.R
import cheysoff.file.manager.data.FileManagerImpl
import cheysoff.file.manager.presention.ViewModel
import cheysoff.file.manager.presention.ViewModel.Companion.currentDirectory
import cheysoff.file.manager.presention.ViewModel.Companion.sortBy
import cheysoff.file.manager.presention.ViewModel.Companion.sortWay
import cheysoff.file.manager.ui.theme.Beuge
import cheysoff.file.manager.ui.theme.Beugelight
import cheysoff.file.manager.ui.theme.DarkBeuge
import cheysoff.file.manager.ui.theme.Gray

class Screen {
    @Composable
    fun ShowFiles(filesList: List<FileData>, viewModel: ViewModel) {
        Card(
            modifier = Modifier.fillMaxSize(), colors = CardDefaults.cardColors(
                containerColor = DarkBeuge,
            ), shape = RoundedCornerShape(0.dp)
        ) {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
//                verticalArrangement = Arrangement.Center,
                verticalArrangement = Arrangement.spacedBy(3.dp)

            ) {
                DisplayTopBar(viewModel)
                DisplayFiles(filesList, viewModel)
            }
        }
    }

    @Composable
    fun DisplayTopBar(viewModel: ViewModel) {
        val mainTextSize = 20.sp
        Card(
            colors = CardDefaults.cardColors(
                containerColor = Beuge,
            ),
            shape = RoundedCornerShape(0.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 3.dp, bottom = 3.dp, start = 10.dp),

                ) {

                Row(horizontalArrangement = Arrangement.spacedBy(15.dp)) {
                    Card(
                        modifier = Modifier.fillMaxWidth(0.1f),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.Transparent,
                        ),
                        shape = RoundedCornerShape(5.dp),
                    ) {
                        Text(
                            text = "Sort by", textAlign = TextAlign.Center, fontSize = mainTextSize
                        )
                    }

                    ColorWidthCardOnClick({
                        Text(
                            text = "Name", textAlign = TextAlign.Center, fontSize = mainTextSize
                        )
                    }, 0.18f, {
                        changeSort(ViewModel.Companion.SortByTypes.ByName, viewModel)
                    }, Beugelight
                    )

                    ColorWidthCardOnClick({
                        Text(
                            text = "Size", textAlign = TextAlign.Center, fontSize = mainTextSize
                        )
                    }, 0.17f, {
                        changeSort(ViewModel.Companion.SortByTypes.BySize, viewModel)
                    }, Beugelight
                    )

                    ColorWidthCardOnClick({
                        Text(
                            text = "Creation Date",
                            textAlign = TextAlign.Center,
                            fontSize = mainTextSize
                        )
                    }, 0.4f, {
                        changeSort(ViewModel.Companion.SortByTypes.ByCreationDate, viewModel)
                    }, Beugelight
                    )

                    ColorWidthCardOnClick({
                        Text(
                            text = "Extension",
                            textAlign = TextAlign.Center,
                            fontSize = mainTextSize
                        )
                    }, 1f, {
                        changeSort(ViewModel.Companion.SortByTypes.ByExtension, viewModel)
                    }, Beugelight
                    )
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun DisplayFiles(filesList: List<FileData>, viewModel: ViewModel) {
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
                    contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()

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
                                modifier = Modifier.size(40.dp),
                                contentScale = ContentScale.Fit
                            )
                            Text(
                                text = "..", fontSize = 20.sp, color = Color.Black
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

                        sortBy = ViewModel.Companion.SortByTypes.ByName
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
                                modifier = Modifier.size(40.dp),
                                contentScale = ContentScale.Fit
                            )
                        }, 0.1f)

                        TransparentWidthCard({
                            Column() {
                                Text(
                                    text = file.name, fontSize = mainTextSize, color = Color.Black
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
                                    modifier = Modifier.size(40.dp),
                                    contentScale = ContentScale.Fit
                                )
                            }
                        }, 1f)

                    }

                }
            }
        }
    }

    fun changeSort(changeType: ViewModel.Companion.SortByTypes, viewModel: ViewModel) {
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
        content: @Composable ColumnScope.() -> Unit, percentage: Float
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(percentage)
                .fillMaxHeight(),
            colors = CardDefaults.cardColors(
                containerColor = Color.Transparent,
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
}