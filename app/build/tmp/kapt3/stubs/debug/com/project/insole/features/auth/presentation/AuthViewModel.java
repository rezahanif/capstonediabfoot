package com.project.insole.features.auth.presentation;

/**
 * ViewModel for authentication screens.
 * Exposes immutable StateFlow for UI consumption.
 * Only accesses domain use cases - no repository or data layer access.
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000>\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0004\b\u0007\u0018\u00002\u00020\u0001B\u001f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\u0002\u0010\bJ\u0016\u0010\u0010\u001a\u00020\u00112\u0006\u0010\u0012\u001a\u00020\u00132\u0006\u0010\u0014\u001a\u00020\u0013J\u0006\u0010\u0015\u001a\u00020\u0011J\u0016\u0010\u0016\u001a\u00020\u00112\u0006\u0010\u0012\u001a\u00020\u00132\u0006\u0010\u0014\u001a\u00020\u0013R\u0014\u0010\t\u001a\b\u0012\u0004\u0012\u00020\u000b0\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\f\u001a\b\u0012\u0004\u0012\u00020\u000b0\r\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\u000fR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0017"}, d2 = {"Lcom/project/insole/features/auth/presentation/AuthViewModel;", "Landroidx/lifecycle/ViewModel;", "loginUseCase", "Lcom/project/insole/features/auth/domain/LoginUseCase;", "signUpUseCase", "Lcom/project/insole/features/auth/domain/SignUpUseCase;", "logoutUseCase", "Lcom/project/insole/features/auth/domain/LogoutUseCase;", "(Lcom/project/insole/features/auth/domain/LoginUseCase;Lcom/project/insole/features/auth/domain/SignUpUseCase;Lcom/project/insole/features/auth/domain/LogoutUseCase;)V", "_authState", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/project/insole/features/auth/presentation/AuthUiState;", "authState", "Lkotlinx/coroutines/flow/StateFlow;", "getAuthState", "()Lkotlinx/coroutines/flow/StateFlow;", "login", "", "email", "", "password", "logout", "signUp", "app_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class AuthViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.project.insole.features.auth.domain.LoginUseCase loginUseCase = null;
    @org.jetbrains.annotations.NotNull()
    private final com.project.insole.features.auth.domain.SignUpUseCase signUpUseCase = null;
    @org.jetbrains.annotations.NotNull()
    private final com.project.insole.features.auth.domain.LogoutUseCase logoutUseCase = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.project.insole.features.auth.presentation.AuthUiState> _authState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.project.insole.features.auth.presentation.AuthUiState> authState = null;
    
    @javax.inject.Inject()
    public AuthViewModel(@org.jetbrains.annotations.NotNull()
    com.project.insole.features.auth.domain.LoginUseCase loginUseCase, @org.jetbrains.annotations.NotNull()
    com.project.insole.features.auth.domain.SignUpUseCase signUpUseCase, @org.jetbrains.annotations.NotNull()
    com.project.insole.features.auth.domain.LogoutUseCase logoutUseCase) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.project.insole.features.auth.presentation.AuthUiState> getAuthState() {
        return null;
    }
    
    public final void login(@org.jetbrains.annotations.NotNull()
    java.lang.String email, @org.jetbrains.annotations.NotNull()
    java.lang.String password) {
    }
    
    public final void signUp(@org.jetbrains.annotations.NotNull()
    java.lang.String email, @org.jetbrains.annotations.NotNull()
    java.lang.String password) {
    }
    
    public final void logout() {
    }
}