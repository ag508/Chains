package com.chain.messaging.di

import com.chain.messaging.core.group.GroupManager
import com.chain.messaging.core.group.GroupManagerImpl
import com.chain.messaging.core.group.InviteLinkGenerator
import com.chain.messaging.core.group.InviteLinkGeneratorImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class GroupModule {
    
    @Binds
    @Singleton
    abstract fun bindGroupManager(
        groupManagerImpl: GroupManagerImpl
    ): GroupManager
    
    @Binds
    @Singleton
    abstract fun bindInviteLinkGenerator(
        inviteLinkGeneratorImpl: InviteLinkGeneratorImpl
    ): InviteLinkGenerator
}