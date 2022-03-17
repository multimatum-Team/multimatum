package com.github.multimatum_team.multimatum

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MultimatumApp: Application() {

    // This class is required by Hilt
    // And code generation seems to be very sensitive to changes in the format of this file
    // tl;dr: BLACK MAGIC, DO NOT TOUCH

}
