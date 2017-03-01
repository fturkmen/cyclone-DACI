package nl.uva.sne.daci.tenant.authzadmin;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.uva.sne.daci.utils.PolicySetupUtil;
import redis.clients.jedis.Jedis;

public class AuthzAdminSvcImpl implements AuthzAdmin{

	private static final Logger logger = LoggerFactory.getLogger(AuthzAdminSvcImpl.class);
	String redisAddress;
	String domain;
	
	
	public AuthzAdminSvcImpl(String redis, String dom){
		redisAddress = redis;
		domain = dom;
	}
	
	
	@Override
	public void putPolicy(String policyId, String policydoc) {
		Jedis jedis = new Jedis(redisAddress);
		try {	
			Map<String, String> intraTenantPolicies = /*PolicySetupUtil.loadPolicyorPolicySets(policyName);//*/PolicySetupUtil.loadPolicies(policydoc);
			String authzPolicyKeyPrefix = String.format(Configuration.REDIS_KEYPREFIX_FORMAT, domain);			
			for(String pId : intraTenantPolicies.keySet()) {
				String pKey = authzPolicyKeyPrefix + ":" + pId;
				//redisInsertedKeys.add(pKey);			
				jedis.set(pKey, intraTenantPolicies.get(pId));
				logger.info("Put intra-tenant policy:" + pKey);
			}
			//if (tenantPolicies != null) return new ArrayList<String>(tenantPolicies.keySet());
			//else return null;
		}catch(Exception e){
			logger.error("Exception in loadIntratenantPolicies --> AuthzAdminImpl " + e.getMessage());
		}finally{
			jedis.disconnect();
		}
	}
	
	
	@Override
	public void deletePolicy(String policyId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getPolicy(String policyId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> listPolicies() {
		// TODO Auto-generated method stub
		return null;
	}

	
	
	public static void main(String[] args){
		
	}
	
	
}
