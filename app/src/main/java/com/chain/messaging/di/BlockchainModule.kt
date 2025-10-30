package com.chain.messaging.di

import com.chain.messaging.core.blockchain.BlockchainManager
import com.chain.messaging.core.blockchain.BlockchainManagerImpl
import com.chain.messaging.core.blockchain.ConsensusHandler
import com.chain.messaging.core.blockchain.TransactionSigner
import com.chain.messaging.core.p2p.P2PManager
import com.chain.messaging.core.p2p.P2PManagerImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class BlockchainModule {
    
    @Binds
    @Singleton
    abstract fun bindBlockchainManager(
        blockchainManagerImpl: BlockchainManagerImpl
    ): BlockchainManager
    
    @Binds
    @Singleton
    abstract fun bindP2PManager(
        p2pManagerImpl: P2PManagerImpl
    ): P2PManager
    
    companion object {
        @Provides
        @Singleton
        fun provideTransactionSigner(keyManager: com.chain.messaging.core.crypto.KeyManager): TransactionSigner {
            return TransactionSigner(keyManager)
        }
        
        @Provides
        @Singleton
        fun provideConsensusHandler(): ConsensusHandler {
            return ConsensusHandler()
        }
        
        @Provides
        @Singleton
        fun provideP2PManagerImpl(): P2PManagerImpl {
            return P2PManagerImpl()
        }
        
        @Provides
        @Singleton
        fun provideBlockchainManagerImpl(
            transactionSigner: TransactionSigner,
            consensusHandler: ConsensusHandler,
            authenticationService: com.chain.messaging.core.auth.AuthenticationService
        ): BlockchainManagerImpl {
            return BlockchainManagerImpl(transactionSigner, consensusHandler, authenticationService)
        }
    }
}