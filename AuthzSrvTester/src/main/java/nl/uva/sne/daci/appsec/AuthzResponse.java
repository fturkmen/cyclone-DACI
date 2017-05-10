package nl.uva.sne.daci.appsec;

//import javax.xml.bind.annotation.XmlRootElement;
//import javax.xml.bind.annotation.XmlType;


//@XmlRootElement(name="AuthzResponse", namespace="http://sne.uva.nl/daci/authzsvc")
//@XmlType(propOrder = {"token", "decision"})
public class AuthzResponse{

	private String token;
		
	private AuthzSvc.DecisionType decision;
	
	public AuthzResponse(){
		
	}
	
	public AuthzResponse(AuthzSvc.DecisionType decision) {
		this.decision = decision;
		this.token = null;
	}
	
	public AuthzResponse(AuthzSvc.DecisionType decision, String token) {
		this.decision = decision;
		this.token = token;
	}
	
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}

	public AuthzSvc.DecisionType getDecision() {
		return decision;
	}
	
	public void setDecision(AuthzSvc.DecisionType decision) {
		this.decision = decision;
	}
}
