package ghww.wcl;

/**
 * Created by tcannon on 4/24/2014.
 */
public class GrindhouseDeviceFactory {

    public static IGrindhouseDevice GetDevice(DeviceType device,CommunicationType commType){
        IWirelessSerial thisComms = GetComms(commType);
        return GetHardware(device,thisComms);
    }

    private static IWirelessSerial GetComms(CommunicationType commType) {
        switch(commType){
            case UNKNOWN:
                return null;
            case BLUETOOTH:
                return new BluetoothSerialService();
            case WIFI:
                break;
            case XBEE:
                break;
            default:
                break;
        }
        return null;
    }

    private static IGrindhouseDevice GetHardware(DeviceType device, IWirelessSerial comms) {
        switch (device) {
            case UNKNOWN:
                return null;
            case NORTHSTAR:
                return new NorthStar(comms);
            case CIRCADIA:
                break;
            case BOTTLENOSE:
                return new Bottlenose(comms);
        }
        return null;
    }

}

