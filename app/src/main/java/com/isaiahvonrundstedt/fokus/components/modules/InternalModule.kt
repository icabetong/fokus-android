package com.isaiahvonrundstedt.fokus.components.modules

import android.content.Context
import com.isaiahvonrundstedt.fokus.components.utils.PermissionManager
import com.isaiahvonrundstedt.fokus.components.utils.PreferenceManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class InternalModule {

    @Singleton
    @Provides
    fun providePreferenceManager(@ApplicationContext context: Context): PreferenceManager {
        return PreferenceManager(context)
    }

    @Singleton
    @Provides
    fun providePermissionManager(@ApplicationContext context: Context): PermissionManager {
        return PermissionManager(context)
    }

}