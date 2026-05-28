package com.project.insole.features.trends.data;

/**
 * Data source for sensor trends and medical summary data.
 * Aggregates data over time periods for analytics.
 */
@kotlin.Metadata(mv = {2, 3, 0}, k = 1, xi = 48, d1 = {"\u00002\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\t\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u0007\n\u0002\b\u0003\u0018\u00002\u00020\u0001B\t\b\u0007\u00a2\u0006\u0004\b\u0002\u0010\u0003J,\u0010\u0004\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00070\u00060\u00052\u0006\u0010\b\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\tH\u0086@\u00a2\u0006\u0004\b\u000b\u0010\fJ\u001e\u0010\r\u001a\b\u0012\u0004\u0012\u00020\u000e0\u00052\u0006\u0010\u000f\u001a\u00020\tH\u0086@\u00a2\u0006\u0004\b\u0010\u0010\u0011J\u0016\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\u00130\u0005H\u0086@\u00a2\u0006\u0004\b\u0014\u0010\u0015\u00a8\u0006\u0016"}, d2 = {"Lcom/project/insole/features/trends/data/TrendsDataSource;", "", "<init>", "()V", "fetchSensorDataByTimeframe", "Lkotlin/Result;", "", "Lcom/project/insole/features/sensor/domain/model/InsoleSensorData;", "startTime", "", "endTime", "fetchSensorDataByTimeframe-0E7RQCE", "(JJLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getDailySummary", "Lcom/project/insole/features/trends/data/DailySummary;", "date", "getDailySummary-gIAlu-s", "(JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getWeeklyHealthScore", "", "getWeeklyHealthScore-IoAF18A", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
public final class TrendsDataSource {
    
    @javax.inject.Inject()
    public TrendsDataSource() {
        super();
    }
}