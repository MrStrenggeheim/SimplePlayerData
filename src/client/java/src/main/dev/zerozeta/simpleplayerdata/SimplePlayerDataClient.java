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
    private final Type playerListType = new TypeToken<ArrayList<String>>() {}.getType();

    /**
     * an interface to the simple player data API
     *
     * @param host the host of the server the service is running on
     * @param port the port number under which the service is running
     */
    public SimplePlayerDataClient(String host, String port) {
        API_URL = "http://" + host + ":" + port;
        GSON = new Gson();
    }

    /**
     * fetches the player list
     *
     * @param names a list of player names the list should be checked for
     * @return the fetched player list
     */
    public List<String> getPlayerList(Iterable<String> names) {
        List<String> playerList = new ArrayList<>();
        URL url;
        try {
            url = new URL(API_URL + "/playerlist/fetch");
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

            String uuidString = "{\"names\": " + GSON.toJson(names) + "}";
            try (OutputStream os = con.getOutputStream()) {
                byte[] input = uuidString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            if (con.getResponseCode() != 200) {
                return playerList;
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
            response = reader.readLine();  // should contain only one line
        } catch (IOException ex) {
            ex.printStackTrace();
            return playerList;
        }
        playerList = GSON.fromJson(response, playerListType);
        return playerList;
    }

    /**
     * updates the player list with the specified name
     *
     * @param name sends a keep alive for the specified name
     */
    public void updatePlayerList(String name) {
        URL url;
        try {
            url = new URL(API_URL + "/playerlist/update");
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
            return;
        }

        HttpURLConnection con;
        try {
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setDoOutput(true);
            String nameData = "name=" + name;
            con.getOutputStream().write(nameData.getBytes(StandardCharsets.UTF_8));
            BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String response = reader.readLine();
            if (!response.contains("Success")) {
                System.err.println("Error while trying to connect to player API: " + response);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
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
            url = new URL(API_URL + "/capes/fetch");
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

            String uuidString = "{\"uuids\": " + GSON.toJson(uuids) + "}";
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
