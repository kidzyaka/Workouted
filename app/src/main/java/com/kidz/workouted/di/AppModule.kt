package com.kidz.workouted.di

import android.app.Application
import androidx.room.Room
import com.kidz.workouted.data.local.WorkoutedDatabase
import com.kidz.workouted.data.local.WorkoutedDatabaseCallback
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideWorkoutedDatabase(
        app: Application,
        callback: WorkoutedDatabaseCallback
    ): WorkoutedDatabase {
        return Room.databaseBuilder(
            app,
            WorkoutedDatabase::class.java,
            WorkoutedDatabase.DATABASE_NAME
        )
            .addCallback(callback)
            .build()
    }

    @Provides
    @Singleton
    fun provideWorkoutDao(db: WorkoutedDatabase) = db.workoutDao
}
