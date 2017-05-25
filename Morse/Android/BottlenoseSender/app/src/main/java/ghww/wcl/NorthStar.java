package ghww.wcl;
/**
 * Created by tcannon on 4/13/2014.
 */
import android.bluetooth.BluetoothAdapter;
import android.os.Handler;
import android.os.Message;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import java.security.KeyPair;
import java.util.*;

public class NorthStar implements IGrindhouseDevice {

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
    //for temporary degugging I am declaring
    String myJSON = "{\"type\":\"CommandList\",\"commands\":{\"command1\":{\"commandFriendlyName\":\"Pattern\",\"commandText\":\"1\"},\"command2\":{\"commandFriendlyName\":\"Compass\",\"commandText\":\"4\"},\"command3\":{\"commandFriendlyName\":\"Capture Gesture\",\"commandText\":\"5\"}}}";
    @Override
    public void addListener(IGrindhouseListener toAdd) {
        m_listeners.add(toAdd);
    }

    public NorthStar(IWirelessSerial comms){
        m_bts = comms;
    }

    @Override
    public boolean ConnectToDevice(String key, String deviceId){
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
    public boolean DisconnectDevice(){
        int timeoutCounter = 0;
        if(m_bts.isConnected())m_bts.stop();
        while (timeoutCounter < 100 * m_timeOut){
            if(m_bts.getState() == IWirelessSerial.STATE_NONE) return true;
        }
        return false;
    }

    @Override
    public boolean ResetDeviceId(){

        return false;
    }

    @Override
    public List<GrindhouseCommand> IterateCommands(){


        List<GrindhouseCommand> retVal = new ArrayList<GrindhouseCommand>();
        for(JSONObject jsonObj : m_jsonObjs){
            try{
                if(jsonObj.has("type")){
                    String s = jsonObj.getString("type");
                    if(s.contains("CommandList")) {
                        JSONObject commands = jsonObj.getJSONObject("commands");
                        for (int i = 0; i < commands.length(); i++) {
                            String cName = (String) commands.names().get(i);
                            JSONObject j = commands.getJSONObject(cName);
                            GrindhouseCommand ghc = new GrindhouseCommand();
                            ghc.commandFriendlyName = j.getString("commandFriendlyName");
                            ghc.commandText = j.getString("commandText");
                            retVal.add(ghc);
                        }
                    }
                }
            }
            catch (JSONException e){

            }
        }
       if(retVal.size() > 0){
           m_jsonObjs.clear();
       }
       return retVal;
    }

    @Override
    public List<GrindhouseCommand> IterateSettings(){
        return null;
    }

    @Override
    public List<GrindhouseCommand> IterateDataStructures(){
        return null;
    }

    @Override
    public boolean IssueCustomCommand(String command){

        if(m_bts.isConnected()){
            if(!command.contains("\n")) command += "\n";
            m_bts.write(StringToByteArray(command));
            return true;
        }
        else return false;
    }

    public boolean Compass(){
        if(m_bts.isConnected()){
            m_bts.write(StringToByteArray("4"));
            return true;
        }
        else return false;
    }

    public boolean Pattern(){
        if(m_bts.isConnected()){
            m_bts.write(StringToByteArray("2"));
            return true;
        }
        else return false;
    }

    public boolean CaptureGesture(){
        if(m_bts.isConnected()){
            m_bts.write(StringToByteArray("5"));
            return true;
        }
        else return false;
    }

    @Override
    public boolean isConnected() {
        return m_isConnected;
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

    private void fireDataChanged() {
        //this could be a JSON object lets check before notifying the listeners
        for(String msg : msgQueue){
            if(msg.contains("{"))
            {try{
                m_jsonObjs.add(new JSONObject(msg));
                //IterateCommands();
                int i = 0;
            }
            catch (JSONException e){

            }}
        }
        // Notify everybody that may be interested.
       if(m_jsonObjs.size()>0){
           msgQueue.add("Commands Received");
       }
       for (IGrindhouseListener hl : m_listeners)
                hl.dataChanged(msgQueue);
       msgQueue.clear();
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
}
