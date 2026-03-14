package com.optimeter.app.presentation.dashboard.tabs;

import com.optimeter.app.domain.repository.HomeRepository;
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
  private final Provider<HomeRepository> homeRepositoryProvider;

  public HomeViewModel_Factory(Provider<HomeRepository> homeRepositoryProvider) {
    this.homeRepositoryProvider = homeRepositoryProvider;
  }

  @Override
  public HomeViewModel get() {
    return newInstance(homeRepositoryProvider.get());
  }

  public static HomeViewModel_Factory create(Provider<HomeRepository> homeRepositoryProvider) {
    return new HomeViewModel_Factory(homeRepositoryProvider);
  }

  public static HomeViewModel newInstance(HomeRepository homeRepository) {
    return new HomeViewModel(homeRepository);
  }
}
