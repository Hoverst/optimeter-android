package com.optimeter.app.presentation.dashboard.tabs;

import androidx.lifecycle.SavedStateHandle;
import com.optimeter.app.domain.repository.HomeRepository;
import com.optimeter.app.domain.repository.SettingsRepository;
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
    "KotlinInternalInJava",
    "cast"
})
public final class HomeViewModel_Factory implements Factory<HomeViewModel> {
  private final Provider<SavedStateHandle> savedStateHandleProvider;

  private final Provider<HomeRepository> homeRepositoryProvider;

  private final Provider<SettingsRepository> settingsRepositoryProvider;

  public HomeViewModel_Factory(Provider<SavedStateHandle> savedStateHandleProvider,
      Provider<HomeRepository> homeRepositoryProvider,
      Provider<SettingsRepository> settingsRepositoryProvider) {
    this.savedStateHandleProvider = savedStateHandleProvider;
    this.homeRepositoryProvider = homeRepositoryProvider;
    this.settingsRepositoryProvider = settingsRepositoryProvider;
  }

  @Override
  public HomeViewModel get() {
    return newInstance(savedStateHandleProvider.get(), homeRepositoryProvider.get(), settingsRepositoryProvider.get());
  }

  public static HomeViewModel_Factory create(Provider<SavedStateHandle> savedStateHandleProvider,
      Provider<HomeRepository> homeRepositoryProvider,
      Provider<SettingsRepository> settingsRepositoryProvider) {
    return new HomeViewModel_Factory(savedStateHandleProvider, homeRepositoryProvider, settingsRepositoryProvider);
  }

  public static HomeViewModel newInstance(SavedStateHandle savedStateHandle,
      HomeRepository homeRepository, SettingsRepository settingsRepository) {
    return new HomeViewModel(savedStateHandle, homeRepository, settingsRepository);
  }
}
