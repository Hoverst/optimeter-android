# Task: Allow Cleartext (HTTP) Traffic in Android App

## 1. Context
We have successfully verified that the physical Android device can reach the Node.js backend over the local Wi-Fi (`http://192.168.0.105:4000`). The network and firewall are perfectly fine. However, the app's requests are silently failing, which means Android's Network Security is blocking the unencrypted `http://` traffic.

## 2. Instructions for AI (Step Fun / Cascade)

> @workspace Please fix the Android network security configuration to allow HTTP traffic for local testing:
>
> 1. **Allow Cleartext Traffic:**
>    - Open `app/src/main/AndroidManifest.xml`.
>    - Add the `android:usesCleartextTraffic="true"` attribute directly into the `<application>` tag.
>
> 2. **Check Internet Permission:**
>    - Ensure `<uses-permission android:name="android.permission.INTERNET" />` is present at the top of `AndroidManifest.xml` (outside the application tag).
>
> 3. **Verify Base URL:**
>    - Double-check that the Network Client (Retrofit/Ktor) base URL is exactly `http://192.168.0.105:4000/`.
> 
> 4. **Add Error Logging:**
>    - If there isn't any, please add a `Log.e("Network", "Error: ", e)` in the `catch` block of the API call so we can see network crashes in Logcat.