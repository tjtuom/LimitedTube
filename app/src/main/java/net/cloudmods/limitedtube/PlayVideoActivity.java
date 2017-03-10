package net.cloudmods.limitedtube;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;

public class PlayVideoActivity extends YouTubeBaseActivity
        implements  YouTubePlayer.OnInitializedListener, YouTubePlayer.PlayerStateChangeListener
{
    public static final String VIDEO_KEY = "videoId";


    private static final String TAG = "LimitedTube:PlayVideoActivity";

    private static final int BACK_PRESS_INTERVAL = 200;
    private static final int BACK_PRESS_AMOUNT = 5;

    private YouTubePlayer mPlayer;
    private String mVideoId;

    private long mBackPressedTime;
    private int mBackPressedCount = BACK_PRESS_AMOUNT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_video);

        Intent intent = getIntent();
        mVideoId = intent.getStringExtra(VIDEO_KEY);

        YouTubePlayerView pview = (YouTubePlayerView) findViewById(R.id.player);
        pview.initialize(API.API_KEY, this);
    }

    @Override
    protected void onDestroy() {
        if (mPlayer != null) {
            mPlayer.release();
        }

        super.onDestroy();
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean wasRestored) {
        mPlayer = youTubePlayer;
        mPlayer.setPlayerStateChangeListener(this);
        mPlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.MINIMAL);
        mPlayer.setFullscreen(true);
        mPlayer.loadVideo(mVideoId);
    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {

    }

    @Override
    public void onLoading() {

    }

    @Override
    public void onLoaded(String s) {
        mPlayer.play();
    }

    @Override
    public void onAdStarted() {

    }

    @Override
    public void onVideoStarted() {
    }

    @Override
    public void onVideoEnded() {
        finish();
    }

    @Override
    public void onError(YouTubePlayer.ErrorReason errorReason) {

    }

    @Override
    public void onBackPressed() {
        long now = System.currentTimeMillis();

        if (now - mBackPressedTime > BACK_PRESS_INTERVAL) {
            mBackPressedCount = BACK_PRESS_AMOUNT;
        }
        else {
            mBackPressedCount--;
        }

        if (mBackPressedCount == 0)
            super.onBackPressed();
        else
            mBackPressedTime = now;
    }
}
