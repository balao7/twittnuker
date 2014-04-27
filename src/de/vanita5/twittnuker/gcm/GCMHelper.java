package de.vanita5.twittnuker.gcm;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;

import de.vanita5.twittnuker.TwittnukerConstants;
import de.vanita5.twittnuker.gcm.backend.PushBackendServer;
import de.vanita5.twittnuker.util.PushBackendHelper;
import de.vanita5.twittnuker.util.SharedPreferencesWrapper;
import de.vanita5.twittnuker.util.Utils;
import retrofit.RetrofitError;

public class GCMHelper implements TwittnukerConstants {

	/*********
	 * UTILS *
	 *********/

	private static boolean isPlayServicesAvailable(final Context context) {
		int resultCode = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(context);
		return resultCode == ConnectionResult.SUCCESS;
	}

	private static int getAppVersion(final Context context) {
		try {
			PackageInfo packageInfo = context.getPackageManager()
					.getPackageInfo(context.getPackageName(), 0);
			return packageInfo.versionCode;
		}
		catch (PackageManager.NameNotFoundException e) {
			// should never happen
			throw new RuntimeException("Could not get package name: " + e);
		}
	}

	private static void storeRegistrationId(final Context context, final String regid) {
		final SharedPreferencesWrapper preferencesWrapper = SharedPreferencesWrapper
				.getInstance(context, SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		preferencesWrapper.edit()
				.putString(KEY_REGID, regid)
				.putInt(KEY_APP_VERSION, getAppVersion(context))
				.commit();
	}

	private static void setPushRegistered(final Context context, final boolean success) {
		final SharedPreferencesWrapper preferencesWrapper = SharedPreferencesWrapper
				.getInstance(context, SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		preferencesWrapper.edit()
				.putBoolean(KEY_PUSH_REGISTERED, success)
				.commit();
	}

	/**
	 * Returns the saved registration id.
	 * CAUTION: The returned reg id may not be valid and/or current
	 * Please use getRegistrationId instead
	 *
	 * @param context
	 * @return
	 */
	public static String getSavedRegistrationId(final Context context) {
		final SharedPreferencesWrapper preferencesWrapper = SharedPreferencesWrapper.
				getInstance(context, SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		return preferencesWrapper.getString(KEY_REGID, "");
	}

	/**
	 * Only returns the saved registration id if the saved reg id
	 * has been generated by this app version.
	 *
	 * This getter should be prefered, as it only returns a valid/current reg id
	 *
	 * @param context
	 * @return
	 */
	private static String getRegistrationId(final Context context) {
		final SharedPreferencesWrapper preferencesWrapper = SharedPreferencesWrapper.
				getInstance(context, SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		final String regid = preferencesWrapper.getString(KEY_REGID, "");

		if (regid.isEmpty()) {
			return "";
		}
		// Check if app was updated; if so, it must clear the registration ID
		// since the existing regID is not guaranteed to work with the new
		// app version.
		int registeredVersion = preferencesWrapper.getInt(KEY_APP_VERSION, Integer.MIN_VALUE);
		int currentVersion = getAppVersion(context);
		if (registeredVersion != currentVersion) {
			return "";
		}
		return regid;
	}

	/******************
	 * PUBLIC METHODS *
	 ******************/

	public static void registerIfNotAlreadyDone(final Context context) {
		if (!isPlayServicesAvailable(context)) {
			//do nothing
			if (Utils.isDebugBuild()) Toast.makeText(context, "Play Services not available on this device!", Toast.LENGTH_LONG).show();
			return;
		}

		final String regid = getRegistrationId(context);
		if (regid.isEmpty()) {
			registerForGCM(context);
		} else {
			if (Utils.isDebugBuild()) {
				Toast.makeText(context, "Already registered!", Toast.LENGTH_LONG).show();
			}
		}
	}

	public static void unregisterGCM(final Context context) {
		//We don't care if play services are available, because we don't unregister from GCM
		//Just unregister from backend!

		//FIXME This is wrong. We should unregister WITH the regid!
		//The reg dd is device specific. A user could have more than one device...

		final String regid = getRegistrationId(context);
		if (regid.isEmpty()) {
			if (Utils.isDebugBuild()) {
				Toast.makeText(context, "Could not find a regid on the device...", Toast.LENGTH_LONG).show();
			}
		} else {
			unregisterFromGCM(context);
		}
	}

	public static void addAccount(final Context context) {
		//TODO Replace Sample Account Id
		final String MOCK_ACCOUNT = "1234567890";

		if(getRegistrationId(context).isEmpty()) {
			//Client is either not registered or has been updated.
			//We first need to (re-)register...
			Toast.makeText(context, "Re-registering...", Toast.LENGTH_SHORT).show();
			registerForGCM(context);
		}
		addAccount(context, MOCK_ACCOUNT);
	}

	public static void removeAccount(final Context context) {
		//TODO Replace Sample Account Id
		final String MOCK_ACCOUNT = "1234567890";

		if(getSavedRegistrationId(context).isEmpty()) {
			//Client has never been registered
			return;
		}
		removeAccount(context, MOCK_ACCOUNT);
	}

	/***************
	 * ASYNC TASKS *
	 ***************/

	private static void registerForGCM(final Context context) {
		new AsyncTask<Void, Void, Void>() {

			private String msg;

			@Override
			protected Void doInBackground(Void... params) {
				try {
					GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);
					final String regid = gcm.register(GCMConfig.SENDER_ID);

					if (sendRegistrationIdToBackend(context, regid)) {
						msg = "Successfully registered for Push!";
						// Persist the regID - no need to register again.
						storeRegistrationId(context, regid);
						setPushRegistered(context, true);
					} else {
						setPushRegistered(context, false);
						if (regid == null || regid.isEmpty()) {
							msg = "Could not register at GCM Service";
						} else {
							msg = "Could not connect to the backend server.";
						}
					}
				} catch (IOException ex) {
					// If there is an error, don't just keep trying to register.
					// Require the user to click a button again, or perform
					// exponential back-off.
					//TODO
					if (Utils.isDebugBuild()) Log.e("GCMHelper", ex.getMessage());
					setPushRegistered(context, false);
				} catch (RetrofitError e) {
					//TODO RetrofitError Handling by HTTP Error
					msg = "Could not connect to the backend server.\nCheck your settings!";
					setPushRegistered(context, false);
				}
				return null;
			}

			@Override
			protected void onPostExecute(Void aVoid) {
				Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
			}
		}.execute();
	}

	private static void unregisterFromGCM(final Context context) {
		//You can unregister from GMC directly.
		//However, it is recommended to unregister from the backend server ONLY!
		new AsyncTask<Void, Void, Void>() {

			private String msg;

			@Override
			protected Void doInBackground(Void... voids) {
				try {
					String regid = getRegistrationId(context);
					if (regid == null || regid.isEmpty()) {
						regid = getSavedRegistrationId(context);
					}

					if (sendUnregisterRequestToBackend(context, regid)) {
						msg = "Successfully unregistered from Push!";
						storeRegistrationId(context, "");
						setPushRegistered(context, false);
					} else {
						msg = "Could not connect to backend server";
					}
				} catch (RetrofitError e) {
					//TODO handle http error code
					msg = "Could not connect to the backend server.\nCheck your settings!";
				}
				return null;
			}

			@Override
			protected void onPostExecute(Void aVoid) {
				Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
			}
		}.execute();
	}

	private static void addAccount(final Context context, final String accountId) {
		new AsyncTask<Void, Void, Void>() {

			private String msg;

			@Override
			protected Void doInBackground(Void... voids) {
				try {
					PushBackendServer.AccountMSG item = addAccountToBackend(context, accountId);
					if (item != null) {
						msg = "Account successfully added";
						//TODO persist settings on device
					} else {
						msg = "Could not add account to server. Response is empty?";
					}
				} catch (RetrofitError e) {
					msg = "Could not connect to backend server";
				}
				return null;
			}

			@Override
			protected void onPostExecute(Void aVoid) {
				Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
			}
		}.execute();
	}

	private static void removeAccount(final Context context, final String accountId) {
		new AsyncTask<Void, Void, Void>() {

			private String msg;


			@Override
			protected Void doInBackground(Void... voids) {
				try {
					if(removeAccountFromBackend(context, accountId)) {
						msg = "Account successfully removed";
					} else {
						msg = "Could not connect to backend server";
					}
				} catch (RetrofitError e) {
					msg = "Retrofit Error";
					//TODO Handle HTTP Error Codes
				}
				return null;
			}

			@Override
			protected void onPostExecute(Void aVoid) {
				Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
			}
		}.execute();
	}

	/**************************
	 * BACKEND SERVER METHODS *
	 **************************/

	private static boolean sendRegistrationIdToBackend(final Context context,
			final String regid) throws RetrofitError {
		// Need to get an access token first
		final String token = PushBackendHelper.getAuthToken(context,
				PushBackendHelper.getSavedAccountName(context));
		if (token == null) return false;

		// token should be good. Transmit
		final PushBackendServer server = PushBackendHelper.getRESTAdapter(context);
		final PushBackendServer.Regid item = new PushBackendServer.Regid();
		item.regid = regid;

		server.registerGCM(token, item);

		return true;
	}

	private static boolean sendUnregisterRequestToBackend(final Context context,
			final String regid) throws RetrofitError {
		// Need to get an access token first
		final String token = PushBackendHelper.getAuthToken(context,
				PushBackendHelper.getSavedAccountName(context));
		if (token == null) {
			return false;
		}

		final PushBackendServer server = PushBackendHelper.getRESTAdapter(context);
		final PushBackendServer.Regid item = new PushBackendServer.Regid();
		item.regid = regid;

		server.unregisterGCM(token, item);

		return true;
	}

	private static PushBackendServer.AccountMSG addAccountToBackend(final Context context, String accountId) throws RetrofitError {
		final String token = PushBackendHelper.getAuthToken(context,
				PushBackendHelper.getSavedAccountName(context));
		if (token == null) {
			return null;
		}

		final PushBackendServer server = PushBackendHelper.getRESTAdapter(context);

		//Create new AccountMSG with only an accountId.
		//The other fields have default values server-side
		final PushBackendServer.AccountMSG account = new PushBackendServer.AccountMSG(accountId);

		//Add account to backend, and receive a persistable AccountItem
		final PushBackendServer.AccountMSG dbItem = server.addAccount(token, account);

		return dbItem;
	}

	private static boolean removeAccountFromBackend(final Context context, final String accountId) throws RetrofitError {
		final String token = PushBackendHelper.getAuthToken(context,
				PushBackendHelper.getSavedAccountName(context));
		if (token == null) {
			return false;
		}

		final PushBackendServer server = PushBackendHelper.getRESTAdapter(context);

		//account to remove
		final PushBackendServer.AccountMSG account = new PushBackendServer.AccountMSG(accountId);
		server.removeAccount(token, account);

		return true;
	}

	//TODO Update settings on server

	//TODO Get settings from server (do we actually need this?)
}
