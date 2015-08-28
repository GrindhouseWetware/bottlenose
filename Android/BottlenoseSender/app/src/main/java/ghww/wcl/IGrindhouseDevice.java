package ghww.wcl;

import java.util.List;

/**
 * Created by tcannon on 4/24/2014.
 */
public interface IGrindhouseDevice {
    int MESSAGE_STATE_CHANGE = 1;
    int MESSAGE_READ = 2;
    int MESSAGE_WRITE = 3;
    int MESSAGE_DEVICE_NAME = 4;
    int MESSAGE_TOAST = 5;
    String DEVICE_NAME = "device_name";

    void addListener(IGrindhouseListener toAdd);

    boolean ConnectToDevice(String key, String deviceId);

    boolean DisconnectDevice();

    boolean ResetDeviceId();

    List<GrindhouseCommand> IterateCommands();

    List<GrindhouseCommand> IterateSettings();

    List<GrindhouseCommand> IterateDataStructures();

    boolean IssueCustomCommand(String command);

    boolean isConnected();
}
