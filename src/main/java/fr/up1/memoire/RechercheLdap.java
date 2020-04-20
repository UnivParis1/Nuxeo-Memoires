package fr.up1.memoire;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.nuxeo.ecm.directory.ldap.LDAPDirectoryFactory;
import org.nuxeo.runtime.api.Framework;

public class RechercheLdap {

	Hashtable<String, String> env;
	DirContext dirContext;

	public RechercheLdap(){
		env = new Hashtable<String, String>();
		env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		LDAPDirectoryFactory factory;
		try{
			factory = (LDAPDirectoryFactory) Framework.getRuntime().getComponent("default");
			env.put( Context.SECURITY_AUTHENTICATION , "simple"                                       );
			env.put( Context.PROVIDER_URL            , factory.getServer("default").getLdapUrls()     );
			env.put( Context.SECURITY_PRINCIPAL      , factory.getServer("default").getBindDn()       );
			env.put( Context.SECURITY_CREDENTIALS    , factory.getServer("default").getBindPassword() );
		}catch(NullPointerException e){
			env.put(Context.PROVIDER_URL, "ldap://ldap-test.univ-paris1.fr");
		}
	}

    public HashMap<String, String> getAttr(String name, String[] attributs) {
    	HashMap<String, String> alReturn = new HashMap<String, String>();
		try {
			dirContext = new InitialDirContext(env);
			Attributes attrs;
		    attrs = dirContext.getAttributes( name , attributs );
		    for(String attribut : attributs){
		    	alReturn.put(attribut, "");
		    	for (Object val : java.util.Collections.list(attrs.get(attribut).getAll())) {
		    		alReturn.put(attribut, (String) val);
		    	}
		    }
		    dirContext.close();
		} catch (NamingException e) {
		    //System.err.println("Erreur lors de l'acces au serveur LDAP" + e);
		    e.printStackTrace();
		}
		return alReturn;
    }

    public ArrayList<String> searchSupannEtuInscription(String eppn) {
    	ArrayList<String> alReturn = new ArrayList<String>();
		try {
			dirContext = new InitialDirContext(env);
		    NamingEnumeration<SearchResult> sr = dirContext.search("ou=people,dc=univ-paris1,dc=fr", "edupersonprincipalname="+eppn, new SearchControls());
		    if(sr.hasMoreElements()){
		    	for (Object val : java.util.Collections.list(sr.next().getAttributes().get("supannEtuInscription").getAll())) {
		    		alReturn.add((String) val);
		    	}
		    }
		    dirContext.close();
		} catch (NamingException e) {
		    e.printStackTrace();
			alReturn.add("");
		} catch (NullPointerException e){
			e.printStackTrace();
			alReturn.add("");
		}
		return alReturn;
    }


}
