package com.project.insole;

import android.app.Activity;
import android.app.Service;
import android.view.View;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import com.project.insole.core.ble.BleConnectionManager;
import com.project.insole.core.ble.BleViewModel;
import com.project.insole.core.ble.BleViewModel_HiltModules;
import com.project.insole.core.ble.InsoleBleManager;
import com.project.insole.core.di.AppModule_ProvideBleConnectionManagerFactory;
import com.project.insole.core.di.AppModule_ProvideBleManagerFactory;
import com.project.insole.core.network.SupabaseClient;
import com.project.insole.features.auth.domain.LoginUseCase;
import com.project.insole.features.auth.domain.LogoutUseCase;
import com.project.insole.features.auth.domain.SignUpUseCase;
import com.project.insole.features.auth.presentation.AuthViewModel;
import com.project.insole.features.auth.presentation.AuthViewModel_HiltModules;
import com.project.insole.features.diagnostics.data.DiagnosticsDataSource;
import com.project.insole.features.diagnostics.domain.GenerateDiagnosticReportUseCase;
import com.project.insole.features.diagnostics.presentation.DiagnosticsViewModel;
import com.project.insole.features.diagnostics.presentation.DiagnosticsViewModel_HiltModules;
import com.project.insole.features.notifications.data.NotificationDataSource;
import com.project.insole.features.notifications.presentation.NotificationsViewModel;
import com.project.insole.features.notifications.presentation.NotificationsViewModel_HiltModules;
import com.project.insole.features.sensor.data.datasource.BleSensorDataSource;
import com.project.insole.features.sensor.data.datasource.SupabaseDataSource;
import com.project.insole.features.sensor.data.repository.SensorRepositoryImpl;
import com.project.insole.features.sensor.domain.repository.SensorRepository;
import com.project.insole.features.sensor.domain.service.StepCounterService;
import com.project.insole.features.sensor.domain.usecase.AnalyzePressureThresholdUseCase;
import com.project.insole.features.sensor.domain.usecase.MapPressureToGridUseCase;
import com.project.insole.features.sensor.domain.usecase.ProcessStepCountUseCase;
import com.project.insole.features.sensor.presentation.SensorViewModel;
import com.project.insole.features.sensor.presentation.SensorViewModel_HiltModules;
import com.project.insole.features.settings.data.SettingsDataSource;
import com.project.insole.features.settings.presentation.SettingsViewModel;
import com.project.insole.features.settings.presentation.SettingsViewModel_HiltModules;
import com.project.insole.features.trends.data.TrendsDataSource;
import com.project.insole.features.trends.presentation.TrendsViewModel;
import com.project.insole.features.trends.presentation.TrendsViewModel_HiltModules;
import dagger.hilt.android.ActivityRetainedLifecycle;
import dagger.hilt.android.ViewModelLifecycle;
import dagger.hilt.android.internal.builders.ActivityComponentBuilder;
import dagger.hilt.android.internal.builders.ActivityRetainedComponentBuilder;
import dagger.hilt.android.internal.builders.FragmentComponentBuilder;
import dagger.hilt.android.internal.builders.ServiceComponentBuilder;
import dagger.hilt.android.internal.builders.ViewComponentBuilder;
import dagger.hilt.android.internal.builders.ViewModelComponentBuilder;
import dagger.hilt.android.internal.builders.ViewWithFragmentComponentBuilder;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories_InternalFactoryFactory_Factory;
import dagger.hilt.android.internal.managers.ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory;
import dagger.hilt.android.internal.managers.SavedStateHandleHolder;
import dagger.hilt.android.internal.modules.ApplicationContextModule;
import dagger.hilt.android.internal.modules.ApplicationContextModule_ProvideContextFactory;
import dagger.internal.DaggerGenerated;
import dagger.internal.DoubleCheck;
import dagger.internal.IdentifierNameString;
import dagger.internal.KeepFieldType;
import dagger.internal.LazyClassKeyMap;
import dagger.internal.MapBuilder;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

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
public final class DaggerInsoleApp_HiltComponents_SingletonC {
  private DaggerInsoleApp_HiltComponents_SingletonC() {
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private ApplicationContextModule applicationContextModule;

    private Builder() {
    }

    public Builder applicationContextModule(ApplicationContextModule applicationContextModule) {
      this.applicationContextModule = Preconditions.checkNotNull(applicationContextModule);
      return this;
    }

    public InsoleApp_HiltComponents.SingletonC build() {
      Preconditions.checkBuilderRequirement(applicationContextModule, ApplicationContextModule.class);
      return new SingletonCImpl(applicationContextModule);
    }
  }

  private static final class ActivityRetainedCBuilder implements InsoleApp_HiltComponents.ActivityRetainedC.Builder {
    private final SingletonCImpl singletonCImpl;

    private SavedStateHandleHolder savedStateHandleHolder;

    private ActivityRetainedCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ActivityRetainedCBuilder savedStateHandleHolder(
        SavedStateHandleHolder savedStateHandleHolder) {
      this.savedStateHandleHolder = Preconditions.checkNotNull(savedStateHandleHolder);
      return this;
    }

    @Override
    public InsoleApp_HiltComponents.ActivityRetainedC build() {
      Preconditions.checkBuilderRequirement(savedStateHandleHolder, SavedStateHandleHolder.class);
      return new ActivityRetainedCImpl(singletonCImpl, savedStateHandleHolder);
    }
  }

  private static final class ActivityCBuilder implements InsoleApp_HiltComponents.ActivityC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private Activity activity;

    private ActivityCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ActivityCBuilder activity(Activity activity) {
      this.activity = Preconditions.checkNotNull(activity);
      return this;
    }

    @Override
    public InsoleApp_HiltComponents.ActivityC build() {
      Preconditions.checkBuilderRequirement(activity, Activity.class);
      return new ActivityCImpl(singletonCImpl, activityRetainedCImpl, activity);
    }
  }

  private static final class FragmentCBuilder implements InsoleApp_HiltComponents.FragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private Fragment fragment;

    private FragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public FragmentCBuilder fragment(Fragment fragment) {
      this.fragment = Preconditions.checkNotNull(fragment);
      return this;
    }

    @Override
    public InsoleApp_HiltComponents.FragmentC build() {
      Preconditions.checkBuilderRequirement(fragment, Fragment.class);
      return new FragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragment);
    }
  }

  private static final class ViewWithFragmentCBuilder implements InsoleApp_HiltComponents.ViewWithFragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private View view;

    private ViewWithFragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;
    }

    @Override
    public ViewWithFragmentCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public InsoleApp_HiltComponents.ViewWithFragmentC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewWithFragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl, view);
    }
  }

  private static final class ViewCBuilder implements InsoleApp_HiltComponents.ViewC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private View view;

    private ViewCBuilder(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public ViewCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public InsoleApp_HiltComponents.ViewC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, view);
    }
  }

  private static final class ViewModelCBuilder implements InsoleApp_HiltComponents.ViewModelC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private SavedStateHandle savedStateHandle;

    private ViewModelLifecycle viewModelLifecycle;

    private ViewModelCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ViewModelCBuilder savedStateHandle(SavedStateHandle handle) {
      this.savedStateHandle = Preconditions.checkNotNull(handle);
      return this;
    }

    @Override
    public ViewModelCBuilder viewModelLifecycle(ViewModelLifecycle viewModelLifecycle) {
      this.viewModelLifecycle = Preconditions.checkNotNull(viewModelLifecycle);
      return this;
    }

    @Override
    public InsoleApp_HiltComponents.ViewModelC build() {
      Preconditions.checkBuilderRequirement(savedStateHandle, SavedStateHandle.class);
      Preconditions.checkBuilderRequirement(viewModelLifecycle, ViewModelLifecycle.class);
      return new ViewModelCImpl(singletonCImpl, activityRetainedCImpl, savedStateHandle, viewModelLifecycle);
    }
  }

  private static final class ServiceCBuilder implements InsoleApp_HiltComponents.ServiceC.Builder {
    private final SingletonCImpl singletonCImpl;

    private Service service;

    private ServiceCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ServiceCBuilder service(Service service) {
      this.service = Preconditions.checkNotNull(service);
      return this;
    }

    @Override
    public InsoleApp_HiltComponents.ServiceC build() {
      Preconditions.checkBuilderRequirement(service, Service.class);
      return new ServiceCImpl(singletonCImpl, service);
    }
  }

  private static final class ViewWithFragmentCImpl extends InsoleApp_HiltComponents.ViewWithFragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private final ViewWithFragmentCImpl viewWithFragmentCImpl = this;

    private ViewWithFragmentCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;


    }
  }

  private static final class FragmentCImpl extends InsoleApp_HiltComponents.FragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl = this;

    private FragmentCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        Fragment fragmentParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return activityCImpl.getHiltInternalFactoryFactory();
    }

    @Override
    public ViewWithFragmentComponentBuilder viewWithFragmentComponentBuilder() {
      return new ViewWithFragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl);
    }
  }

  private static final class ViewCImpl extends InsoleApp_HiltComponents.ViewC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final ViewCImpl viewCImpl = this;

    private ViewCImpl(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }
  }

  private static final class ActivityCImpl extends InsoleApp_HiltComponents.ActivityC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl = this;

    private ActivityCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, Activity activityParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;


    }

    @Override
    public void injectMainActivity(MainActivity arg0) {
      injectMainActivity2(arg0);
    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return DefaultViewModelFactories_InternalFactoryFactory_Factory.newInstance(getViewModelKeys(), new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl));
    }

    @Override
    public Map<Class<?>, Boolean> getViewModelKeys() {
      return LazyClassKeyMap.<Boolean>of(MapBuilder.<String, Boolean>newMapBuilder(7).put(LazyClassKeyProvider.com_project_insole_features_auth_presentation_AuthViewModel, AuthViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_project_insole_core_ble_BleViewModel, BleViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_project_insole_features_diagnostics_presentation_DiagnosticsViewModel, DiagnosticsViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_project_insole_features_notifications_presentation_NotificationsViewModel, NotificationsViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_project_insole_features_sensor_presentation_SensorViewModel, SensorViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_project_insole_features_settings_presentation_SettingsViewModel, SettingsViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_project_insole_features_trends_presentation_TrendsViewModel, TrendsViewModel_HiltModules.KeyModule.provide()).build());
    }

    @Override
    public ViewModelComponentBuilder getViewModelComponentBuilder() {
      return new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public FragmentComponentBuilder fragmentComponentBuilder() {
      return new FragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }

    @Override
    public ViewComponentBuilder viewComponentBuilder() {
      return new ViewCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }

    private MainActivity injectMainActivity2(MainActivity instance) {
      MainActivity_MembersInjector.injectBleConnectionManager(instance, singletonCImpl.provideBleConnectionManagerProvider.get());
      return instance;
    }

    @IdentifierNameString
    private static final class LazyClassKeyProvider {
      static String com_project_insole_core_ble_BleViewModel = "com.project.insole.core.ble.BleViewModel";

      static String com_project_insole_features_notifications_presentation_NotificationsViewModel = "com.project.insole.features.notifications.presentation.NotificationsViewModel";

      static String com_project_insole_features_settings_presentation_SettingsViewModel = "com.project.insole.features.settings.presentation.SettingsViewModel";

      static String com_project_insole_features_sensor_presentation_SensorViewModel = "com.project.insole.features.sensor.presentation.SensorViewModel";

      static String com_project_insole_features_diagnostics_presentation_DiagnosticsViewModel = "com.project.insole.features.diagnostics.presentation.DiagnosticsViewModel";

      static String com_project_insole_features_auth_presentation_AuthViewModel = "com.project.insole.features.auth.presentation.AuthViewModel";

      static String com_project_insole_features_trends_presentation_TrendsViewModel = "com.project.insole.features.trends.presentation.TrendsViewModel";

      @KeepFieldType
      BleViewModel com_project_insole_core_ble_BleViewModel2;

      @KeepFieldType
      NotificationsViewModel com_project_insole_features_notifications_presentation_NotificationsViewModel2;

      @KeepFieldType
      SettingsViewModel com_project_insole_features_settings_presentation_SettingsViewModel2;

      @KeepFieldType
      SensorViewModel com_project_insole_features_sensor_presentation_SensorViewModel2;

      @KeepFieldType
      DiagnosticsViewModel com_project_insole_features_diagnostics_presentation_DiagnosticsViewModel2;

      @KeepFieldType
      AuthViewModel com_project_insole_features_auth_presentation_AuthViewModel2;

      @KeepFieldType
      TrendsViewModel com_project_insole_features_trends_presentation_TrendsViewModel2;
    }
  }

  private static final class ViewModelCImpl extends InsoleApp_HiltComponents.ViewModelC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ViewModelCImpl viewModelCImpl = this;

    private Provider<AuthViewModel> authViewModelProvider;

    private Provider<BleViewModel> bleViewModelProvider;

    private Provider<DiagnosticsViewModel> diagnosticsViewModelProvider;

    private Provider<NotificationsViewModel> notificationsViewModelProvider;

    private Provider<SensorViewModel> sensorViewModelProvider;

    private Provider<SettingsViewModel> settingsViewModelProvider;

    private Provider<TrendsViewModel> trendsViewModelProvider;

    private ViewModelCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, SavedStateHandle savedStateHandleParam,
        ViewModelLifecycle viewModelLifecycleParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;

      initialize(savedStateHandleParam, viewModelLifecycleParam);

    }

    private ProcessStepCountUseCase processStepCountUseCase() {
      return new ProcessStepCountUseCase(singletonCImpl.stepCounterServiceProvider.get());
    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandle savedStateHandleParam,
        final ViewModelLifecycle viewModelLifecycleParam) {
      this.authViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 0);
      this.bleViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 1);
      this.diagnosticsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 2);
      this.notificationsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 3);
      this.sensorViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 4);
      this.settingsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 5);
      this.trendsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 6);
    }

    @Override
    public Map<Class<?>, javax.inject.Provider<ViewModel>> getHiltViewModelMap() {
      return LazyClassKeyMap.<javax.inject.Provider<ViewModel>>of(MapBuilder.<String, javax.inject.Provider<ViewModel>>newMapBuilder(7).put(LazyClassKeyProvider.com_project_insole_features_auth_presentation_AuthViewModel, ((Provider) authViewModelProvider)).put(LazyClassKeyProvider.com_project_insole_core_ble_BleViewModel, ((Provider) bleViewModelProvider)).put(LazyClassKeyProvider.com_project_insole_features_diagnostics_presentation_DiagnosticsViewModel, ((Provider) diagnosticsViewModelProvider)).put(LazyClassKeyProvider.com_project_insole_features_notifications_presentation_NotificationsViewModel, ((Provider) notificationsViewModelProvider)).put(LazyClassKeyProvider.com_project_insole_features_sensor_presentation_SensorViewModel, ((Provider) sensorViewModelProvider)).put(LazyClassKeyProvider.com_project_insole_features_settings_presentation_SettingsViewModel, ((Provider) settingsViewModelProvider)).put(LazyClassKeyProvider.com_project_insole_features_trends_presentation_TrendsViewModel, ((Provider) trendsViewModelProvider)).build());
    }

    @Override
    public Map<Class<?>, Object> getHiltViewModelAssistedMap() {
      return Collections.<Class<?>, Object>emptyMap();
    }

    @IdentifierNameString
    private static final class LazyClassKeyProvider {
      static String com_project_insole_core_ble_BleViewModel = "com.project.insole.core.ble.BleViewModel";

      static String com_project_insole_features_diagnostics_presentation_DiagnosticsViewModel = "com.project.insole.features.diagnostics.presentation.DiagnosticsViewModel";

      static String com_project_insole_features_trends_presentation_TrendsViewModel = "com.project.insole.features.trends.presentation.TrendsViewModel";

      static String com_project_insole_features_notifications_presentation_NotificationsViewModel = "com.project.insole.features.notifications.presentation.NotificationsViewModel";

      static String com_project_insole_features_auth_presentation_AuthViewModel = "com.project.insole.features.auth.presentation.AuthViewModel";

      static String com_project_insole_features_sensor_presentation_SensorViewModel = "com.project.insole.features.sensor.presentation.SensorViewModel";

      static String com_project_insole_features_settings_presentation_SettingsViewModel = "com.project.insole.features.settings.presentation.SettingsViewModel";

      @KeepFieldType
      BleViewModel com_project_insole_core_ble_BleViewModel2;

      @KeepFieldType
      DiagnosticsViewModel com_project_insole_features_diagnostics_presentation_DiagnosticsViewModel2;

      @KeepFieldType
      TrendsViewModel com_project_insole_features_trends_presentation_TrendsViewModel2;

      @KeepFieldType
      NotificationsViewModel com_project_insole_features_notifications_presentation_NotificationsViewModel2;

      @KeepFieldType
      AuthViewModel com_project_insole_features_auth_presentation_AuthViewModel2;

      @KeepFieldType
      SensorViewModel com_project_insole_features_sensor_presentation_SensorViewModel2;

      @KeepFieldType
      SettingsViewModel com_project_insole_features_settings_presentation_SettingsViewModel2;
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final ViewModelCImpl viewModelCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          ViewModelCImpl viewModelCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.viewModelCImpl = viewModelCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // com.project.insole.features.auth.presentation.AuthViewModel 
          return (T) new AuthViewModel(new LoginUseCase(), new SignUpUseCase(), new LogoutUseCase());

          case 1: // com.project.insole.core.ble.BleViewModel 
          return (T) new BleViewModel(singletonCImpl.provideBleManagerProvider.get(), singletonCImpl.stepCounterServiceProvider.get());

          case 2: // com.project.insole.features.diagnostics.presentation.DiagnosticsViewModel 
          return (T) new DiagnosticsViewModel(new DiagnosticsDataSource(), new GenerateDiagnosticReportUseCase());

          case 3: // com.project.insole.features.notifications.presentation.NotificationsViewModel 
          return (T) new NotificationsViewModel(new NotificationDataSource());

          case 4: // com.project.insole.features.sensor.presentation.SensorViewModel 
          return (T) new SensorViewModel(singletonCImpl.bindSensorRepositoryProvider.get(), new MapPressureToGridUseCase(), viewModelCImpl.processStepCountUseCase(), new AnalyzePressureThresholdUseCase());

          case 5: // com.project.insole.features.settings.presentation.SettingsViewModel 
          return (T) new SettingsViewModel(new SettingsDataSource());

          case 6: // com.project.insole.features.trends.presentation.TrendsViewModel 
          return (T) new TrendsViewModel(new TrendsDataSource());

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ActivityRetainedCImpl extends InsoleApp_HiltComponents.ActivityRetainedC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl = this;

    private Provider<ActivityRetainedLifecycle> provideActivityRetainedLifecycleProvider;

    private ActivityRetainedCImpl(SingletonCImpl singletonCImpl,
        SavedStateHandleHolder savedStateHandleHolderParam) {
      this.singletonCImpl = singletonCImpl;

      initialize(savedStateHandleHolderParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandleHolder savedStateHandleHolderParam) {
      this.provideActivityRetainedLifecycleProvider = DoubleCheck.provider(new SwitchingProvider<ActivityRetainedLifecycle>(singletonCImpl, activityRetainedCImpl, 0));
    }

    @Override
    public ActivityComponentBuilder activityComponentBuilder() {
      return new ActivityCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public ActivityRetainedLifecycle getActivityRetainedLifecycle() {
      return provideActivityRetainedLifecycleProvider.get();
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // dagger.hilt.android.ActivityRetainedLifecycle 
          return (T) ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory.provideActivityRetainedLifecycle();

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ServiceCImpl extends InsoleApp_HiltComponents.ServiceC {
    private final SingletonCImpl singletonCImpl;

    private final ServiceCImpl serviceCImpl = this;

    private ServiceCImpl(SingletonCImpl singletonCImpl, Service serviceParam) {
      this.singletonCImpl = singletonCImpl;


    }
  }

  private static final class SingletonCImpl extends InsoleApp_HiltComponents.SingletonC {
    private final ApplicationContextModule applicationContextModule;

    private final SingletonCImpl singletonCImpl = this;

    private Provider<InsoleBleManager> provideBleManagerProvider;

    private Provider<BleConnectionManager> provideBleConnectionManagerProvider;

    private Provider<StepCounterService> stepCounterServiceProvider;

    private Provider<BleSensorDataSource> bleSensorDataSourceProvider;

    private Provider<SupabaseClient> supabaseClientProvider;

    private Provider<SupabaseDataSource> supabaseDataSourceProvider;

    private Provider<SensorRepositoryImpl> sensorRepositoryImplProvider;

    private Provider<SensorRepository> bindSensorRepositoryProvider;

    private SingletonCImpl(ApplicationContextModule applicationContextModuleParam) {
      this.applicationContextModule = applicationContextModuleParam;
      initialize(applicationContextModuleParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final ApplicationContextModule applicationContextModuleParam) {
      this.provideBleManagerProvider = DoubleCheck.provider(new SwitchingProvider<InsoleBleManager>(singletonCImpl, 1));
      this.provideBleConnectionManagerProvider = DoubleCheck.provider(new SwitchingProvider<BleConnectionManager>(singletonCImpl, 0));
      this.stepCounterServiceProvider = DoubleCheck.provider(new SwitchingProvider<StepCounterService>(singletonCImpl, 2));
      this.bleSensorDataSourceProvider = DoubleCheck.provider(new SwitchingProvider<BleSensorDataSource>(singletonCImpl, 4));
      this.supabaseClientProvider = DoubleCheck.provider(new SwitchingProvider<SupabaseClient>(singletonCImpl, 6));
      this.supabaseDataSourceProvider = DoubleCheck.provider(new SwitchingProvider<SupabaseDataSource>(singletonCImpl, 5));
      this.sensorRepositoryImplProvider = new SwitchingProvider<>(singletonCImpl, 3);
      this.bindSensorRepositoryProvider = DoubleCheck.provider((Provider) sensorRepositoryImplProvider);
    }

    @Override
    public void injectInsoleApp(InsoleApp insoleApp) {
    }

    @Override
    public Set<Boolean> getDisableFragmentGetContextFix() {
      return Collections.<Boolean>emptySet();
    }

    @Override
    public ActivityRetainedComponentBuilder retainedComponentBuilder() {
      return new ActivityRetainedCBuilder(singletonCImpl);
    }

    @Override
    public ServiceComponentBuilder serviceComponentBuilder() {
      return new ServiceCBuilder(singletonCImpl);
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // com.project.insole.core.ble.BleConnectionManager 
          return (T) AppModule_ProvideBleConnectionManagerFactory.provideBleConnectionManager(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), singletonCImpl.provideBleManagerProvider.get());

          case 1: // com.project.insole.core.ble.InsoleBleManager 
          return (T) AppModule_ProvideBleManagerFactory.provideBleManager(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 2: // com.project.insole.features.sensor.domain.service.StepCounterService 
          return (T) new StepCounterService();

          case 3: // com.project.insole.features.sensor.data.repository.SensorRepositoryImpl 
          return (T) new SensorRepositoryImpl(singletonCImpl.bleSensorDataSourceProvider.get(), singletonCImpl.supabaseDataSourceProvider.get(), singletonCImpl.provideBleManagerProvider.get());

          case 4: // com.project.insole.features.sensor.data.datasource.BleSensorDataSource 
          return (T) new BleSensorDataSource(singletonCImpl.provideBleManagerProvider.get());

          case 5: // com.project.insole.features.sensor.data.datasource.SupabaseDataSource 
          return (T) new SupabaseDataSource(singletonCImpl.supabaseClientProvider.get());

          case 6: // com.project.insole.core.network.SupabaseClient 
          return (T) new SupabaseClient();

          default: throw new AssertionError(id);
        }
      }
    }
  }
}
