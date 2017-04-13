package com.example.msi.dragstreetview;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by MSI on 2017/4/13.
 */

public class CheckStreetViewAvailability extends AsyncTask<LatLng, Void, Boolean> {

    @Override
    protected Boolean doInBackground(LatLng... param) {
        String sLat = new String(String.valueOf(param[0].latitude));
        String sLng = new String(String.valueOf(param[0].longitude));
        String urlTemplate = "https://maps.googleapis.com/maps/api/streetview/metadata?size=200x100&location=#,%&key=AIzaSyAN0mv-2HUaBcRD0V8yOsV-11pW8-WD1J4";
        String sUrl = urlTemplate.replace("#", sLat).replace("%", sLng);
        HttpURLConnection connection = null;
        BufferedReader reader = null;

        try {
            URL url = new URL(sUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();


            InputStream stream = connection.getInputStream();

            reader = new BufferedReader(new InputStreamReader(stream));

            StringBuffer buffer = new StringBuffer();
            String line = "";

            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
                Log.d("Response: ", "> " + line);   //here u ll get whole response...... :-)
            }

            JSONObject jsonObj = new JSONObject(buffer.toString());
            if (jsonObj != null) {
                try {
                    String status = jsonObj.getString("status");
                    if (status.equals("OK")) {
                        Log.d("return","true");
                        return true;
                    } else {
                        return false;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}