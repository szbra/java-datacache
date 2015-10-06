package com.ibm.cloudoe.ecaas.samples;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.ibm.json.java.JSONObject;
import com.ibm.websphere.objectgrid.ObjectGrid;
import com.ibm.websphere.objectgrid.ObjectGridException;
import com.ibm.websphere.objectgrid.ObjectMap;
import com.ibm.websphere.objectgrid.Session;

/**
 * Define the elastic caching Operation, mainly in order to program operation.
 * 
 * You can refer to the Elastic Caching Java Native API Specification
 * http://pic.dhe.ibm.com/infocenter/wdpxc/v2r5/index.jsp?topic=%2Fcom.ibm.websphere.datapower.xc.doc%2Fcxslibertyfeats.html
 */
public class ECacheConnection {

	// define session instance of ObjectGrid
	private static Session ogSession;

	// define temporary store grid entries keys
	private static List<String> keys;

	// define the map name of the stored keys
	private static String keysMapName = "sample2.NONE.P";
	// define the key name of the stored keys
	private static String keyNameOfKeysMap = "keys.store";

	static {
		initECaaS();
	}

	/**
	 * Initialize the session instance of ObjectGrid.
	 */
	public static void initECaaS() {
		
		// There are two ways of obtaining the connection information for some services in Java 
		
		// Method 1: Auto-configuration and JNDI		
		// The Liberty buildpack automatically generates server.xml configuration 
		// stanzas for the DataCache service which contain the credentials needed to 
		// connect to the service. The buildpack generates a JNDI name following  
		// the convention of "wxs/<service_name>" where the <service_name> is the 
		// name of the bound service. Then a JNDI lookup is all that is needed to 
		// obtain an ObjectGrid instance as shown below. Note that the service name 
		// for this boilerplate also contains the application name but in general 
		// that is not required.
		
		try {
			InitialContext ic = new InitialContext();
			// Default service instance name is "<appname>-DataCache"
			ObjectGrid og = (ObjectGrid) ic.lookup("wxs/" + getAppName() + "-DataCache");
			ogSession = og.getSession();
		} catch (NamingException e) {
			System.out.println("Failed to find cache configuration in server.xml!");
			e.printStackTrace();
		} catch (ObjectGridException e) {
			System.out.println("Failed to connect to grid!");
			e.printStackTrace();
		} 

		// Method 2: Parsing VCAP_SERVICES environment variable
		// The VCAP_SERVICES environment variable contains all the credentials of 
		// services bound to this application. You can parse it to obtain the information 
		// needed to connect to the DataCache service. DataCache is a service
		// that the Liberty buildpack auto-configures as described above, so parsing
		// VCAP_SERVICES is not a best practice. The following commented-out code is 
		// an example of how you would do that to connect to the DataCache service,
		// though, if you opted-out of the auto-configuration.
		
//		String username = null;
//		String password = null;
//		String endpoint = null;
//		String gridName = null;
		
//		Map<String, String> env = System.getenv();
//		String vcap = env.get("VCAP_SERVICES");
//
//		boolean foundService = false;
//		if (vcap == null) {
//			System.out.println("No VCAP_SERVICES found");
//		} else {
//			try {
//				// parse the VCAP JSON structure
//				JSONObject obj = JSONObject.parse(vcap);
//				for (Iterator<?> iter = obj.keySet().iterator(); iter.hasNext();) {
//					String key = (String) iter.next();
//					System.out.printf("Found service: %s\n", key);
//					if (key.startsWith("DataCache")) {
//						JSONArray val = (JSONArray)obj.get(key)!=null?(JSONArray)obj.get(key):null;
//						if(val!=null){
//							JSONObject serviceAttr = val.get(0)!=null?(JSONObject)val.get(0):null;
//							JSONObject credentials = serviceAttr!=null?(serviceAttr.get("credentials")!=null?(JSONObject)serviceAttr.get("credentials"):null):null;
//							username = credentials.get("username") !=null?(String) credentials.get("username"):"";
//							password =  (String) credentials.get("password") !=null?(String) credentials.get("password"):"";
//							endpoint =  (String) credentials.get("catalogEndPoint") !=null?(String) credentials.get("catalogEndPoint"):"";
//							gridName =  (String) credentials.get("gridName") !=null?(String) credentials.get("gridName"):"";
//							System.out.println("Found configured username: " + username);
//							System.out.println("Found configured password: " + password);
//							System.out.println("Found configured endpoint: " + endpoint);
//							System.out.println("Found configured gridname: " + gridName);
//							foundService = true;
//							break;
//						}
//					}
//				}
//			} catch (Exception e) {
//			}
//		}
//		if (!foundService) {
//			System.out.println("Did not find WXS service, using defaults");
//		}
//		try {
//			// ObjectGridManager is responsible for creating or retrieving local
//			// ObjectGrid instances and connecting to distributed ObjectGrid servers.
//			ObjectGridManager ogm = ObjectGridManagerFactory
//					.getObjectGridManager();
//			// Connect to the catalog service by obtaining a
//			// ClientClusterContext instance.
//			ClientSecurityConfiguration csc = null;
//			// Set the necessary security credentials.
//			csc = ClientSecurityConfigurationFactory
//					.getClientSecurityConfiguration();
//			csc.setCredentialGenerator(new UserPasswordCredentialGenerator(
//					username, password));
//			csc.setSecurityEnabled(true);
//			ClientClusterContext ccc = ogm.connect(endpoint, csc, null);
//			// Obtain an ObjectGrid instance.
//			ObjectGrid clientGrid = ogm.getObjectGrid(ccc, gridName);
//			// Get a Session instance.
//			ogSession = clientGrid.getSession();
//		} catch (Exception e) {
//			System.out.println("Failed to connect to grid!");
//			e.printStackTrace();
//		}		
	}
	
	private static String getAppName() {
		String app = System.getenv("VCAP_APPLICATION");
		if (app == null) {
			System.out.println("No VCAP_APPLICATION found");
		} else {
			try {
				JSONObject obj = JSONObject.parse(app);
				String name = (String) obj.get("application_name");
				if (name == null) {
					System.out.println("VCAP_APPLICATION application_name not set");
				} else {
					return name;
				}
			} catch (IOException e) {
				System.out.println("Failed to parse VCAP_APPLICATION for application_name");
			}
		}
		return null;
	}

	/**
	 * Get value of this key in mapName
	 * 
	 * @param mapName
	 * @param key
	 * @return
	 * @throws ObjectGridException
	 */
	public static Object getData(String mapName, String key)
			throws ObjectGridException {
		ObjectMap map = ogSession.getMap(mapName);
		return map.get(key);
	}

	/**
	 * Update or insert this value in mapName
	 * 
	 * @param mapName
	 * @param key
	 * @param newValue
	 * @throws ObjectGridException
	 */
	public static void postData(String mapName, String key, String newValue)
			throws ObjectGridException {
		ObjectMap map = ogSession.getMap(mapName);
		map.upsert(key, newValue);
		postKeyTemp(key);
	}

	/**
	 * Delete this key/value in mapName
	 * 
	 * @param mapName
	 * @param key
	 * @throws ObjectGridException
	 */
	public static void deleteData(String mapName, String key)
			throws ObjectGridException {
		ObjectMap map = ogSession.getMap(mapName);
		map.remove(key);
		deleteKeyTemp(key);
	}

	/**
	 * Get all ECache Object in mapName
	 * 
	 * @param mapName
	 * @return
	 * @throws ObjectGridException
	 */
	public static List<ECache> getAllData(String mapName)
			throws ObjectGridException {
		ObjectMap map = ogSession.getMap(mapName);
		keys = getAllKeys(keysMapName);
		List<String> values = map.getAll(keys);
		return getECaches(keys, values);
	}

	/**
	 * Get all keys in mapName
	 * 
	 * @param map
	 * @return
	 * @throws ObjectGridException
	 */
	public static List<String> getAllKeys(String mapName)
			throws ObjectGridException {
		ObjectMap keysMap = ogSession.getMap(mapName);
		keys = keysMap.get(keyNameOfKeysMap) != null ? (List<String>) keysMap
				.get(keyNameOfKeysMap) : new ArrayList<String>();
		return keys;
	}

	/**
	 * Add this key in temp keys map
	 * 
	 * @param key
	 * @throws ObjectGridException
	 */
	private static void postKeyTemp(String key) throws ObjectGridException {
		ObjectMap keysMap = ogSession.getMap(keysMapName);
		keys = getAllKeys(keysMapName);
		if (!keys.contains(key))
			keys.add(key);
		keysMap.upsert(keyNameOfKeysMap, keys);
	}

	/**
	 * Delete this key/value in temp keys map
	 * 
	 * @param key
	 * @throws ObjectGridException
	 */
	private static void deleteKeyTemp(String key) throws ObjectGridException {
		ObjectMap keysMap = ogSession.getMap(keysMapName);
		keys = getAllKeys(keysMapName);
		if (keys.contains(key))
			keys.remove(key);
		keysMap.upsert(keyNameOfKeysMap, keys);
	}

	/**
	 * Get all ECache Object
	 * 
	 * @param keys
	 * @param values
	 * @return
	 */
	public static List<ECache> getECaches(List<String> keys, List<String> values) {
		List<ECache> res = new ArrayList<ECache>();
		for (int i = 0; i < keys.size(); i++) {
			res.add(new ECache(keys.get(i), values.get(i)));
		}
		return res;
	}

}