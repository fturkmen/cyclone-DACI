package nl.uva.sne.daci.context;

import java.util.HashMap;
import java.util.Map;

import nl.uva.sne.daci.context.ContextRequest;

public class ContextRequestImpl implements ContextRequest {

	private Map<String, String> subject;
	
	/**
	 * Only contains attributes for resource (incl action) & environment
	 */
	private Map<String, String> permission;
	
	public ContextRequestImpl(Map<String, String> subject, Map<String, String> permission){
		this.subject = subject;
		this.permission = permission;
	}
	
	@Override
	public Map<String, String> getSubjectAttributes() {
		return subject;
	}

	@Override
	public Map<String, String> getPermissionAttributes() {
		return permission;
	}

	@Override
	public Map<String, String> getAttributes() {
		Map<String, String> attrs = new HashMap<String, String>(subject.size() + permission.size());
		attrs.putAll(subject);
		attrs.putAll(permission);
		return attrs;
	}
	
	//FT:03.02.2017 : Added JSON  serialization/deserialization requirements ...
	public ContextRequestImpl(){}
	public void setSubject(Map<String, String> attrs){subject= attrs;}
	public void setPermission(Map<String, String> perm){permission= perm;}
	public Map<String, String> getSubject(){return subject;}
	public Map<String, String> getPermission(){return permission;}
	
	
	@Override
	public String toString(){
		StringBuilder builder = new StringBuilder();
		
//		Map<String, String> attrs = getAttributes();
//		builder.append("Attributes:" + attrs.size() + "|");		
//		for(String id: attrs.keySet())
//			builder.append(id + " >>>> " + attrs.get(id) + "|");

		builder.append("Subject attributes:");
		for(String id: this.subject.keySet())
			builder.append(id + " >>>> " + this.subject.get(id) + "|");
		
		builder.append("Permission attributes:");
		for(String id: this.permission.keySet())
			builder.append(id + " >>>> " + this.permission.get(id) + "|");
		
		return builder.toString();
	}

}
