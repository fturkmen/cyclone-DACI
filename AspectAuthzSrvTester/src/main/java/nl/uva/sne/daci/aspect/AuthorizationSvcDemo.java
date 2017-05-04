package nl.uva.sne.daci.aspect;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;


public class AuthorizationSvcDemo {


	
	/*  This application will try to access private data or execute an application
	 *  on behalf of a user, and authorization service will check whether the user
	 *  is authorized to do so... 
	 *  */
	public static void main(String[] args) throws Exception{
		
		AuthorizationSvcDemo app = new AuthorizationSvcDemo();
		/*Add tenant*/
		
		/*Add policies: provider, intertenant and intratenant*/
		
		 
		
		
		/*Check the authorizations ...*/
		AuthzRequest ar = createRequest("Bioinformatician", "HG1", "read");	
		//app.readPrivateData_AOP(ar,"Bioinformatics_IFB_Tenant1");
		if (/*app.readPrivateData_Integrated(ar)*/ app.readPrivateData_AOP(ar,"Bioinformatics_IFB_Tenant1")) System.out.println("SUCCESS!");
		else System.out.println("NOT AUTHORIZED!!!");
	}
	
	
	/*This will be direct check, call the service...*/
	public boolean readPrivateData_Integrated(AuthzRequest ar, String tenantId) throws Exception{
			
			
			AuthzSvc.DecisionType res = authorize(ar, tenantId).getDecision();
			if (res.equals(AuthzSvc.DecisionType.PERMIT))
				return readPrivateData();
			else return false;
	}
	
	
	/** AOP annotation will come here...*/
	public boolean readPrivateData_AOP(AuthzRequest ar, String tenantId){
		System.out.println("Calling readPrivateData method");
		return readPrivateData();
	}
	
	
	private boolean readPrivateData(){
		try (InputStream in = Files.newInputStream(Paths.get("sensitiveFile.txt"));
			    BufferedReader reader =
			      new BufferedReader(new InputStreamReader(in))) {
			    String line = null;
			    while ((line = reader.readLine()) != null) {
			        System.out.println(line);
			    }
			    return true;
			} catch (IOException x) {
			    System.err.println(x);
			}
		
		return false;
	}
	
	
	
	private static final String SUBJECT_ID = //"urn:oasis:names:tc:xacml:1.0:subject:subject-id";
			"urn:oasis:names:tc:xacml:1.0:subject:subject-role";
	private static final String RESOURCE_ID = "urn:oasis:names:tc:xacml:1.0:resource:resource-id";
	private static final String ACTION_ID = "urn:oasis:names:tc:xacml:1.0:action:action-id";
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
	
	
	
	
	public static AuthzResponse authorize(AuthzRequest req, String tenantId) throws Exception {
		
		String output = null;
        String url = "http://localhost:8080/pdps/" + tenantId+"/decision";
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
