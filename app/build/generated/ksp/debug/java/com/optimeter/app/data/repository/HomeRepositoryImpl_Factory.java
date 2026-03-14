package com.optimeter.app.data.repository;

import com.optimeter.app.data.remote.api.OptimeterApiService;
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
public final class HomeRepositoryImpl_Factory implements Factory<HomeRepositoryImpl> {
  private final Provider<OptimeterApiService> apiProvider;

  public HomeRepositoryImpl_Factory(Provider<OptimeterApiService> apiProvider) {
    this.apiProvider = apiProvider;
  }

  @Override
  public HomeRepositoryImpl get() {
    return newInstance(apiProvider.get());
  }

  public static HomeRepositoryImpl_Factory create(Provider<OptimeterApiService> apiProvider) {
    return new HomeRepositoryImpl_Factory(apiProvider);
  }

  public static HomeRepositoryImpl newInstance(OptimeterApiService api) {
    return new HomeRepositoryImpl(api);
  }
}
