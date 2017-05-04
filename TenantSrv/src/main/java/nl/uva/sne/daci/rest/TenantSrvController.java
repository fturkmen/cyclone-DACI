package nl.uva.sne.daci.rest;


import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;

import nl.uva.sne.daci.tenant.authzadmin.PAP;
import nl.uva.sne.daci.tenant.tenantadmin.TenantSvc;
import nl.uva.sne.daci.tenant.tenantadmin.TenantSvcImpl;

import org.springframework.web.bind.annotation.RequestMethod;
//import org.springframework.web.bind.annotation.ExceptionHandler; 

import org.springframework.web.multipart.MultipartFile;


/*** TODO : Add logging here ...*/
@RestController
@EnableAutoConfiguration
public class TenantSrvController{

	PAP pap;
     
	
	
    /***********    POLICY MANAGEMENT *********/
	    
    /*Upload Provider policies ...*/
	@PostMapping("providerPolicy")
    public /*List<String>*/void providerPolicy(@RequestParam(value="providerId", defaultValue="provider") String providerId,
    										   @RequestParam(value="redisAddress", defaultValue="localhost") String redisAddress,
    									  	   @RequestParam(value="domain", defaultValue="demo-uva") String domain,
    									  	   @RequestParam(value="policy") MultipartFile policyFile) {
    	try {
    		/*TODO Here there should be a way of checking the provider, i.e. if she is stored in redis*/
    		pap =  new PAP(domain);    		
  	      	pap.setProviderPolicy(redisAddress, new ByteArrayInputStream(policyFile.getBytes()));
    	}catch(Exception e) {
			throw new RuntimeException("Couldn't set the Provider policies", e);
		}
    }
    
    
 
    /*Upload intertenant policies ...*/
	@PostMapping("intertenantPolicy")
    public /*List<String>*/void intertenantPolicy(@RequestParam(value="redisAddress", defaultValue="localhost") String redisAddress,
	  		  									  @RequestParam(value="domain", defaultValue="demo-uva") String domain,
	  		  									  @RequestParam(value="policy") MultipartFile policyFile) {
    	try {
    		pap =  new PAP(domain);    		
  	      	pap.setIntertenantPolicy(redisAddress, new ByteArrayInputStream(policyFile.getBytes()));
    	}catch(Exception e) {
			throw new RuntimeException("Couldn't set the Trust policies", e);
		}
    }
    
    
    /*Upload intratenant policies ...*/
	@PostMapping("tenantUserPolicy")
    public /*List<String>*/void tenantUserPolicy(@RequestParam(value="tenantId", defaultValue="tenant") String tenantId,
    											 @RequestParam(value="redisAddress", defaultValue="localhost") String redisAddress,
	  		  									 @RequestParam(value="domain", defaultValue="demo-uva") String domain,
	  		  									 @RequestParam(value="policy") MultipartFile policyFile) {
    	try {
    		/*Here there should be a way of checking if the tenant is stored registered already (i.e. stored in redis)*/
    		TenantSvcImpl tsc = new TenantSvcImpl(redisAddress, domain);
    		if (!tsc.checkTenant(tenantId)){
    			System.out.println("Couldn't find the tenant");
    			System.err.println("Couldn't find the tenant");
    			return;
    		};
    		pap =  new PAP(domain);    		
  	      	pap.setIntratenantPolicy(redisAddress, new ByteArrayInputStream(policyFile.getBytes()));
    	}catch(Exception e) {
			throw new RuntimeException("Couldn't set the User policies", e);
		}
    }
    
    
    
   
    
    
    /***********    TENANT MANAGEMENT *********/
    
	  /*Tenant Creation*/
	  @RequestMapping(
				value = "tenants",
		    	method = RequestMethod.PUT,
		    	consumes = { "application/json",  "application/xml"},
		    	produces = { "application/json",  "application/xml"}
				 )
	  //@ExceptionHandler(IOException.class)
	  //@ExceptionHandler(Exception.class)
	  public boolean tenantCreation(@RequestParam(value="redisAddress", defaultValue="localhost") String redisAddress,
									 @RequestParam(value="domain", defaultValue="demo-uva") String domain,
									 @RequestParam(value="tenantId", defaultValue="tenant") String tenantId/*,
									 @RequestParam(value="request") AuthzRequest request*/) {
		  	
		  try {
			  TenantSvc tsc = new TenantSvcImpl(redisAddress, domain);
			  return tsc.createTenant(tenantId);
			}catch(Exception e) {
				throw new RuntimeException("Couldn't add the tenant", e);
			}
		 
		  //ev = new Evaluator(redisAddress, domain);
		  //return ev.checkAuthorization(tenantId, request);
	}
  
	  
	  
	  
	  /*Tenant Removal*/
	  @RequestMapping(
				value = "tenants",
		    	method = RequestMethod.DELETE,
				consumes = { "application/json",  "application/xml"},
				produces = { "application/json",  "application/xml"}
				 )
	  //@ExceptionHandler(IOException.class)
	  //@ExceptionHandler(Exception.class)
	  public boolean tenantDeletion(@RequestParam(value="redisAddress", defaultValue="localhost") String redisAddress,
									 @RequestParam(value="domain", defaultValue="demo-uva") String domain,
									 @RequestParam(value="tenantId", defaultValue="tenant") String tenantId/*,
									 @RequestParam(value="request") AuthzRequest request*/) {
		  	
		  try {
			  TenantSvc tsc = new TenantSvcImpl(redisAddress, domain);
			  return tsc.removeTenant(tenantId);
			}catch(Exception e) {
				throw new RuntimeException("Couldn't remove the tenant", e);
			}
		  
		  //ev = new Evaluator(redisAddress, domain);
		  //return ev.checkAuthorization(tenantId, request);
	}  

     
}
