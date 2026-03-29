package com.skillfield.androidhomeautomator.core.di

import com.skillfield.androidhomeautomator.core.storage.CredentialsManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    // All dependencies are provided via NetworkModule and StorageModule
    // This module is a placeholder for any app-level dependencies
}
