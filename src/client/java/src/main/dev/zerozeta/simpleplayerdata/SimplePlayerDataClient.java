package main.dev.zerozeta.simpleplayerdata;

import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class SimplePlayerDataClient {

    private final String API_URL;

    /**
     * an interface to the simple player data API
     *
     * @param host the host of the server the service is running on
     * @param port the port number under which the service is running
     */
    public SimplePlayerDataClient(String host, String port) {
        API_URL = "http://" + host + ":" + port + "/api/players";
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
                url = new URL(API_URL);
            } else {
                url = new URL(API_URL + "?player=" + playerName);
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
                return new ArrayList<String>();
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
            response = reader.readLine();  // should contain only one line

        } catch (IOException ex) {
            ex.printStackTrace();
            return new ArrayList<String>();
        }

        JSONArray json = new JSONArray(response);
        List<String> playerList = new ArrayList<String>();
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
}
