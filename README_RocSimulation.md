# RocSimulation.java Changes and Improvements

## Overview
This document outlines the changes and improvements made to `RocSimulation.java` to support PCAP/TCP functionality while maintaining compatibility with the existing CODA framework.

## Major Changes

### 1. PCAP/TCP Mode Support
- Added new configuration parameters for PCAP mode:
  ```xml
  <mode>pcap</mode>
  <pcapFile>/path/to/pcap/file</pcapFile>
  <basePort>4000</basePort>
  <defaultTargetIp>127.0.0.1</defaultTargetIp>
  <maxConnections>10</maxConnections>
  <bufferSize>8192</bufferSize>
  <connectionTimeout>5000</connectionTimeout>
  ```
- Implemented PCAP file reading and packet extraction
- Added TCP socket management for each source IP

### 2. State Machine Enhancements
- Added PCAP-specific state handling in:
  - `prestart()`: Initializes PCAP reading and TCP sockets
  - `go()`: Starts packet replay
  - `end()`: Closes TCP sockets
  - `reset()`: Resets PCAP/TCP components

### 3. Thread Management
- Added PCAP packet replay thread
- Maintained existing event generation threads
- Implemented thread synchronization for PCAP mode

### 4. Buffer Management
- Added PCAP packet buffer handling
- Integrated with existing ByteBufferSupply
- Optimized memory usage for large PCAP files

### 5. Error Handling
- Added PCAP-specific error handling
- Improved socket error recovery
- Added PCAP file validation

## Configuration Options

### PCAP Mode Configuration
| Parameter | Default | Description |
|-----------|---------|-------------|
| mode | "event" | Operation mode: "event" or "pcap" |
| pcapFile | - | Path to PCAP file |
| basePort | 4000 | Starting port for TCP sockets |
| defaultTargetIp | 127.0.0.1 | Default target IP for TCP connections |
| maxConnections | 10 | Maximum connections per socket |
| bufferSize | 8192 | TCP socket buffer size |
| connectionTimeout | 5000 | TCP connection timeout (ms) |

## Usage Examples

### 1. Basic PCAP Mode
```xml
<module>
    <name>ROC</name>
    <class>org.jlab.coda.emu.modules.RocSimulation</class>
    <configuration>
        <mode>pcap</mode>
        <pcapFile>/path/to/file.pcap</pcapFile>
    </configuration>
</module>
```

### 2. Advanced PCAP Mode
```xml
<module>
    <name>ROC</name>
    <class>org.jlab.coda.emu.modules.RocSimulation</class>
    <configuration>
        <mode>pcap</mode>
        <pcapFile>/path/to/file.pcap</pcapFile>
        <basePort>5000</basePort>
        <defaultTargetIp>192.168.1.100</defaultTargetIp>
        <maxConnections>20</maxConnections>
        <bufferSize>16384</bufferSize>
        <connectionTimeout>10000</connectionTimeout>
    </configuration>
</module>
```

## Performance Considerations

### Memory Usage
- PCAP mode requires more memory for packet buffering
- Recommended JVM settings:
  ```bash
  java -Xmx4g -Xms2g ...
  ```

### Network Performance
- TCP socket buffer size affects throughput
- Connection timeout impacts error recovery
- Multiple connections per IP improve performance

## Error Handling

### Common Errors
1. PCAP File Errors:
   - File not found
   - Invalid format
   - Read permissions

2. TCP Socket Errors:
   - Port in use
   - Connection refused
   - Timeout

3. Memory Errors:
   - Out of memory
   - Buffer overflow

### Error Recovery
- Automatic socket reconnection
- PCAP file validation
- Memory usage monitoring

## Future Improvements

### Planned Enhancements
1. Compression support for PCAP files
2. Packet filtering capabilities
3. Performance monitoring
4. Enhanced error recovery
5. Multi-file PCAP support

### Potential Optimizations
1. Zero-copy packet handling
2. Improved buffer management
3. Better thread synchronization
4. Enhanced logging
5. Performance metrics

## Dependencies

### Required Libraries
- cMsg-6.0.jar
- jevio-6.0.jar
- disruptor-3.4.3.jar (Java 8) or disruptor-4.0.0.jar (Java 15)

### System Requirements
- Java 8 or Java 15
- Sufficient memory for PCAP file processing
- Network access for TCP connections

## Troubleshooting

### Common Issues
1. Memory Issues:
   - Increase JVM heap size
   - Optimize buffer sizes
   - Monitor memory usage

2. Network Issues:
   - Check firewall settings
   - Verify port availability
   - Test network connectivity

3. Performance Issues:
   - Adjust buffer sizes
   - Optimize thread count
   - Monitor system resources

### Debugging Tips
1. Enable debug logging
2. Monitor TCP connections
3. Check PCAP file integrity
4. Verify configuration
5. Monitor system resources

## Support

For issues and questions:
1. Check the CODA documentation
2. Review error logs
3. Contact the development team
4. Submit bug reports
5. Request enhancements

## Comparison with Original Version

### 1. Core Functionality
| Feature | Original Version | Revised Version |
|---------|-----------------|-----------------|
| Event Generation | Primary focus | Maintained with enhancements |
| PCAP Support | Not present | Added as new mode |
| TCP Socket Management | Not present | Added for PCAP mode |
| State Machine | Basic states | Enhanced with PCAP states |
| Thread Management | Single event thread | Multiple threads for events and PCAP |

### 2. State Machine Differences
| State | Original Behavior | New Behavior |
|-------|------------------|--------------|
| CONFIGURED | Basic initialization | Added PCAP configuration |
| PRESTARTED | Event setup | Added PCAP file loading |
| ACTIVE | Event generation | Added PCAP replay |
| DOWNLOADED | Basic cleanup | Added socket cleanup |
| ERROR | Basic error handling | Enhanced error recovery |

### 3. Configuration Options
| Configuration | Original | New |
|--------------|----------|-----|
| Mode | Event only | Event or PCAP |
| File Input | Not applicable | PCAP file support |
| Network Settings | Not applicable | TCP socket configuration |
| Buffer Management | Basic | Enhanced for PCAP |
| Error Handling | Basic | Comprehensive |

### 4. Performance Enhancements
| Aspect | Original | New |
|--------|----------|-----|
| Memory Usage | Basic buffer management | Optimized for large PCAP files |
| Network Throughput | Not applicable | TCP socket optimization |
| Thread Efficiency | Single thread | Multi-threaded PCAP support |
| Error Recovery | Basic | Advanced with auto-recovery |

### 5. Code Structure
| Component | Original | New |
|-----------|----------|-----|
| Class Structure | Single class | Enhanced with inner classes |
| Thread Management | Simple | Complex with synchronization |
| Buffer Handling | Basic | Advanced with pooling |
| Error Handling | Simple | Comprehensive |

### 6. Key Additions
1. PCAP Mode Support:
   - PCAP file reading
   - Packet extraction
   - TCP socket management
   - Source IP handling

2. Enhanced State Machine:
   - PCAP-specific states
   - Improved state transitions
   - Better error handling
   - Recovery mechanisms

3. Thread Management:
   - PCAP replay thread
   - TCP socket threads
   - Synchronization mechanisms
   - Thread pooling

4. Buffer Management:
   - PCAP packet buffers
   - TCP socket buffers
   - Buffer pooling
   - Memory optimization

5. Error Handling:
   - PCAP file validation
   - Socket error recovery
   - Memory management
   - State recovery

### 7. Backward Compatibility
- All original event generation functionality maintained
- Existing configurations still supported
- No breaking changes to original API
- Seamless integration with CODA framework

### 8. Migration Guide
To migrate from the original version:
1. Update configuration files to include PCAP mode if needed
2. Ensure required JAR files are present
3. Configure network settings for TCP mode
4. Update memory settings for PCAP processing
5. Test both event and PCAP modes 