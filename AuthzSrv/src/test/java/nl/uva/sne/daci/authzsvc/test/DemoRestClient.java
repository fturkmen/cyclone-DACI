package nl.uva.sne.daci.authzsvc.test;


import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

import nl.uva.sne.daci.authzsvc.AuthzRequest;
import nl.uva.sne.daci.authzsvc.AuthzResponse;
import nl.uva.sne.daci.authzsvcimp.Configuration;
import nl.uva.sne.daci.tenant.TenantManager;
import redis.clients.jedis.Jedis;

public class DemoRestClient {
	

	public static void main(String[] args) {
        DemoRestClient restClient = new DemoRestClient();
        try {
        	
        	List<String> tenants = new ArrayList<String>();
        	tenants.add("Bioinformatics_IFB_Tenant1");
        	restClient.setupTenantIdentifiers(tenants, "localhost","demo-uva");
        
    		AuthzSrvImplTester ast = new AuthzSrvImplTester("localhost","demo-uva");
    		ast.initSampleRequests();
    		
    		for (AuthzRequest ar : ast.getRequests()){
    			System.out.println(restClient.authorize(ar, "Bioinformatics_IFB_Tenant1").getDecision().toString());
    		}
    		
        } catch (Exception e) {
            e.printStackTrace(); //To change body of catch statement use File | Settings | File Templates.
        }

	}

	private void setupTenantIdentifiers(List<String> tenants, String redisAddress, String domain) {
		Jedis jedis = new Jedis(redisAddress);
		
		try {
			TenantManager tenantMgr = new TenantManager(domain, redisAddress);
			StringBuilder builder = new StringBuilder();
			
			int index = 0;
			builder.append(tenants.get(index++));		
			for(; index < tenants.size(); index++) {
				builder.append(Configuration.TENANTID_DELIMITER + tenants.get(index));
			}
			
			jedis.set(tenantMgr.getTenantConfigKey(), builder.toString());
			
		}finally{		
			jedis.disconnect();
		}
	}
	

	public AuthzResponse authorize(AuthzRequest req, String tenantId) throws Exception {
		
		String output = null;
        String url = "http://localhost:8089/pdps/" + tenantId+"/decision";
        HttpClient client = HttpClientBuilder.create().build();
        ObjectMapper mapper = new ObjectMapper();
        try{
            HttpPost mPost = new HttpPost(url);
            
            mPost.setHeader("Content-Type", "application/json");
            mPost.setHeader("accept", "application/json");
            
            mPost.setEntity(new StringEntity(tenantId));
            mPost.setEntity(new StringEntity(mapper.writeValueAsString(req)));         
            
           /* RestTemplate restTemplate = new RestTemplate();
            restTemplate.exchange(url, mPost, mPost.getEntity(), ContextRequestImpl.class);*/
            HttpResponse response = client.execute(mPost); 
            
            output = response.toString();
            mPost.releaseConnection( );

            return mapper.readValue(response.getEntity().getContent(),AuthzResponse.class);
        }catch(Exception e){
        	throw new Exception("Exception in adding bucket : " + e.getMessage());
        	
        }	
	}


	 
}
