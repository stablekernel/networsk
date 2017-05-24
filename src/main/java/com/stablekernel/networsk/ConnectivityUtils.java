package com.stablekernel.networsk;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
                Process ping = runtime.exec("/system/bin/ping -q -c 1 -W 1 " + ipAddress);
                StreamGobbler outputGobbler = new StreamGobbler(ping.getInputStream());
                StreamGobbler errorGobbler = new StreamGobbler(ping.getErrorStream());
                outputGobbler.start();
                errorGobbler.start();
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

    static class StreamGobbler extends Thread {
        InputStream is;

        public StreamGobbler(InputStream is) {
            this.is = is;
        }

        @Override
        public void run() {
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line = null;
            try {
                while ((line = br.readLine()) != null) {
                    if (Networsk.isDebug()) Log.v(Networsk.TAG, "GOBBLED: " + line);
                }
            } catch (IOException e) {
                if (Networsk.isDebug()) Log.e(Networsk.TAG, "error while gobbling stream", e);
            }
        }
    }
}
