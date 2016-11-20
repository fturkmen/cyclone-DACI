package nl.uva.sne.daci.authzsvcpdp;

public interface PDPSvcPool {
	PDPSvc getService(String id) throws Exception;
	
//	void addService(String id, PDPSvc svc);
//	
//	void removeService(String id);		
}
