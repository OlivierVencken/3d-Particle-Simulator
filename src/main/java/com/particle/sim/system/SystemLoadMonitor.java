package com.particle.sim.system;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.nio.charset.StandardCharsets;
import com.sun.management.OperatingSystemMXBean;
import java.time.Duration;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public final class SystemLoadMonitor {
    private static final double UNKNOWN_LOAD = -1.0;
    private static final long UNKNOWN_MEMORY = -1L;
    private static final long SAMPLE_INTERVAL_NANOS = Duration.ofSeconds(1).toNanos();
    private static final long WINDOWS_SAMPLE_TIMEOUT_SECONDS = 3;

    private final OperatingSystemMXBean operatingSystem;
    private final boolean windows = System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win");

    private long lastSampleNanos;
    private SystemLoadSnapshot snapshot = new SystemLoadSnapshot(
            UNKNOWN_LOAD,
            UNKNOWN_MEMORY,
            UNKNOWN_MEMORY,
            UNKNOWN_LOAD);
    private CompletableFuture<WindowsPerformanceSample> windowsPerformanceFuture;

    public SystemLoadMonitor() {
        operatingSystem = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
    }

    public SystemLoadSnapshot snapshot() {
        long now = System.nanoTime();
        if (now - lastSampleNanos < SAMPLE_INTERVAL_NANOS) {
            return snapshot;
        }

        lastSampleNanos = now;
        WindowsPerformanceSample windowsPerformance = latestWindowsPerformance();
        MemorySample memory = memorySample();
        snapshot = new SystemLoadSnapshot(
                windowsPerformance.cpuLoad() >= 0.0 ? windowsPerformance.cpuLoad() : systemCpuLoad(),
                memory.usedBytes(),
                memory.totalBytes(),
                windowsPerformance.gpuLoad());
        return snapshot;
    }

    private double systemCpuLoad() {
        return normalizeLoad(operatingSystem.getCpuLoad());
    }

    private MemorySample memorySample() {
        long totalMemory = operatingSystem.getTotalMemorySize();
        long freeMemory = operatingSystem.getFreeMemorySize();
        if (totalMemory <= 0L || freeMemory < 0L) {
            return new MemorySample(UNKNOWN_MEMORY, UNKNOWN_MEMORY);
        }

        return new MemorySample(totalMemory - freeMemory, totalMemory);
    }

    private WindowsPerformanceSample latestWindowsPerformance() {
        if (!windows) {
            return new WindowsPerformanceSample(UNKNOWN_LOAD, UNKNOWN_LOAD);
        }

        if (windowsPerformanceFuture != null && windowsPerformanceFuture.isDone()) {
            try {
                return windowsPerformanceFuture.getNow(new WindowsPerformanceSample(UNKNOWN_LOAD, UNKNOWN_LOAD));
            } finally {
                windowsPerformanceFuture = null;
            }
        }

        if (windowsPerformanceFuture == null) {
            windowsPerformanceFuture = CompletableFuture.supplyAsync(this::queryWindowsPerformance);
        }

        return new WindowsPerformanceSample(snapshot.cpuLoad(), snapshot.gpuLoad());
    }

    private WindowsPerformanceSample queryWindowsPerformance() {
        Process process = null;
        try {
            process = new ProcessBuilder(
                    "powershell",
                    "-NoProfile",
                    "-Command",
                    "$cpu = Get-CimInstance Win32_PerfFormattedData_PerfOS_Processor "
                            + "| Where-Object { $_.Name -eq '_Total' } "
                            + "| Select-Object -ExpandProperty PercentProcessorTime; "
                            + "$gpu = (Get-Counter '\\GPU Engine(*)\\Utilization Percentage').CounterSamples "
                            + "| Measure-Object -Property CookedValue -Maximum "
                            + "| Select-Object -ExpandProperty Maximum; "
                            + "Write-Output ('CPU=' + $cpu); "
                            + "Write-Output ('GPU=' + $gpu)")
                    .redirectErrorStream(true)
                    .start();

            if (!process.waitFor(WINDOWS_SAMPLE_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                return new WindowsPerformanceSample(UNKNOWN_LOAD, UNKNOWN_LOAD);
            }

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                double cpuLoad = UNKNOWN_LOAD;
                double gpuLoad = UNKNOWN_LOAD;
                String line;
                while ((line = reader.readLine()) != null) {
                    String normalizedLine = line.trim().replace(',', '.');
                    if (normalizedLine.startsWith("CPU=")) {
                        cpuLoad = normalizePercent(parseDoubleOrUnknown(normalizedLine.substring(4)));
                    } else if (normalizedLine.startsWith("GPU=")) {
                        gpuLoad = normalizePercent(parseDoubleOrUnknown(normalizedLine.substring(4)));
                    }
                }

                return new WindowsPerformanceSample(cpuLoad, gpuLoad);
            }
        } catch (IOException | InterruptedException ex) {
            if (ex instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            return new WindowsPerformanceSample(UNKNOWN_LOAD, UNKNOWN_LOAD);
        } finally {
            if (process != null && process.isAlive()) {
                process.destroyForcibly();
            }
        }
    }

    private double parseDoubleOrUnknown(String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException ex) {
            return UNKNOWN_LOAD;
        }
    }

    private double normalizePercent(double percent) {
        if (percent < 0.0) {
            return UNKNOWN_LOAD;
        }

        return clamp(percent / 100.0);
    }

    private double normalizeLoad(double load) {
        return load < 0.0 ? UNKNOWN_LOAD : clamp(load);
    }

    private double clamp(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }

    private record MemorySample(long usedBytes, long totalBytes) {
    }

    private record WindowsPerformanceSample(double cpuLoad, double gpuLoad) {
    }
}
