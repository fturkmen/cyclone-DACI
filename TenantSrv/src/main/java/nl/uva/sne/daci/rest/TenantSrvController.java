package nl.uva.sne.daci.rest;


import java.util.List;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import nl.uva.sne.daci.tenant.authzadmin.PAP;
import nl.uva.sne.daci.tenant.svcmanagement.TenantSvc;
import nl.uva.sne.daci.tenant.svcmanagement.TenantSvcImpl;

import org.springframework.web.bind.annotation.RequestMethod;
//import org.springframework.web.bind.annotation.ExceptionHandler; 

@RestController
public class TenantSrvController{

	PAP pap;
     
    /*Upload tenant policies ...*/
    @RequestMapping(
    		value = "pdps/{tenantId}/policies/{policyId}",
	    	method = {RequestMethod.GET,RequestMethod.POST},
	    	consumes = { "application/json","application/xml"},
	    	produces = { "application/json","application/xml"}
    			 )
    public List<String> policy(@RequestParam(value="redisAddress", defaultValue="localhost") String redisAddress,
    								@RequestParam(value="domain", defaultValue="localhost") String domain,
    								@RequestParam(value="policy") String policyFile) {
    	try {
    		pap =  new PAP(domain);
    		return pap.setupPolicies(redisAddress, domain);
    	}catch(Exception e) {
			throw new RuntimeException("Couldn't get the policies", e);
		}
    	
    }
    
    
	  /*Tenant User Creation*/
	  @RequestMapping(
				value = "tenants",
		    	method = RequestMethod.GET,
		    	consumes = { "application/json",  "application/xml"},
		    	produces = { "application/json",  "application/xml"}
				 )
	  //@ExceptionHandler(IOException.class)
	  //@ExceptionHandler(Exception.class)
	  public boolean tenant(@RequestParam(value="redisAddress", defaultValue="localhost") String redisAddress,
									 @RequestParam(value="domain", defaultValue="demo-uva") String domain,
									 @RequestParam(value="tenantId") String tenantId/*,
									 @RequestParam(value="request") AuthzRequest request*/) {
		  	
		  
		  try {
			  TenantSvc tsc = new TenantSvcImpl(redisAddress, domain);
			  return tsc.createTenant(tenantId);
			}catch(Exception e) {
				throw new RuntimeException("Couldn't get pdp", e);
			}
		  //ev = new Evaluator(redisAddress, domain);
		  //return ev.checkAuthorization(tenantId, request);
	}
  
	  
	  /*Tenant User Creation*/
	  @RequestMapping(
				value = "tenants/{tenantId}",
		    	method = RequestMethod.GET,
		    	consumes = { "application/json",  "application/xml"},
		    	produces = { "application/json",  "application/xml"}
				 )
	  //@ExceptionHandler(IOException.class)
	  //@ExceptionHandler(Exception.class)
	  public boolean tenantDeletion(@RequestParam(value="redisAddress", defaultValue="localhost") String redisAddress,
									 @RequestParam(value="domain", defaultValue="demo-uva") String domain,
									 @RequestParam(value="tenantId") String tenantId/*,
									 @RequestParam(value="request") AuthzRequest request*/) {
		  	
		  
		  try {
			  TenantSvc tsc = new TenantSvcImpl(redisAddress, domain);
			  return tsc.removeTenant(tenantId);
			}catch(Exception e) {
				throw new RuntimeException("Couldn't get pdp", e);
			}
		  //ev = new Evaluator(redisAddress, domain);
		  //return ev.checkAuthorization(tenantId, request);
	}  

     
}
