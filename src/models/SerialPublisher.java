package models;

import models.db.DB_ValuesSensors;
import org.jfree.data.time.TimeSeriesCollection;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SerialPublisher implements Flow.Publisher<FridgeState>, IQuery {
    static final boolean BDD = true;
    private DB_ValuesSensors db ;

    private static final Logger logger = Logger.getLogger("SerialPublisher");
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private List<SerialSubscription> subscriptions = Collections.synchronizedList(new ArrayList<SerialSubscription>());
    private final CompletableFuture<Void> terminated = new CompletableFuture<>();
    private ICommunicator<FridgeState> communicator;

    public SerialPublisher(ICommunicator<FridgeState> communicator) {
        this.communicator = communicator;
        startBDD();
    }

    private void startBDD(){
        System.out.println("BDD : " + BDD);
        if (BDD)
        {
            try {
                this.db = new DB_ValuesSensors(this);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void subscribe(Flow.Subscriber<? super FridgeState> subscriber) {
        if (this.executorService.isShutdown())
            this.executorService = Executors.newSingleThreadExecutor();
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
        return this.db.select_DampnessSerie(fridgeState, dateStart, dateEnd);
    }

    @Override
    public double pntRosee_Value(FridgeState fridgeState) {
        float h = 0;
        float t = 0;
        for (Measurement measurement : fridgeState.getMeasurements()){
            if (Objects.equals(measurement.getLabel(), "Dampness"))
                h = measurement.getValue();
            if (Objects.equals(measurement.getLabel(), "Inside temperature"))
                t = measurement.getValue();
        }
     //   System.out.println("Température intérieure : " + t);
     //   System.out.println("Humidité : " + h);
        return Math.pow(h/100,1.0/8.0)*(112+(0.9*t))+(0.1 * t)-112;
    }

    @Override
    public Enum_AlarmStates pntRosee_Alarm(FridgeState fridgeState) {
        double pntRosee = pntRosee_Value(fridgeState);
        float t = 0;
        for (Measurement measurement : fridgeState.getMeasurements()){
            if (Objects.equals(measurement.getLabel(), "Module temperature"))
                t = measurement.getValue();
        }
    //    System.out.println("Température module : " + t);
    //    System.out.println("PntRosee : " + pntRosee);
        if (t > (pntRosee + 1) )
            return Enum_AlarmStates.GOOD;
        else if ((t <= (pntRosee + 1)) && (t > pntRosee))
            return Enum_AlarmStates.WARNING;
        else
            return Enum_AlarmStates.CRITICAL;
    }

    @Override
    public FridgeState getFridgeStateExample() {
        FridgeState fridgeState = new FridgeState();
        fridgeState.setBrink((float) 18);
        fridgeState.setMeasuredAt(new Date());
        ArrayList<Measurement> m = new ArrayList<>();
        Measurement m1 = new Measurement(); m1.setLabel("Inside temperature"); m1.setSensor("dht22");
        Measurement m2 = new Measurement(); m2.setLabel("Outside temperature"); m2.setSensor("thermistor");
        Measurement m3 = new Measurement(); m3.setLabel("Module temperature"); m3.setSensor("thermistor");
        Measurement m4 = new Measurement(); m4.setLabel("Dampness"); m4.setSensor("dht22");
        m.add(m1); m.add(m2); m.add(m3); m.add(m4);
        fridgeState.setMeasurements(m);
        return fridgeState;
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

        @Override
        public void request(long n) {
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
                if (!this.executor.isShutdown()) {
                    this.executor.execute(() -> {
                        FridgeState fridgeState = communicator.readData();
                        logger.log(Level.INFO, "Publishing item");
                        if (BDD)
                            db.insertAllValues(fridgeState);
                        this.subscriber.onNext(fridgeState);
                    });
                }
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
