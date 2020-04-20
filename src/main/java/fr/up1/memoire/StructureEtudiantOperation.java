/**
 *
 */

package fr.up1.memoire;

import org.nuxeo.ecm.automation.OperationContext;
import org.jboss.seam.Seam;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.automation.core.collectors.DocumentModelCollector;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.runtime.transaction.TransactionHelper;

/**
 * @author aridereau
 */

@Operation(id=StructureEtudiantOperation.ID, category=Constants.CAT_EXECUTION, label="UP1 Mémoire Structure Étudiants", description="This operation creates the base of project Mémoires if not exist (domain, année, ufr, diplôme).")
public class StructureEtudiantOperation {

    public static final String ID = "UP1.StructureEtudiantOperation";

    @Context
    CoreSession coreSession;

    @Context
    Seam seam;

    @Param(name = "Étudiant", required = true)
    String userUID;

    @Param(name = "Année courante", required = true)
    Integer anneeCourant;

    @Param(name = "Code de l’année du diplôme (M2)", required = true)
    String codeAnneeDiplome;

    @Context
    protected OperationContext ctx;

    @OperationMethod(collector=DocumentModelCollector.class)
    public DocumentModel run(DocumentModel input) throws Exception {
        boolean started = false;
        boolean ok = false;
        // initialize repositories with a tx active
        try {
            started = !TransactionHelper.isTransactionActiveOrMarkedRollback()
                    && TransactionHelper.startTransaction();
            // DO YOUR STUFF HERE
        	String mess;
        	Structure createMemoireStructure = new Structure(coreSession);
        	createMemoireStructure.set(userUID,anneeCourant,codeAnneeDiplome);
        	createMemoireStructure.runUnrestricted();
        	ctx.put("strUFR",createMemoireStructure.userSI.get_userUfr_Id());
        	ctx.put("strDPL",createMemoireStructure.userSI.get_userDpl_Id());
        	ctx.put("strPath",createMemoireStructure.getPathDiplome());
        	ctx.put("strPathDoc",createMemoireStructure.getPathDoc());

        	mess = createMemoireStructure.getMessage();

        	Object[] params = new Object[0];
        	String severityStr = StatusMessage.Severity.INFO.name();
        	StatusMessage.Severity severity = StatusMessage.Severity.valueOf(severityStr);
        	FacesMessages facesMessages = (FacesMessages) Contexts.getConversationContext().get(FacesMessages.class);
        	facesMessages.addFromResourceBundle(severity, mess, params);
        	

            ok = true;
        } finally {
            if (started) {
                try {
                    if (!ok) {
                        TransactionHelper.setTransactionRollbackOnly();
                    }
                } finally {
                    TransactionHelper.commitOrRollbackTransaction();
                }
            }
        }
		return input;
    }

}
