package nl.uva.sne.daci.tenant.svcmanagement;

public interface TenantSvc {
	
	public boolean createTenant(String tenantId);
	
	//public int instantiateTenant(String tenantId);
	
	public boolean removeTenant(String tenantId);
}
