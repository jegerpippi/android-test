/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.test.internal.runner.hidden;

import android.app.Instrumentation;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;

/**
 * This is the ExposedInstrumentationApi class which gets packaged into the aar. The
 * execStartActivity methods will be available through {@link Instrumentation} at runtime and exist
 * in the android.jar on the device.
 *
 * @hide
 */
@RestrictTo(Scope.LIBRARY)
public abstract class ExposedInstrumentationApi extends Instrumentation {}
