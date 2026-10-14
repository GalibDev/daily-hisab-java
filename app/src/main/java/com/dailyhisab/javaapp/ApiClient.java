package com.dailyhisab.javaapp;

import android.os.Handler;
import android.os.Looper;
import org.json.*;
import java.net.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;

/** Serial background requests keep network I/O away from the UI thread. */
public final class ApiClient {
    public interface Callback { void done(Object result, String error, int status); }
    private final ExecutorService executor=Executors.newSingleThreadExecutor();
    private final Handler main=new Handler(Looper.getMainLooper());
    public void request(String base,String token,String method,String path,JSONObject body,Callback callback) {
        executor.execute(() -> {
            Object result=null;String error=null;int status=0;HttpURLConnection connection=null;
            try {
                connection=(HttpURLConnection)new URL(base+"/api"+path).openConnection();
                connection.setConnectTimeout(10000);connection.setReadTimeout(15000);connection.setRequestMethod(method);
                connection.setInstanceFollowRedirects(false);
                connection.setRequestProperty("Accept","application/json");
                if(!token.isEmpty())connection.setRequestProperty("Authorization","Bearer "+token);
                if(body!=null){
                    connection.setDoOutput(true);connection.setRequestProperty("Content-Type","application/json; charset=utf-8");
                    try(OutputStream out=connection.getOutputStream()){out.write(body.toString().getBytes(StandardCharsets.UTF_8));}
                }
                status=connection.getResponseCode();
                InputStream stream=status>=200&&status<300?connection.getInputStream():connection.getErrorStream();
                String text="";
                if(stream!=null)try(InputStream in=stream){text=new String(in.readAllBytes(),StandardCharsets.UTF_8);}
                result=text.isEmpty()?new JSONObject():new JSONTokener(text).nextValue();
                if(status<200||status>=300)error=result instanceof JSONObject?((JSONObject)result).optString("message","Request failed ("+status+")"):"Request failed";
            } catch(Exception e){error="Cannot reach server. Check the server URL, connection and backend.";}
            finally{if(connection!=null)connection.disconnect();}
            Object data=result;String problem=error;int code=status;
            main.post(()->callback.done(data,problem,code));
        });
    }
    public void close(){executor.shutdownNow();}
}
