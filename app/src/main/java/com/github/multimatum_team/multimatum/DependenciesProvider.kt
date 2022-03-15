package com.github.multimatum_team.multimatum

import android.content.Context
import android.content.Context.MODE_PRIVATE
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.qualifiers.ApplicationContext

@Module
@InstallIn(ActivityComponent::class)
object DependenciesProvider {

    private const val SHARED_PREF_ID = "com.github.multimatum_team.multimatum.SharedPrefId"

    @Provides
    fun provideSharedPreferences(@ApplicationContext applicationContext: Context): SharedPrefAccessor =
        SharedPrefAccessor.of(
            applicationContext.getSharedPreferences(SHARED_PREF_ID, MODE_PRIVATE)
        )

}