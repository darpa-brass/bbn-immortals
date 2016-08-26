
The gradle wrapper has been added to make the version of
gradle actually loaded unimportant.
It is run by
```bash
./gradlew build
```

The Android SDK needs to be present.
Instructions can be found here...
http://developer.android.com/sdk/index.html
http://developer.android.com/sdk/installing/index.html

```bash
export ANDROID_HOME=~/Android/Sdk
export PATH=.:/opt/android/android-studio/bin:$ANDROID_HOME/tools:$ANDROID_HOME/platform-tools:$PATH
```

Android items get installed into ~/Android/Sdk/
```bash
  android
```  
The Android items which need to be installed are:

  * Android SDK Tools:  25.1.1
  * Android SDK Platform-tools: 23.1
  * Android SDK Build-tools 21.1.2
  * Android SDK Build-tools 19.1
  * Android SDK 5.0.1 (API 21) [All]
  * Extras (some extras only become available after the previous have been installed)
     * Android Support Repository: 29
     * Android Support Library: 23.2.1
