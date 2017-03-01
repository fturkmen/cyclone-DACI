package nl.uva.sne.daci.rest;


import java.util.List;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;


import org.springframework.web.bind.annotation.RequestMethod;
//import org.springframework.web.bind.annotation.ExceptionHandler; 



import nl.uva.sne.daci.authzsvc.AuthzRequest;
import nl.uva.sne.daci.authzsvc.AuthzResponse;
import nl.uva.sne.daci.authzsvcimp.AuthzSvcImpl;
import nl.uva.sne.daci.authzsvcimp.Configuration;
import nl.uva.sne.daci.authzsvcpdp.PDPSvc;
import nl.uva.sne.daci.authzsvcpdp.PDPSvcPoolImpl;
import nl.uva.sne.daci.authzsvcpolicy.PolicyManager;

@RestController
@EnableAutoConfiguration
public class AuthzSrvController{


  /*Creation of a PDP Instance*/
  @RequestMapping(
  			value = "pdps/{tenantId}",
	    	method = RequestMethod.GET,
	    	consumes = { "application/json",  "application/xml"},
	    	produces = { "application/json",  "application/xml"}
  			 )
  public PDPSvc pdpInstance(/*@RequestParam(value="redisAddress", defaultValue="localhost") String redisAddress,
								 @RequestParam(value="domain", defaultValue="demo-uva") String domain,*/
								 /*@RequestParam(value="tenantId")*/ String tenantId/*,
								 @RequestParam(value="request") AuthzRequest request*/) {
	  	
	  PolicyManager policyMgr = new PolicyManager(/*authzPolicyKeyPrefix*/ "MYPrefix", Configuration.REDIS_SERVER_ADDRESS);
	  
	  try {
		  PDPSvcPoolImpl pool = new PDPSvcPoolImpl(policyMgr);
		  PDPSvc newPDP = pool.getService(tenantId + "");
		  return newPDP;
		}catch(Exception e) {
			throw new RuntimeException("Couldn't get pdp", e);
		}
	  //ev = new Evaluator(redisAddress, domain);
	  //return ev.checkAuthorization(tenantId, request);
  }

  

  
  /*Policy Evaluation */
  @RequestMapping(
  			value = "pdps/{tenantId}/decision",
	    	method = RequestMethod.GET,
	    	consumes = { "application/json",  "application/xml"},
	    	produces = { "application/json",  "application/xml"}
  			 )
  //@ExceptionHandler(IOException.class)
  //@ExceptionHandler(Exception.class)
  public PDPSvc pdp(@RequestParam(value="redisAddress", defaultValue="localhost") String redisAddress,
								 @RequestParam(value="domain", defaultValue="demo-uva") String domain,
								 @RequestParam(value="tenantId") String tenantId/*,
								 @RequestParam(value="request") AuthzRequest request*/) {
	  	
	  PolicyManager policyMgr = new PolicyManager(/*authzPolicyKeyPrefix*/ "MYPrefix", Configuration.REDIS_SERVER_ADDRESS);
	  
	  try {
		  /*Policy Evaluation Code*/
		  
		}catch(Exception e) {
			throw new RuntimeException("Couldn't get pdp", e);
		}
	  return null;
	  //ev = new Evaluator(redisAddress, domain);
	  //return ev.checkAuthorization(tenantId, request);
  }
  
  
  
  @RequestMapping(
  			value = "pdps/{tenantId}/hello",
	    	method = RequestMethod.GET
  			 )
  //@ExceptionHandler(IOException.class)
  //@ExceptionHandler(Exception.class)
  public String hello(@RequestParam(value="redisAddress", defaultValue="localhost") String redisAddress,
								 @RequestParam(value="domain", defaultValue="demo-uva") String domain,
								 @PathVariable String tenantId/*,
								 @RequestParam(value="request") AuthzRequest request*/) {
	  try {
		  return "Hello: Authorization Service --> Address:" + redisAddress + " Domain:" + domain + " tenantId:"+ tenantId;
		}catch(Exception e) {
			throw new RuntimeException("Couldn't get the message", e);
		}

  }

  

  
    
}
