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
public final class ReadingRepositoryImpl_Factory implements Factory<ReadingRepositoryImpl> {
  private final Provider<OptimeterApiService> apiProvider;

  public ReadingRepositoryImpl_Factory(Provider<OptimeterApiService> apiProvider) {
    this.apiProvider = apiProvider;
  }

  @Override
  public ReadingRepositoryImpl get() {
    return newInstance(apiProvider.get());
  }

  public static ReadingRepositoryImpl_Factory create(Provider<OptimeterApiService> apiProvider) {
    return new ReadingRepositoryImpl_Factory(apiProvider);
  }

  public static ReadingRepositoryImpl newInstance(OptimeterApiService api) {
    return new ReadingRepositoryImpl(api);
  }
}
