package com.empatica.sample;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.Spotify;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerNotificationCallback;
import com.spotify.sdk.android.player.PlayerState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.Playlist;
import kaaes.spotify.webapi.android.models.PlaylistTrack;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class MainActivity extends Activity implements
        PlayerNotificationCallback, ConnectionStateCallback {

    // Larrmoej
    private static final String CLIENT_SECRET ="c11029a26e094395a6e60e8d22050663";
    private static final String CLIENT_ID = "f18769af7d9446449f50b343023cc487";
    // Zacke
    //private static final String CLIENT_ID = "28280d98f8124d5699a0a27537e6e2f8";
    //private static final String CLIENT_SECRET = "4d4ab16f86f949dcbb8b860797f3e300";
    private static final String REDIRECT_URI = "moodify-api-spotify-login://callback";
    private static final int REQUEST_CODE = 1337;
    private SpotifyPlayList playList = new SpotifyPlayList();
    private HashMap<String,String> playlistHASH = new HashMap<>();
    private List<String> playListUri = new ArrayList<String>();
    private PlayListAPP softPlaylist;
    private PlayListAPP regularPlaylist;
    TextView textFieldTrackName, textFieldPlayListName;
    private String uriPlayer= "uri";
    private int songNumber = 0;
    private UserInput userInput = new UserInput("yolo");
    private String input = "";


    private Player mPlayer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /*
        TextView trackName = findViewById(R.id.trackName);
        TextView playlistName = findViewById(R.id.playlistName);
        Button skipSong = findViewById(R.id.skipSong);
 
 
        skipSong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userInput.setText(input);
            }
        });
        */


        final AuthenticationRequest request = new AuthenticationRequest.Builder(CLIENT_ID, AuthenticationResponse.Type.TOKEN, REDIRECT_URI)
                .setScopes(new String[]{"user-read-private", "playlist-read", "playlist-read-private", "streaming"})
                .build();

        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);

        createButtons();

    }

    public void createButtons() {

        final Button connectEmpatica = (Button) findViewById(R.id.connectEmpatica);
        final Button disconnectEmpatica = (Button) findViewById(R.id.disconnectEmpatica);
        final Button skipSong = (Button) findViewById(R.id.skipSong);

        connectEmpatica.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startService(new Intent(MainActivity.this, EmpaticaService.class));
                connectEmpatica.setVisibility(View.INVISIBLE);
                disconnectEmpatica.setVisibility(View.VISIBLE);
            }
        });

        disconnectEmpatica.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                stopService(new Intent(MainActivity.this, EmpaticaService.class));
                connectEmpatica.setVisibility(View.VISIBLE);
                disconnectEmpatica.setVisibility(View.INVISIBLE);
            }
        });

        skipSong.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mPlayer.skipToNext();
            }
        });


    }

    @Override
    protected void onActivityResult(final int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if(requestCode == REQUEST_CODE){
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode,intent);

            switch (response.getType()){
                case TOKEN:
                    SpotifyApi api = new SpotifyApi();
                    api.setAccessToken(response.getAccessToken());
                    final Config playerConfig = new Config(this, response.getAccessToken(), CLIENT_ID);
                    SpotifyService spotify = api.getService();
 
                    /*Soft playlist*/
                    spotify.getPlaylist("fanos", "1mCf6v2iL6pattoFszfv6n", new Callback<Playlist>() {
                        @Override
                        public void success(Playlist playlist, Response response) {
                            int i = 0;
                            Pager<?> jao = playlist.tracks;
                            List<PlaylistTrack> temp = (List<PlaylistTrack>) jao.items;
                            softPlaylist = new PlayListAPP(temp.size(), playlist.name);

                            for (PlaylistTrack te : temp) {
                                softPlaylist.addToList(i, te.track.uri);
                                softPlaylist.setPlayListTrackName(i, te.track.name);
                                i++;
                            }
                            Spotify.getPlayer(playerConfig, this, new Player.InitializationObserver() {
                                @Override
                                public void onInitialized(final Player player) {
                                    Log.d("Uri path2", uriPlayer);
                                    mPlayer = player;
                                    mPlayer.addConnectionStateCallback(MainActivity.this);
                                    mPlayer.addPlayerNotificationCallback(MainActivity.this);
                                    mPlayer.play(softPlaylist.playListTracksUri);
                                }
                                @Override
                                public void onError(Throwable throwable) {
                                    Log.d("Uri fail", uriPlayer);
                                    Log.e("Spotifyy", "Could not initialize player: " + throwable.getMessage());
                                }
                            });
                        }
                        @Override
                        public void failure(RetrofitError error) {
                        }
                    });
                    break;
                case ERROR:
                    Log.d("facking", "error");
                    break;
                default:
            }
        }
    }

    public String getUri(int position){
        return playListUri.get(position);
    }

    @Override
    public void onLoggedIn() {
        Log.e("MainActivity", "User logged in");
    }

    @Override
    public void onLoggedOut() {
        Log.e("MainActivity", "User logged out");
    }

    @Override
    public void onLoginFailed(Throwable error) {
        Log.e("MainActivity", "Login failed");
    }

    @Override
    public void onTemporaryError() {
        Log.e("MainActivity", "Temporary error occurred");
    }

    @Override
    public void onConnectionMessage(String message) {
        Log.e("MainActivity", "Received connection message: " + message);
    }

    @Override
    public void onPlaybackEvent(EventType eventType, PlayerState playerState) {
        Log.d("MainActivity", "Playback event received: " + eventType.name());
        switch (eventType) {
            case PLAY:
                /*
                if(softPlaylist !=null){
                    textFieldPlayListName.setText(softPlaylist.getPlayListname());

                }
                */
                break;
            case PAUSE:
                break;
            case TRACK_CHANGED:
                Log.d("Changed","daw");
                Log.d("next song", "new song lol: " + Integer.toString(songNumber));
                if(softPlaylist !=null) {
                    /*
                    if ((songNumber) < softPlaylist.getNumberOfsongs())
                        textFieldTrackName.setText(softPlaylist.getNameOfTrack(songNumber));
                        */
                }

                songNumber++;
                break;
            case SKIP_NEXT:
                break;
            case SKIP_PREV:
                break;
            case SHUFFLE_ENABLED:
                break;
            case SHUFFLE_DISABLED:
                break;
            case REPEAT_ENABLED:
                break;
            case REPEAT_DISABLED:
                break;
            case BECAME_ACTIVE:
                break;
            case BECAME_INACTIVE:
                break;
            case LOST_PERMISSION:
                break;
            case AUDIO_FLUSH:
                break;
            case END_OF_CONTEXT:
                break;
            case EVENT_UNKNOWN:
                break;
            default:
                break;
        }
    }

    @Override
    public void onPlaybackError(ErrorType errorType, String errorDetails) {
        Log.d("MainActivity", "Playback error received: " + errorType.name());
        switch (errorType) {
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        Spotify.destroyPlayer(this);
        super.onDestroy();
    }

    // Update a label with some text, making sure this is run in the UI thread
    private void updateLabel(final TextView label, final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                label.setText(text);
            }
        });
    }

    public void recieve() {

    }



}