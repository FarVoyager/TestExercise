package com.example.testexercise.domain.details

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.testexercise.data.retrofit.Author
import com.example.testexercise.data.retrofit.RetrofitRepository
import com.example.testexercise.data.room.RoomRepository
import com.example.testexercise.domain.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DetailsViewModel(
    private val author: Author,
    private val repository: RetrofitRepository,
    private val roomRepository: RoomRepository,
    private val isOnlineOnStart: Boolean
) : BaseViewModel() {
    private val _viewState = MutableStateFlow(DetailsViewState())
    val viewState: StateFlow<DetailsViewState> = _viewState

    init {
        updateNetworkStatus(isOnlineOnStart)
    }

    private fun loadRepos() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.repos(author.reposUrl).catch {
                println("Error while loading repos: ${it.message}")
            }.collect { repos ->
                if (repos.isEmpty()) Log.d(
                    "Unexpected Behavior",
                    "Received data is empty. Maybe some of Repo fields are null"
                )

                _viewState.update {
                    it.copy(reposList = repos, isLoading = false)
                }
                roomRepository.insertRepos(repos, author.id)
                repository.followers(author.subscriptionsUrl).catch {
                    println("Error while loading followers: ${it.message}")
                }.collect { followers ->
                    _viewState.update {
                        it.copy(followersQty = followers.size)
                    }
                    roomRepository.updateSubscriptionsQtyByAuthorId(followers.size, author.id)
                }
            }
        }
    }

    private fun loadReposOffline(authorId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            roomRepository.getReposByAuthorId(authorId).collect { repos ->
                roomRepository.getAuthorSubscriptionsQtyByAuthorId(authorId)
                    .collect { followersQty ->
                        _viewState.update {
                            it.copy(
                                isLoading = false,
                                reposList = repos,
                                followersQty = followersQty,
                                isOfflineLoaded = true
                            )
                        }
                    }
            }
        }
    }

    override fun updateNetworkStatus(isOnline: Boolean) {
        Log.d("VVV", "ISONLINE: $isOnline")
        _viewState.update {
            it.copy(isOnline = isOnline)
        }
        if (_viewState.value.isOnline) {
            loadRepos()
        }
    }

    override fun loadLastData() {
        loadReposOffline(author.id)
    }

}