package me.thenightmancodeth.cirkit.Backend;

import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fi.iki.elonen.NanoHTTPD;
import me.thenightmancodeth.cirkit.MainActivity;

/***************************************
 * Created by TheNightman on 11/21/16. *
 *                                     *
 * NanoHTTPd server that listens for   *
 * pushes from other nodes on cirkit   *
 * network.                            *
 ***************************************/

public class CirkitServer extends NanoHTTPD {
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
        //Holds json object data
        Map<String, String> jsonBody = new HashMap<String, String>();
        //Gets HTTP request method
        Method method = session.getMethod();
        //If the http request is a POST request,
        if (POST.equals(method)) {
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
        }
        //Runs listener from Constructor passing push value
        listener.onPushRec(extractVal(tem));
        //Returns response to client node
        return new Response(Response.Status.OK, MIME_PLAINTEXT, "Push: " +push +" received");
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
    public String extractVal(String dataRaw) {
        //{"KEY","DATA DATA"}
        String re1=".*?";	// Non-greedy match on filler
        String re2="\".*?\"";	// Uninteresting: string
        String re3=".*?";	// Non-greedy match on filler
        String re4="(\".*?\")";	// Double Quote String 1
        Pattern patt = Pattern.compile(re1+re2+re3+re4,Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher matc = patt.matcher(dataRaw);
        if (matc.find()) {
            return matc.group(1);
        }
        return null;
    }
}
