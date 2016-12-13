package com.gigaspaces.gigapro.convert.property;

import com.gigaspaces.sync.DataSyncOperation;
import com.gigaspaces.sync.OperationsBatchData;
import com.gigaspaces.sync.SpaceSynchronizationEndpoint;
import com.gigaspaces.sync.TransactionData;

/**
 * Created by IntelliJ IDEA.
 * User: jason
 * Date: 12/7/16
 * Time: 12:04 PM
 */
public class TestDeletesSynchronizationEndpoint extends SpaceSynchronizationEndpoint{

    @Override
    public void onTransactionSynchronization(TransactionData transactionData) {
        for( DataSyncOperation operation : transactionData.getTransactionParticipantDataItems() ){
            logInfo(operation, "xyz");
        }
        super.onTransactionSynchronization(transactionData);
    }

    @Override
    public void onOperationsBatchSynchronization(OperationsBatchData batchData) {
        for( DataSyncOperation operation : batchData.getBatchDataItems() ){
            logInfo(operation, "batch");
        }
        super.onOperationsBatchSynchronization(batchData);
    }

    private void logInfo(DataSyncOperation operation, String operationSource) {
        Object id = operation.getSpaceId();
        String spaceId = ( id instanceof String )? (String) id : id.toString();
        Object object = operation.getDataAsObject();
        final String aSyncOperations = "Persisting (from " + operationSource + ") : spaceId = " +
                spaceId +
                " : " +
                object.toString();
        System.out.println(aSyncOperations);
    }
}
