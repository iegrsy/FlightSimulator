package com.ieg.flightsimulatorvrhead;

import android.util.Log;

import java.util.Iterator;

import FlightSimulator.FlightSimulatorServiceGrpc;
import FlightSimulator.Fsp;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class VideoStreamUtil {
    private static final String TAG = VideoStreamUtil.class.getSimpleName();

    private String host;
    private int port;

    private boolean isConnect = false;
    private ManagedChannel channel;

    public VideoStreamUtil(String host, int port) {
        this.host = host;
        this.port = port;

        try {
            channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
            isConnect = true;
        } catch (Exception e) {
            e.printStackTrace();
            isConnect = false;
        }
    }

    public void connect(ChangeFrameListener listener) {
        Log.d("debug", "Called connect.");
        GetFrames(listener);
    }

    public void disconnect() {
        if (channel != null) {
            channel.shutdownNow();
            channel = null;
        }
    }

    public boolean isConnect() {
        return isConnect;
    }

    public interface ChangeFrameListener {
        public void onChange(Fsp.CameraStreamQ streamQ);
    }

    private void GetFrames(final ChangeFrameListener listener) {
        if (channel != null) {
            new Thread(new Runnable() {
                @Override
                public void run() throws RuntimeException {
                    try {
                        isConnect = true;
                        FlightSimulatorServiceGrpc.FlightSimulatorServiceBlockingStub blockingStub =
                                FlightSimulatorServiceGrpc.newBlockingStub(channel);

                        Iterator<Fsp.CameraStreamQ> frames = blockingStub.getCameraStream(Fsp.DummyQ.newBuilder().build());

                        while (frames.hasNext()) {
                            if (channel == null)
                                break;

                            Fsp.CameraStreamQ element = frames.next();

                            if (listener != null)
                                listener.onChange(element);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        isConnect = false;
                    }
                }
            }).start();
        } else {
            isConnect = false;
        }
    }
}
