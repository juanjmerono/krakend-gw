package com.example.srv;

import javax.net.ssl.HttpsURLConnection;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RemoteServlet extends HttpServlet {

    private final Pattern pat = Pattern.compile(".*\"access_token\"\\s*:\\s*\"([^\"]+)\".*");
        
    private String getClientCredentials() {
        String content = "scope=openid&grant_type=client_credentials&client_id="+this.getServletConfig().getInitParameter("client-id")+"&client_secret="+this.getServletConfig().getInitParameter("client-secret");
        BufferedReader reader = null;
        HttpsURLConnection connection = null;
        String returnValue = "";
        try {
            URL url = new URL(this.getServletConfig().getInitParameter("token-uri"));
            connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("Accept", "application/json");
            PrintStream os = new PrintStream(connection.getOutputStream());
            os.print(content);
            os.close();
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line = null;
            StringWriter out = new StringWriter(connection.getContentLength() > 0 ? connection.getContentLength() : 2048);
            while ((line = reader.readLine()) != null) {
                out.append(line);
            }
            String response = out.toString();
            Matcher matcher = pat.matcher(response);
            if (matcher.matches() && matcher.groupCount() > 0) {
                returnValue = matcher.group(1);
            }
        } catch (Exception e) {
            System.out.println("Error : " + e.getMessage());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                }
            }
            connection.disconnect();
        }
        return returnValue;
    }

    private String callBearerToken(String pathInfo, String bearerToken) {
        BufferedReader reader = null;
        String response = "";
        try {
            String [] parts = pathInfo.split("/");
            if (parts.length != 4) throw new Exception("Path incorrecto: "+pathInfo);
            URL url = new URL("http://"+parts[2]+":8080/"+parts[1]+"/"+parts[3]);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("Authorization", "Bearer " + bearerToken);
            connection.setDoOutput(true);
            connection.setRequestMethod("GET");
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line = null;
            StringWriter out = new StringWriter(connection.getContentLength() > 0 ? connection.getContentLength() : 2048);
            while ((line = reader.readLine()) != null) {
                out.append(line);
            }
            response = out.toString();
        } catch (Exception e) {
            response = e.getMessage();
        }
        return response;
    }

    public void service( HttpServletRequest req, HttpServletResponse res ) throws IOException {
        PrintWriter out = res.getWriter();
        out.println( callBearerToken(req.getPathInfo(), getClientCredentials()) );
        out.close();
    }    

}

