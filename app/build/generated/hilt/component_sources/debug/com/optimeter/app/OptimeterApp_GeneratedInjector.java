package com.optimeter.app;

import dagger.hilt.InstallIn;
import dagger.hilt.codegen.OriginatingElement;
import dagger.hilt.components.SingletonComponent;
import dagger.hilt.internal.GeneratedEntryPoint;

@OriginatingElement(
    topLevelClass = OptimeterApp.class
)
@GeneratedEntryPoint
@InstallIn(SingletonComponent.class)
public interface OptimeterApp_GeneratedInjector {
  void injectOptimeterApp(OptimeterApp optimeterApp);
}
