package de.jamoo.muzei;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class Utils {

    public static String convertDurationtoString(long duration) {
        int hour = (int) (duration / 3600000);
        int min = (int) ((duration - (hour * 3600000)) / 60000);
        int sec = (int) ((duration - (hour * 3600000) - (min * 60000)) / 1000);
        StringBuilder builder = new StringBuilder();
        if(hour != 0){
        	builder.append(hour).append("h");
        }
        if(min != 0){
            builder.append(" ").append(firstDigit(min)).append("m");
        }
        if (sec != 0){
            builder.append(" ").append(firstDigit(sec)).append("s");
        }
        return builder.toString();
    }

    private static String firstDigit(int min) {
        if (min < 10) {
            return "0" + String.valueOf(min);
        }
        return String.valueOf(min);
    }

    public static boolean isWifiConnected(Context context) {
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return  mWifi.isConnected();
    }

    public static void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
                closeable = null;
            } catch (final IOException ignored) {
                // Nothing to do
            }
        }
    }
    
    public static String toString(InputStream in) throws IOException {
        final BufferedReader reader = new BufferedReader(new InputStreamReader(in, "iso-8859-1"), 8);
        final StringBuilder builder = new StringBuilder(in.available());
        String line;
        while ((line = reader.readLine()) != null) {
            builder.append(line);
        }
        return builder.toString();
    }
}
