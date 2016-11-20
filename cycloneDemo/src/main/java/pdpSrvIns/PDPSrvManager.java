package pdpSrvIns;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nl.uva.sne.daci.authzsvc.AuthzResponse;
import nl.uva.sne.daci.authzsvc.AuthzSvc;
import nl.uva.sne.daci.authzsvc.AuthzSvc.DecisionType;
import nl.uva.sne.daci.authzsvcimp.ContextHandler;
import nl.uva.sne.daci.authzsvcpdp.PDPSvc;
import nl.uva.sne.daci.authzsvcpdp.PDPSvcPool;
import nl.uva.sne.daci.authzsvcpdp.PDPSvcPoolImpl;
import nl.uva.sne.daci.authzsvcpolicy.PolicyManager;
import nl.uva.sne.daci.context.ContextRequest;
import nl.uva.sne.daci.context.ContextRequestBuilder;
import nl.uva.sne.daci.context.ContextResponse;
import nl.uva.sne.daci.context.tenant.TenantManager;
import nl.uva.sne.daci.contextimpl.ContextManager;
import nl.uva.sne.daci.contextsvcimpl.Configuration;
import nl.uva.sne.daci.contextsvcimpl.ContextSvcImpl;
import nl.uva.sne.daci.setup.PolicySetupUtil;
import nl.uva.sne.daci.utils.XACMLUtil;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.RequestType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ResponseType;
import redis.clients.jedis.Jedis;

public class PDPSrvManager {

	
	
	PDPSvcPool servicePool;
	PolicyManager policyMgr;
	XACMLUtil xacmlUtil;
	ContextSvcImpl contextSvc;
	ContextManager ctxMan;
	
	
	String REDIS_SERVER_ADDRESS;
	private ArrayList<String> redisInsertedKeys;
	
	public PDPSrvManager(String redisServerAddress){
		REDIS_SERVER_ADDRESS = redisServerAddress;
		xacmlUtil = new XACMLUtil();
		redisInsertedKeys = new ArrayList<String>();
	}
	
	public void init(String policyPrefix, String domainName){
		policyMgr = new PolicyManager(domainName + ":" + policyPrefix, REDIS_SERVER_ADDRESS);
		servicePool = new PDPSvcPoolImpl(policyMgr);
		contextSvc = new ContextSvcImpl(domainName, REDIS_SERVER_ADDRESS);
		//ctxMan = new ContextManager(domainName, REDIS_SERVER_ADDRESS);
	}
	
	
	public PDPSvc getPDPSrv(String serviceIdentifier){
		try{
			//for(String tenantId : tenantMgr.getTenantIdentifiers()) {
			
			return servicePool.getService(serviceIdentifier);
		//}
		} catch(Exception e) {
			throw new RuntimeException("Error loading policies", e);
		}
		//ContextHandler ctxHandler = new ContextHandler(this.contextsvc, servicePool, tenantMgr);
	}
	
	
	private AuthzResponse validateContext(ContextRequest ctxRequest) throws Exception {	
		
		
		ContextResponse resp = contextSvc.validate(ctxRequest);
		
		switch(resp.getDecision()) {
			case DENY:
				return new AuthzResponse(DecisionType.DENY);
			case ERROR:
				return new AuthzResponse(DecisionType.ERROR);
			case NEED_REMOTE_DECISION:
				return new AuthzResponse(DecisionType.PERMIT, resp.getGrantToken());
			case PERMIT:
				return new AuthzResponse(DecisionType.PERMIT);
			default:
				return new AuthzResponse(DecisionType.ERROR);
		}		
	}

	

	
	
	
	private void setupTenantIdentifiers(List<String> tenants, String domain) {
		Jedis jedis = new Jedis(REDIS_SERVER_ADDRESS);
		
		try {
			TenantManager tenantMgr = new TenantManager(domain, REDIS_SERVER_ADDRESS);
			StringBuilder builder = new StringBuilder();
			
			int index = 0;
			builder.append(tenants.get(index++));		
			for(; index < tenants.size(); index++) {
				builder.append( Configuration.TENANTID_DELIMITER + tenants.get(index));
			}
			
			/////////////////////
			//System.out.println("TENANT ID :" + builder);
			/////////////////////
			jedis.set(tenantMgr.getTenantConfigKey(), builder.toString());
			redisInsertedKeys.add(tenantMgr.getTenantConfigKey());
		}finally{		
			jedis.disconnect();
		}
	}
	
	
	
	
	/*Setup policies ...*/
	private void setupPolicies(Map<String, String> listOfPolicies, String domain, String authzPolicyKeyPrefix) throws Exception {
		
		Jedis jedis = new Jedis(REDIS_SERVER_ADDRESS);
		try {
			
			// Load provider policies to redis
			/*redisInsertedKeys.add(ctxMan.getProviderPolicyKey());
			jedis.set(ctxMan.getProviderPolicyKey(), PolicySetupUtil.loadPolicySet(PROVIDER_LAL_POLICY));*/
		
			// load inter-tenant policies to redis
			/*for (String policyFile : listOfPolicies){
				Map<String, String> tenantPolicies = PolicySetupUtil.loadPolicies(policyFile);
				for(String pId : tenantPolicies.keySet()) {
					String pKey = ctxMan.getInterTenantPolicyKey(pId);
					redisInsertedKeys.add(pKey);
					
					jedis.set(pKey, tenantPolicies.get(pId));			
				}
			}*/
			

			for (Map.Entry<String,String> policyFile : listOfPolicies.entrySet()){
				Map<String, String> intraTenantPolicies = null;
				if ((intraTenantPolicies = PolicySetupUtil.loadPolicyorPolicySets(policyFile.getValue())) != null){
					Map.Entry<String, String> entry = intraTenantPolicies.entrySet().iterator().next();
					String pKey = domain + ":" + authzPolicyKeyPrefix + ":" + policyFile.getKey();//entry.getKey();
					//System.out.println("KEY : " + pKey);
					redisInsertedKeys.add(pKey);			
					jedis.set(pKey, entry.getValue());
				}else {
					intraTenantPolicies = PolicySetupUtil. loadPolicies(policyFile.getValue());
					//String authzPolicyKeyPrefix = String.format( nl.uva.sne.daci.authzsvcimp.Configuration.REDIS_KEYPREFIX_FORMAT, domain);			
					for(String pId : intraTenantPolicies.keySet()) {
						String pKey = domain + ":" + authzPolicyKeyPrefix + ":" + policyFile.getKey();//pId;
						//System.out.println("KEY : " + pKey);
						redisInsertedKeys.add(pKey);			
						jedis.set(pKey, intraTenantPolicies.get(pId));
					}
				}
			}
			

		}finally{
			jedis.disconnect();
		}
	}
	
	
	public AuthzResponse evaluateRequest(Map<String, String> request, String serviceIdentifier){
		RequestType xacmlRequest = xacmlUtil.createRequest(request);
		PDPSvc pdp = getPDPSrv(serviceIdentifier);
		ResponseType xacmlResponse = pdp.evaluate(xacmlRequest);
		DecisionType pdpDecision = ContextHandler.getDecision(xacmlResponse);
		
		try{
			if (pdpDecision == AuthzSvc.DecisionType.PERMIT) {
				ContextRequest ctxRequest = ContextRequestBuilder.create(serviceIdentifier, request);				
				return validateContext(ctxRequest);
			}
			else {
				return new AuthzResponse(pdpDecision);
			}
		}catch(Exception e) {
			throw new RuntimeException("Error evaluating policies ", e);
		}
	}
	
	
	public static void main(String[] args){
		PDPSrvManager pm = new PDPSrvManager("localhost");
		pm.init("uva-demo","bioinformatics");
		ArrayList<String> tenants = new ArrayList<String>();
		tenants.add("tenant1");
		tenants.add("tenant2");
		Map<String,String> policies = new HashMap<String,String>();
		policies.put("tenant1","policies/BioinformaticsCycloneUC1Policy-NoNamespace-SNE-Parseable.xml");
		policies.put("tenant2",/*"policies/BioinformaticsCycloneUC2Policy-NoNamespace.xml"*/"policies/BioinformaticsCycloneUC2Policy-NoNamespace-SNE-Parseable.xml");
		pm.setupTenantIdentifiers(tenants, "bioinformatics");
		try{
			pm.setupPolicies(policies, "bioinformatics", "uva-demo");
		}catch(Exception e){
			System.err.println("Exception in in setting up policies");
		}
		
		//pm.getPDPSrv("tenant1");	
			
		HashMap<String, String> reqValues = new HashMap<String, String>();
		reqValues.put("urn:oasis:names:tc:xacml:1.0:subject:subject-id", "susan");
		AuthzResponse ar = pm.evaluateRequest(reqValues, "tenant1");
		//System.out.println("Result " + ar.getDecision().toString());
		
	}
	
}
