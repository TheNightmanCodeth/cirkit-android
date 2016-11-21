package me.thenightmancodeth.cirkit;

import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fi.iki.elonen.NanoHTTPD;

/**
 * Created by andrewdiragi on 11/21/16.
 */

public class CirkitServer extends NanoHTTPD {
    private MainActivity.OnPushReceivedListener listener;
    public CirkitServer(MainActivity.OnPushReceivedListener l) throws IOException {
        super(6969);
        this.listener = l;
    }

    @Override
    public Response serve(IHTTPSession session) {
        Map<String, String> jsonBody = new HashMap<String, String>();
        Method method = session.getMethod();
        if (method.POST.equals(method)) {
            try {
                session.parseBody(jsonBody);
            } catch (IOException ioe) {
                return new Response(Response.Status.INTERNAL_ERROR, MIME_PLAINTEXT, "SERVER INTERNAL ERROR: IOException: " + ioe.getMessage());
            } catch (ResponseException re) {
                return new Response(re.getStatus(), MIME_PLAINTEXT, re.getMessage());
            }
        }
        String tem = "";
        for (Map.Entry<String, String> k: session.getParms().entrySet()) {
            tem = k.getKey();
        }
        String push = extractVal(tem);
        listener.onPushRec(push);
        return new Response(Response.Status.OK, MIME_PLAINTEXT, "Push: " +push +" received");
    }

    public void setListener(MainActivity.OnPushReceivedListener listener) {
        this.listener = listener;
    }

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
