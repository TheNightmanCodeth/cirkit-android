package me.thenightmancodeth.cirkit.backend.controllers;

import android.util.Log;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fi.iki.elonen.NanoHTTPD;
import me.thenightmancodeth.cirkit.backend.models.Push;
import me.thenightmancodeth.cirkit.views.MainActivity;

/***************************************
 * Created by TheNightman on 11/21/16. *
 *                                     *
 * NanoHTTPd server that listens for   *
 * pushes from other nodes on cirkit   *
 * network.                            *
 ***************************************/

public class CirkitServer extends NanoHTTPD {
    private final String TAG = "SERVER";
    private MainActivity.OnPushReceivedListener listener;
    public CirkitServer(MainActivity.OnPushReceivedListener l) throws IOException {
        //Sets port to listen on
        super(6969);
        //Sets listener to be run when push received
        this.listener = l;
    }

    /**
     * Method run when request is made to :6969
     */
    @Override
    public Response serve(IHTTPSession session) {
        String remoteIP = session.getHeaders().get("remote-addr");
        Log.e(TAG, "Received push from IP: " +remoteIP);
        //TODO: check if device is already registered, if not register it to realm DB
        //Holds json object data
        Map<String, String> jsonBody = new HashMap<String, String>();
        //Gets HTTP request method
        Method method = session.getMethod();
        //If the http request is a POST request,
        if (method.POST.equals(method)) {
            try {
                //Parse JSON data from post body
                session.parseBody(jsonBody);
            } catch (IOException ioe) {
                return new Response(Response.Status.INTERNAL_ERROR, MIME_PLAINTEXT,
                        "SERVER INTERNAL ERROR: IOException: " + ioe.getMessage());
            } catch (ResponseException re) {
                return new Response(re.getStatus(), MIME_PLAINTEXT, re.getMessage());
            }
        }
        //Holds body from POST request
        String tem = "";
        for (Map.Entry<String, String> k: session.getParms().entrySet()) {
            tem = k.getKey();
            Log.e(TAG, tem);
        }
        //Runs listener from Constructor passing push value
        listener.onPushRec(new Push(extractVal(tem, "msg"), remoteIP));
        //Returns response to client node
        return new Response(Response.Status.OK, MIME_PLAINTEXT,
                "Push: " +extractVal(tem, "msg") +" received from: " +extractVal(tem, "sender"));
    }

    /**
     * Used to set listener to be run on request received
     * @param listener
     */
    public void setListener(MainActivity.OnPushReceivedListener listener) {
        this.listener = listener;
    }

    /**
     * Extracts value from response string using Regular Expressions
     * @param dataRaw - Raw JSON string from post body
     * @return - Value from json pair
     */
    public String extractVal(String dataRaw, String key) {
        //{"msg":"DATA DATA", "from":"289.35.22.252"}
        String re1=".*?";	// Non-greedy match on filler
        String re2="\"(.*?)\"";	// Uninteresting: string
        String re3=".*?";	// Non-greedy match on filler
        String re4="\"(.*?)\"";	// Double Quote String 1
        Pattern patt = Pattern.compile(re1+re2+re3+re4,Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher matc = patt.matcher(dataRaw);
        while (matc.find()) {
            //Group 1 should be the key (msg)
            Log.i(TAG, matc.groupCount() +"");
            if (matc.group(1).equals(key)) {
                try {
                    return URLDecoder.decode(matc.group(2), "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }

        return null;
    }
}
