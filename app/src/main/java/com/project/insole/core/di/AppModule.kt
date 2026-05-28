package com.project.insole.core.di

import android.content.Context
import com.project.insole.core.ble.InsoleBleManager
import com.project.insole.core.notifications.InsoleNotificationManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt dependency injection module for app-wide singleton dependencies.
 * Include BLE manager, network client, database instances here.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideBleManager(@ApplicationContext context: Context): InsoleBleManager {
        return InsoleBleManager(context)
    }

    @Provides
    @Singleton
    fun provideNotificationManager(@ApplicationContext context: Context): InsoleNotificationManager {
        return InsoleNotificationManager(context)
    }
}
