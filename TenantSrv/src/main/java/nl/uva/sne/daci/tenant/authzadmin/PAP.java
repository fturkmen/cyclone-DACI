package nl.uva.sne.daci.tenant.authzadmin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import nl.uva.sne.daci.tenant.tenantadmin.Configuration;
import nl.uva.sne.daci.utils.PolicySetupUtil;
import redis.clients.jedis.Jedis;
import java.io.*;


public class PAP {

	//private ArrayList<String> redisInsertedKeys;
	
	private static final String INTRATENANT_POLICY1 = "policies/BioinformaticsCyclone.IFB1_Tenant.xml";
	private static final String INTRATENANT_POLICY2 = "policies/BioinformaticsCyclone.IFB2_Tenant.xml";
	private static final String INTRATENANT_POLICY3 = "policies/BioinformaticsCyclone.ICL_Tenant.xml";
	
	
	private static final String PROVIDER_POLICY1 = "policies/BioinformaticsCyclone.LAL_ProviderPolicySet.xml";
	private static final String PROVIDER_POLICY2 = "policies/BioinformaticsCyclone.WTHI_ProviderPolicySet.xml";
	
	private static final String INTERTENANT_POLICY1 = "policies/BioinformaticsCyclone.IntertenantPoliciesIFB1_Tenant.xml";
	private static final String INTERTENANT_POLICY2 = "policies/BioinformaticsCyclone.IntertenantPoliciesIFB2_Tenant.xml";
	
	
	
	private String domain;
	
	public PAP(String dom){
		domain = dom;
	}
	
	private String getInterTenantPolicyKey(String trustor) {
		if (trustor == null || trustor.isEmpty())
			throw new RuntimeException("Empty trustor tenant identifier");
		return String.format(Configuration.INTERTENANT_KEY_STYLE, domain) + trustor;
	}
	
	private String getProviderPolicyKey() {
		return String.format(Configuration.PROVIDER_KEY_STYLE, domain);
	}
	
	
	private <T> Map<String, String> loadIntertenantPolicies(T policyName, Jedis jedis){
		try {
			
			// load inter-tenant policies to redis
			Map<String, String> tenantPolicies = null;
			if (policyName instanceof InputStream)
				tenantPolicies = PolicySetupUtil.loadPolicies((InputStream)policyName);
			else 
				tenantPolicies = PolicySetupUtil.loadPolicies((String) policyName);
			
			for(String pId : tenantPolicies.keySet()) {	
				
				String pKey = getInterTenantPolicyKey(pId);
				//redisInsertedKeys.add(pKey);
				jedis.set(pKey, tenantPolicies.get(pId));			
			}
			return tenantPolicies;
		}catch(Exception e){
			System.err.println("Exception in loadIntertenantPolicies" + e.getMessage());
			return null;
		}finally{
			jedis.disconnect();
		}
	}
	

	
	private <T> Map<String, String> loadIntratenantPolicies(T policyName, Jedis jedis, String domain){
		try{
			
			Map<String, String> intraTenantPolicies = null;
			if (policyName instanceof InputStream)	
				intraTenantPolicies = PolicySetupUtil.loadPolicies((InputStream)policyName);
				//intraTenantPolicies = PolicySetupUtil.loadPolicyorPolicySets((InputStream)policyName);
			else 
				intraTenantPolicies = PolicySetupUtil.loadPolicies((String)policyName);
				//intraTenantPolicies = PolicySetupUtil.loadPolicyorPolicySets((String)policyName);
			
			String authzPolicyKeyPrefix = String.format( Configuration.REDIS_KEYPREFIX_FORMAT, domain);			
			for(String pId : intraTenantPolicies.keySet()) {
				String pKey = authzPolicyKeyPrefix + ":" + pId;
				//redisInsertedKeys.add(pKey);			
				jedis.set(pKey, intraTenantPolicies.get(pId));
				System.out.println("Put intra-tenant policy:" + pKey);
			}
			return intraTenantPolicies;
		}catch(Exception e){
			System.err.println("Exception in loadIntratenantPolicies"+ e.getMessage());
			return null;
		}finally{
			jedis.disconnect();
		}
	}
	

	
	
	public void setProviderPolicy(String redisAddress, InputStream policy) throws Exception{
		Jedis jedis = new Jedis(redisAddress);
		try {
			jedis.set(getProviderPolicyKey(), PolicySetupUtil.loadPolicySet(policy));
		}finally{
			jedis.disconnect();
		}
	}
	
	public void setIntertenantPolicy(String redisAddress, InputStream policy) throws Exception{
		Jedis jedis = new Jedis(redisAddress);
		try {
			loadIntertenantPolicies(policy, jedis);
		}finally{
			jedis.disconnect();
		}
	}
	
	public void setIntratenantPolicy(String redisAddress, InputStream policy) throws Exception{
		Jedis jedis = new Jedis(redisAddress);
		try {
			loadIntratenantPolicies(policy, jedis, domain);
		}finally{
			jedis.disconnect();
		}
	}
	
	
	/*Setup policies ...*/
	public List<String> setupPolicies(String redisAddress, String domain) throws Exception {
	
		Jedis jedis = new Jedis(redisAddress);
		try {	
			// Load provider policies to redis
			//redisInsertedKeys.add(ctxMan.getProviderPolicyKey());
			jedis.set(getProviderPolicyKey(), PolicySetupUtil.loadPolicySet(PROVIDER_POLICY1));
			
			loadIntertenantPolicies(INTERTENANT_POLICY1, jedis);
			loadIntertenantPolicies(INTERTENANT_POLICY2, jedis);

			Map<String, String> tenantPolicies = loadIntratenantPolicies(INTRATENANT_POLICY1, jedis,domain);
			tenantPolicies.putAll(loadIntratenantPolicies(INTRATENANT_POLICY2, jedis, domain));
			
			return new ArrayList<String>(tenantPolicies.keySet());
		}finally{
			jedis.disconnect();
		}
	}
	
}
