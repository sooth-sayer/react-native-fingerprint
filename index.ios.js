export default class FingerPrint {
    static cancelAuthentication() {
        return Promise.reject();
    }

    static hasPermission() {
        return Promise.reject(false);
    }

    static hasEnrolledFingerprints() {
        return Promise.reject(false);
    }

    static isHardwareDetected() {
        return Promise.reject(false);
    }

    static authenticate() {
        return Promise.reject(new Error('Is not supported on IOS'));
    }
}
