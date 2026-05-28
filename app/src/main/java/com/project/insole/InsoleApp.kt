package com.project.insole

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Base Application class for the project.
 * Annotated with @HiltAndroidApp to trigger Hilt's code generation.
 */
@HiltAndroidApp
class InsoleApp : Application()
