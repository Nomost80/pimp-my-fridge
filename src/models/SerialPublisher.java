package models;

import models.db.DB_ValuesSensors;
import org.jfree.data.time.TimeSeriesCollection;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SerialPublisher implements Flow.Publisher<FridgeState>, IQuery {
    static final boolean BDD = false;
    private DB_ValuesSensors db ;

    private static final Logger logger = Logger.getLogger("SerialPublisher");
    final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private List<SerialSubscription> subscriptions = Collections.synchronizedList(new ArrayList<SerialSubscription>());
    private final CompletableFuture<Void> terminated = new CompletableFuture<>();
    private ICommunicator<FridgeState> communicator;

    public SerialPublisher(ICommunicator<FridgeState> communicator) {
        this.communicator = communicator;
        startBDD();
    }

    private void startBDD(){
        if (BDD)
        {
            try {
                this.db = new DB_ValuesSensors();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void subscribe(Flow.Subscriber<? super FridgeState> subscriber) {
        SerialSubscription subscription = new SerialSubscription(subscriber, this.executorService);
        this.subscriptions.add(subscription);
        subscriber.onSubscribe(subscription);
    }

    public void waitUntilTerminated() throws InterruptedException {
        try {
            terminated.get();
        } catch (ExecutionException e) {
            logger.log(Level.SEVERE, e.toString());
        }
    }

    @Override
    public TimeSeriesCollection select_TemperaturesSeries(FridgeState fridgeState, String dateStart, String dateEnd) {
        if (!BDD)
            return null;
        return this.db.select_TemperaturesSeries(fridgeState, dateStart, dateEnd);
    }

    @Override
    public TimeSeriesCollection select_DampnessSerie(FridgeState fridgeState, String dateStart, String dateEnd) {
        if (!BDD)
            return null;
        return this.db.select_TemperaturesSeries(fridgeState, dateStart, dateEnd);
    }

    @Override
    public boolean ckeck_PntRosee() {
        return true;
    }

    private class SerialSubscription implements Flow.Subscription {

        private Flow.Subscriber<? super FridgeState> subscriber;
        private final ExecutorService executor;
        /* Ce type est thread-safe */
        private AtomicBoolean isCanceled;

        public SerialSubscription(Flow.Subscriber<? super FridgeState> subscriber, ExecutorService executor) {
            this.subscriber = subscriber;
            this.executor = executor;
            this.isCanceled = new AtomicBoolean(false);
        }

        public AtomicBoolean getIsCanceled() {
            return isCanceled;
        }

        @Override
        public void request(long n) {
            logger.log(Level.INFO, "request => isCanceled : " + this.isCanceled.get());
            if (this.isCanceled.get())
                return;
            if (n < 0)
                this.executor.execute(() -> this.subscriber.onError(new IllegalArgumentException()));
            else
                publishItems(n);
        }

        @Override
        public void cancel() {
            this.isCanceled.set(true);
            synchronized (subscriptions) {
                subscriptions.remove(this);
                if (subscriptions.size() == 0)
                    shutdown();
            }
        }

        private void publishItems(long n) {
            for (int i = 0; i < n; i++) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    logger.log(Level.SEVERE, e.toString());
                }
                this.executor.execute(() -> {
                    FridgeState fridgeState = communicator.readData();
                    logger.log(Level.INFO, "Publishing item");
                    if (BDD)
                        db.insertAllValues(fridgeState);
                    this.subscriber.onNext(fridgeState);
                });
            }
        }

        private void shutdown() {
            logger.log(Level.INFO, "Shutdown executor...");
            this.executor.shutdown();
            Executors.newSingleThreadExecutor().submit(() -> {
                logger.log(Level.INFO, "Shutdown complete !");
                terminated.complete(null);
            });
        }
    }
}
