package com.optimeter.app.di;

import com.optimeter.app.data.remote.api.OptimeterApiService;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import retrofit2.Retrofit;

@ScopeMetadata("javax.inject.Singleton")
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
public final class AppModule_ProvideOptimeterApiServiceFactory implements Factory<OptimeterApiService> {
  private final Provider<Retrofit> retrofitProvider;

  public AppModule_ProvideOptimeterApiServiceFactory(Provider<Retrofit> retrofitProvider) {
    this.retrofitProvider = retrofitProvider;
  }

  @Override
  public OptimeterApiService get() {
    return provideOptimeterApiService(retrofitProvider.get());
  }

  public static AppModule_ProvideOptimeterApiServiceFactory create(
      Provider<Retrofit> retrofitProvider) {
    return new AppModule_ProvideOptimeterApiServiceFactory(retrofitProvider);
  }

  public static OptimeterApiService provideOptimeterApiService(Retrofit retrofit) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideOptimeterApiService(retrofit));
  }
}
