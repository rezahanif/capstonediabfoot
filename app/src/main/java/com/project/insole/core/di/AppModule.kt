package com.project.insole.core.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
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
    fun provideAppContext(): String {
        // Provide application context
        return "app_context"
    }
}
