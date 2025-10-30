package com.chain.messaging.presentation.chat;

import com.chain.messaging.core.auth.AuthenticationService;
import com.chain.messaging.core.media.MediaHandler;
import com.chain.messaging.domain.usecase.AddReactionUseCase;
import com.chain.messaging.domain.usecase.GetMessagesUseCase;
import com.chain.messaging.domain.usecase.SendMessageUseCase;
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
public final class ChatViewModel_Factory implements Factory<ChatViewModel> {
  private final Provider<GetMessagesUseCase> getMessagesUseCaseProvider;

  private final Provider<SendMessageUseCase> sendMessageUseCaseProvider;

  private final Provider<AddReactionUseCase> addReactionUseCaseProvider;

  private final Provider<MediaHandler> mediaHandlerProvider;

  private final Provider<AuthenticationService> authenticationServiceProvider;

  public ChatViewModel_Factory(Provider<GetMessagesUseCase> getMessagesUseCaseProvider,
      Provider<SendMessageUseCase> sendMessageUseCaseProvider,
      Provider<AddReactionUseCase> addReactionUseCaseProvider,
      Provider<MediaHandler> mediaHandlerProvider,
      Provider<AuthenticationService> authenticationServiceProvider) {
    this.getMessagesUseCaseProvider = getMessagesUseCaseProvider;
    this.sendMessageUseCaseProvider = sendMessageUseCaseProvider;
    this.addReactionUseCaseProvider = addReactionUseCaseProvider;
    this.mediaHandlerProvider = mediaHandlerProvider;
    this.authenticationServiceProvider = authenticationServiceProvider;
  }

  @Override
  public ChatViewModel get() {
    return newInstance(getMessagesUseCaseProvider.get(), sendMessageUseCaseProvider.get(), addReactionUseCaseProvider.get(), mediaHandlerProvider.get(), authenticationServiceProvider.get());
  }

  public static ChatViewModel_Factory create(
      Provider<GetMessagesUseCase> getMessagesUseCaseProvider,
      Provider<SendMessageUseCase> sendMessageUseCaseProvider,
      Provider<AddReactionUseCase> addReactionUseCaseProvider,
      Provider<MediaHandler> mediaHandlerProvider,
      Provider<AuthenticationService> authenticationServiceProvider) {
    return new ChatViewModel_Factory(getMessagesUseCaseProvider, sendMessageUseCaseProvider, addReactionUseCaseProvider, mediaHandlerProvider, authenticationServiceProvider);
  }

  public static ChatViewModel newInstance(GetMessagesUseCase getMessagesUseCase,
      SendMessageUseCase sendMessageUseCase, AddReactionUseCase addReactionUseCase,
      MediaHandler mediaHandler, AuthenticationService authenticationService) {
    return new ChatViewModel(getMessagesUseCase, sendMessageUseCase, addReactionUseCase, mediaHandler, authenticationService);
  }
}
