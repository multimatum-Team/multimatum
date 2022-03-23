package com.github.multimatum_team.multimatum

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import com.github.multimatum_team.multimatum.model.Deadline
import com.github.multimatum_team.multimatum.model.DeadlineState
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDate

@Module
@InstallIn(ActivityComponent::class)
object DependenciesProvider {

    private const val SHARED_PREF_ID = "com.github.multimatum_team.multimatum.SharedPrefId"

    @Provides
    fun provideSharedPreferences(@ApplicationContext applicationContext: Context): SharedPreferences =
        applicationContext.getSharedPreferences(SHARED_PREF_ID, MODE_PRIVATE)

    @Provides
    fun providesDemoList(): List<Deadline> = listOf(
        Deadline("Number 1", DeadlineState.TODO, LocalDate.now().plusDays(1)),
        Deadline("Number 2", DeadlineState.TODO, LocalDate.now().plusDays(7)),
        Deadline("Number 3", DeadlineState.DONE, LocalDate.of(2022, 3, 30)),
        Deadline("Number 4", DeadlineState.TODO, LocalDate.of(2022, 3, 1))
    )

}