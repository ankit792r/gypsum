package com.system74.gypsum.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.system74.gypsum.apps.AppKind
import com.system74.gypsum.apps.AppRepository
import com.system74.gypsum.apps.HostedApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class HomeUiState(
    val apps: List<HostedApp> = emptyList(),
    val runtimeVersion: String = "",
    val isLoading: Boolean = true,
    val message: String? = null,
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AppRepository(application)

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repository.uninstall("hello-cli")
            }
            refresh()
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, message = null) }

            try {
                val apps = withContext(Dispatchers.IO) { repository.listApps() }
                _uiState.update {
                    it.copy(
                        apps = apps,
                        isLoading = false,
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        message = e.message ?: "Failed to load apps",
                    )
                }
            }
        }
    }

    fun importBinary(uri: Uri, name: String, kind: AppKind) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    repository.installFromUri(uri, name, kind)
                }
                refresh()
                showMessage("Imported $name")
            } catch (e: Exception) {
                showMessage(e.message ?: "Import failed")
            }
        }
    }

    fun uninstall(appId: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repository.uninstall(appId)
            }
            refresh()
            showMessage("Uninstalled")
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }

    fun setRuntimeVersion(version: String) {
        _uiState.update { it.copy(runtimeVersion = version) }
    }

    private fun showMessage(message: String) {
        _uiState.update { it.copy(message = message) }
    }
}
