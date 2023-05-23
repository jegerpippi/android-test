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

package androidx.test.espresso.device.controller

import androidx.annotation.RestrictTo
import androidx.test.platform.device.DeviceController
import androidx.test.platform.device.UnsupportedDeviceOperationException
import androidx.test.espresso.device.util.getMapOfDeviceStateNamesToIdentifiers
import androidx.test.espresso.device.util.executeShellCommand
import androidx.test.espresso.device.controller.DeviceMode

/**
 * Implementation of {@link DeviceController} for tests run on a physical device.
 *
 * @hide
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
class PhysicalDeviceController : DeviceController {
  override fun setDeviceMode(deviceMode: Int) {
    val deviceIdentifiersMap = getMapOfDeviceStateNamesToIdentifiers()
    if (deviceMode == DeviceMode.FLAT.mode) {
      if (deviceIdentifiersMap.containsKey("OPENED")) {
        setDeviceState(deviceIdentifiersMap.get("OPENED")!!)
      } else {
        throw UnsupportedDeviceOperationException(
          "Flat mode is not supported on this device."
        )
      }
    } else if (deviceMode == DeviceMode.TABLETOP.mode || deviceMode == DeviceMode.BOOK.mode) {
      if (deviceIdentifiersMap.containsKey("HALF_OPENED")) {
        setDeviceState(deviceIdentifiersMap.get("HALF_OPENED")!!)
      } else {
        val deviceModeString = if (deviceMode == DeviceMode.TABLETOP.mode) "Tabletop" else "Book"
        throw UnsupportedDeviceOperationException(
          "${deviceModeString} mode is not supported on this device."
        )
      }
    } else if (deviceMode == DeviceMode.CLOSED.mode) {
      if (deviceIdentifiersMap.containsKey("CLOSED")) {
        setDeviceState(deviceIdentifiersMap.get("CLOSED")!!)
      } else {
        throw UnsupportedDeviceOperationException(
          "Closed mode is not supported on this device."
        )
      }
    } else {
      throw UnsupportedDeviceOperationException(
          "The requested device mode is not supported."
        )
    }
  }

  override fun setScreenOrientation(screenOrientation: Int) {
    // TODO(b/203092519) Investigate supporting screen orientation rotation on real devices.
    throw UnsupportedDeviceOperationException(
      "Setting screen orientation is not supported on physical devices."
    )
  }
    
  private fun setDeviceState(deviceIdentifier: String) {
    executeShellCommand("cmd device_state state ${deviceIdentifier}")
  }
}
