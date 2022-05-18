package com.github.multimatum_team.multimatum

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Context.SENSOR_SERVICE
import android.content.SharedPreferences
import android.hardware.SensorManager
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.CodeScannerView
import com.github.multimatum_team.multimatum.repository.*
import com.github.multimatum_team.multimatum.service.ClockService
import com.github.multimatum_team.multimatum.service.SystemClockService
import com.github.multimatum_team.multimatum.util.JsonDeadlineConverter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DependenciesProvider {

    private const val SHARED_PREF_ID = "com.github.multimatum_team.multimatum.SharedPrefId"

    @Provides
    fun provideSharedPreferences(@ApplicationContext applicationContext: Context): SharedPreferences =
        applicationContext.getSharedPreferences(SHARED_PREF_ID, MODE_PRIVATE)

    @Provides
    fun provideSensorManager(@ApplicationContext applicationContext: Context): SensorManager =
        applicationContext.getSystemService(SENSOR_SERVICE) as SensorManager
}

@Module
@InstallIn(SingletonComponent::class)
object ClockModule {
    @Singleton
    @Provides
    fun provideClockService(): ClockService =
        SystemClockService()
}

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {
    @Singleton
    @Provides
    fun provideFirebaseAuth(): FirebaseAuth =
        FirebaseAuth.getInstance()

    @Singleton
    @Provides
    fun provideFirebaseFirestore(): FirebaseFirestore =
        FirebaseFirestore.getInstance()

    @Singleton
    @Provides
    fun provideFirebaseStorage(): FirebaseStorage =
        FirebaseStorage.getInstance()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class FirebaseRepositoryModule {
    @Singleton
    @Binds
    abstract fun provideDeadlineRepository(impl: FirebaseDeadlineRepository): DeadlineRepository

    @Singleton
    @Binds
    abstract fun provideGroupRepository(impl: FirebaseGroupRepository): GroupRepository

    @Singleton
    @Binds
    abstract fun provideAuthRepository(impl: FirebaseAuthRepository): AuthRepository

    @Singleton
    @Binds
    abstract fun provideUserRepository(impl: FirebaseUserRepository): UserRepository

    @Singleton
    @Binds
    abstract fun providePdfRepository(impl: FirebasePdfRepository): PdfRepository
}

/**
 * Produces a CodeScanner using the provided function
 *
 * This class allows mocking users of CodeScanner while allowing them to parametrize
 * the CodeScanner with their own Context and CodeScannerView
 */
data class CodeScannerProducer(val produce: (Context, CodeScannerView) -> CodeScanner)

@Module
@InstallIn(SingletonComponent::class)
object CodeScannerModule {

    @Provides
    fun provideCodeScannerProducer(): CodeScannerProducer =
        CodeScannerProducer { ctx, view -> CodeScanner(ctx, view) }

    @Provides
    fun provideJsonDeadlineConverter(): JsonDeadlineConverter =
        JsonDeadlineConverter()

}
