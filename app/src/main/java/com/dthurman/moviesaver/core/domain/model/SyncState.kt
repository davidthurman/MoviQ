package com.dthurman.moviesaver.core.domain.model

enum class SyncState {
    PENDING_CREATE,
    PENDING_UPDATE,
    PENDING_DELETE,
    SYNCED,
    FAILED
}

