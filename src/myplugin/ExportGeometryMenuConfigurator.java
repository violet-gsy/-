package myplugin;

import com.nomagic.actions.AMConfigurator;
import com.nomagic.actions.ActionsCategory;
import com.nomagic.actions.ActionsManager;

public class ExportGeometryMenuConfigurator implements AMConfigurator {

    private static final String CATEGORY_ID = "DEMO_EXPORT_GEO_CATEGORY";

    @Override
    public void configure(ActionsManager manager) {
        ActionsCategory category = new ActionsCategory(CATEGORY_ID, "Simulation Export");
        category.addAction(new ExportActiveDiagramGeometryAction());
        category.addAction(new StartSyncServerAction());
        manager.addCategory(category);
    }
}