package co.edu.cecar.smartbookmobile.core.security

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.preferencesDataStore by preferencesDataStore(name = "smartbook_preferences")

class AppPreferencesStore(private val context: Context) {
    private val lastSyncKey = longPreferencesKey("last_sync_epoch")

    val lastSyncEpoch: Flow<Long> = context.preferencesDataStore.data.map { it[lastSyncKey] ?: 0L }

    suspend fun updateLastSync(epoch: Long) {
        context.preferencesDataStore.edit { prefs ->
            prefs[lastSyncKey] = epoch
        }
    }
}
