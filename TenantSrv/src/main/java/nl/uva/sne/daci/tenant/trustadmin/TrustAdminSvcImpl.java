package nl.uva.sne.daci.tenant.trustadmin;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import nl.uva.sne.daci.tenant.trustadmin.Configuration;
import nl.uva.sne.daci.utils.PolicySetupUtil;
import redis.clients.jedis.Jedis;

public class TrustAdminSvcImpl implements TrustAdminSvc{

	private static final Logger logger = LoggerFactory.getLogger(TrustAdminSvcImpl.class);
	String redisAddress;
	String domain;
	
	private String interTenantKeyPrefix;
	
	
	public TrustAdminSvcImpl(String redis, String dom){
		redisAddress = redis;
		domain = dom;
		interTenantKeyPrefix = String.format(Configuration.INTERTENANT_KEY_STYLE, domain);
	}
	
	
	@Override
	public void setTrustPolicy(String trusteeId, String policydoc) {

		Jedis jedis = new Jedis(redisAddress);
		try {	
			Map<String, String> tenantPolicies = PolicySetupUtil.loadPolicies(policydoc);
			for(String pId : tenantPolicies.keySet()) {				
				String pKey = interTenantKeyPrefix + pId;
				//redisInsertedKeys.add(pKey);
				jedis.set(pKey, tenantPolicies.get(pId));			
			}
		}catch(Exception e){
			logger.error("Exception in loadIntertenantPolicies --> TrustAdmin"+ e.getMessage());
		}finally{
			jedis.disconnect();
		}
	}

	@Override
	public String getTrustPolicy(String trusteeId, String policydoc) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> listTrustPolicies() {
		// TODO Auto-generated method stub
		return null;
	}
	

}
