package com.optimizer.performance_demo; // This package name must match your existing one

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@RestController
public class DataController {

    private static final Random random = new Random();

    @GetMapping("/api/fast")
    public String getFastData() {
        return "This was fast!";
    }

    @GetMapping("/api/slow/{id}")
    public String getSlowData(@PathVariable String id) throws InterruptedException {
       
        long delay = 200 + random.nextInt(1000);
        TimeUnit.MILLISECONDS.sleep(delay);
    
        if (random.nextInt(100) == 1) {
            throw new RuntimeException("Simulated database connection failure!");
        }

        return "Data for " + id + " (after " + delay + "ms)";
    }
}