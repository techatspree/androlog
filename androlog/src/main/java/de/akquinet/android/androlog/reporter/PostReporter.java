/*
 * Copyright 2010 akquinet
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.akquinet.android.androlog.reporter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import de.akquinet.android.androlog.Log;

/**
 * Reporter posting the report to a given URL. It uses a plain HTTP POST.
 */
public class PostReporter implements Reporter {

    /**
     * Mandatory Property to set the URL.
     */

    public static final String ANDROLOG_REPORTER_POST_URL = "androlog.reporter.post.url";

    /**
     * The URL object.
     */
    private URL url;

    /**
     * Configures the POST Reporter. The given configuration <b>must</b> contain
     * the {@link PostReporter#ANDROLOG_REPORTER_POST_URL} property and it must
     * be a valid {@link URL}.
     *
     * @see de.akquinet.android.androlog.reporter.Reporter#configure(java.util.Properties)
     */
    @Override
    public void configure(Properties configuration) {
        String u = configuration.getProperty(ANDROLOG_REPORTER_POST_URL);
        if (u == null) {
            Log.e(this, "The Property " + ANDROLOG_REPORTER_POST_URL
                    + " is mandatory");
            return;
        }
        try {
            url = new URL(u);
        } catch (MalformedURLException e) {
            Log.e(this, "The Property " + ANDROLOG_REPORTER_POST_URL
                    + " is not a valid url", e);
        }
    }

    /**
     * If the reporter was configured correctly, post the report to the set URL.
     *
     * @see de.akquinet.android.androlog.reporter.Reporter#send(android.content.Context,
     *      java.lang.String, java.lang.Throwable)
     */
    @Override
    public boolean send(Context context, String mes, Throwable err) {
        if (url != null) {
            String report = new Report(context, mes, err).getReportAsJSON()
                    .toString();
            try {
                postReport(url, report);
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * Executes the given request as a HTTP POST action.
     *
     * @param url
     *            the url
     * @param params
     *            the parameter
     * @return the response as a String.
     * @throws IOException
     *             if the server cannot be reached
     */
    public static void post(URL url, String params) throws IOException {
        URLConnection conn = url.openConnection();
        if (conn instanceof HttpURLConnection) {
            ((HttpURLConnection) conn).setRequestMethod("POST");
            conn.setRequestProperty("Content-Type" , "application/json");
        }
        OutputStreamWriter writer = null;
        try {
            conn.setDoOutput(true);
            writer = new OutputStreamWriter(conn.getOutputStream(),
                    Charset.forName("UTF-8"));
            // write parameters
            writer.write(params);
            writer.flush();
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    public static void postReport(URL url, String param) throws IOException {
        // Create a new HttpClient and Post Header
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(url.toExternalForm());

        try {
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("report", param));
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            // Execute HTTP Post Request
            httpclient.execute(httppost);
        } catch (ClientProtocolException e) {
            throw new IOException(e.getMessage());
        }
    }

    /**
     * Reads an input stream and returns the result as a String.
     *
     * @param in
     *            the input stream
     * @return the read String
     * @throws IOException
     *             if the stream cannot be read.
     */
    public static String read(InputStream in) throws IOException {
        InputStreamReader isReader = new InputStreamReader(in, "UTF-8");
        BufferedReader reader = new BufferedReader(isReader);
        StringBuffer out = new StringBuffer();
        char[] c = new char[4096];
        for (int n; (n = reader.read(c)) != -1;) {
            out.append(new String(c, 0, n));
        }
        reader.close();

        return out.toString();
    }

}
