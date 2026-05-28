package com.project.insole.features.trends.presentation;

/**
 * ViewModel for trends/analytics screen.
 * Shows medical summary by timeframe (daily, weekly, monthly).
 */
@kotlin.Metadata(mv = {2, 3, 0}, k = 1, xi = 48, d1 = {"\u00002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0010\t\n\u0000\b\u0007\u0018\u00002\u00020\u0001B\u0011\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0004\b\u0004\u0010\u0005J\u0006\u0010\r\u001a\u00020\u000eJ\u000e\u0010\u000f\u001a\u00020\u000e2\u0006\u0010\u0010\u001a\u00020\u0011R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\b0\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\t\u001a\b\u0012\u0004\u0012\u00020\b0\n\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\f\u00a8\u0006\u0012"}, d2 = {"Lcom/project/insole/features/trends/presentation/TrendsViewModel;", "Landroidx/lifecycle/ViewModel;", "trendsDataSource", "Lcom/project/insole/features/trends/data/TrendsDataSource;", "<init>", "(Lcom/project/insole/features/trends/data/TrendsDataSource;)V", "_trendsState", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/project/insole/features/trends/presentation/TrendsUiState;", "trendsState", "Lkotlinx/coroutines/flow/StateFlow;", "getTrendsState", "()Lkotlinx/coroutines/flow/StateFlow;", "loadTrends", "", "selectDate", "dateMs", "", "app_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class TrendsViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.project.insole.features.trends.data.TrendsDataSource trendsDataSource = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.project.insole.features.trends.presentation.TrendsUiState> _trendsState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.project.insole.features.trends.presentation.TrendsUiState> trendsState = null;
    
    @javax.inject.Inject()
    public TrendsViewModel(@org.jetbrains.annotations.NotNull()
    com.project.insole.features.trends.data.TrendsDataSource trendsDataSource) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.project.insole.features.trends.presentation.TrendsUiState> getTrendsState() {
        return null;
    }
    
    public final void loadTrends() {
    }
    
    public final void selectDate(long dateMs) {
    }
}