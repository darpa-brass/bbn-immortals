package mil.darpa.immortals.dfus.location;

import android.content.Context;
import android.Manifest;
import android.content.pm.PackageManager;
import java.lang.reflect.Method;

public class NoUICompatibility {

	private NoUICompatibility() {}
	
	public static int checkSelfPermission(Context context, String requestedPermission) {
		
		return PackageManager.PERMISSION_DENIED;
		
	}
	
	public static boolean shouldShowRequestPermissionRationale(Context context, String requestedPermission) {
		
		return false;//don't show rationale since we have no UI
	}
	
	public static void requestPermissions(Object context, String[] permissions, int requestCode) {
		
		//Simulate asking user for permission and the user approving
		//Then perform call back to calling code to report results of user's choice
		
		int[] grantResults = new int[1];
		
		grantResults[0] = PackageManager.PERMISSION_GRANTED;
		
		try {
			Method permissionResult
				= LocationProviderAndroidGpsBuiltIn.class.getMethod("onRequestPermissionsResult", int.class, String[].class, int[].class);
			permissionResult.invoke(context, requestCode, permissions, grantResults);
		} catch (Exception e) {
			System.out.println("Failure in NoUICompatibility.requestPermissions:" + e);
			//Something wrong; not sending out callback
		}

	}

}
