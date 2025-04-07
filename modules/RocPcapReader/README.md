# EMU PCAP-based ROC Module

## Overview

The PCAP-based ROC (Readout Controller) module is a new component for the EMU (Event Management Unit) framework that:
- Reads PCAP files from a specified location
- Analyzes source IPs in the PCAP file
- Creates and manages TCP sockets for each unique source IP
- Routes data to appropriate downstream processing systems
- Integrates with EMU and CODA frameworks

## Requirements

- Java 8 or higher
- EMU 3.3 or higher
- pcap4j library
- CODA framework

## Installation

1. Add the module to your EMU installation:
```bash
cd <emu_dir>/modules
mkdir RocPcapReader
```

2. Add the required dependencies to your build system:
```xml
<dependencies>
    <dependency>
        <groupId>org.pcap4j</groupId>
        <artifactId>pcap4j-core</artifactId>
        <version>1.8.2</version>
    </dependency>
    <dependency>
        <groupId>org.pcap4j</groupId>
        <artifactId>pcap4j-packetfactory-static</artifactId>
        <version>1.8.2</version>
    </dependency>
</dependencies>
```

## Configuration

The module is configured through EMU's XML-based configuration system. Here's an example configuration:

```xml
<module name="RocPcapReader">
    <!-- Basic configuration -->
    <attribute name="pcapFile" value="/scratch/jeng-yuan/your_file.pcap"/>
    <attribute name="basePort" value="12345"/>
    <attribute name="defaultTargetIp" value="localhost"/>
    
    <!-- Source IP specific configurations -->
    <sourceIpConfig>
        <sourceIp ip="192.168.1.100">
            <targetIp>192.168.1.200</targetIp>
            <port>12345</port>
        </sourceIp>
        <sourceIp ip="192.168.1.101">
            <targetIp>192.168.1.201</targetIp>
            <port>12346</port>
        </sourceIp>
    </sourceIpConfig>
    
    <!-- Socket configuration -->
    <socketConfig>
        <maxConnections>5</maxConnections>
        <bufferSize>65536</bufferSize>
        <connectionTimeout>5000</connectionTimeout>
    </socketConfig>
    
    <!-- Optional filtering -->
    <filter>
        <include>192.168.1.100,192.168.1.101</include>
        <exclude>192.168.1.103</exclude>
    </filter>
</module>
```

### Configuration Parameters

#### Basic Configuration
- `pcapFile`: Path to the PCAP file to read
- `basePort`: Base port number for TCP sockets
- `defaultTargetIp`: Default target IP for TCP connections (default: localhost)

#### Source IP Configuration
- `sourceIpConfig`: Container for source IP specific settings
  - `sourceIp`: Configuration for each source IP
    - `ip`: Source IP address
    - `targetIp`: Target IP for this source's data
    - `port`: Port number for this source's socket

#### Socket Configuration
- `socketConfig`: Global socket settings
  - `maxConnections`: Maximum number of concurrent connections per socket
  - `bufferSize`: Socket buffer size in bytes
  - `connectionTimeout`: Connection timeout in milliseconds

#### Filter Configuration
- `filter`: Optional packet filtering
  - `include`: Comma-separated list of source IPs to include
  - `exclude`: Comma-separated list of source IPs to exclude

## Usage

1. Configure the module in your EMU configuration file
2. Start the EMU with the new ROC module:
```bash
coda_emu_simroc <name> <rc_address>
```
3. The module will:
   - Read the specified PCAP file
   - Create TCP sockets for each source IP
   - Route data to configured downstream systems

## Implementation Details

### Class Structure

```java
org.jlab.coda.emu.modules.RocPcapReader
org.jlab.coda.emu.modules.RocPcapReader.SourceIpInfo
org.jlab.coda.emu.modules.RocPcapReader.SocketManager
```

### Key Components

1. **PCAP File Reader**
   - Uses pcap4j for reading PCAP files
   - Analyzes source IPs and creates mappings
   - Handles packet filtering

2. **Socket Management**
   - Creates and manages TCP sockets per source IP
   - Handles connection lifecycle
   - Implements data routing

3. **Data Processing**
   - Processes packets from PCAP file
   - Maintains event ordering
   - Handles flow control

4. **EMU Integration**
   - Implements EMU module interface
   - Handles configuration through EMU system
   - Integrates with CODA run control

## Development Status

- [ ] Basic class structure
- [ ] PCAP file reading
- [ ] Source IP analysis
- [ ] TCP socket management
- [ ] Data processing pipeline
- [ ] EMU integration
- [ ] Testing and validation

## Contributing

Please follow the standard EMU development guidelines when contributing to this module.

## License

This module is part of the EMU framework and is subject to the same license terms as EMU. 