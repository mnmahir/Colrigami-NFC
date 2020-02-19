package com.example.colrigaminfc;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.app.FragmentManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.ContactsContract;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import java.util.Arrays;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;


import androidx.fragment.app.Fragment;

import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private static final String ADDRESS = "B8:27:EB:38:FC:A2";  //BLE MAC Address
    private static final String CAPSTONE_SERVICE_UUID = "13333333-3333-3333-3333-333333333337";
    private static final String CAPSTONE_CHARACTERISTICS_UUID = "13333333-3333-3333-3333-333333330001";
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice device;
    private BluetoothGatt bluetoothGatt;
    private BluetoothGattCharacteristic charCapstone;
    private EditText editText;
    private NfcAdapter nfcAdapter;
    private TextView textview;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean("videoIsPlaying",videoIsPlaying);
        outState.putBoolean("orientPotrait", orientToPotrait);
        outState.putBoolean("isVidPause",vidIsPause);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bluetoothManager = (BluetoothManager)this.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if(getIntent().getAction().equals(NfcAdapter.ACTION_NDEF_DISCOVERED)){
            readIntent(getIntent());
        }
        if (savedInstanceState != null){
            videoIsPlaying = savedInstanceState.getBoolean("videoIsPlaying");
            orientToPotrait = savedInstanceState.getBoolean("orientPotrait");
            vidIsPause = savedInstanceState.getBoolean("isVidPause");
            //setVideoIsPlaying(videoIsPlaying);
        }
    }

    //Global Variables
    String payload = "";
    boolean videoIsPlaying = false;
    boolean orientToPotrait = true;
    boolean vidIsPause = false;
    boolean playRemotely = false;
    int VidNum = 0;
    String VidName = "";

    //BLE
    public void BLEconnect(View view){
        device = mBluetoothAdapter.getRemoteDevice(ADDRESS);
        bluetoothGatt = device.connectGatt(getApplicationContext(), true, new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                super.onConnectionStateChange(gatt, status, newState);
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    gatt.discoverServices();
                }
            }
            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                super.onServicesDiscovered(gatt, status);
                charCapstone = gatt.getService(UUID.fromString(CAPSTONE_SERVICE_UUID))
                        .getCharacteristic(UUID.fromString(CAPSTONE_CHARACTERISTICS_UUID));
                bluetoothGatt = gatt;
            }
        });
        ToggleButton remoteplay = (ToggleButton) findViewById(R.id.remoteToggle);
        playRemotely = remoteplay.isChecked();
    }

    public void BLEdisconnect(){
        bluetoothGatt.disconnect();
    }

    public void BLEbuttonpauseplay(View view){
        if(playRemotely && videoIsPlaying) {
            ImageButton playpause = (ImageButton) findViewById(R.id.buttonPlayPause);
            BLEsend((byte)10);
            if(vidIsPause){
                vidIsPause = false;
                makeToast("Video resumed");
                playpause.setImageResource(android.R.drawable.ic_media_pause);
            }
            else{
                vidIsPause = true;
                makeToast("Video paused");
                playpause.setImageResource(android.R.drawable.ic_media_play);
            }
        }
    }
    public void BLEbuttonDecSpd(View view){
        if(playRemotely) BLEsend((byte)11);
        if (videoIsPlaying) makeToast("Speed decreased");
    }
    public void BLEbuttonIncSpd(View view){
        if(playRemotely) BLEsend((byte)12);
        if (videoIsPlaying) makeToast("Speed increased");
    }
    public void BLEbuttonRw30(View view){
        if(playRemotely) BLEsend((byte)13);
        if (videoIsPlaying) makeToast("Rewind 30 seconds");
    }
    public void BLEbuttonFF(View view){
        if(playRemotely) BLEsend((byte)14);
        if (videoIsPlaying) makeToast("Forward 30 seconds");
    }
    public void BLEbuttonStop(View view){
        if(playRemotely) {
            ImageView recentPlayVid = (ImageView) findViewById(R.id.imageView);
            recentPlayVid.setImageResource(R.drawable.logobg);
            if (videoIsPlaying) makeToast("Video stopped");
            videoIsPlaying = false;
            BLEsend((byte)15);
            displayVidName("-");

        }
    }

    public void BLEsend(byte value){
        charCapstone.setValue(new byte[]{value});
        bluetoothGatt.writeCharacteristic(charCapstone);
    }
    //BLE
    //NFC
    public BluetoothAdapter getmBluetoothAdapter() {
        return mBluetoothAdapter;
    }

    private Origami[] origamies = {
            new Origami("Frog Origami", R.drawable.frogorigami, R.raw.frogorigami),
            new Origami("Pickachu Origami", R.drawable.pikachuorigami, R.raw.pikachuorigami),
            new Origami("Rabbit Origami", R.drawable.rabbitorigami,R.raw.rabbitorigami),
    };
    //Main activity
    private void setVideoIsPlaying(boolean sw){
        videoIsPlaying = sw;
    }

    private void playVideo() {
        getFragmentManager().beginTransaction().replace(android.R.id.content, ViewVideoFragment.newInstance(origamies[VidNum].getVideo()),"VideoFrag")
                .addToBackStack(null).commit();
    }
    private void pausePlayVid(){
        ViewVideoFragment videoFragment = (ViewVideoFragment) getFragmentManager().findFragmentByTag("VideoFrag");
        if (!vidIsPause){
            videoFragment.pauseVid();
            vidIsPause = true;
            makeToast("Video paused");
        }else{
            videoFragment.playVid();
            vidIsPause = false;
            makeToast("Video resumed");
        }
    }
    private void displayVidName(String message){
        TextView vidNameText = (TextView) findViewById(R.id.vidName);
        vidNameText.setText(message);
    }

    //For NFC
    private void readIntent(Intent intent) {
        Parcelable[] parcelables = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        for(int i=0;i<parcelables.length;i++){
            NdefMessage message = (NdefMessage)parcelables[i];
            NdefRecord[] records = message.getRecords();
            for(int j = 0;j<records.length;j++){
                NdefRecord record = records[j];
                if(new String(record.getType()).equals("T")) {
                    byte[] original = record.getPayload();
                    byte[] value = Arrays.copyOfRange(original, 3, original.length);
                    payload = new String(value); //value or original
                    VidNum = Integer.parseInt(payload);

                    if (playRemotely){
                        BLEsend((byte) VidNum);
                        if (!videoIsPlaying && VidNum >= 0 && VidNum <=3){
                            videoIsPlaying = true;
                            ImageView recentPlayVid = (ImageView) findViewById(R.id.imageView);
                            recentPlayVid.setImageResource(origamies[VidNum].getImage());
                            VidName = origamies[VidNum].getTitle();
                            displayVidName(VidName);
                            makeToast("Now playing: "+VidName);
                        }else if(VidNum == 15 && videoIsPlaying){
                            videoIsPlaying = false;
                            ImageView recentPlayVid = (ImageView) findViewById(R.id.imageView);
                            recentPlayVid.setImageResource(R.drawable.logobg);
                            displayVidName("-");
                        }else if(VidNum == 10 && videoIsPlaying) {
                            ImageButton playpause = (ImageButton) findViewById(R.id.buttonPlayPause);
                            BLEsend((byte)10);
                            if(vidIsPause){
                                vidIsPause = false;
                                makeToast("Video resumed");
                                playpause.setImageResource(android.R.drawable.ic_media_play);
                            }
                            else{
                                vidIsPause = true;
                                makeToast("Video paused");
                                playpause.setImageResource(android.R.drawable.ic_media_pause);
                            }
                        }else break;
                    }else{
                        if (!videoIsPlaying){
                            if (VidNum <=3 && VidNum >= 0){
                                videoIsPlaying = true;
                                orientToPotrait = false;
                                ImageView recentPlayVid = (ImageView) findViewById(R.id.imageView);
                                recentPlayVid.setImageResource(origamies[VidNum].getImage());
                                VidName = origamies[VidNum].getTitle();
                                playVideo();
                                makeToast("Now playing remotely: "+VidName);
                                videoIsPlaying = true;
                                orientToPotrait = true;
                            }
                            else break;
                        }else{
                            if (VidNum == 15){
                                videoIsPlaying = false;
                                orientToPotrait = true;
                                makeToast("Video stopped");
                                onBackPressed();
                            }else if(VidNum == 10 && videoIsPlaying){  //pause and play
                                pausePlayVid();
                            }else {
                                orientToPotrait = false;
                                onBackPressed();
                                VidNum = Integer.parseInt(payload);
                                ImageView recentPlayVid = (ImageView) findViewById(R.id.imageView);
                                recentPlayVid.setImageResource(origamies[VidNum].getImage());
                                VidName = origamies[VidNum].getTitle();
                                playVideo();
                                makeToast("Now playing: "+VidName);
                                videoIsPlaying = true;
                                orientToPotrait = true;
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        readIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        PendingIntent pi = PendingIntent.getActivity(this,0,
                new Intent(this,getClass()),0);
        IntentFilter filter = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        try {
            filter.addDataType("text/plain");
        } catch (IntentFilter.MalformedMimeTypeException e) {
            e.printStackTrace();
        }
        IntentFilter[] filters = {filter};

        nfcAdapter.enableForegroundDispatch(this, pi, filters, null );
    }

    @Override
    protected void onPause() {
        super.onPause();
        nfcAdapter.disableForegroundDispatch(this);
    }
    //For video
    @Override
    public void onBackPressed() {
        FragmentManager fragmentManager = getFragmentManager();
        if(fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStack();
        } else {
            super.onBackPressed();
        }
        if(videoIsPlaying){
            videoIsPlaying = false;
        }
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        // handle the back button directly to avoid pressing back button to close the entire app
    }
    private void makeToast(String m){
        Toast.makeText(this, m, Toast.LENGTH_SHORT).show();
    }
}

