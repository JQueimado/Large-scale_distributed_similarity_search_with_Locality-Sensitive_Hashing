package Factories.ComponentFactories;

import Factories.Factory;
import Factories.FactoryImpl;
import NetworkLayer.Message;
import SystemLayer.Components.TaskImpl.Worker.ModelInsertWorkerTask;
import SystemLayer.Components.TaskImpl.Worker.WorkerTask;
import SystemLayer.Containers.DataContainer;
import SystemLayer.SystemExceptions.UnknownConfigException;

public class TaskFactory extends FactoryImpl {

    private static final String config_name = "TASK_MODEL";
    private enum configs {LDLSH, TRADITIONAL}

    private QueryTaskFactory queryTaskFactory = null;

    public TaskFactory(DataContainer appContext){
        super(appContext);
    }

    private configs getConfig() throws UnknownConfigException {
        String config_value = "";
        try {
            config_value = appContext.getConfigurator().getConfig(config_name);
            return configs.valueOf(config_value);
        }catch (Exception e){
            throw new UnknownConfigException(config_name, config_value);
        }
    }

    //WorkerInsert
    public WorkerTask getNewWorkerInserterTask(Message message) throws Exception {
        configs config = getConfig();

        switch (config){
            case LDLSH -> {
                return new ModelInsertWorkerTask(message, appContext);
            }

            default -> {
                return null;
            }
        }
    }

    //WorkerQueryTask
    public WorkerTask getNewWorkerQueryTask(Message message) throws Exception {
        configs config = getConfig();

        switch (config){
            case LDLSH -> {
                if( queryTaskFactory == null )
                    queryTaskFactory = new QueryTaskFactory(appContext);
                return queryTaskFactory.getNewQueryTask(message);
            }

            default -> {
                return null;
            }
        }
    }

}
