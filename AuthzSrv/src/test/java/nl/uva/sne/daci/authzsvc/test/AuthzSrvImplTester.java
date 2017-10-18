package nl.uva.sne.daci.authzsvc.test;


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
/*import nl.uva.sne.daci.contextimpl.ContextManager;
import nl.uva.sne.daci.contextimpl.ContextRequestImpl;
import nl.uva.sne.daci.contextsvc.ContextSvc;
import nl.uva.sne.daci.contextsvcimpl.Configuration;
import nl.uva.sne.daci.contextsvcimpl.ContextSvcImpl;
import nl.uva.sne.daci.setup.PolicySetupUtil;*/
import nl.uva.sne.daci.tenant.TenantManager;
import redis.clients.jedis.Jedis;

public class AuthzSrvImplTester {
	
	/*private static final String INTRATENANT_POLICY1 = "policies/BioinformaticsCyclone.IFB1_Tenant.xml";

	private static final String PROVIDER_POLICY1 = "policies/BioinformaticsCyclone.LAL_ProviderPolicySet.xml";

	private static final String INTERTENANT_POLICY1 = "policies/BioinformaticsCyclone.IntertenantPolicies.xml";

	*/
	
	List<AuthzRequest> authzRequests;
	

	private static final String SUBJECT_ID = //"urn:oasis:names:tc:xacml:1.0:subject:subject-id";
											"urn:oasis:names:tc:xacml:1.0:subject:subject-role";	
	
	private static final String RESOURCE_ID = "urn:oasis:names:tc:xacml:1.0:resource:resource-id";
	
	private static final String ACTION_ID = "urn:oasis:names:tc:xacml:1.0:action:action-id";
	
	
	private AuthzSvcImpl authzsvc;
	//private ContextSvcImpl ctxsvc;
	



	public List<AuthzRequest> getRequests(){
		return authzRequests;
	}
	
	public static void main(String[] args){
		AuthzSrvImplTester ast = new AuthzSrvImplTester("localhost","demo-uva");
		
		ast.initSampleRequests();
		for (AuthzRequest ar : ast.getRequests()){
			System.out.println(ast.checkAuthorization("Bioinformatics_IFB_Tenant1", ar).getDecision().toString());
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
	public AuthzSrvImplTester(String redisAddress, String domain){
				
		/*Instantiate authorization service ...*/
		authzsvc = new AuthzSvcImpl(/*FT:03.02.2017*//*ctxsvc*/);
		authzsvc.init();
	}
	
	
	/*Create request/s*/	
	public static AuthzRequest createRequest(String subjectRole, String resourceId, String actionId) {
		AuthzRequest request = new AuthzRequest();
		Map<String, String> attrs = new HashMap<String, String>();
		attrs.put(SUBJECT_ID, subjectRole);
		attrs.put(RESOURCE_ID, resourceId);
		attrs.put(ACTION_ID, actionId);
		request.setAttributes(attrs);
		return request;
	}
	

	
	
	
	/*AUTHORIZATION SERVICE : INITIALIZE REQUESTS ...*/
	public void initSampleRequests() {
		authzRequests = new ArrayList<AuthzRequest>();
		String[][] requests = 	{
				/*{"admin", String.format("%sComputingNode", "http://demo3.uva.nl/vi/438/"), "SLI:Operate-VR:Stop"},
				{"admin", String.format("%sComputingNode", "http://demo3.uva.nl/vi/438/"), "SLI:Operate-VR:Stop"},
				{"admin", String.format("%sComputingNode", "http://demo3.uva.nl/vi/438/"), "SLI:Operate-VR:Stop"},
				{"admin", String.format("%sComputingNode", "http://demo3.uva.nl/vi/438/"), "SLI:Operate-VR:Stop"},
				
				{"admin", String.format("%sComputingNode", "http://demo3.uva.nl/vi/438/"), "SLI:Operate-VR:Stop"},
				{"admin", String.format("%sComputingNode", "http://demo3.uva.nl/vi/438/"), "SLI:Operate-VR:Stop"},
					
				{"admin", String.format("%sComputingNode", "http://demo3.uva.nl/vi/438/"), "SLI:Operate-VR:Stop"},
				{"admin", String.format("%sComputingNode", "http://demo3.uva.nl/vi/438/"), "SLI:Operate-VR:Stop"}*/
				{"Bioinformatician", "HG1", "read"},
				{"Bioinformatician", "App1", "execute"},
				{"Physicist", "HG1", "read"},
				{"Bioinformatician", "HG2", "write"},
				
				{"Researcher", "RD1", "read"},
				{"Bioinformatician", "RD1", "write"},
					
				{"Chemist", "RD2", "read"},
				{"john", "RD2", "write"},				
				{"Bioinformatician", "App2", "execute"},		
				{"Bioinformatician", "HG2", "write"},
				{"Bioinformatician", "App1", "execute"},
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
