package nl.uva.sne.daci.contextsvc.test;


import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;

import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
//import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.SerializableEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
//import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

import nl.uva.sne.daci.context.ContextRequest;
import nl.uva.sne.daci.context.ContextResponse;
import nl.uva.sne.daci.contextimpl.ContextBaseResponse;
import nl.uva.sne.daci.contextimpl.ContextRequestImpl;
import nl.uva.sne.daci.contextsvcimpl.Configuration;
import nl.uva.sne.daci.contextsvcimpl.ContextSvcImpl;




public class DemoRestClient {
	
	
	  /***** TESTING CODE  -  BEGIN*/
		private static final String INTRATENANT_POLICY1 = "policies/BioinformaticsCyclone.IFB1_Tenant.xml";
		private static final String PROVIDER_POLICY1 = "policies/BioinformaticsCyclone.LAL_ProviderPolicySet.xml";
		private static final String INTERTENANT_POLICY1 = "policies/BioinformaticsCyclone.IntertenantPolicies.xml";
		
		private static String redisAddress = "localhost";
		private static String domain = "demo-uva";
		
	  /***** TESTING CODE  - END*/	
		

	
	public static void main(String[] args) {
        DemoRestClient restClient = new DemoRestClient();
        try {
        	
  		  /***** TESTING CODE  -  BEGIN*/
  			ContextSrvImplTester c = new ContextSrvImplTester();
  			List<String> tenants = c.setupPolicies(redisAddress,domain);
  			c.setupTenantIdentifiers(tenants, redisAddress, domain);
  	      /***** TESTING CODE  - END*/	
  			
        	
        	/*Build the context request here ...*/
        	
    		String tenantId = "";//"Bioinformatics_IFB_Tenant1";
    		ContextRequest req = c.buildContextRequest(tenantId);
    		
    		ContextResponse res = restClient.validate((ContextRequestImpl)req, /*"Bioinformatics_IFB_Tenant1"*/tenantId); //!!!!tenantId=null CHECK THIS, THIS MAY CREATE A PROBLEM!!!!
    		System.out.println("Response : " + /*res.getDecision().toString()*/ res.getDecision().toString());
        } catch (Exception e) {
            e.printStackTrace(); //To change body of catch statement use File | Settings | File Templates.
        }

	}
	

	private ContextBaseResponse  validate(ContextRequestImpl req, String tenantId) throws Exception {
	

        String url = "http://localhost:8090/contexts";
        HttpClient client = HttpClientBuilder.create().build();
        ObjectMapper mapper = new ObjectMapper();
        try{
            HttpPost mPost = new HttpPost(url);

            /*GrantTokenType gt = convertGrantToken(URLDecoder.decode(token, "UTF-8"));
            System.out.println("Here we are..");
            String tokennew = convertGrantToken(gt);
            System.out.println("Here we are.." + tokennew);*/
  
            mPost.setHeader("Content-Type", "application/json");
            mPost.setHeader("accept", "application/json");
            
            mPost.setEntity(new StringEntity(tenantId));
            mPost.setEntity(new StringEntity(mapper.writeValueAsString(req)));         
            
           /* RestTemplate restTemplate = new RestTemplate();
            restTemplate.exchange(url, mPost, mPost.getEntity(), ContextRequestImpl.class);*/
            HttpResponse response = client.execute(mPost); 
            
            //String output = response.toString();
            mPost.releaseConnection( );

            return mapper.readValue(response.getEntity().getContent(),ContextBaseResponse.class);
        }catch(Exception e){
        	throw new Exception("Exception in adding bucket : " + e.getMessage());
        	
        }	
	}
	
	 
}
