package com.example.moviehw.presentation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import com.example.moviehw.R
import com.example.moviehw.data.retrofit.Author
import com.example.moviehw.domain.details.DetailsViewModel
import org.koin.androidx.compose.getViewModel
import org.koin.core.parameter.parametersOf

@RequiresApi(Build.VERSION_CODES.M)
@OptIn(ExperimentalCoilApi::class)
@Composable
fun DetailsScreen(author: Author, navController: NavController) {

    val viewModel: DetailsViewModel = getViewModel { parametersOf(author, navController) }
    val viewState by viewModel.viewState.collectAsState()

    Scaffold(
        modifier = Modifier
            .padding(10.dp)
            .fillMaxSize()
    ) {
        if (!viewState.isOnline && !viewState.isOfflineLoaded) {
            NoInternetPlaceHolder(viewModel = viewModel)
        } else {
            if (viewState.isOfflineDataEmpty) {
                EmptyOfflineDataPlaceHolder(viewModel)
            } else {
                DetailsCard(viewModel, author)
            }
        }
    }
}

@ExperimentalCoilApi
@Composable
private fun DetailsCard(viewModel: DetailsViewModel, author: Author) {
    val viewState by viewModel.viewState.collectAsState()
    Card(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 10.dp, end = 10.dp, top = 5.dp, bottom = 10.dp),
        elevation = 5.dp
    ) {
        if (viewState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(modifier = Modifier)
            }
        } else {
            Column {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        modifier = Modifier
                            .padding(5.dp)
                            .clip(CircleShape)
                            .height(150.dp),
                        contentScale = ContentScale.FillHeight,
                        painter = rememberImagePainter(author.avatarUrl),
                        contentDescription = ""
                    )
                    Text(
                        modifier = Modifier
                            .padding( start = 20.dp, end = 20.dp, top = 5.dp)
                            .fillMaxWidth()
                            .wrapContentHeight(),
                        fontSize = 12.sp,
                        color = Color.DarkGray,
                        textAlign = TextAlign.Center,
                        text = if (!viewState.isOnline) stringResource(R.string.details_screen_offline_data_marking) else ""
                    )
                    Text(
                        modifier = Modifier
                            .padding( start = 20.dp, end = 20.dp, bottom = 20.dp, top = 3.dp)
                            .wrapContentHeight(),
                        style = MaterialTheme.typography.h4,
                        textAlign = TextAlign.Center,
                        text = author.login
                    )
                    Text(
                        modifier = Modifier
                            .padding(bottom = 10.dp)
                            .wrapContentHeight(),
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        text = "Subscriptions: ${viewState.followersQty}"
                    )
                    Divider(
                        modifier = Modifier
                            .height(1.dp)
                            .padding(horizontal = 20.dp),
                        color = Color(0xFFA2A2A2)
                    )
                }
                Column {
                    Text(
                        modifier = Modifier
                            .padding(horizontal = 20.dp, vertical = 10.dp)
                            .wrapContentHeight(),
                        style = MaterialTheme.typography.h6,
                        color = Color.Gray,
                        text = stringResource(R.string.details_screen_repositories_header),
                    )

                    LazyColumn(
                        modifier = Modifier.padding(
                            start = 20.dp,
                            end = 20.dp,
                            bottom = 20.dp
                        )
                    ) {
                        items(viewState.reposList) {
                            Text(
                                modifier = Modifier.padding(5.dp),
                                text = it.name
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyOfflineDataPlaceHolder(viewModel: DetailsViewModel) {
    Column(
        modifier = Modifier.padding(20.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            modifier = Modifier
                .padding(start = 20.dp, end = 20.dp, top = 100.dp),
            style = MaterialTheme.typography.h5,
            textAlign = TextAlign.Center,
            text = stringResource(R.string.details_screen_empty_offline_data_text)
        )

        Button(modifier = Modifier
            .fillMaxWidth()
            .padding(30.dp),
            onClick = {
                viewModel.onBackPressed()
            }) {
            Text(text = stringResource(R.string.details_screen_empty_offline_data_btn_text))
        }
    }
}

