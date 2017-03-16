#react-native-fingerprint

Fingerprint android auth for react-native (android only).

##API

####`.authenticate():Promise<null>`
Starts authentication.
Returns a Promise.

####`.hasPermission(): Promise<boolean>`
Will check if `android.permission.USE_FINGERPRINT` is granted to this app.

####`hasEnrolledFingerprints(): Promise<boolean>`
Determine if there is at least one fingerprint enrolled.

####`isHardwareDetected(): Promise<boolean>`
Determine if fingerprint hardware is present and functional.

##Installation
`npm i react-native-fingerprint --save`

Don't forget to add the permission to your manifest:

`android/app/src/main/AndroidManifest.xml`
 ```diff
 <manifest xmlns:android="http://schemas.android.com/apk/res/android" package="com.example">
+    <uses-permission android:name="android.permission.USE_FINGERPRINT" />
 ```
###Automatic installation
Run `react-native link` after npm install.

###Manual installation

`android/app/build.gradle`
```diff
dependencies {
+   compile project(path: ':react-native-fingerprint')
    compile fileTree(dir: "libs", include: ["*.jar"])
    compile "com.android.support:appcompat-v7:23.0.1"
    compile "com.facebook.react:react-native:+"  // From node_modules
}
```

`android/settings.gradle`
```diff
include ':app'
+include ':react-native-fingerprint'
+project(':react-native-fingerprint').projectDir = new File(rootProject.projectDir, '../node_modules/react-native-fingerprint/android')
```

`android/app/src/main/java/com.your.package/MainApplication.java`
```diff
import com.facebook.react.ReactApplication;
+ import che.rn.fingerprint.FingerPrintPackage;
```
```diff
        @Override
        protected List<ReactPackage> getPackages() {
            return Arrays.<ReactPackage>asList(
-                   new MainReactPackage()
+                   new MainReactPackage(),
+                   new FingerPrintPackage()
            );
        }
```
