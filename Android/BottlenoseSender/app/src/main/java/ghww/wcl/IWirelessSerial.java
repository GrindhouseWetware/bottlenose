package ghww.wcl;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;

/**
 * Created by tcannon on 4/23/2014.
 */
public interface IWirelessSerial {
    // Constants that indicate the current connection state
    int STATE_NONE = 0;       // we're doing nothing
    int STATE_LISTEN = 1;     // now listening for incoming connections
    int STATE_CONNECTING = 2; // now initiating an outgoing connection
    int STATE_CONNECTED = 3;  // now connected to a remote device

    int getState();

    boolean isConnected();

    void start();

    void connect(String deviceAddress, Handler handler);

    void connected(BluetoothSocket socket, BluetoothDevice device);

    void stop();

    void write(byte[] out);
}
