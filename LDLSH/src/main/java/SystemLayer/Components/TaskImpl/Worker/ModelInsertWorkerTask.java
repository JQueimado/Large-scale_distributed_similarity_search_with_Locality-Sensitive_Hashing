package SystemLayer.Components.TaskImpl.Worker;

import NetworkLayer.Message;
import SystemLayer.Components.DataProcessor.DataProcessor;
import SystemLayer.Components.MultiMapImpl.MultiMap;
import SystemLayer.Containers.DataContainer;
import SystemLayer.Data.DataObjectsImpl.DataObject;
import SystemLayer.Data.DataUnits.ModelMultimapValue;
import SystemLayer.SystemExceptions.InvalidMessageTypeException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ModelInsertWorkerTask implements WorkerTask {

    private final Message insertRequest;

    private final DataContainer appContext;

    private final String hash_config;
    private final String erasure_config;
    private final String uid_config;
    private final int bands;

    public ModelInsertWorkerTask(Message insertRequest, DataContainer appContext ) throws Exception {

        if( insertRequest.getType() != Message.types.INSERT_REQUEST )
            throw new Exception("Invalid Message type for InsertTask");

        this.insertRequest = insertRequest;
        this.appContext = appContext;
        this.hash_config = appContext.getConfigurator().getConfig("LSH_HASH");
        this.erasure_config = appContext.getConfigurator().getConfig("ERASURE_CODES");
        this.uid_config = appContext.getConfigurator().getConfig("UNIQUE_IDENTIFIER");
        this.bands = Integer.parseInt( appContext.getConfigurator().getConfig("N_BANDS") );
    }

    @Override
    public DataObject call() throws Exception {
        if( insertRequest.getType() != Message.types.INSERT_REQUEST )
            throw new InvalidMessageTypeException(
                    Message.types.INSERT_REQUEST,
                    insertRequest.getType());

        DataObject object = (DataObject) insertRequest.getBody().get(0);

        //PREPROCESS
        DataProcessor.ProcessedData processedData = appContext.getDataProcessor().preProcessData(object);

        //Package and Insert
        try {
            MultiMap[] multiMaps = appContext.getMultiMaps();
            //Shuffle indexes
            List<Integer> indexes = new ArrayList<>();
            for ( int i=0; i<multiMaps.length; i++ ){
                indexes.add( i );
            }
            Collections.shuffle(indexes);

            //Insert
            for ( int i = 0; i<multiMaps.length; i++ ){
                MultiMap multiMap = multiMaps[i];

                ModelMultimapValue modelMultimapValue = new ModelMultimapValue(
                        processedData.object_lsh(),
                        processedData.object_uid(),
                        processedData.object_erasureCodes().getBlockAt(indexes.get(i))
                );

                multiMap.insert(
                        processedData.object_lsh(),
                        modelMultimapValue
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return object;
    }
}
