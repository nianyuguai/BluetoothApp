package nianyu.app;

import java.lang.reflect.Method;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

public class BluetoothMethod {
	/** 
     * ���豸��� �ο�Դ�룺platform/packages/apps/Settings.git 
     * /Settings/src/com/android/settings/bluetooth/CachedBluetoothDevice.java 
     */  
    static public boolean createBond(Class btClass,BluetoothDevice btDevice) throws Exception {  
        Method createBondMethod = btClass.getMethod("createBond");  
        Boolean returnValue = (Boolean) createBondMethod.invoke(btDevice);  
        return returnValue.booleanValue();  
    }  
    
    /** 
     * ���豸������ �ο�Դ�룺platform/packages/apps/Settings.git 
     * /Settings/src/com/android/settings/bluetooth/CachedBluetoothDevice.java 
     */  
    static public boolean removeBond(Class btClass,BluetoothDevice btDevice) throws Exception {  
        Method removeBondMethod = btClass.getMethod("removeBond");  
        Boolean returnValue = (Boolean) removeBondMethod.invoke(btDevice);  
        return returnValue.booleanValue();  
    } 
    
    static public BluetoothSocket createRfcommSocket(Class btClass,BluetoothDevice btDevice)throws Exception{
    	Method createRfcommSocketMethod = btClass.getMethod("createRfcommSocket");
    	BluetoothSocket returnValue = (BluetoothSocket)createRfcommSocketMethod.invoke(btDevice);
    	return returnValue;
    }
    
    static public boolean setPin(Class btClass, BluetoothDevice btDevice,
			String str) throws Exception
	{
		try
		{
			Method removeBondMethod = btClass.getDeclaredMethod("setPin",
					new Class[]{byte[].class});
			Boolean returnValue = (Boolean) removeBondMethod.invoke(btDevice,
					new Object[]
					{str.getBytes()});
		}
		catch (SecurityException e)
		{
			// throw new RuntimeException(e.getMessage());
			e.printStackTrace();
		}
		catch (IllegalArgumentException e)
		{
			// throw new RuntimeException(e.getMessage());
			e.printStackTrace();
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;

	}

	// ȡ���û�����
	static public boolean cancelPairingUserInput(Class btClass,
			BluetoothDevice device)

	throws Exception
	{
		Method createBondMethod = btClass.getMethod("cancelPairingUserInput");
	    //cancelBondProcess();
		Boolean returnValue = (Boolean) createBondMethod.invoke(device);
		return returnValue.booleanValue();
	}

	// ȡ�����
	static public boolean cancelBondProcess(Class btClass,
			BluetoothDevice device)

	throws Exception
	{
		Method createBondMethod = btClass.getMethod("cancelBondProcess");
		Boolean returnValue = (Boolean) createBondMethod.invoke(device);
		return returnValue.booleanValue();
	}

}
