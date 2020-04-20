/**
 *
 */

package fr.up1.memoire;

import java.io.Serializable;
import java.util.List;


//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
//import org.jboss.seam.faces.FacesMessages;
//import org.jboss.seam.international.StatusMessage;
import org.nuxeo.ecm.core.api.NuxeoException;
//import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.platform.ui.web.api.NavigationContext;
import org.nuxeo.ecm.webapp.documentsLists.DocumentsListsManager;

/**
 *
 * Code skeleton for a Seam bean that will manage a simple action.
 * This can be used to :
 *  - do a navigation
 *  - do some modification on the currentDocument (or other docs)
 *  - create new documents
 *   - send/retrive info from an external service
 *   - ...
 */

@Name("memoireActions")
@Scope(ScopeType.EVENT)
public class MemoireActionsBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @In(create = true)
    protected NavigationContext navigationContext;

    @In(create = true, required = false)
    protected NuxeoPrincipal currentNuxeoPrincipal;

    @In(create = true)
    protected DocumentsListsManager documentsListsManager;

    protected List<DocumentModel> getCurrentlySelectedDocuments() {

        if (navigationContext.getCurrentDocument().isFolder()) {
            return documentsListsManager.getWorkingList(DocumentsListsManager.CURRENT_DOCUMENT_SELECTION);
        } else {
            return null;
        }
    }

    public boolean lifeCycleNotDepot() throws NuxeoException{

    	List<DocumentModel> selectedDocs = getCurrentlySelectedDocuments();
        if (selectedDocs != null && ! selectedDocs.isEmpty() ) {
        	for (DocumentModel documentModel : selectedDocs) {
        		if(	"mem-dc-memoire".equals(documentModel.getType() )
        		&&	! "Depot".equals(documentModel.getCurrentLifeCycleState()) ){
        			return true;
        		}
        	}
        }
    	return false;

   }

}
