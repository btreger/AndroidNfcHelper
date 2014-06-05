package eu.treger.android.nfchelper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.annotation.SuppressLint;
import android.content.Context;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.os.Build;
import android.util.Log;

/**
 * Copyright 2014 Bastian Treger
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.* You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * 
 * 
 * With the help of AndroidNfcHepler you can check current Near field communication (NFC) state.
 * In detail you can check:
 * 
 * - if the device is Near Field Communication capable / has NFC hardware
 * - if NFC is enabled 
 * - if Android Beam (p2p protocol) is enabled
 * 
 * NFC is badly supported since Android 2.3.0 (API 9). The APIs was improved in Android 2.3.3 (API 10).
 * Therefore AndroidNfcHepler infers at least API 10 for proper NFC usage.
 * 
 * @author btreger
 */
public class AndroidNfcHelper {
	private static final String TAG = AndroidNfcHelper.class.getSimpleName();
	private static final int MIN_SDK_INT_FOR_NFC = Build.VERSION_CODES.GINGERBREAD_MR1;

	/**
	 * Return the NfcAdapter object or null. All devices without a hardware NFC module
	 * will return null. All devices with Android 2.3.0 (API 9) or lower will return null,
	 * caused by missing API support. 
	 * 
	 * @param context Android Context
	 * @return NfcAdapter object or null
	 */
	@SuppressLint("NewApi")
	public static NfcAdapter getNfcAdapter(Context context){
		if (Build.VERSION.SDK_INT < MIN_SDK_INT_FOR_NFC) {
			return null;
		} else {
			final NfcManager manager = (NfcManager) context.getSystemService(Context.NFC_SERVICE);
			final NfcAdapter adapter = manager.getDefaultAdapter();
			return adapter;
		}		
	}

	/** 
	 * Returns true is an NFC module is integrated into the devices.
	 * Attention! For devices with NFC but with an Android version below API 10
	 * false will be return. Caused by a missing NFC API below API 10.
	 * 
	 * @param context Android Context
	 * @return true if device is NFC capable
	 * 
	 * @see #getNfcAdapter(Context)
	 */
	@SuppressLint("NewApi")
	public static boolean isNfcCapable(Context context) {
		// all devices before API 10 are not NFC capable because of missing API support.
		if (Build.VERSION.SDK_INT < MIN_SDK_INT_FOR_NFC) {
			return false;
		} else {
			return (getNfcAdapter(context) != null);
		}
	}

	/**
	 * Returns true is the NFC module is enables / switched on.
	 * Attention! For devices with enabled NFC but with an Android version below API 10
	 * false will be return. Caused by a missing NFC API below API 10.
	 * 
	 * @param context Android Context
	 * @return true if NFC is enabled
	 * 
	 * @see http://developer.android.com/reference/android/nfc/NfcAdapter.html#isEnabled%28%29
	 * @see #getNfcAdapter(Context)
	 */
	@SuppressLint("NewApi")
	public static boolean isNfcEnabled(Context context) {
		// all devices before API 10 are not NFC capable because of missing API support.
		// Therefore is nfc at all not enabled on these devices
		if (Build.VERSION.SDK_INT < MIN_SDK_INT_FOR_NFC) {
			return false;
		} else {
			final NfcAdapter adapter = getNfcAdapter(context);
			return (adapter != null && adapter.isEnabled());
		}
	}

	/**
	 * Returns true if Android Beam is enabled. 
	 * Beam is a high level protocol for peer to peer (p2p) connections via NFC. Android Beam is available
	 * since Android 4.0 (API 14). The android method isNdefPushEnabled is hidden in API 14 and 15, so 
	 * we have to use reflection .
	 * 
	 * @param context Android Context
	 * @return true if Android Beam is enabled
	 * 
	 * @see http://developer.android.com/reference/android/nfc/NfcAdapter.html#isNdefPushEnabled%28%29
	 * @see #getNfcAdapter(Context)
	 * @see #isNfcEnabled(Context)
	 */
	public static boolean isAndroidBeamEnabled(Context context) {
		boolean result = false;
		try {
			final NfcAdapter adapter = getNfcAdapter(context);
			if (adapter != null) {
				final Class<?> classAdapter = Class.forName(adapter.getClass().getName());
				final Method isNdefPushEnabled = classAdapter.getDeclaredMethod("isNdefPushEnabled");
				final boolean beamState = (Boolean) isNdefPushEnabled.invoke(adapter);
				result = (isNfcEnabled(context) && beamState);
			}
		} catch (ClassNotFoundException e) {
			Log.v(TAG, "isAndroidBeamEnabled(): " + e.getMessage());
		} catch (NoSuchMethodException e) {
			Log.v(TAG, "isAndroidBeamEnabled(): " + e.getMessage());
		} catch (IllegalAccessException e) {
			Log.v(TAG, "isAndroidBeamEnabled(): " + e.getMessage());
		} catch (IllegalArgumentException e) {
			Log.v(TAG, "isAndroidBeamEnabled(): " + e.getMessage());
		} catch (InvocationTargetException e) {
			Log.v(TAG, "isAndroidBeamEnabled(): " + e.getMessage());
		} 
		return result;
	}
}