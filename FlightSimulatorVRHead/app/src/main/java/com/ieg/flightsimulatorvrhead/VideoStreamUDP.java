package com.ieg.flightsimulatorvrhead;

import android.app.Activity;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class VideoStreamUDP {
    public static final String UDP_BROADCAST = "UDPBroadcast";

    private static String localIP = null;
    private static int listenPort = 9999;

    private DatagramSocket socket;

    private Boolean shouldRestartSocketListen = true;

    private ChangeFrameListener frameListener = null;

    public interface ChangeFrameListener {
        public void onChange(byte[] data);
    }

    public void setFrameListener(ChangeFrameListener frameListener) {
        this.frameListener = frameListener;
    }

    public VideoStreamUDP(@NonNull Activity context) {
        try {
            localIP = Helper.getLocalIp(context);
        } catch (Exception e) {
            e.printStackTrace();
            localIP = "192.168.1.22";
        }
    }

    private void listen(Integer port) {
        try {
            DatagramSocket udpSocket = new DatagramSocket(port);
            byte[] message = new byte[40054];
            DatagramPacket packet = new DatagramPacket(message, message.length);
            Log.i("UDP client: ", "about to wait to receive " + localIP +":" + port);
            udpSocket.receive(packet);
            String text = new String(message, 0, packet.getLength());
            Log.d("Received data", text);

            if (packet.getLength() > 0)
                if (frameListener != null)
                    frameListener.onChange(packet.getData());
        } catch (IOException e) {
            Log.e("debug", "UDP client error: " + e.getMessage());
        }
    }

    public void startListen() {
        Thread UDPBroadcastThread = new Thread(new Runnable() {
            public void run() {
                try {
                    Integer port = listenPort;
                    while (shouldRestartSocketListen) {
                        listen(port);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        UDPBroadcastThread.start();
    }

    public void stopListen() {
        shouldRestartSocketListen = false;
        if (socket != null)
            socket.close();
    }
}

