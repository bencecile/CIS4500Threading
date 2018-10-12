package threads.bcecile.threadingexercise;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    // All of our onClick handlers
    public void startCPUSingle(View view) {
        ExecutorService service = Executors.newSingleThreadExecutor();
        doCpuOperation(service, "CPU Single");
    }
    public void startCPUMulti(View view) {
        int cores = Runtime.getRuntime().availableProcessors();
        ExecutorService service = Executors.newFixedThreadPool(cores);
        doCpuOperation(service, "CPU Multi");
    }
    public void startCPUMultiMore(View view) {
        int cores = Runtime.getRuntime().availableProcessors();
        ExecutorService service = Executors.newFixedThreadPool(cores * 5);
        doCpuOperation(service, "CPU Multi More");
    }
    public void startNetworkSingle(View view) {
        ExecutorService service = Executors.newSingleThreadExecutor();
        doNetworkTask(service, "Network Single");
    }
    public void startNetworkMultiSame(View view) {
        int cores = Runtime.getRuntime().availableProcessors();
        ExecutorService service = Executors.newFixedThreadPool(cores);
        doNetworkTask(service, "Network Multi Same");
    }
    public void startNetworkMultiMore(View view) {
        int cores = Runtime.getRuntime().availableProcessors();
        ExecutorService service = Executors.newFixedThreadPool(cores * 5);
        doNetworkTask(service, "Network Multi More");
    }

    // Our 2 operation methods
    public void doCpuOperation(ExecutorService service, String taskName) {
        timeThreadExecution(service, () -> {
            Random random = new Random();
            for (int i = 0; i < 100_000_000; i++) {
                random.nextInt();
            }
        }, 10, taskName);
    }
    public void doNetworkTask(ExecutorService service, String taskName) {
        timeThreadExecution(service, () -> {
            try {
                // Make the URL
                URL url = new URL("https", "httpbin.org", "get");

                // Open the connection
                HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
                // Setup the connection before we send
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                conn.setRequestProperty("Accept", "application/json");

                // Connect and retrieve the contents sent back
                conn.connect();

                byte[] buffer = new byte[4096];
                ByteArrayOutputStream outStream = new ByteArrayOutputStream();
                InputStream in = conn.getInputStream();

                // Read the entire input stream
                int readSize;
                while ((readSize = in.read(buffer)) > 0) {
                    outStream.write(buffer, 0, readSize);
                }

                // Just create the new String because this would be similar to what a normal
                //  application might do, but we don't need to do anything with it
                new String(outStream.toByteArray(), "UTF-8");
            } catch (java.io.IOException e) {
                e.printStackTrace();
            }
        }, 20, taskName);
    }

    // Our method to actually time execution of the tasks
    public void timeThreadExecution(ExecutorService service, Runnable task, int amount,
                                    String taskName) {
        // Create a new thread that we can easily sleep in while we wait for the Executor to finish
        new Thread(() -> {
            Log.i("Timing Stats", "Starting " + taskName);
            // Keep track of when we started
            long startTime = new Date().getTime();

            // Add all of the amount of tasks to our Executor
            for (int i = 0; i < amount; i++) {
                service.submit(task);
            }

            // Shutdown the service
            service.shutdown();

            // Wait for the service to finish
            while (!service.isTerminated()) {
                try {
                    service.awaitTermination(1, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            // Get the time it took for everything to finish
            long elapsed = new Date().getTime() - startTime;

            // Log that time now
            Log.i("Timing Stats", String.format("%s: Took %d ms", taskName, elapsed));
        }).start();
    }
}
