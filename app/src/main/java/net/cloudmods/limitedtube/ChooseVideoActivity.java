package net.cloudmods.limitedtube;

import android.app.Activity;
import android.app.ListActivity;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.youtube.player.YouTubeApiServiceUtil;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubeThumbnailLoader;
import com.google.android.youtube.player.YouTubeThumbnailView;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.PlaylistItemListResponse;

import android.content.Intent;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.view.ViewGroup.LayoutParams;

import java.io.IOException;
import java.util.List;

public final class ChooseVideoActivity extends ListActivity {

    private static final String TAG = "LimitedTube:ChooseVideoActivity";

    /** The request code when calling startActivityForResult to recover from an API service error. */
    private static final int RECOVERY_DIALOG_REQUEST = 1;

    private YouTube mYoutube;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create a progress bar to display while the list loads
        ProgressBar progressBar = new ProgressBar(this);
        GridLayout.LayoutParams lp = new GridLayout.LayoutParams(
                new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        lp.setGravity(Gravity.CENTER);
        progressBar.setLayoutParams(lp);

        progressBar.setIndeterminate(true);
        getListView().setEmptyView(progressBar);

        // Must add the progress bar to the root of the layout
        ViewGroup root = (ViewGroup) findViewById(android.R.id.content);
        root.addView(progressBar);

        checkYouTubeApi();
        new LoadPlaylistItemsTask(this).execute("PLhHMtHwQcXI_30_0hL5rnhn5Sd57xq0ql");
    }

    private void checkYouTubeApi() {
        YouTubeInitializationResult errorReason =
                YouTubeApiServiceUtil.isYouTubeApiServiceAvailable(this);
        if (errorReason.isUserRecoverableError()) {
            errorReason.getErrorDialog(this, RECOVERY_DIALOG_REQUEST).show();
        } else if (errorReason != YouTubeInitializationResult.SUCCESS) {
            String errorMessage =
                    String.format(getString(R.string.error_player), errorReason.toString());
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RECOVERY_DIALOG_REQUEST) {
            // Recreate the activity if user performed a recovery action
            recreate();
        }
    }

    private class LoadPlaylistItemsTask extends AsyncTask<String, Void, List<PlaylistItem>> {

        private final Activity mContext;

        public LoadPlaylistItemsTask(Activity context) {
            super();
            mContext = context;
        }

        @Override
        protected List<PlaylistItem> doInBackground(String... params) {
            try {
                mYoutube = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(), null)
                        .setApplicationName("LimitedTube").build();

                YouTube.PlaylistItems.List list = mYoutube.playlistItems().list("id,snippet");
                list.setKey(API.API_KEY);
                list.setMaxResults(50l);
                list.setPlaylistId(params[0]);

                PlaylistItemListResponse response = list.execute();
                List<PlaylistItem> items = response.getItems();

                return items;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<PlaylistItem> items) {

            if (items == null)
                return;

            setListAdapter(new VideolistAdapter(mContext, items));
        }
    }

    private class VideolistAdapter extends ArrayAdapter<PlaylistItem> {
        private final Activity mContext;

        private final List<PlaylistItem> mItems;

        public VideolistAdapter(Activity context, List<PlaylistItem> items) {
            super(context, R.layout.video_list_item, items);

            mContext = context;
            mItems = items;
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            final PlaylistItem item = mItems.get(position);

            LayoutInflater inflater = mContext.getLayoutInflater();
            View rowView = inflater.inflate(R.layout.video_list_item, null,true);

            TextView textView = (TextView) rowView.findViewById(R.id.text);
            YouTubeThumbnailView thumbnailView = (YouTubeThumbnailView) rowView.findViewById(R.id.thumbnail);

            textView.setText(item.getSnippet().getTitle());

            thumbnailView.initialize(API.API_KEY, new YouTubeThumbnailView.OnInitializedListener() {
                @Override
                public void onInitializationSuccess(YouTubeThumbnailView youTubeThumbnailView, YouTubeThumbnailLoader youTubeThumbnailLoader) {
                    youTubeThumbnailLoader.setVideo(item.getSnippet().getResourceId().getVideoId());
                }

                @Override
                public void onInitializationFailure(YouTubeThumbnailView youTubeThumbnailView, YouTubeInitializationResult youTubeInitializationResult) {

                }
            });

            rowView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, PlayVideoActivity.class);
                    intent.putExtra(PlayVideoActivity.VIDEO_KEY, item.getSnippet().getResourceId().getVideoId());
                    startActivity(intent);
                }
            });

            return rowView;
        }
    }
}

