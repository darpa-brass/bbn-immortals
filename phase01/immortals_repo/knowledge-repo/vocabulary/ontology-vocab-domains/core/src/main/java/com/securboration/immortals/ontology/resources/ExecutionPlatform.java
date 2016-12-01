package com.securboration.immortals.ontology.resources;

import com.securboration.immortals.ontology.resources.compute.Cpu;
import com.securboration.immortals.ontology.resources.compute.Gpu;
import com.securboration.immortals.ontology.resources.logical.LogicalResource;
import com.securboration.immortals.ontology.resources.logical.OperatingSystem;
import com.securboration.immortals.ontology.resources.memory.PhysicalMemoryResource;
import com.securboration.immortals.ontology.resources.network.NetworkInterface;

/**
 * A platform on which execution can occur. The platform is a combination of
 * physical (e.g., memory, cpu) and logical (e.g., OS version, system libraries)
 * components.
 * 
 * @author jstaples
 *
 */
@com.securboration.immortals.ontology.annotations.RdfsComment(
    "A platform on which execution can occur. The platform is a combination" +
    " of physical (e.g., memory, cpu) and logical (e.g., OS version, system" +
    " libraries) components.  @author jstaples ")
public class ExecutionPlatform extends PlatformResource {

    /**
     * Processors available for general purpose computation
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "Processors available for general purpose computation")
    private Cpu[] cpus;

    /**
     * Processors available for graphics processing
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "Processors available for graphics processing")
    private Gpu[] gpus;

    /**
     * The operating system and any patches/versions
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "The operating system and any patches/versions")
    private OperatingSystem os;

    /**
     * Libraries available on the platform
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "Libraries available on the platform")
    private LogicalResource[] platformLibraries;

    /**
     * The device's physical memory
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "The device's physical memory")
    private PhysicalMemoryResource deviceMemory;

    /**
     * The device's disk hardware
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "The device's disk hardware")
    private DiskResource[] disks;

    /**
     * The mechanisms by which the device can connect to a network
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "The mechanisms by which the device can connect to a network")
    private NetworkInterface[] networkInterfaces;

    public Cpu[] getCpus() {
        return cpus;
    }

    public void setCpus(Cpu[] cpus) {
        this.cpus = cpus;
    }

    public Gpu[] getGpus() {
        return gpus;
    }

    public void setGpus(Gpu[] gpus) {
        this.gpus = gpus;
    }

    public OperatingSystem getOs() {
        return os;
    }

    public void setOs(OperatingSystem os) {
        this.os = os;
    }

    public LogicalResource[] getPlatformLibraries() {
        return platformLibraries;
    }

    public void setPlatformLibraries(LogicalResource[] platformLibraries) {
        this.platformLibraries = platformLibraries;
    }

    public PhysicalMemoryResource getDeviceMemory() {
        return deviceMemory;
    }

    public void setDeviceMemory(PhysicalMemoryResource deviceMemory) {
        this.deviceMemory = deviceMemory;
    }

    public DiskResource[] getDisks() {
        return disks;
    }

    public void setDisks(DiskResource[] disks) {
        this.disks = disks;
    }

    public NetworkInterface[] getNetworkInterfaces() {
        return networkInterfaces;
    }

    public void setNetworkInterfaces(NetworkInterface[] networkInterfaces) {
        this.networkInterfaces = networkInterfaces;
    }

}
