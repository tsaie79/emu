package org.jlab.coda.emu.modules.pcap;

import java.io.*;
import java.util.*;

/**
 * Reads PCAP files and extracts source IP addresses.
 */
public class PcapReader {
    private Set<String> sourceIPs = new HashSet<>();

    public void readPcap(String pcapFile) throws IOException {
        // For now, we'll just read a simple text file with IP addresses
        // In a real implementation, this would parse actual PCAP files
        try (BufferedReader reader = new BufferedReader(new FileReader(pcapFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Simple IP address extraction - in real implementation,
                // this would parse PCAP packets
                if (line.matches("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}")) {
                    sourceIPs.add(line);
                }
            }
        }
    }

    public Set<String> getSourceIPs() {
        return sourceIPs;
    }
} 