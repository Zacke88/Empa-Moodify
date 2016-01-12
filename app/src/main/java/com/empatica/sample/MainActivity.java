package com.empatica.sample;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
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
    //private static final String CLIENT_SECRET ="c11029a26e094395a6e60e8d22050663";
    //private static final String CLIENT_ID = "f18769af7d9446449f50b343023cc487";
    // Zacke
    private static final String CLIENT_ID = "28280d98f8124d5699a0a27537e6e2f8";
    private static final String CLIENT_SECRET = "4d4ab16f86f949dcbb8b860797f3e300";
    private static final String REDIRECT_URI = "moodify-api-spotify-login://callback";
    private static final int REQUEST_CODE = 1337;
    private PlayListAPP softPlaylist;
    private PlayListAPP regularPlaylist;
    private String uriPlayer= "uri";
    private String currentPlaylist = "7b9XqnXw5J47tmn0Y0IZeW";
    private String mood = "";
    private int songNumber = 0;
    private double stressLevel = 0;


    private boolean isFirst = true;
    private boolean isNext = false;
    private boolean isPrev = false;

    private TextView deviceNameLabel;
    private TextView textResult;
    private TextView trackName;
    private TextView playlistName;

    private Button connectEmpatica;
    private Button disconnectEmpatica;
    private Button skipSong;
    private Button prevSong;

    private ImageView iv;

    private MyBroadcastReceiver myBroadcastReceiver;

    Vibrator v;

    final AuthenticationRequest request = new AuthenticationRequest.Builder(CLIENT_ID, AuthenticationResponse.Type.TOKEN, REDIRECT_URI)
            .setScopes(new String[]{"user-read-private", "playlist-read", "playlist-read-private", "streaming"})
            .build();

    private Player mPlayer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        iv = (ImageView)findViewById(R.id.imageView);
        connectEmpatica = (Button) findViewById(R.id.connectEmpatica);
        disconnectEmpatica = (Button) findViewById(R.id.disconnectEmpatica);
        skipSong = (Button) findViewById(R.id.skipSong);
        prevSong = (Button) findViewById(R.id.prevSong);
        //textResult = (TextView)findViewById(R.id.result);
        //deviceNameLabel = (TextView) findViewById(R.id.deviceName);
        trackName = (TextView) findViewById(R.id.trackName);
        playlistName = (TextView) findViewById(R.id.playlistName);

        iv.setImageResource(R.drawable.neutral);

        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);

        moodChange();

        createButtons();

        createBroadcaster();

    }

    public void createBroadcaster() {
        myBroadcastReceiver = new MyBroadcastReceiver();

        //register BroadcastReceiver
        IntentFilter intentFilter = new IntentFilter(EmpaticaService.ACTION_MyIntentService);
        registerReceiver(myBroadcastReceiver, intentFilter);

    }

    public void createButtons() {
        skipSong.setEnabled(false);
        prevSong.setEnabled(false);

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
                stressLevel = stressLevel + 0.1;
                moodChange();
            }
        });

        prevSong.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mPlayer.skipToPrevious();
                stressLevel = stressLevel - 0.1;
                moodChange();
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
                    spotify.getPlaylist("spotify", currentPlaylist, new Callback<Playlist>() {
                        @Override
                        public void success(Playlist playlist, Response response) {
                            int i = 0;
                            Pager<?> jao = playlist.tracks;
                            List<PlaylistTrack> temp = (List<PlaylistTrack>) jao.items;
                            softPlaylist = new PlayListAPP(temp.size(), playlist.name);

                            for (PlaylistTrack te : temp) {
                                softPlaylist.addToList(i, te.track.uri);
                                softPlaylist.setPlayListTrackName(i, te.track.name);
                                Log.e("track", te.track.name);
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
                                    Log.e("Spotify", "Could not initialize player: " + throwable.getMessage());
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

    // TODO Byt inte låt i slutet av playlist
    @Override
    public void onPlaybackEvent(EventType eventType, PlayerState playerState) {
        Log.d("MainActivity", "Playback event received: " + eventType.name());

        switch (eventType) {
            case PLAY:
                    playlistName.setText(softPlaylist.getPlayListname());
                    playlistName.setGravity(Gravity.CENTER_HORIZONTAL);
                break;
            case PAUSE:
                break;
            case TRACK_CHANGED:
                skipSong.setEnabled(true);
                if(songNumber < softPlaylist.getNumberOfsongs()-1 && !isFirst && !isPrev) {
                    songNumber++;
                    prevSong.setEnabled(true);
                    Log.e("msg", "ÄR VI HÄR INNE ELLER");
                }
                if(isPrev && songNumber > 0) {
                    songNumber--;
                }
                if (songNumber >= softPlaylist.getNumberOfsongs()-1) {
                    skipSong.setEnabled(false);
                }
                if (songNumber == 0) {
                    prevSong.setEnabled(false);
                }
                if ((songNumber) < softPlaylist.getNumberOfsongs()) {
                    trackName.setText(softPlaylist.getNameOfTrack(songNumber));
                    trackName.setGravity(Gravity.CENTER_HORIZONTAL);
                }

                isNext = false;
                isFirst = false;
                isPrev = false;

                break;
            case SKIP_NEXT:
                //songNumber++;
                isNext = true;
                break;
            case SKIP_PREV:
                int i = 0;
                if (songNumber > 0) {
                    songNumber--;
                    i++;
                }
                if (songNumber > 0) {
                    songNumber--;
                    i++;
                }
                if(i == 1) {
                    isPrev = true;
                }
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
        super.onDestroy();
        Spotify.destroyPlayer(this);
        //un-register BroadcastReceiver
        unregisterReceiver(myBroadcastReceiver);
    }

    public class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String result = intent.getStringExtra(EmpaticaService.EXTRA_KEY_OUT);
            //textResult.setText(result);
        }
    }

    public void moodChange() {

        if(stressLevel < 0.2) {
            if(mood.equals("happy")) {
                Toast.makeText(this, "Mood changed to calm", Toast.LENGTH_LONG).show();
                v.vibrate(500);
                iv.setImageResource(R.drawable.happy);
                mood = "happy";
                currentPlaylist = "1j96MGp5j4jRsgZp4vMxQG";
                AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);
            }
        }
        else if(stressLevel < 0.5) {
            if(mood.equals("neutral")) {
                Toast.makeText(this, "Mood changed to neutral", Toast.LENGTH_LONG).show();
                v.vibrate(500);
                iv.setImageResource(R.drawable.neutral);
                mood = "neutral";
                currentPlaylist = "65V6djkcVRyOStLd8nza8E";
                AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);
            }
        }
        else {
            if(mood.equals("sad")) {
                Toast.makeText(this, "Mood changed to stressed", Toast.LENGTH_LONG).show();
                v.vibrate(500);
                iv.setImageResource(R.drawable.sad);
                mood = "sad";
                currentPlaylist = "7b9XqnXw5J47tmn0Y0IZeW";
                AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);
            }
        }
    }
}