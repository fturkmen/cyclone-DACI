package nl.uva.sne.daci.aspect;

public aspect AuthCheck {

    pointcut secureAccess(AuthzRequest ar, String tenantId) 
        : execution(* *.readPrivateData_AOP(AuthzRequest, String)) && args(ar, tenantId);

    boolean around(AuthzRequest ar, String tenantId) : secureAccess(ar, tenantId) {
        System.out.println("Call the authorization service");
        AuthzSvc.DecisionType res = null;
		try{
			res = AuthorizationSvcDemo.authorize(ar, tenantId).getDecision();
		}catch(Exception ex){
			System.out.println("Exception in Authz service access ");
		}
		
		if (res.equals(AuthzSvc.DecisionType.PERMIT))
			return proceed(ar,tenantId);
		else return false;
    }
}
