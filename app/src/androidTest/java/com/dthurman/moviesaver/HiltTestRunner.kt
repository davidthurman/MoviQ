package com.dthurman.moviesaver

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import androidx.work.Configuration
import androidx.work.WorkManager
import dagger.hilt.android.testing.HiltTestApplication

class HiltTestRunner : AndroidJUnitRunner() {

    override fun newApplication(cl: ClassLoader?, name: String?, context: Context?): Application {
        return super.newApplication(cl, HiltTestApplication::class.java.name, context)
    }

    override fun onCreate(arguments: android.os.Bundle?) {
        super.onCreate(arguments)
        
        val context = targetContext.applicationContext
        
        val config = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.DEBUG)
            .build()
        
        WorkManager.initialize(context, config)
    }
}
