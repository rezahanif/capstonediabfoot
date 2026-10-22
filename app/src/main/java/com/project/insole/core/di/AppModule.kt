package com.project.insole.core.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.project.insole.core.ble.InsoleBleManager
import com.project.insole.core.ble.BleConnectionManager
import com.project.insole.core.database.InsoleDatabase
import com.project.insole.core.database.dao.HourlyStepDao
import com.project.insole.core.database.dao.StepDao
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
    fun provideBleConnectionManager(
        @ApplicationContext context: Context,
        bleManager: InsoleBleManager
    ): BleConnectionManager {
        return BleConnectionManager(context, bleManager)
    }

    @Provides
    @Singleton
    fun provideNotificationManager(@ApplicationContext context: Context): InsoleNotificationManager {
        return InsoleNotificationManager(context)
    }

    /**
     * v2→v3: adds the per-foot step columns. A real migration (not
     * fallbackToDestructiveMigration) because wiping historical step data is
     * unacceptable for a medical monitoring app.
     */
    private val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE steps ADD COLUMN leftCount INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE steps ADD COLUMN rightCount INTEGER NOT NULL DEFAULT 0")
        }
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): InsoleDatabase {
        return Room.databaseBuilder(
            context,
            InsoleDatabase::class.java,
            "insole_db"
        )
        .addMigrations(MIGRATION_2_3)
        .build()
    }

    @Provides
    @Singleton
    fun provideStepDao(database: InsoleDatabase): StepDao {
        return database.stepDao()
    }

    @Provides
    @Singleton
    fun provideHourlyStepDao(database: InsoleDatabase): HourlyStepDao {
        return database.hourlyStepDao()
    }
}
