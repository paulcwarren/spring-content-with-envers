package gettingstarted;

import org.springframework.content.commons.annotations.HandleBeforeSetContent;
import org.springframework.content.commons.annotations.StoreEventHandler;

@StoreEventHandler
public class AuditedSetContentEventHandler {

    @HandleBeforeSetContent
    public void onBeforeSetContent(File file) {
        file.setContentId(null);
    }
}

