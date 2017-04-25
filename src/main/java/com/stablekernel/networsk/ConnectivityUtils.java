package com.stablekernel.networsk;

import android.util.Log;

import java.io.IOException;
import java.util.concurrent.Callable;

import rx.Observable;
import rx.functions.Func1;

public final class ConnectivityUtils {

    ConnectivityUtils() {
        throw new UnsupportedOperationException();
    }

    public static boolean isInternetAvailable() {
        return pingIp("4.2.2.2");
    }

    /**
     * Send a PING to the given ip address.
     *
     * @param ipAddress the ip address to send a PING to.
     * @return true if PING completes without error.
     */
    private static boolean pingIp(String ipAddress) {
        if (Networsk.isDebug()) Log.d(Networsk.TAG, "PING " + ipAddress);
        boolean result = false;
        if (ipAddress != null && ipAddress.trim().length() > 0) {
            Runtime runtime = Runtime.getRuntime();
            try {
                Process ping = runtime.exec("/system/bin/ping -c 1 " + ipAddress);
                int exitValue = ping.waitFor();
                if (Networsk.isDebug()) Log.d(Networsk.TAG, "PING exitValue: " + exitValue);
                result = exitValue == 0;
            } catch (IOException | InterruptedException e) {
                Log.e(Networsk.TAG, "Couldn't complete PING of " + ipAddress, e);
            }
        }
        return result;
    }

    public static Observable<Boolean> pingReactive(final String ipAddress) {
        return Observable
                .fromCallable(new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        if (Networsk.isDebug()) Log.d(Networsk.TAG, "pingReactive: call: " + System.currentTimeMillis());
                        boolean ipResponds = pingIp(ipAddress);
                        if (Networsk.isDebug()) Log.d(Networsk.TAG, "ipResponds: " + ipResponds);
                        return ipResponds;
                    }
                })
                .onErrorResumeNext(new Func1<Throwable, Observable<? extends Boolean>>() {
                    @Override
                    public Observable<? extends Boolean> call(Throwable throwable) {
                        if (Networsk.isDebug()) Log.e(Networsk.TAG, "PING failed", throwable);
                        return Observable.just(false);
                    }
                });
    }
}
