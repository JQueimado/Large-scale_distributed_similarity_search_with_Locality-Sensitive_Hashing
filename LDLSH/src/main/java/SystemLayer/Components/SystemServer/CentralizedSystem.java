package SystemLayer.Components.SystemServer;

import Factories.ComponentFactories.MultimapFactory;
import NetworkLayer.Message;
import SystemLayer.Components.MultiMapImpl.MultiMap;
import SystemLayer.Components.TaskImpl.Worker.InsertWorkerTask;
import SystemLayer.Components.TaskImpl.Worker.StandardQueryWorkerTask;
import SystemLayer.Components.TaskImpl.Worker.WorkerTask;
import SystemLayer.Containers.Configurator.Configurator;
import SystemLayer.Containers.DataContainer;
import SystemLayer.Data.DataObjectsImpl.DataObject;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class CentralizedSystem implements SystemServer {

    private DataContainer context;

    public CentralizedSystem( DataContainer context ) throws Exception {
        this.context = context;

        //Setup
        Configurator configurator = context.getConfigurator();
        int bands = Integer.parseInt( configurator.getConfig("N_BANDS") );

        //-MultiMaps
        MultimapFactory multimapFactory = new MultimapFactory();
        String multimapConfig = configurator.getConfig("MULTIMAP");
        MultiMap[] multiMaps = new MultiMap[bands];
        for ( int i = 0; i<bands; i++ ){
            MultiMap current = multimapFactory.getNewMultiMap(multimapConfig);
            current.setHashBlockPosition(i);
            current.setTotalBlocks(bands);
            multiMaps[i] = current;
        }
        context.setMultiMaps(multiMaps);

        //-Executor Service
        int threads = Integer.parseInt( configurator.getConfig("N_THREADS") );
        ExecutorService executorService = Executors.newFixedThreadPool( threads );
        context.setExecutorService(executorService);

    }

    @Override
    public Future insert(DataObject object) throws Exception {
        Message insertMessage = context.getMessageFactory().getMessage( Message.types.INSERT_REQUEST );
        insertMessage.setBody( object );
        WorkerTask insertWorkerTask = new InsertWorkerTask(insertMessage, context );
        return context.getExecutorService().submit(insertWorkerTask);
    }

    @Override
    public DataObject query(DataObject queryObject) throws Exception {
        Message queryMessage = context.getMessageFactory().getMessage( Message.types.QUERY_REQUEST );
        queryMessage.setBody( queryObject );
        WorkerTask queryWorkerTask = new StandardQueryWorkerTask(queryMessage, context );
        Future<DataObject> response = context.getExecutorService().submit(queryWorkerTask);
        return response.get();
    }
}
