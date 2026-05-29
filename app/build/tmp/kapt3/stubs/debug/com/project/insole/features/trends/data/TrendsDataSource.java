package com.project.insole.features.trends.data;

/**
 * Data source for sensor trends and medical summary data.
 * Aggregates data over time periods for analytics.
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00002\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\t\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u0007\n\u0002\b\u0003\b\u0007\u0018\u00002\u00020\u0001B\u0007\b\u0007\u00a2\u0006\u0002\u0010\u0002J2\u0010\u0003\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00060\u00050\u00042\u0006\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\bH\u0086@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b\n\u0010\u000bJ$\u0010\f\u001a\b\u0012\u0004\u0012\u00020\r0\u00042\u0006\u0010\u000e\u001a\u00020\bH\u0086@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b\u000f\u0010\u0010J\u001c\u0010\u0011\u001a\b\u0012\u0004\u0012\u00020\u00120\u0004H\u0086@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b\u0013\u0010\u0014\u0082\u0002\u000b\n\u0002\b!\n\u0005\b\u00a1\u001e0\u0001\u00a8\u0006\u0015"}, d2 = {"Lcom/project/insole/features/trends/data/TrendsDataSource;", "", "()V", "fetchSensorDataByTimeframe", "Lkotlin/Result;", "", "Lcom/project/insole/features/sensor/domain/model/InsoleSensorData;", "startTime", "", "endTime", "fetchSensorDataByTimeframe-0E7RQCE", "(JJLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getDailySummary", "Lcom/project/insole/features/trends/data/DailySummary;", "date", "getDailySummary-gIAlu-s", "(JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getWeeklyHealthScore", "", "getWeeklyHealthScore-IoAF18A", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
public final class TrendsDataSource {
    
    @javax.inject.Inject()
    public TrendsDataSource() {
        super();
    }
}