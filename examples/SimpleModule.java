package examples;

import org.jlab.coda.emu.EmuModule;
import org.jlab.coda.emu.support.codaComponent.CODAState;
import org.jlab.coda.emu.support.codaComponent.CODAStateIF;
import org.jlab.coda.emu.support.control.CmdExecException;
import org.jlab.coda.emu.support.configurer.DataNotFoundException;
import org.jlab.coda.emu.support.transport.DataChannel;
import org.jlab.coda.emu.EmuEventNotify;

import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class SimpleModule implements EmuModule {
    private final String name;
    private CODAStateIF state = CODAState.BOOTED;
    private final ArrayList<DataChannel> outputChannels = new ArrayList<>();
    private final ArrayList<DataChannel> inputChannels = new ArrayList<>();
    private final AtomicReference<String> error = new AtomicReference<>();
    
    public SimpleModule(String name) {
        this.name = name;
    }
    
    @Override
    public String name() {
        return name;
    }
    
    @Override
    public void go() {
        System.out.println("Module " + name + ": GO");
        state = CODAState.ACTIVE;
    }
    
    @Override
    public void end() {
        System.out.println("Module " + name + ": END");
        state = CODAState.DOWNLOADED;
    }
    
    @Override
    public void pause() {
        System.out.println("Module " + name + ": PAUSE");
        state = CODAState.PAUSED;
    }
    
    @Override
    public void prestart() {
        System.out.println("Module " + name + ": PRESTART");
        state = CODAState.PAUSED;
    }
    
    @Override
    public void download() {
        System.out.println("Module " + name + ": DOWNLOAD");
        state = CODAState.DOWNLOADED;
    }
    
    @Override
    public void reset() {
        System.out.println("Module " + name + ": RESET");
        state = CODAState.BOOTED;
        error.set(null);
    }
    
    @Override
    public void registerEndCallback(EmuEventNotify callback) {
        // Not needed for this simple example
    }
    
    @Override
    public void registerPrestartCallback(EmuEventNotify callback) {
        // Not needed for this simple example
    }
    
    @Override
    public String getAttr(String name) throws DataNotFoundException {
        throw new DataNotFoundException("Attribute not found: " + name);
    }
    
    @Override
    public int getIntAttr(String name) throws DataNotFoundException {
        throw new DataNotFoundException("Attribute not found: " + name);
    }
    
    @Override
    public void addInputChannels(ArrayList<DataChannel> channels) {
        if (channels != null) {
            inputChannels.addAll(channels);
        }
    }
    
    @Override
    public void addOutputChannels(ArrayList<DataChannel> channels) {
        if (channels != null) {
            outputChannels.addAll(channels);
        }
    }
    
    @Override
    public void clearChannels() {
        outputChannels.clear();
        inputChannels.clear();
    }
    
    @Override
    public CODAStateIF state() {
        return state;
    }
    
    @Override
    public Object[] getStatistics() {
        return new Object[0];
    }
    
    @Override
    public void adjustStatistics(long eventsAdded, long wordsAdded) {
        // Not needed for this simple example
    }
    
    @Override
    public boolean representsEmuStatistics() {
        return false;
    }
    
    @Override
    public int getEventProducingThreadCount() {
        return 0;
    }
    
    @Override
    public ByteOrder getOutputOrder() {
        return ByteOrder.BIG_ENDIAN;
    }
    
    @Override
    public int[] getOutputLevels() {
        return new int[0];
    }
    
    @Override
    public int[] getInputLevels() {
        return new int[0];
    }
    
    @Override
    public String[] getOutputNames() {
        return new String[0];
    }
    
    @Override
    public String[] getInputNames() {
        return new String[0];
    }
    
    @Override
    public int getInternalRingCount() {
        return 0;
    }
    
    @Override
    public ArrayList<DataChannel> getOutputChannels() {
        return outputChannels;
    }
    
    @Override
    public ArrayList<DataChannel> getInputChannels() {
        return inputChannels;
    }
    
    @Override
    public EmuEventNotify getEndCallback() {
        return null;
    }
    
    @Override
    public EmuEventNotify getPrestartCallback() {
        return null;
    }
    
    @Override
    public String getError() {
        return error.get();
    }
} 