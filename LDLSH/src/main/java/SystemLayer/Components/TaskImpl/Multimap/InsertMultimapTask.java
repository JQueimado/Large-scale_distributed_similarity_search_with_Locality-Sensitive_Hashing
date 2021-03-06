package SystemLayer.Components.TaskImpl.Multimap;

import NetworkLayer.Message;
import NetworkLayer.MessageImpl;
import SystemLayer.Components.MultiMapImpl.MultiMap;
import SystemLayer.Containers.DataContainer;
import SystemLayer.Data.DataUnits.MultiMapValue;
import SystemLayer.Data.ErasureCodesImpl.ErasureCodesImpl.ErasureBlock;
import SystemLayer.Data.LSHHashImpl.LSHHash;
import SystemLayer.Data.UniqueIndentifierImpl.UniqueIdentifier;

import java.util.ArrayList;
import java.util.List;

public class InsertMultimapTask implements MultimapTask {

    private final Message insertMessage;
    private final DataContainer appContext;

    public InsertMultimapTask( Message insertMessage, DataContainer appContext ){
        this.insertMessage = insertMessage;
        this.appContext = appContext;
    }

    @Override
    public Message call() throws Exception {
        if( insertMessage.getBody().size() != 2 )
            throw new Exception("Invalid body Size for message type: INSERT_MESSAGE");

        LSHHash hash = (LSHHash) insertMessage.getBody().get(0);
        MultiMapValue value = (MultiMapValue) insertMessage.getBody().get(1);

        List<Object> responseBody = new ArrayList<>();
        try {
            MultiMap[] multiMaps = appContext.getMultiMaps();
            for ( MultiMap multiMap : multiMaps ){
                multiMap.insert(hash, value);
            }
            responseBody.add(true);
        } catch (Exception e) {
            e.printStackTrace();
            appContext.getCommunicationLayer();
            responseBody.add(false);
        }

        return new MessageImpl(Message.types.INSERT_MESSAGE_RESPONSE, responseBody);
    }
}
