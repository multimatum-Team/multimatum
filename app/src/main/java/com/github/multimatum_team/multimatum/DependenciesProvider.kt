package com.github.multimatum_team.multimatum

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import com.github.multimatum_team.multimatum.repository.DeadlineRepository
import com.github.multimatum_team.multimatum.repository.FirebaseDeadlineRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(ActivityComponent::class)
object DependenciesProvider {

    private const val SHARED_PREF_ID = "com.github.multimatum_team.multimatum.SharedPrefId"

    @Provides
    fun provideSharedPreferences(@ApplicationContext applicationContext: Context): SharedPreferences =
        applicationContext.getSharedPreferences(SHARED_PREF_ID, MODE_PRIVATE)
}

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Singleton
    @Provides
    fun provideDeadlineRepository(): DeadlineRepository =
        FirebaseDeadlineRepository()
}