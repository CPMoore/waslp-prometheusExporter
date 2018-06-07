package io.github.cpmoore.waslp.metrics;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.MBeanServerConnection;
import javax.management.ObjectInstance;
import javax.management.ObjectName;



public class RoutedJmxScraper {
	
	public RoutedJmxScraper(JmxRouter router) {
		this.isLocal=true; 
		this.beanConn=router.getMainConnection();
		this.hostName=router.getConnectionHost();
		this.serverName=router.getConnectionServerName();
		this.serverUserDir=router.getConnectionUserDir();
		isLibertyConnection=router.isLibertyServer();
		this.id=router.getURL(); 
		this.formattedName=this.id;
	}
	
	public RoutedJmxScraper(JmxRouter router,String hostName,String serverUserDir,String serverName){
		this.isLocal=false; 
		this.hostName=hostName;
		this.serverUserDir=serverUserDir;
		this.serverName=serverName;
		isLibertyConnection=true;
		this.id=hostName+","+serverUserDir+","+serverName;
		this.formattedName="Host="+hostName+",UserDirectory="+serverUserDir+",Name="+serverName;
		try { 
			    this.beanConn=router.getServerContext(hostName, serverUserDir, serverName);
			}catch(Exception e) {
				logger.log(Level.SEVERE, "Could not get a connection to "+getName(), e);
		} 
	}
	private static String klass=RoutedJmxScraper.class.getName();
	private static Logger logger = Logger.getLogger(klass);
	private String hostName;
	private String serverUserDir;
	private Boolean isLibertyConnection;
	private String formattedName;
	private String id;
	private String serverName;
	private Boolean isLocal=true;
	private MBeanServerConnection beanConn;

	
	private final JmxMBeanPropertyCache jmxMBeanPropertyCache=new JmxMBeanPropertyCache();
	public String getHostName() {
		return hostName;
	}
	public String getServerUserDir() {
		return serverUserDir;
	}
	public String getServerName() {
		return serverName;
	}
	@Override
	public String toString() {
		return getName();
	}
	public Boolean isLocalConnection() {
		
		return isLocal;
	}
	public String getId() {
		return this.id;
	}
	public String getName() {
		return formattedName;
	}
	
	

	public void doScrape(JmxMBeanProcessor.MBeanReceiver receiver,Config config) throws Exception {
		       
	           Set<ObjectName> mBeanNames = new HashSet<ObjectName>();
	           for (ObjectName name : config.whitelistObjectNames) {
	               for (ObjectInstance instance : beanConn.queryMBeans(name, null)) {
	                   mBeanNames.add(instance.getObjectName());
	               }
	           }

	           for (ObjectName name : config.blacklistObjectNames) {
	               for (ObjectInstance instance : beanConn.queryMBeans(name, null)) {
	                   mBeanNames.remove(instance.getObjectName());
	               }
	           }

	           jmxMBeanPropertyCache.onlyKeepMBeans(mBeanNames);

	           for (ObjectName objectName : mBeanNames) {
	               long start = System.nanoTime();
	               JmxMBeanProcessor.scrapeBean(beanConn,receiver, objectName,jmxMBeanPropertyCache);
	               logger.fine("tim: " + (System.nanoTime() - start) + " ns for " + objectName.toString());
	           }
	   }	
 

	
	
}
