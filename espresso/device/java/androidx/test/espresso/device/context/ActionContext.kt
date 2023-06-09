/*
 * Copyright (C) 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.test.espresso.device.context

import android.content.Context
import androidx.annotation.RestrictTo
import androidx.annotation.RestrictTo.Scope

/**
 * Interface to provide context to device actions.
 *
 * @hide
 */
@RestrictTo(Scope.LIBRARY)
interface ActionContext {
  /** Get the application context. */
  val applicationContext: Context

  /**
   * Get the test app's context, e.g. the Instrumentation test app's context when running on an
   * emulator.
   */
  val testContext: Context
}
