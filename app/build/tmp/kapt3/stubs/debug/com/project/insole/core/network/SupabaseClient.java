package com.project.insole.core.network;

/**
 * Supabase Client wrapper.
 */
@javax.inject.Singleton()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00000\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\b\u0007\u0018\u00002\u00020\u0001B\u0007\b\u0007\u00a2\u0006\u0002\u0010\u0002J\u0012\u0010\b\u001a\u00020\t2\n\u0010\n\u001a\u00060\u000bj\u0002`\fJ\u0016\u0010\r\u001a\u00020\u000e2\u0006\u0010\u0007\u001a\u00020\u00042\u0006\u0010\u0003\u001a\u00020\u0004R\u0010\u0010\u0003\u001a\u0004\u0018\u00010\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0007\u001a\u0004\u0018\u00010\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u000f"}, d2 = {"Lcom/project/insole/core/network/SupabaseClient;", "", "()V", "anonKey", "", "isInitialized", "", "projectUrl", "handleNetworkError", "Lcom/project/insole/core/network/NetworkException;", "error", "Ljava/lang/Exception;", "Lkotlin/Exception;", "initialize", "", "app_debug"})
public final class SupabaseClient {
    private boolean isInitialized = false;
    @org.jetbrains.annotations.Nullable()
    private java.lang.String projectUrl;
    @org.jetbrains.annotations.Nullable()
    private java.lang.String anonKey;
    
    @javax.inject.Inject()
    public SupabaseClient() {
        super();
    }
    
    /**
     * Initialize Supabase client with project credentials.
     */
    public final void initialize(@org.jetbrains.annotations.NotNull()
    java.lang.String projectUrl, @org.jetbrains.annotations.NotNull()
    java.lang.String anonKey) {
    }
    
    /**
     * Handles generic network errors that may occur during API calls.
     */
    @org.jetbrains.annotations.NotNull()
    public final com.project.insole.core.network.NetworkException handleNetworkError(@org.jetbrains.annotations.NotNull()
    java.lang.Exception error) {
        return null;
    }
}