package authSrvTester;

import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import nl.uva.sne.daci.authzsvc.AuthzRequest;
import nl.uva.sne.daci.authzsvc.AuthzResponse;
import nl.uva.sne.daci.authzsvc.AuthzSvc;
import nl.uva.sne.daci.authzsvc.AuthzSvc.DecisionType;
import nl.uva.sne.daci.authzsvcimp.AuthzSvcImpl;
import nl.uva.sne.daci.context.tenant.TenantManager;
import nl.uva.sne.daci.contextimpl.ContextManager;
import nl.uva.sne.daci.contextimpl.ContextRequestImpl;
import nl.uva.sne.daci.contextsvc.ContextSvc;
import nl.uva.sne.daci.contextsvcimpl.Configuration;
import nl.uva.sne.daci.contextsvcimpl.ContextSvcImpl;
import nl.uva.sne.daci.setup.PolicySetupUtil;
import redis.clients.jedis.Jedis;

public class AuthzServiceTester {

	public static final String REDIS_SERVER_ADDRESS = "localhost";
	
	private ArrayList<String> redisInsertedKeys;
	public static final String DOMAIN = "demo-uva";
	
	private static final String INTRATENANT_POLICY1 = "policies/BioinformaticsCyclone.IFB1_Tenant.xml";
	private static final String INTRATENANT_POLICY2 = "policies/BioinformaticsCyclone.IFB2_Tenant.xml";
	private static final String INTRATENANT_POLICY3 = "policies/BioinformaticsCyclone.ICL_Tenant.xml";
	
	
	private static final String PROVIDER_POLICY1 = "policies/BioinformaticsCyclone.LAL_ProviderPolicySet.xml";
	private static final String PROVIDER_POLICY2 = "policies/BioinformaticsCyclone.WTHI_ProviderPolicySet.xml";
	
	private static final String INTERTENANT_POLICY1 = "policies/BioinformaticsCyclone.IntertenantPoliciesIFB1_Tenant.xml";
	private static final String INTERTENANT_POLICY2 = "policies/BioinformaticsCyclone.IntertenantPoliciesIFB2_Tenant.xml";
	
	
	
	List<AuthzRequest> authzRequests;
	
	private static final String SUBJECT_ID = "urn:oasis:names:tc:xacml:1.0:subject:subject-id";	
	
	private static final String RESOURCE_ID = "urn:oasis:names:tc:xacml:1.0:resource:resource-id";
	
	private static final String ACTION_ID = "urn:oasis:names:tc:xacml:1.0:action:action-id";
	
	private AuthzSvcImpl authzsvc;
	private ContextSvcImpl ctxsvc;
	
	
	


	private List<AuthzRequest> getRequests(){
		return authzRequests;
	}
	
	public static void main(String[] args){
		AuthzServiceTester ast = new AuthzServiceTester();
		
		ast.initSampleRequests();
		for (AuthzRequest ar : ast.getRequests()){
			System.out.println(ast.checkAuthorization("IFB1_Tenant", ar).getDecision().toString());
		}
		
	}
	
	
	/*Flow
	 * (1) Set the tenants and their policies... setupTenantIdentifiers(List of strings) and setupPolicies()
	 * 	   (1.1) Tenant identifiers are given by the user
	 * 	   (1.2) Set provider (map to ctxMan.getProviderPolicyKey()) and 
	 * 			tenant policies (by using ctxMan.getInterTenantPolicyKey(pId))... Note: Tenant policies are set by PsetId and the policy itself
	 * 
	 * (2) Instantiate context service and initialize it by loading policies (provider, inter-tenant) 
	 *     (2.1) Load context information for each Tenant from redis
	 *     (2.2) Load provider (2.2.1) and intertenant contexts (2.2.2) --> ctxMan.loadProviderContexts() and ctxMan.loadInterTenantContexts()
	 *     		(2.2.1) Create Context with the pair <urn:oasis:names:tc:xacml:1.0:subject:subject-id = provider, provider policy>
	 *     				where policy is retrieved from redis by "this.providerPolicyKey = urn:eu:geysers:daci:" + DOMAIN + ":policy:xacml3:provider:root"
	 *     		(2.2.2) Load internant contexts by using the identifiers set (setupTenantIdentifiers(List<String>)) 
	 * 
	 * (3) Instantiate Authorization Service, initialize it by creating a service pool
	 * */
	public AuthzServiceTester(){
		redisInsertedKeys = new ArrayList<String>();
		
		try{
			List<String> tenants = setupPolicies();
			/////////////////////
			System.out.println("TENANT IDs ...");
			for (String str : tenants)
				System.out.println("Tenant Id : " + str);
			/////////////////////
			setupTenantIdentifiers(tenants);
		}catch(Exception e){
			System.err.println("Exception in Constructor");
		}
		
		ctxsvc = new ContextSvcImpl(DOMAIN, REDIS_SERVER_ADDRESS);
		ctxsvc.init();
		
		/*Instantiate authorization service ...*/
		authzsvc = new AuthzSvcImpl(ctxsvc);
		authzsvc.init();
	}
	
	
	
	
	private Map<String, String> loadIntertenantPolicies(String policyName, ContextManager ctxMan, Jedis jedis){
		try {
			// load inter-tenant policies to redis
			Map<String, String> tenantPolicies = PolicySetupUtil.loadPolicies(policyName);
			for(String pId : tenantPolicies.keySet()) {				
				String pKey = ctxMan.getInterTenantPolicyKey(pId);
				redisInsertedKeys.add(pKey);
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
	
	private Map<String, String> loadIntratenantPolicies(String policyName, ContextManager ctxMan, Jedis jedis){
		try{
			Map<String, String> intraTenantPolicies = PolicySetupUtil.loadPolicyorPolicySets(policyName);//loadPolicies(BIOINFO_UC1_POLICY);
			String authzPolicyKeyPrefix = String.format( nl.uva.sne.daci.authzsvcimp.Configuration.REDIS_KEYPREFIX_FORMAT, DOMAIN);			
			for(String pId : intraTenantPolicies.keySet()) {
				String pKey = authzPolicyKeyPrefix + ":" + pId;
				redisInsertedKeys.add(pKey);			
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
	
	
	/*Setup policies ...*/
	private List<String> setupPolicies() throws Exception {
		
		ContextManager ctxMan = new ContextManager(DOMAIN, REDIS_SERVER_ADDRESS);
		
		
		Jedis jedis = new Jedis(REDIS_SERVER_ADDRESS);
		try {	
			// Load provider policies to redis
			redisInsertedKeys.add(ctxMan.getProviderPolicyKey());
			jedis.set(ctxMan.getProviderPolicyKey(), PolicySetupUtil.loadPolicySet(PROVIDER_POLICY1));
			
			/*WE HAVE TO SET INTRA TENANT POLICIES : Slipstream/IFBPortal policies ...*/
			
			loadIntertenantPolicies(INTERTENANT_POLICY1, ctxMan, jedis);
			loadIntertenantPolicies(INTERTENANT_POLICY2, ctxMan, jedis);

			Map<String, String> tenantPolicies = loadIntratenantPolicies(INTRATENANT_POLICY1, ctxMan, jedis);
			tenantPolicies.putAll(loadIntratenantPolicies(INTRATENANT_POLICY2, ctxMan, jedis));
			
			return new ArrayList<String>(tenantPolicies.keySet());
		}finally{
			jedis.disconnect();
		}
	}
	
	
	
	
	/*SETUP TENANTS ... */
	private void setupTenantIdentifiers(List<String> tenants) {
		Jedis jedis = new Jedis(REDIS_SERVER_ADDRESS);
		
		try {
			TenantManager tenantMgr = new TenantManager(DOMAIN, REDIS_SERVER_ADDRESS);
			StringBuilder builder = new StringBuilder();
			
			int index = 0;
			builder.append(tenants.get(index++));		
			for(; index < tenants.size(); index++) {
				builder.append(Configuration.TENANTID_DELIMITER + tenants.get(index));
			}
			
			jedis.set(tenantMgr.getTenantConfigKey(), builder.toString());
			redisInsertedKeys.add(tenantMgr.getTenantConfigKey());
		}finally{		
			jedis.disconnect();
		}
	}

	

	
	
	/*Create request/s*/	
	private static AuthzRequest createRequest(String subjectRole, String resourceId, String actionId) {
		AuthzRequest request = new AuthzRequest();
		Map<String, String> attrs = new HashMap<String, String>();
		attrs.put(SUBJECT_ID, subjectRole);
		attrs.put(RESOURCE_ID, resourceId);
		attrs.put(ACTION_ID, actionId);
		request.setAttributes(attrs);
		return request;
	}
	

	
	
	
	/*AUTHORIZATION SERVICE : INITIALIZE REQUESTS ...*/
	private void initSampleRequests() {
		authzRequests = new ArrayList<AuthzRequest>();
		String[][] requests = 	{
				{"alice", "HG1", "read"},
				{"alice", "App1", "execute"},
				{"bob", "HG1", "read"},
				{"alice", "HG2", "write"},
				
				{"susan", "RD1", "read"},
				{"susan", "RD1", "write"},
					
				{"john", "RD2", "read"},
				{"john", "RD2", "write"},				
				{"bob", "App2", "execute"},		
				{"susan", "App2", "execute"},
				{"mary", "App1", "execute"},
				{"mary", "App2", "execute"}
		};
		

		for(int i = 0; i < requests.length; i++) {
			/*Map<String, String> subject = new HashMap<String, String>();
			subject.put(SUBJECT_ID, requests[i][0]);
			
			Map<String, String> permission = new HashMap<String, String>();
			permission.put(RESOURCE_ID, requests[i][1]);
			permission.put(ACTION_ID, requests[i][2]);*/
			
			AuthzRequest ar = createRequest(requests[i][0], requests[i][1], requests[i][2]);
			
			authzRequests.add(ar);
		}
	}
	
	
	public AuthzResponse checkAuthorization(String encodedTenantId, AuthzRequest request){
		String tenantId;

		try {
			tenantId = URLDecoder.decode(encodedTenantId, "UTF-8");			
		} catch (UnsupportedEncodingException e1) {
			return new AuthzResponse(DecisionType.ERROR);
		}
		long startTime = System.nanoTime();
		//String authzPolicyKeyPrefix = String.format( nl.uva.sne.daci.authzsvcimp.Configuration.REDIS_KEYPREFIX_FORMAT, DOMAIN);	
		AuthzResponse res = authzsvc.authorize(/*authzPolicyKeyPrefix + ":" + */encodedTenantId, request);
		long stopTime = System.nanoTime();
		return res;
		
	}

}
