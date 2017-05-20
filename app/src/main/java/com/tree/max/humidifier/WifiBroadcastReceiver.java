package com.tree.max.humidifier;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by max on 17-5-20.
 */

public class WifiBroadcastReceiver extends BroadcastReceiver {

    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private  MainActivity mainActivity;
    public WifiBroadcastReceiver(WifiP2pManager mManager, WifiP2pManager.Channel channel,MainActivity mainActivity)
    {
        this.mainActivity = mainActivity;
        this.mChannel = channel;
        this.mManager = mManager;
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        String action  = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                Toast.makeText(mainActivity,"WIFI已打开",Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(mainActivity,"WIFI已关闭",Toast.LENGTH_LONG).show();
            }


        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            // Call WifiP2pManager.requestPeers() to get a list of current peers
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            // Respond to new connection or disconnections
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            // Respond to this device's wifi state changing
        }

    }
}
