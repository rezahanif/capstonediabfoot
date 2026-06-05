package com.project.insole.features.sensor.data.datasource;

import com.project.insole.core.network.SupabaseClient;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class SupabaseDataSource_Factory implements Factory<SupabaseDataSource> {
  private final Provider<SupabaseClient> supabaseClientProvider;

  public SupabaseDataSource_Factory(Provider<SupabaseClient> supabaseClientProvider) {
    this.supabaseClientProvider = supabaseClientProvider;
  }

  @Override
  public SupabaseDataSource get() {
    return newInstance(supabaseClientProvider.get());
  }

  public static SupabaseDataSource_Factory create(Provider<SupabaseClient> supabaseClientProvider) {
    return new SupabaseDataSource_Factory(supabaseClientProvider);
  }

  public static SupabaseDataSource newInstance(SupabaseClient supabaseClient) {
    return new SupabaseDataSource(supabaseClient);
  }
}
