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

import java.security.Timestamp;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.Playlist;
import kaaes.spotify.webapi.android.models.PlaylistTrack;
import retrofit.Callback;
import retrofit.RestAdapter;
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
    private String currentPlaylist = "4csYv6Cm7OI6VHJ1U7nJXz";
    private String playlistUser = "spotify";
    private String mood = "neutral";
    private int songNumber = 0;
    private double stressLevel = 0;

    private SongPlaying songPlaying = new SongPlaying();
    private final String sadSmiley="http://i64.tinypic.com/23l0yq.png";
    private final String happySmiley="http://i67.tinypic.com/dw3vdl.png";
    private final String neutralSmiley="http://i66.tinypic.com/2ah84sm.png";
    private String smiley = "http://i66.tinypic.com/2ah84sm.png";

    private boolean isFirst = true;
    private boolean isNext = false;
    private boolean isPrev = false;
    private boolean recievedThings = false;
    public boolean changedMood = false;

    private TextView deviceNameLabel;
    private TextView textResult;
    private TextView trackName;
    private TextView playlistName;

    private Button connectEmpatica;
    private Button disconnectEmpatica;
    private Button skipSong;
    private Button prevSong;

    private ImageView iv;
    private ImageView iv2;

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
        iv2 = (ImageView) findViewById(R.id.imageView2);
        connectEmpatica = (Button) findViewById(R.id.connectEmpatica);
        disconnectEmpatica = (Button) findViewById(R.id.disconnectEmpatica);
        skipSong = (Button) findViewById(R.id.skipSong);
        prevSong = (Button) findViewById(R.id.prevSong);
        //textResult = (TextView)findViewById(R.id.result);
        //deviceNameLabel = (TextView) findViewById(R.id.deviceName);
        trackName = (TextView) findViewById(R.id.trackName);
        //playlistName = (TextView) findViewById(R.id.playlistName);

        iv.setImageResource(R.drawable.nothing);
        iv2.setImageResource(R.drawable.note);

        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);

        //moodChange();

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
                stressLevel += 0.1;
                moodChange();
            }
        });

        prevSong.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mPlayer.skipToPrevious();
                stressLevel -= 0.1;
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
                    spotify.getPlaylist(playlistUser, currentPlaylist, new Callback<Playlist>() {
                        @Override
                        public void success(Playlist playlist, Response response) {
                            int i = 0;
                            songNumber = 0;
                            changedMood = false;
                            isFirst = true;
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

    @Override
    public void onPlaybackEvent(EventType eventType, PlayerState playerState) {
        Log.d("MainActivity", "Playback event received: " + eventType.name());

        switch (eventType) {
            case PLAY:
                break;
            case PAUSE:
                break;
            case TRACK_CHANGED:
                if(!changedMood) {
                    skipSong.setEnabled(true);
                    //playlistName.setText(softPlaylist.getPlayListname());
                    //playlistName.setGravity(Gravity.CENTER_HORIZONTAL);
                    if (songNumber < softPlaylist.getNumberOfsongs() - 1 && !isFirst && !isPrev) {
                        songNumber++;
                        prevSong.setEnabled(true);
                    }
                    if (isPrev && songNumber > 0) {
                        songNumber--;
                    }
                    if (songNumber >= softPlaylist.getNumberOfsongs() - 1) {
                        skipSong.setEnabled(false);
                    }
                    if (songNumber == 0) {
                        prevSong.setEnabled(false);
                    }
                    if ((songNumber) < softPlaylist.getNumberOfsongs()) {
                        trackName.setText(softPlaylist.getNameOfTrack(songNumber));
                        trackName.setGravity(Gravity.CENTER_HORIZONTAL);
                        songPlaying.setName(" " + softPlaylist.getNameOfTrack(songNumber));
                        //java.util.Date date= new java.util.Date();
                        // String date = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        java.util.Date now = new java.util.Date();
                        String strDate = sdf.format(now);
                        songPlaying.setTime(strDate);
                        //songPlaying.setTime(new Timestamp(Long.toString(date.getTime())date.getTime()));
                    }

                    Log.d("msg", songPlaying.getTime());

                    if (recievedThings) {

                        MoodifyAPI moodifyAPI = ServiceGenerator.createService(MoodifyAPI.class);
                        moodifyAPI.postSong(songPlaying, new Callback<SongPlaying>() {
                            @Override
                            public void success(SongPlaying songPlaying, Response response) {
                                Log.d("response", response.getReason());
                            }

                            @Override
                            public void failure(RetrofitError error) {
                                Log.d("response", "this guy");
                                error.printStackTrace();
                            }
                        });
                    }

                    isNext = false;
                    isFirst = false;
                    isPrev = false;
                }

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
        //unregisterReceiver(myBroadcastReceiver);
    }

    public class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String result = intent.getStringExtra(EmpaticaService.EXTRA_KEY_OUT);
            stressLevel = Double.parseDouble(result);
            moodChange();
        }
    }

    public void moodChange() {

        Log.e("msg", Double.toString(stressLevel));

        if(stressLevel < 1) {
            if(!mood.equals("happy")) {
                recievedThings = true;
                Toast.makeText(this, "Mood changed to calm", Toast.LENGTH_LONG).show();
                v.vibrate(500);
                iv.setImageResource(R.drawable.happy);
                mood = "happy";
                currentPlaylist = "4csYv6Cm7OI6VHJ1U7nJXz";
                playlistUser = "spotify";
                //smiley = happySmiley;
                songPlaying.setPicture(happySmiley);
                changedMood = true;
                AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);
            }
        }
        else if(stressLevel < 1.1) {
            if(!mood.equals("neutral")) {
                recievedThings = true;
                Toast.makeText(this, "Mood changed to neutral", Toast.LENGTH_LONG).show();
                v.vibrate(500);
                iv.setImageResource(R.drawable.neutral);
                mood = "neutral";
                currentPlaylist = "65V6djkcVRyOStLd8nza8E";
                playlistUser = "spotify";
                smiley = neutralSmiley;
                songPlaying.setPicture(neutralSmiley);
                changedMood = true;
                AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);
            }
        }
        else {
            if(!mood.equals("sad")) {
                recievedThings = true;
                Toast.makeText(this, "Mood changed to stressed", Toast.LENGTH_LONG).show();
                v.vibrate(500);
                iv.setImageResource(R.drawable.sad);
                mood = "sad";
                currentPlaylist = "1YRQAGw7qVJCLxWFGDsS3l";
                playlistUser = "spotify";
                smiley = sadSmiley;
                songPlaying.setPicture(sadSmiley);
                changedMood = true;
                AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);
            }
        }
    }
}