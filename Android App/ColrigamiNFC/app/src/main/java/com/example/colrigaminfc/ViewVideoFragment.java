package com.example.colrigaminfc;

import android.app.Fragment;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.VideoView;

import androidx.annotation.Nullable;

public class ViewVideoFragment extends Fragment {

    private int video;
    private View view;
    private VideoView videoView;
    private int stopPosition;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // obtain the resource ID of the video in the bundle added in newInstance static method
        Bundle bundle = getArguments();
        int video = bundle.getInt("video");
        this.video = video;
        // make sure the orientation of the fragment is landscape
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

    }

    public void pauseVid(){
        videoView.pause();
        stopPosition = videoView.getCurrentPosition();
    }
    public void playVid(){
        videoView.seekTo(stopPosition);
        videoView.start();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_video, container, false);

        // identify the VideoView widget in the view of the fragment
        videoView = (VideoView)view.findViewById(R.id.videoView);

        // Generate the Uri of the video to be played from the resource ID of the video
        Uri uri = Uri.parse("android.resource://" + getActivity().getPackageName() + "/" + video);

        // Set the Uri of the video to be played by the VideoView
        videoView.setVideoURI(uri);

        // Add a MediaController at the bottom of the VideoView to handle play/pause/fast forward/backward
        MediaController mediaController = new MediaController(getActivity());
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);

        // Start the video
        videoView.start();
        return view;
    }


    // newInstance method allows the resource ID of the video to be played to be sent to the ViewVideoFragment
    // by putting the resource ID of the video in a bundle
    public static ViewVideoFragment newInstance(int video) {
        ViewVideoFragment fragment = new ViewVideoFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("video", video);
        fragment.setArguments(bundle);
        return fragment;
    }
}

