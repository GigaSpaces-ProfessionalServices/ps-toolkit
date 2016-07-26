package com.gigaspaces.gigapro;

import com.gigaspaces.client.ChangeSet;
import com.gigaspaces.query.IdQuery;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.GigaSpaceConfigurer;
import org.openspaces.core.space.SpaceProxyConfigurer;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by IntelliJ IDEA.
 * User: jason
 * Date: 7/18/16
 * Time: 3:43 PM
 */
public class Simulation {

    private static final ExecutorService POOL = Executors.newFixedThreadPool(32);
    private static final int NUM_SPACE_IDS = 2^20;
    private static final int DEFAULT_TIMEOUT = 1024;
    private static final int OTHER_SPACE_IDS_SIZE = 128;

    private final Random random = new Random();
    private final Object lock = new Object();
    private final List<String> otherSpaceIds = new ArrayList<>(OTHER_SPACE_IDS_SIZE);

    private GigaSpace gigaSpace;

    public static void main(String[] args){

        Simulation sim = new Simulation();

        sim.createGigaSpace();
        sim.generateThenWriteSkus();
        sim.createOtherSpaceIds();

        sim.simulate(1,1,1,1,1);

    }

    private void simulate(int numReaders, int numReadMultiples,
                          int numWriteThenTakes, int numWriteMultiples,
                          int numUpdaters) {
        for( int i=0; i<numReaders; i++)
            POOL.submit(new ReadWorker());
        for( int i=0; i<numReadMultiples; i++)
            POOL.submit(new ReadMultipleWorker());
        for( int i=0; i<numWriteThenTakes; i++)
            POOL.submit(new WriteThenTakeByIdWorker());
        for( int i=0; i<numWriteMultiples; i++)
            POOL.submit(new WriteMultipleThenTakeByIdsWorker());
        for( int i=0; i<numUpdaters; i++)
            POOL.submit(new UpdateWorker());
        try {
            POOL.awaitTermination(1, TimeUnit.DAYS);
        } catch (InterruptedException e){
            Thread.currentThread().interrupt();
        }
    }

    private class WriteMultipleThenTakeByIdsWorker extends DefaultWorker{
        @Override
        void work() {
            String[] ids = ids();
            Sku sku1 = new Sku();
            sku1.setSpaceId(ids[0]);
            sku1.setSkuNumber("xyz");
            sku1.setInventoryCount(1);
            Sku sku2 = new Sku();
            sku2.setSpaceId(ids[1]);
            sku2.setSkuNumber("xyz");
            sku2.setInventoryCount(1);
            gigaSpace.writeMultiple(new Sku[]{sku1,sku2});
            gigaSpace.takeByIds(Sku.class, new String[]{ids[0], ids[1]});
        }
    }

    private class ReadMultipleWorker extends DefaultWorker{
        @Override
        void work() {
            gigaSpace.readByIds(Sku.class, ids());
        }
    }

    private String[] ids() {
        String[] ids = new String[2];
        Integer spaceId = random.nextInt(NUM_SPACE_IDS);
        ids[0] = String.valueOf(spaceId);
        ids[1] = String.valueOf(spaceId - 1);
        return ids;
    }

    private void createGigaSpace(){
        SpaceProxyConfigurer configurer =
                new SpaceProxyConfigurer("/*/inventory-space");
        configurer.lookupGroups("grafanaSpace");
        configurer.lookupLocators("10.8.1.194");
        gigaSpace = new GigaSpaceConfigurer(configurer.space()).gigaSpace();
    }

    private void generateThenWriteSkus() {
        int batchSize = 8192;
        List<Sku> skus = new ArrayList<>(batchSize);
        Sku[] arr = new Sku[batchSize];
        for( int i=0; i<NUM_SPACE_IDS; i++) {
            skus.add(fabricateSku(i));
            if( i % batchSize == 0 && i > 0 ){
                skus.toArray(arr);
                gigaSpace.writeMultiple(arr);
            }
        }
    }

    private Sku fabricateSku(Integer spaceId){
        Sku sku = new Sku();
        sku.setSpaceId(String.valueOf(spaceId));
        sku.setSkuNumber(String.valueOf(NUM_SPACE_IDS-spaceId));
        sku.setInventoryCount(64+random.nextInt(64));
        return sku;
    }

    private class ReadWorker extends DefaultWorker{
        @Override
        void work() {
            String spaceId = String.valueOf(random.nextInt(NUM_SPACE_IDS));
            gigaSpace.readById(new IdQuery<>(Sku.class, spaceId));
        }
    }

    private void createOtherSpaceIds(){
        for( int i=OTHER_SPACE_IDS_SIZE; i<OTHER_SPACE_IDS_SIZE+NUM_SPACE_IDS; i++){
            otherSpaceIds.add(String.valueOf(i));
        }
    }

    private String otherSpaceId(){
        synchronized (lock){
            return otherSpaceIds.get(random.nextInt(OTHER_SPACE_IDS_SIZE));
        }
    }

    private class WriteThenTakeByIdWorker extends DefaultWorker{
        @Override
        void work() {
            synchronized (lock) {
                String spaceId = otherSpaceId();
                Sku sku = new Sku();
                sku.setSpaceId(spaceId);
                sku.setSkuNumber(spaceId);
                gigaSpace.write(sku);
                sku.setSkuNumber(null);
                sku.setInventoryCount(null);
                gigaSpace.takeById(Sku.class,sku.getSpaceId());
            }
        }
    }

    private class UpdateWorker extends DefaultWorker{
        @Override
        void work() {
            String spaceId = ids()[0];
            IdQuery<Sku> idQry = new IdQuery<>(Sku.class, spaceId);
            int delta = random.nextBoolean()? -1 : 1;
            gigaSpace.asyncChange(idQry, new ChangeSet().increment("inventoryCount", delta));
        }
    }

    private abstract class DefaultWorker extends Worker{
        DefaultWorker(){
            super(DEFAULT_TIMEOUT);
        }
    }

    private abstract class Worker implements Runnable {

        private final long msDelay;

        Worker(long msDelay){
            this.msDelay = msDelay;
        }

        abstract void work();

        public final void run(){
            while(true) try {
                work();
                Thread.sleep(msDelay);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

    }

}
