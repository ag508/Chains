package com.chain.messaging.presentation.chatlist;

import com.chain.messaging.domain.usecase.ArchiveChatUseCase;
import com.chain.messaging.domain.usecase.DeleteChatUseCase;
import com.chain.messaging.domain.usecase.GetChatsUseCase;
import com.chain.messaging.domain.usecase.PinChatUseCase;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava"
})
public final class ChatListViewModel_Factory implements Factory<ChatListViewModel> {
  private final Provider<GetChatsUseCase> getChatsUseCaseProvider;

  private final Provider<ArchiveChatUseCase> archiveChatUseCaseProvider;

  private final Provider<DeleteChatUseCase> deleteChatUseCaseProvider;

  private final Provider<PinChatUseCase> pinChatUseCaseProvider;

  public ChatListViewModel_Factory(Provider<GetChatsUseCase> getChatsUseCaseProvider,
      Provider<ArchiveChatUseCase> archiveChatUseCaseProvider,
      Provider<DeleteChatUseCase> deleteChatUseCaseProvider,
      Provider<PinChatUseCase> pinChatUseCaseProvider) {
    this.getChatsUseCaseProvider = getChatsUseCaseProvider;
    this.archiveChatUseCaseProvider = archiveChatUseCaseProvider;
    this.deleteChatUseCaseProvider = deleteChatUseCaseProvider;
    this.pinChatUseCaseProvider = pinChatUseCaseProvider;
  }

  @Override
  public ChatListViewModel get() {
    return newInstance(getChatsUseCaseProvider.get(), archiveChatUseCaseProvider.get(), deleteChatUseCaseProvider.get(), pinChatUseCaseProvider.get());
  }

  public static ChatListViewModel_Factory create(Provider<GetChatsUseCase> getChatsUseCaseProvider,
      Provider<ArchiveChatUseCase> archiveChatUseCaseProvider,
      Provider<DeleteChatUseCase> deleteChatUseCaseProvider,
      Provider<PinChatUseCase> pinChatUseCaseProvider) {
    return new ChatListViewModel_Factory(getChatsUseCaseProvider, archiveChatUseCaseProvider, deleteChatUseCaseProvider, pinChatUseCaseProvider);
  }

  public static ChatListViewModel newInstance(GetChatsUseCase getChatsUseCase,
      ArchiveChatUseCase archiveChatUseCase, DeleteChatUseCase deleteChatUseCase,
      PinChatUseCase pinChatUseCase) {
    return new ChatListViewModel(getChatsUseCase, archiveChatUseCase, deleteChatUseCase, pinChatUseCase);
  }
}
