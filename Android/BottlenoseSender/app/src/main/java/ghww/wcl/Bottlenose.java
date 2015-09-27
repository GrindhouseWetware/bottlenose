package ghww.wcl;

import android.os.Handler;
import android.os.Message;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

/**
 * Created by TimmyC on 8/27/2015.
 */
public class Bottlenose implements IGrindhouseDevice {
    private IWirelessSerial m_bts;
    private boolean m_isCollectingJSON = false;
    private int m_timeOut = 2;
    private List<IGrindhouseListener> m_listeners = new ArrayList<IGrindhouseListener>();
    private boolean m_isConnected;
    private List<JSONObject> m_jsonObjs = new ArrayList<JSONObject>();
    private Queue<String> msgQueue = new ArrayDeque<String>();
    private Queue<String> potentialJSON = new ArrayDeque<String>();
    private String m_ConnectedDeviceName;
    private Message currentMSG = null;
    public Bottlenose(IWirelessSerial comms){
        m_bts = comms;
    }

    @Override
    public void addListener(IGrindhouseListener toAdd) {
        m_listeners.add(toAdd);
    }

    @Override
    public boolean ConnectToDevice(String key, String deviceId) {
        int timeoutCounter = 0;

        try {
            m_bts.connect(deviceId,mHandlerBT);
        }
        catch (Exception e){
            msgQueue.add("Device Fail Truck - " + e.getMessage());
            fireDataChanged();
        }

        while (timeoutCounter < 100 * m_timeOut){
            if(m_bts.isConnected()){

                return true;
            }
            timeoutCounter++;
        }
        if(msgQueue.size() > 0) fireDataChanged();
        return false;
    }

    @Override
    public boolean DisconnectDevice() {
        int timeoutCounter = 0;
        if(m_bts.isConnected())m_bts.stop();
        while (timeoutCounter < 100 * m_timeOut){
            if(m_bts.getState() == IWirelessSerial.STATE_NONE) return true;
        }
        return false;
    }

    @Override
    public boolean ResetDeviceId() {
        return false;
    }

    @Override
    public List<GrindhouseCommand> IterateCommands() {
        return null;
    }

    @Override
    public List<GrindhouseCommand> IterateSettings() {
        return null;
    }

    @Override
    public List<GrindhouseCommand> IterateDataStructures() {
        return null;
    }

    @Override
    public boolean IssueCustomCommand(String command) {
        if(m_bts.isConnected()){
            //if(!command.contains("\n")) command += "\n";
            m_bts.write(StringToByteArray(command));
            return true;
        }
        else return false;
    }

    @Override
    public boolean isConnected() {
        return m_isConnected;
    }
    private final Handler mHandlerBT = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            currentMSG = msg;
            msgQueuingThread.run();

        }
    };
    private final Runnable msgQueuingThread  = new Runnable() {
        @Override
        public void run() {
            switch (currentMSG.what) {
                case MESSAGE_STATE_CHANGE:
                    switch (currentMSG.arg1) {
                        case IWirelessSerial.STATE_CONNECTED:
                            m_isConnected = true;
                            //ExtractJSONObjects(msgQueue);
                            msgQueue.add("You are connected");
                            fireDataChanged();
                            break;

                        case IWirelessSerial.STATE_CONNECTING:
                            msgQueue.add("You are connecting....");
                            fireDataChanged();
                            break;

                        case IWirelessSerial.STATE_LISTEN:
                        case IWirelessSerial.STATE_NONE:
                            m_isConnected = false;
                            msgQueue.add("Disconnected....");
                            fireDataChanged();
                            break;
                    }
                    break;
                case MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) currentMSG.obj;
                    msgQueue.add(new String(writeBuf));
                    fireDataChanged();
                    break;

                case MESSAGE_READ:
                    byte[] readBuf = (byte[]) currentMSG.obj;
                    String str = new String(readBuf);
                    msgQueue.add(str);
                    //fireDataChanged();
                    break;

                case MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    m_ConnectedDeviceName = currentMSG.getData().getString(DEVICE_NAME);
                    msgQueue.add("Connected to "+ m_ConnectedDeviceName);
                    break;
                case MESSAGE_TOAST:
                    String str2 = currentMSG.getData().getString("toast");
                    if(str2 != null)msgQueue.add(str2);
                    break;
            }
            if(msgQueue.size() > 0) fireDataChanged();
        }
    };
    private void fireDataChanged() {
        // Notify everybody that may be interested.
        if(m_jsonObjs.size()>0){
            msgQueue.add("Commands Received");
        }
        for (IGrindhouseListener hl : m_listeners)
            hl.dataChanged(msgQueue);
        msgQueue.clear();
    }
    private byte[] StringToByteArray(String str) {
        byte[] retVal =  new byte[str.length()];
        int i = 0;
        for (char c : str.toCharArray()) {
            retVal[i] = (byte) c;
            i++;
        }
        return retVal;
    }
}
