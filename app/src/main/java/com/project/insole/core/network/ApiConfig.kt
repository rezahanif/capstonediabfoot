package com.project.insole.core.network

import com.project.insole.BuildConfig

/**
 * Global API configuration.
 *
 * The base URL is injected at build time from Gradle (`BuildConfig.API_BASE_URL`):
 * `-Pdiabfoot.apiBaseUrl=...`, the `DIABFOOT_API_BASE_URL` env var, or
 * `local.properties` — see `app/build.gradle.kts`. Never edit a literal here to
 * point at another environment.
 */
object ApiConfig {
    /**
     * Primary base URL for the Insole Cloud API.
     * Production is reached through the Cloudflare Tunnel over public HTTPS.
     */
    val baseUrl: String = BuildConfig.API_BASE_URL.trimEnd('/')

    /**
     * Local development fallback (ADB reverse over USB cable: `adb reverse tcp:8080 tcp:8080`).
     * Only usable in debug builds; release builds never probe localhost.
     */
    const val LOCAL_FALLBACK_URL: String = "http://localhost:8080"

    /** Localhost fallback and cleartext dev hosts are debug-only. */
    val localFallbackEnabled: Boolean get() = BuildConfig.DEBUG
}
