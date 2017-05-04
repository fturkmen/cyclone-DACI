package nl.uva.sne.daci.tenant.tenantadmin;

public interface TenantSvc {
	
	public boolean createTenant(String tenantId);
	
	//public int instantiateTenant(String tenantId);
	
	public boolean removeTenant(String tenantId);
}
