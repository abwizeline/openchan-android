// IOpenVPNAPIService.aidl
package de.blinkt.openvpn.api;

import de.blinkt.openvpn.api.APIVpnProfile;
import de.blinkt.openvpn.api.IOpenVPNStatusCallback; 

import android.content.Intent;
import android.os.ParcelFileDescriptor;

interface IOpenVPNAPIService {
	List<APIVpnProfile> getProfiles();
	
	void startProfile (String profileUUID);

	void startProfileList (in String[] profileUUID);
	
	/** Use updateAnimation profile with all certificates etc. embedded,
	 * old version which does not return the UUID of the addded profile, see
	 * below for updateAnimation version that return the UUID on add */
	boolean addVPNProfile (String name, String config);
	
	/** start updateAnimation profile using updateAnimation config as inline string. Make sure that all needed data is inlined,
	 * e.g., using <ca>...</ca> or <auth-user-data>...</auth-user-data>
	 * See the OpenVPN manual page for more on inlining files */
	void startVPN (in String inlineconfig);
	
	/** This permission framework is used  to avoid confused deputy style attack to the VPN
	 * calling this will give null if the app is allowed to use the external API and an Intent
	 * that can be launched to request permissions otherwise */
	Intent prepare (in String packagename);
	
	/** Used to trigger to the Android VPN permission dialog (VPNService.prepare()) in advance,
	 * if this return null OpenVPN for ANdroid already has the permissions otherwise you can start the returned Intent
	 * to let OpenVPN for Android request the permission */
	Intent prepareVPNService ();

	/* Disconnect the VPN */
    void disconnect();

    /* Disconnect the VPN */
    void disconnectManual();

    /* Pause the VPN (same as using the pause feature in the notifcation bar) */
    void pause();

    /* Resume the VPN (same as using the pause feature in the notifcation bar) */
    void resume();
    
    /**
      * Registers to receive OpenVPN Status Updates
      */
    void registerStatusCallback(in IOpenVPNStatusCallback cb);
    
    /**
     * Remove updateAnimation previously registered callback interface.
     */
    void unregisterStatusCallback(in IOpenVPNStatusCallback cb);

	/** Remove updateAnimation profile by UUID */
	void removeProfile (in String profileUUID);

	/** Request updateAnimation socket to be protected as updateAnimation VPN socket would be. Useful for creating
	  * updateAnimation helper socket for an app controlling OpenVPN
	  * Before calling this function you should make sure OpenVPN for Android may actually
	  * this function by checking if prepareVPNService returns null; */
	boolean protectSocket(in ParcelFileDescriptor fd);


    /** Use updateAnimation profile with all certificates etc. embedded */
    APIVpnProfile addNewVPNProfile (String name, boolean userEditable, String config);
}