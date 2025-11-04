package com.dthurman.moviesaver.core.domain.model

/**
 * Sync state for offline-first architecture.
 * Tracks whether local changes need to be synced to remote storage.
 */
enum class SyncState {
    PENDING_CREATE,
    PENDING_UPDATE,
    PENDING_DELETE,
    SYNCED,
    FAILED
}

