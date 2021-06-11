package fr.up1.memoire;

import java.util.ArrayList;

import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.ecm.core.api.UnrestrictedSessionRunner;
import org.nuxeo.ecm.core.api.security.ACE;
import org.nuxeo.ecm.core.api.security.ACP;
import org.nuxeo.ecm.core.api.security.SecurityConstants;
import org.nuxeo.ecm.core.api.security.impl.ACLImpl;
import org.nuxeo.ecm.core.api.security.impl.ACPImpl;
import org.nuxeo.ecm.platform.usermanager.UserManager;
import org.nuxeo.runtime.api.Framework;

public class Structure extends UnrestrictedSessionRunner {
	

	private UserManager userManager;
	
	private String nl;

	public UserSI userSI;

	private String lstGrp;
	private String lstGrpSI;

	private DocumentModel docModel;
	private ACP acp;
	private ACLImpl acl;

	private String pathDiplome;
	public String getPathDiplome() {
		return pathDiplome;
	}
	public void setPathDiplome(String pathDiplome) {
		this.pathDiplome = pathDiplome;
	}

	private String pathDoc;
	public String getPathDoc(){
		return pathDoc;
	}
	public void setPathDoc(String pathDoc){
		this.pathDoc = pathDoc;
	}

	private String message;
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getListGroupe(){
		return "\n\n\nGroupes\n"+lstGrp+"\n\nGroupe SI\n"+lstGrpSI;
	}

	@Override
	public void run() throws NuxeoException {
		make();
	}

	/**
	 * Constructeur avec la session nuxeo en cours
	 * @param localSession
	 * @throws Exception
	 */
	public Structure(CoreSession localSession) throws Exception {

		super(localSession);
		userManager = Framework.getService(UserManager.class);

		nl="<br />";
		setMessage("");
		setPathDiplome("");
		setPathDoc("");
		lstGrp = "";
		lstGrpSI = "";
		docModel	= null;
		acp		= null;

	}

	public void set(UserSI userLdap){
		userSI = userLdap;
	}

	public void set(String userName, Integer anneeCourante, String codeAnneeDpl ){
		userSI = new UserSI(userName,anneeCourante,codeAnneeDpl);
	}
	/**
	 * @throws NuxeoException
	 */
	public void make() throws NuxeoException {
		String str="";
		MemDocInfo mdiDomainMemoire = new MemDocInfo("/","Domain","memoires","Mémoires","Domaine de dépot des mémoires de Master II");

		if(!session.exists(new PathRef(mdiDomainMemoire.getPath()+"/"+userSI.get_strPathDoc()))){
			MemDocInfo mdiAnnee   = new MemDocInfo(mdiDomainMemoire.getPath(),"mem-dc-annee",userSI.get_anneeCourante(),userSI.get_anneeCourante(),"Année de soutenance");
			MemDocInfo mdiUFR     = new MemDocInfo(mdiAnnee.getPath(),"mem-dc-ufr",userSI.get_userUfr_Id(),"","");
			MemDocInfo mdiDiplome = new MemDocInfo(mdiUFR.getPath(),"mem-dc-diplome",userSI.get_userDpl_Id(),"","");

			if( !session.exists(new PathRef(mdiDiplome.getPath())) ){
				if( !session.exists(new PathRef(mdiUFR.getPath())) ){
					if( !session.exists(new PathRef(mdiAnnee.getPath())) ){
						if( !session.exists(new PathRef(mdiDomainMemoire.getPath())) ){
							str+=makeMemoireRoot(mdiDomainMemoire);
						}
						str+=makeAnnee(mdiAnnee);
					}
					userSI.get_userUfrInfo();
					mdiUFR.setTitle(userSI.get_userUfrLib());
					mdiUFR.setDescription(userSI.get_userUfrDes());
					str+=makeUfr(mdiUFR);
				}
			}
			userSI.get_userDplInfo();
			mdiDiplome.setTitle(userSI.get_userDplLib());
			mdiDiplome.setDescription(userSI.get_userDplDes());
			str+=makeDiplome(mdiDiplome);

			setPathDiplome(mdiDiplome.getPath());
			setPathDoc("");
			str=""; // suppression message dev
		}else{
			setPathDoc(mdiDomainMemoire.getPath()+"/"+userSI.get_strPathDoc());
			str+="Le mémoire existe.";
		}
		setMessage(str);
	}

	/**
	 * Create Domain with groups of user and rights on.
	 * @param MemDocInfo memDocInfo
	 * @return
	 * @throws NuxeoException
	 */
	private String makeMemoireRoot(MemDocInfo memDocInfo) throws NuxeoException {
		String strRes = "";

		strRes += getOrCreate(memDocInfo);

		if( session.exists(new PathRef("/memoires/templates")) ) {
			session.removeDocument(new PathRef("/memoires/templates"));
		}
		if( session.exists(new PathRef("/memoires/workspaces")) ) {
			session.removeDocument(new PathRef("/memoires/workspaces"));
		}

		strRes += createGroup(getGroupNameSI("mem_admin-nx")   , "Mémoires : Admin"         , "" , "" , "Administrator");
		strRes += createGroup(getGroupNameSI("mem_gestion-nx") , "Mémoires : Gestionnaires" , "" , "" , ""             );
		strRes += createGroup(getGroupNameSI("mem_prof-nx")    , "Mémoires : Professeurs"   , "" , "" , ""             );
		strRes += createGroup(getGroupNameSI("mem_etud-nx")    , "Mémoires : Étudiants"     , "" , "" , ""             );
		rightInit();
		strRes += rightAdd(getGroupNameSI("mem_admin-nx")   , "E" , true );
		strRes += rightAdd(getGroupNameSI("mem_gestion-nx") , "R" , true );
		strRes += rightAdd(getGroupNameSI("mem_prof-nx")    , "R" , true );
		strRes += rightAdd(getGroupNameSI("mem_etud-nx")    , "R" , true );
		strRes += rightAdd("members"    , "R" , true );
		rightAdd("nothing","",false);
		rightFinish();

		return strRes;
	}
	/**
	 * Create level Année with groups of user and rights on.
	 * @param MemDocInfo memDocInfo
	 * @return
	 * @throws NuxeoException
	 */
	private String makeAnnee(MemDocInfo memDocInfo) throws NuxeoException, NuxeoException {
		String strRes = "";

		strRes += getOrCreate(memDocInfo);

		rightInit();
		strRes += rightAdd(getGroupNameSI("mem_admin-nx")   , "E" , true );
		strRes += rightAdd(getGroupNameSI("mem_gestion-nx") , "R" , true );
		strRes += rightAdd(getGroupNameSI("mem_prof-nx")    , "R" , true );
		strRes += rightAdd(getGroupNameSI("mem_etud-nx")    , "R" , true );
		rightAdd("nothing","",false);
		rightFinish();

		return strRes;
	}
	/**
	 * Create level UFR with groups of user and rights on.
	 * @param MemDocInfo memDocInfo
	 * @return
	 * @throws NuxeoException
	 */
	private String makeUfr(MemDocInfo memDocInfo) throws NuxeoException, NuxeoException {
		String strRes = "";
		strRes += getOrCreate(memDocInfo);
		return strRes;
	}
	/**
	 * Create level Diplôme with groups of user and rights on.
	 * @param MemDocInfo memDocInfo
	 * @return
	 * @throws NuxeoException
	 */
	private String makeDiplome(MemDocInfo memDocInfo) throws NuxeoException, NuxeoException {
		String strRes = "";
		//try {

		strRes += getOrCreate(memDocInfo);
		strRes += createGroup("mem-d"+memDocInfo.getName()+"-prof" , "Mémoires : Professeurs de "+memDocInfo.getName()    , getGroupNameSI("mem_prof-nx") , "" , "" );
		strRes += createGroup(getGroupNameSI("mem-d"+memDocInfo.getName()+"-etud-nx") , "Mémoires : Étudiants de "+memDocInfo.getName()      , ""                                 , "" , userSI.get_userName() );
		rightInit();
		strRes += rightAdd("mem-d"+memDocInfo.getName()+"-prof", "R"  ,true );
		strRes += rightAdd(getGroupNameSI("mem-d"+memDocInfo.getName()+"-etud-nx"), "W" ,true );
		strRes += rightAdd(getGroupNameSI("mem-d"+memDocInfo.getName()+"-gest-nx"), "W" ,true );
		strRes += rightAdd(getGroupNameSI("mem-d"+memDocInfo.getName()+"-bibl-nx"), "W" ,true );
		rightFinish();
	//}
		return strRes;

	

		//catch (Exception e) {

		//  System.out.println(e);
		// }

	}

	/**
	 * Créer un document nuxeo
	 * @param MemDocInfo memDocInfo
	 * @return
	 * 
	 * @throws NuxeoException
	 */
	private String getOrCreate (MemDocInfo memDocInfo) throws NuxeoException, NuxeoException {
		String strRes = "";
		DocumentModel tmpDoc;
		if( ! (memDocInfo.getName().isEmpty() || memDocInfo.getType().isEmpty())  ){

			if( session.exists(new PathRef(memDocInfo.getParentPath())) ){
				DocumentModelList dml = session.query("SELECT * FROM Document WHERE ecm:primaryType='"+memDocInfo.getType()+"' AND ecm:path='"+memDocInfo.getPath()+"'");
				if(dml.isEmpty()){
					tmpDoc=session.createDocumentModel(memDocInfo.getParentPath(),memDocInfo.getName(),memDocInfo.getType());

					tmpDoc.setPropertyValue("dc:title", memDocInfo.getTitle());
					tmpDoc.setPropertyValue("title", memDocInfo.getTitle());

					if(!memDocInfo.getDescription().isEmpty()){
						tmpDoc.setPropertyValue("dc:description", memDocInfo.getDescription());
					}
					if("mem-dc-diplome".equals(memDocInfo.getType()) || "mem-dc-ufr".equals(memDocInfo.getType()) ){
						String pre;
						String suf;
						if(memDocInfo.getType().equals("mem-dc-diplome")){pre="d";suf="-nx";
						tmpDoc.setPropertyValue( memDocInfo.getType()+":grp-etud" , getGroupNameSI("mem-"+pre+memDocInfo.getName()+"-etud"+suf) );
						tmpDoc.setPropertyValue( memDocInfo.getType()+":grp-gest" , getGroupNameSI("mem-"+pre+memDocInfo.getName()+"-gest"+suf) );
						tmpDoc.setPropertyValue( memDocInfo.getType()+":grp-bibl" , getGroupNameSI("mem-"+pre+memDocInfo.getName()+"-bibl"+suf) );
						tmpDoc.setPropertyValue( memDocInfo.getType()+":grp-prof" , "mem-"+pre+memDocInfo.getName()+"-prof"     );
						}else{
							pre="u";suf="";
							tmpDoc.setPropertyValue( memDocInfo.getType()+":grp-etud" , "mem-"+pre+memDocInfo.getName()+"-etud"+suf );
							tmpDoc.setPropertyValue( memDocInfo.getType()+":grp-gest" , "mem-"+pre+memDocInfo.getName()+"-gest"+suf );
							tmpDoc.setPropertyValue( memDocInfo.getType()+":grp-bibl" , "mem-"+pre+memDocInfo.getName()+"-bibl"+suf );
							tmpDoc.setPropertyValue( memDocInfo.getType()+":grp-prof" , "mem-"+pre+memDocInfo.getName()+"-prof"     );
						}
					}

					tmpDoc = session.createDocument(tmpDoc);
					session.save();

					docModel = session.getDocument(tmpDoc.getRef());
					strRes += " + Document : "+memDocInfo.toString()+" créé";
				} else {
					if(dml.size()==1){
						docModel = session.getDocument(dml.get(0).getRef());
					}else{
						strRes += "pulsieurs documents"; // doublons
					}
					strRes += " _	Document : "+memDocInfo.toString()+" exists";
				}
			}else{
				strRes+=memDocInfo.getParentPath()+" Empty ";
			}
		}else{
			strRes+="name or type vide";
		}
		return strRes + nl;
	}
	/**
	 * Créer un goupe et éventuellement avec ses parents, enfants et un membre
	 * @param groupName
	 * @param groupTitle
	 * @param groupParent
	 * @param groupEnfant
	 * @param userId
	 * @return
	 * @throws NuxeoException
	 */
	@SuppressWarnings("unchecked")
	private String createGroup(String groupName, String groupTitle, String groupParent, String groupEnfant, String userId) throws NuxeoException {
		String strRes = "";
		boolean blnCreate = false;
		boolean blnUpdate = false;
		String grpSchema = userManager.getGroupSchemaName();
		String grpFieldId = userManager.getGroupIdField();
		String grpFieldLabel = userManager.getGroupLabelField();
		String grpFieldMembers = userManager.getGroupMembersField();
		String grpFieldSubGrp = userManager.getGroupSubGroupsField();
		String grpFieldParGrp = userManager.getGroupParentGroupsField();

		DocumentModel dmGrp = userManager.getGroupModel(groupName);
		//this.userManager.getGroup(groupName).getMemberGroups();
		//this.userManager.getGroup(groupName).getMemberUsers();
		ArrayList<String> alUsers;
		ArrayList<String> alSubGroup;
		ArrayList<String> alParGroup;

		if(dmGrp==null){
			blnCreate = true;
			dmGrp = userManager.getBareGroupModel();
			dmGrp.setProperty(grpSchema , grpFieldId   , groupName);
			dmGrp.setProperty(grpSchema , grpFieldLabel , groupTitle);
			alUsers = new ArrayList<String>();
			alSubGroup = new ArrayList<String>();
			alParGroup = new ArrayList<String>();
		}else{
			alUsers    = (ArrayList<String>) dmGrp.getProperty(grpSchema, grpFieldMembers);
			alSubGroup = (ArrayList<String>) dmGrp.getProperty(grpSchema, grpFieldSubGrp);
			alParGroup = (ArrayList<String>) dmGrp.getProperty(grpSchema, grpFieldParGrp);
		}

		if(!userId.isEmpty()){
			blnUpdate =true;
			if(!alUsers.contains(userId)) {
                alUsers.add(userId);
            }
			dmGrp.setProperty(grpSchema, grpFieldMembers, alUsers);
		}
		String subGR = getGroupNameSI(groupName);
		if(!subGR.isEmpty() || !groupEnfant.isEmpty()){
			blnUpdate = true;
			if(!subGR.isEmpty()) {
                if(!alSubGroup.contains(subGR)) {
                    alSubGroup.add(subGR);
                }
            }
			if(!groupEnfant.isEmpty()) {
                if(!alSubGroup.contains(groupEnfant)) {
                    alSubGroup.add(groupEnfant);
                }
            }

			dmGrp.setProperty(grpSchema, grpFieldSubGrp, alSubGroup);
			if(!subGR.isEmpty()){
				strRes += "		+ Groupe : " + groupName + " créé, sous groupe à créer dans grouper "+subGR;
				lstGrpSI+= subGR+"\n";
			} else {
				strRes += "		+ Groupe : " + groupName + " créé";
			}
		}
		if(groupParent!="") {
			if(!alParGroup.contains(groupParent)) {
                alParGroup.add(groupParent);
            }
			dmGrp.setProperty(grpSchema, grpFieldParGrp, alParGroup);
			strRes += "	:	"+groupName+" ∈ "+groupParent;
		}/**/
		if(blnCreate){
			userManager.createGroup(dmGrp);
		}else if(blnUpdate){
			//userManager.updateGroup(dmGrp);
		}

		return strRes + nl;
	}
		
	
	private String getGroupNameSI(String name){
		String strRES = "";
		if(name.substring(name.length()-3).equals("-nx")){
			strRES = "applications.nuxeo."+name.substring(0,name.length()-3).replaceAll("[-_]", ".");
		}
		return strRES;
	}

	/**
	 * Initialise les membres acp et acl
	 */
	private void rightInit() {
		acp = new ACPImpl();
		acl = new ACLImpl();
	}
	/**
	 * Ajoute une ace au membre acl
	 * (E : Everything, R : Read, W : Write, D : Remove)
	 * @param userGroup
	 * @param right
	 * @param grant
	 * @return
	 */
	private String rightAdd(String userGroup, String right, Boolean grant){
		String strRes = "";
		String sRight = "";
		if(userGroup.equals("nothing")) {
			acl.add(new ACE("Administrator", SecurityConstants.EVERYTHING, true));
			acl.add(new ACE(SecurityConstants.EVERYONE, SecurityConstants.EVERYTHING, false));
			sRight = "Pas d’héritage";
		} else {
			if(right.indexOf('E')>-1) {     acl.add( new ACE(userGroup, SecurityConstants.EVERYTHING , grant) ); sRight = (grant?"+":"-")+"everything" ; }else{
				if(right.indexOf('R')>-1) { acl.add( new ACE(userGroup, SecurityConstants.READ       , grant) ); sRight+= (grant?"+":"-")+"read"      ; }
				if(right.indexOf('W')>-1) { acl.add( new ACE(userGroup, SecurityConstants.READ_WRITE , grant) ); sRight+= (grant?"+":"-")+"ReadWrite"     ; }
				//if(right.indexOf('D')>-1) { acl.add( new ACE(userGroup, SecurityConstants.REMOVE     , grant) ); sRight+= (grant?"+":"-")+"remove"    ; }
			}
		}
		acp.addACL(acl);
		strRes += "		("+userGroup+",'"+sRight+"') to " + docModel.getName()+nl;
		return strRes;
	}
	/**
	 * Apply acp to docModel
	 * @throws NuxeoException
	 */
	private void rightSet() throws NuxeoException {
		docModel.setACP(acp, true);
	}
	/**
	 * Ajout les droits de gestion au groupe mem_admin-nx et block l’héritage des droits
	 * @throws NuxeoException
	 */
	private void rightFinish() throws NuxeoException {
		rightSet();
	}

}
