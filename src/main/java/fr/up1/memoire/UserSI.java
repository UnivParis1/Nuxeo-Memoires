package fr.up1.memoire;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.util.HashMap;

public class UserSI {

	private RechercheLdap rLdap;

	private String userName;
	private Integer userIns;
	private String anneeCourante;
	private String codeAnneeDiplome;

	private String userUfr_Id;
	private String userUfrLib;
	private String userUfrDes;

	private String userDpl_Id;
	private String userDplLib;
	private String userDplDes;


	private boolean userLdap;

	public UserSI(String username, Integer anneeCourante, String codeAnneeDiplome){

		rLdap = new RechercheLdap();

		userName = username;
		userIns  = anneeCourante-1;
		this.anneeCourante = anneeCourante.toString();
		this.codeAnneeDiplome = codeAnneeDiplome;

		userUfr_Id = "";
		userUfrLib = "";
		userUfrDes = "";

		userDpl_Id = "";
		userDplLib = "";
		userDplDes = "";

		ArrayList<String> results = rLdap.searchSupannEtuInscription(userName);

		Pattern p = Pattern.compile( ".*anneeinsc="+(userIns.toString())+".*cursusann=\\{.*\\}"+this.codeAnneeDiplome+".*affect=(\\w+).*etape=\\{.*\\}(\\w+).*" );

		Matcher m;
		userLdap = false;
		// TODO multivalue
		for( String res : results ){
			m = p.matcher(res);
			if( m.matches()) {
				userLdap = true;
				userUfr_Id = m.group(1);
				userDpl_Id = m.group(2);
				break;
			}
		}
		if(!userLdap){
			userUfr_Id = "ufr";
			userDpl_Id = "master";
		}
	}

	public String get_strPathDoc() {
		return get_anneeCourante()+"/"+get_userUfr_Id()+"/"+get_userDpl_Id()+"/"+get_anneeCourante()+"_"+get_userName() ;
	}

	public String get_userName() {
		return userName;
	}

	public String get_anneeCourante() {
		return anneeCourante;
	}

	public String get_userUfr_Id() {
		return userUfr_Id;
	}
	public void get_userUfrInfo() {
		if(userLdap){
			HashMap<String, String> infos = rLdap.getAttr("supannCodeEntite="+get_userUfr_Id()+",ou=structures,dc=univ-paris1,dc=fr", new String[] {"ou","description" }) ;
			userUfrLib =infos.get("ou") ;
			userUfrDes =infos.get("description") ;
		}else{
			userUfrLib = "UFR";
			userUfrDes = "Dossier par défaut pour les utilisateurs n’ayant pas de correspondance LDAP";
		}
	}
	public String get_userUfrLib() {
		return (!userUfrLib.isEmpty()?userUfrLib:userUfr_Id);
	}
	public String get_userUfrDes() {
		return (!userUfrDes.isEmpty()?userUfrDes:"");
	}

	public String get_userDpl_Id() {
		return userDpl_Id;
	}
	public void get_userDplInfo() {
		if(userLdap){
			HashMap<String, String> infos = rLdap.getAttr("ou="+get_userDpl_Id()+",ou="+(userIns.toString())+",ou=diploma,o=Paris1,dc=univ-paris1,dc=fr",  new String[] {"description"}) ;
			String strRES="";
			Pattern pM2P = Pattern.compile( ".+ - [mM]aster +2 +[pP]ro +(.+)" );
			Matcher mM2P;
			mM2P = pM2P.matcher( infos.get("description") );
			if ( mM2P.matches() ) {
                strRES = "M2P "+mM2P.group(1);
            } else{
				Pattern pM2R = Pattern.compile( ".+ - [mM]aster +2 +[rR]ech +(.+)" );
				Matcher mM2R;
				mM2R = pM2R.matcher( infos.get("description") );
				if ( mM2R.matches() ) {
                    strRES = "M2R "+mM2R.group(1);
                }
			}
			if(!strRES.isEmpty()) {
                userDplLib = strRES;
            } else {
                userDplLib = infos.get("description");
            }
			userDplDes = infos.get("description") ;
		}else{
			userDplLib = "Master";
			userDplDes = "Dossier par défaut pour les utilisateurs n’ayant pas de correspondance LDAP ou des retardataires";
		}
	}
	public String get_userDplLib() {
		return userDplLib;
	}
	public String get_userDplDes() {
		return userDplDes;
	}

}
