import {
  NativeModules,
  DeviceEventEmitter,
} from 'react-native';

import invariant from 'fbjs/lib/invariant';

const FingerPrintNative = NativeModules.FingerPrintAndroid;

const _handlers = new Map();

class FingerPrintAndroid {
  static addEventListener(type: string, handler: Function) {
    invariant(
      [FingerPrintNative.ERROR_EVENT_NAME].indexOf(type) !== -1,
      'Trying to subscribe to unknown event: "%s"', type,
    );
    _handlers.set(
      handler,
      DeviceEventEmitter.addListener(FingerPrintNative.ERROR_EVENT_NAME, handler),
    );
  }

  static removeEventListener(type: string, handler: Function) {
    invariant(
      [FingerPrintNative.ERROR_EVENT_NAME].indexOf(type) !== -1,
      'Trying to subscribe to unknown event: "%s"', type,
    );

    const emitterHandler = _handlers.get(handler);
    if (emitterHandler) {
      emitterHandler.remove();
      _handlers.delete(handler);
    }
  }
}

Object.keys(FingerPrintNative).forEach((k) => {
  FingerPrintAndroid[k] = FingerPrintNative[k];
});

FingerPrintAndroid.authenticate = () => {
  return FingerPrintNative.authenticate()
  .catch((error) => {
    throw new AuthError(error.code, error.message);
  });
};

FingerPrintAndroid.cancelAuthentication = FingerPrintNative.cancelAuthentication;
FingerPrintAndroid.isHardwareDetected = FingerPrintNative.isHardwareDetected;
FingerPrintAndroid.hasEnrolledFingerprints = FingerPrintNative.hasEnrolledFingerprints;
FingerPrintAndroid.hasPermission = FingerPrintNative.hasPermission;

class AuthError extends Error {
  constructor(alias, message) {
    super();
    this.type = 'ERROR';
    this.message_alias = alias;
    this.message = message;
  }
}

export default FingerPrintAndroid;
