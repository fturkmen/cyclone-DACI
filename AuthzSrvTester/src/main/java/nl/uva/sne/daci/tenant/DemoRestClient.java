package nl.uva.sne.daci.tenant;

import java.io.File;
import java.io.FileInputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.entity.mime.MultipartEntityBuilder;


public class DemoRestClient {
	

	static String lalProviderPolicy = "policies/BioinformaticsCyclone.LAL_ProviderPolicySet.xml";
	static String intertenantPolicy = "policies/BioinformaticsCyclone.IntertenantPolicies.xml";
	static String intratenantPolicy = "policies/BioinformaticsCyclone.IFB1_Tenant.xml";
	
	
	public static void main(String[] args) {
        DemoRestClient restClient = new DemoRestClient();
        try {
        	restClient.createTenant("Bioinformatics_IFB_Tenant1");
        	restClient.setPolicy(lalProviderPolicy, "providerPolicy");
        	restClient.setPolicy(intertenantPolicy, "intertenantPolicy");
        	restClient.setPolicy(intratenantPolicy, "tenantUserPolicy");
        	
        } catch (Exception e) {
            e.printStackTrace(); //To change body of catch statement use File | Settings | File Templates.
        }
	}

	

	private void  setPolicy(String policyFile, String endPoint) throws Exception {
		
		String output = null;
        String url = "http://localhost:8092/" + endPoint;
        HttpClient client = HttpClientBuilder.create().build();
        //ObjectMapper mapper = new ObjectMapper();
        try{
            HttpPost mPost = new HttpPost(url);
  
            //mPost.setHeader("Content-Type", "application/xml");
            //mPost.setHeader("accept", "application/xml");
            
            mPost.setEntity(new StringEntity("localhost"));
            mPost.setEntity(new StringEntity("demo-uva"));
            
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            File f = new File(policyFile);
            builder.addBinaryBody(
            	    "policy",
            	    new FileInputStream(f),
            	    ContentType.APPLICATION_OCTET_STREAM,
            	    f.getName()
            	);

            HttpEntity multipart = builder.build();
            mPost.setEntity(multipart);
            	
            //FileEntity entity = new FileEntity(new File(policyFile));
            //entity.setContentType(ContentType.APPLICATION_XML.getMimeType());
            //mPost.setEntity(entity);      

            HttpResponse response = client.execute(mPost); 
            
            output = response.toString();
            mPost.releaseConnection( );
            System.out.println("Response : " + output);
            
        }catch(Exception e){
        	throw new Exception("Exception in adding bucket : " + e.getMessage());
        	
        }	
	}




	private void  createTenant(String tenantId) throws Exception {
		
		String output = null;
        String url = "http://localhost:8092/tenants";
        HttpClient client = HttpClientBuilder.create().build();
        //ObjectMapper mapper = new ObjectMapper();
        try{
            HttpPut mPut = new HttpPut(url);
              
            mPut.setHeader("Content-Type", "application/json");
            //mPut.setHeader("accept", "application/json");
            
            mPut.setEntity(new StringEntity("localhost"));
            mPut.setEntity(new StringEntity("demo-uva"));
            mPut.setEntity(new StringEntity(tenantId));
            //FileEntity entity = new FileEntity(new File(policyFile));
            //entity.setContentType(ContentType.APPLICATION_XML.getMimeType());
            //mPost.setEntity(entity);      

            HttpResponse response = client.execute(mPut); 
            
            output = response.toString();
            mPut.releaseConnection( );
            System.out.println("Response... : " + output);
            
        }catch(Exception e){
        	throw new Exception("Exception in adding bucket : " + e.getMessage());
        	
        }	
	}
	
	/*TODO : Add removeTenant call as well*/

	 
}
