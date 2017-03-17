package che.rn.fingerprint;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;
import android.support.v4.os.CancellationSignal;
import android.support.v4.app.ActivityCompat;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

public class FingerPrintModule extends ReactContextBaseJavaModule {

  public static final String TAG = "FingerPrintModule";

  public static final String EVENT_NAME = "fingerPrintModuleEvent";

  public static Map<Integer, String> acquireCodeToAlias;
  public static Map<Integer, String> errorCodeToAlias;
  public static String failedAlias;
  public static String unknownAlias;
  public static String acquireErrorAlias;
  static {
    acquireCodeToAlias = new HashMap<>();
    errorCodeToAlias = new HashMap<>();
    acquireCodeToAlias.put(3, "FINGERPRINT_ACQUIRED_IMAGER_DIRTY");
    acquireCodeToAlias.put(2, "FINGERPRINT_ACQUIRED_INSUFFICIENT");
    acquireCodeToAlias.put(1, "FINGERPRINT_ACQUIRED_PARTIAL");
    acquireCodeToAlias.put(5, "FINGERPRINT_ACQUIRED_TOO_FAST");
    acquireCodeToAlias.put(4, "FINGERPRINT_ACQUIRED_TOO_SLOW");
    errorCodeToAlias.put(5, "FINGERPRINT_ERROR_CANCELED");
    errorCodeToAlias.put(1, "FINGERPRINT_ERROR_HW_UNAVAILABLE");
    errorCodeToAlias.put(7, "FINGERPRINT_ERROR_LOCKOUT");
    errorCodeToAlias.put(4, "FINGERPRINT_ERROR_NO_SPACE");
    errorCodeToAlias.put(3, "FINGERPRINT_ERROR_TIMEOUT");
    errorCodeToAlias.put(2, "FINGERPRINT_ERROR_UNABLE_TO_PROCESS");
    unknownAlias = "UNKNOWN";
    failedAlias = "FAILED";
    acquireErrorAlias = "ACQUIRE_ERROR";
  }

  CancellationSignal cancellationSignal;
  static int currentCbId = 0;

  public FingerPrintModule(ReactApplicationContext reactContext) {
    super(reactContext);
  }

  public FingerprintManagerCompat getFpManager() {
    return FingerprintManagerCompat.from(getReactApplicationContext());
  }

  @Override
  public Map<String, Object> getConstants() {
    final Map<String, Object> constants = new HashMap<>();
    for (String alias : acquireCodeToAlias.values()) {
      constants.put(alias, alias);
    }
    for (String alias : errorCodeToAlias.values()) {
      constants.put(alias, alias);
    }
    constants.put(unknownAlias, unknownAlias);
    constants.put(failedAlias, failedAlias);
    constants.put("ERROR_EVENT_NAME", EVENT_NAME);
    return constants;
  }

  @ReactMethod
  public void hasPermission(Promise promise) {
    try {
      int fingerprintPermission = ActivityCompat.checkSelfPermission(getReactApplicationContext(), Manifest.permission.USE_FINGERPRINT);
      promise.resolve(fingerprintPermission == PackageManager.PERMISSION_GRANTED);
    } catch (Exception ex) {
      promise.reject(ex);
    }
  }

  @ReactMethod
  public void hasEnrolledFingerprints(Promise promise) {
    try {
      boolean hasEnrolledFingerprints = getFpManager().hasEnrolledFingerprints();
      promise.resolve(hasEnrolledFingerprints);
    } catch (Exception ex) {
      promise.reject(ex);
    }
  }

  @ReactMethod
  public void isHardwareDetected(Promise promise) {
    try {
      promise.resolve(getFpManager().isHardwareDetected());
    } catch (Exception ex) {
      promise.reject(ex);
    }
  }

  @ReactMethod
  public void authenticate(final Promise promise) {
    Log.d(TAG, "Authenticate");
    try {
      cancellationSignal = new CancellationSignal();
      getFpManager().authenticate(
        null,
        0,
        cancellationSignal,
        new AuthCallback(promise, ++currentCbId),
        null
      );
    } catch (Exception ex) {
      promise.reject(ex);
    }
  }

  @ReactMethod
  public void cancelAuthentication(Promise promise) {
    Log.d(TAG, "Cancel request");
    if (cancellationSignal.isCanceled()) {
      Log.w(TAG, "Already canceled, skip it");
      return;
    }

    try {
      cancellationSignal.cancel();
      promise.resolve(null);
    } catch(Exception e) {
      promise.reject(e);
    }
  }

  @Override
  public String getName() {
    return "FingerPrintAndroid";
  }


  private class AuthCallback extends FingerprintManagerCompat.AuthenticationCallback {
    private Promise promise;
    private int id;
    private boolean finished;

    public AuthCallback(Promise promise, int id) {
      this.promise = promise;
      this.id = id;
    }

    @Override
    public void onAuthenticationError(int errMsgId, CharSequence errString) {
      super.onAuthenticationError(errMsgId, errString);
      Log.e(TAG, "Fingerprint auth error: " + "(cb id: " + id + "), " + errString);
      if (finished) {
        Log.w(TAG, "Already finished, skip it");
        Log.w(TAG, "Should not be called according to android FingerPrintCompat doc :(");
        return;
      }

      rejectWithError(errMsgId, errString.toString());
      finished = true;
    }

    @Override
    public void onAuthenticationFailed() {
      super.onAuthenticationFailed();
      Log.w(TAG, "Fingerprint auth failed " + "(cb id: " + id + ")");
      sendFailedMessage();
    }

    @Override
    public void onAuthenticationHelp(int helpMsg, CharSequence helpString) {
      super.onAuthenticationHelp(helpMsg, helpString);
      Log.i(TAG, "Fingerprint auth acquire help: " + "(cb id: " + id + "), " + helpString);
      sendAcquireMessage(helpMsg, helpString.toString());
    }

    @Override
    public void onAuthenticationSucceeded(FingerprintManagerCompat.AuthenticationResult result) {
      super.onAuthenticationSucceeded(result);
      Log.d(TAG, "Fingerprint auth success " + "(cb id: " + id + ")");
      promise.resolve(true);
    }

    private void rejectWithError(int code, String message) {
      String alias = errorCodeToAlias.containsKey(code) ? errorCodeToAlias.get(code) : unknownAlias;
      promise.reject(alias, message);
    }

    private void sendFailedMessage() {
      WritableMap params = Arguments.createMap();
      params.putString("type", failedAlias);
      sendEvent(EVENT_NAME, params);
    }

    private void sendAcquireMessage(int code, String message) {
      WritableMap params = Arguments.createMap();
      params.putString("type", acquireErrorAlias);
      params.putString("message_alias", acquireCodeToAlias.containsKey(code) ? acquireCodeToAlias.get(code) : unknownAlias);
      params.putString("message", message);
      sendEvent(EVENT_NAME, params);
    }

    private void sendEvent(String name, @Nullable WritableMap params) {
      FingerPrintModule.this.getReactApplicationContext()
        .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
        .emit(name, params);
    }
  }

}
