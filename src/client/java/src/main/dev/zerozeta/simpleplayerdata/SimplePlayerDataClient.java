package main.dev.zerozeta.simpleplayerdata;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimplePlayerDataClient {

    private final String API_URL;
    private final Gson GSON;
    private final Type capeMapType = new TypeToken<HashMap<String, String>>() {
    }.getType();

    /**
     * an interface to the simple player data API
     *
     * @param host the host of the server the service is running on
     * @param port the port number under which the service is running
     */
    public SimplePlayerDataClient(String host, String port) {
        API_URL = "http://" + host + ":" + port + "/api";
        GSON = new Gson();
    }

    /**
     * fetches the player list and registers <code>playerName</code>
     *
     * @param playerName the name that should be used to update the player list
     * @return the fetched player list
     */
    public List<String> getPlayerList(String playerName) {
        URL url;
        try {
            if (playerName == null) {
                url = new URL(API_URL + "/players");
            } else {
                url = new URL(API_URL + "/players?player=" + playerName);
            }
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
            return null;
        }

        HttpURLConnection con;
        String response;
        try {
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");

            if (con.getResponseCode() != 200) {
                return new ArrayList<>();
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
            response = reader.readLine();  // should contain only one line

        } catch (IOException ex) {
            ex.printStackTrace();
            return new ArrayList<>();
        }

        JSONArray json = new JSONArray(response);
        List<String> playerList = new ArrayList<>();
        for (int i = 0; i < json.length(); i++) {
            playerList.add(json.getString(i));
        }
        return playerList;
    }

    /**
     * fetches the player list without updating it
     *
     * @return the fetched player list
     */
    public List<String> getPlayerList() {
        return getPlayerList(null);
    }

    /**
     * fetches the cape URLs for the players with the given UUIDs
     *
     * @param uuids the UUIDs to check for capes
     * @return a map of the cape URLs for the provided UUIDs
     */
    public Map<String, String> getCapeURLs(Iterable<String> uuids) {
        Map<String, String> urls = new HashMap<>();
        URL url;
        try {
            url = new URL(API_URL + "/capes");
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
            return null;
        }

        HttpURLConnection con;
        String response;
        try {
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json; utf-8");
            con.setRequestProperty("Accept", "application/json");
            con.setDoOutput(true);

            String uuidString = "{\"players\": " + GSON.toJson(uuids) + "}";
            try (OutputStream os = con.getOutputStream()) {
                byte[] input = uuidString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            if (con.getResponseCode() != 200) {
                return urls;
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
            response = reader.readLine();  // should contain only one line

        } catch (IOException ex) {
            ex.printStackTrace();
            return urls;
        }

        urls = GSON.fromJson(response, capeMapType);
        return urls;
    }
}
