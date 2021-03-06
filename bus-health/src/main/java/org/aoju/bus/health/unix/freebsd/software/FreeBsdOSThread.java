/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org OSHI and other contributors.                 *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 ********************************************************************************/
package org.aoju.bus.health.unix.freebsd.software;

import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.lang.RegEx;
import org.aoju.bus.health.Builder;
import org.aoju.bus.health.Executor;
import org.aoju.bus.health.builtin.software.AbstractOSThread;
import org.aoju.bus.health.builtin.software.OSProcess;

import java.util.List;

/**
 * @author Kimi Liu
 * @version 6.0.9
 * @since JDK 1.8+
 */
public class FreeBsdOSThread extends AbstractOSThread {

    private int threadId;
    private String name = Normal.EMPTY;
    private OSProcess.State state = OSProcess.State.INVALID;
    private long minorFaults;
    private long majorFaults;
    private long startMemoryAddress;
    private long contextSwitches;
    private long kernelTime;
    private long userTime;
    private long startTime;
    private long upTime;
    private int priority;

    public FreeBsdOSThread(int processId, String[] split) {
        super(processId);
        updateAttributes(split);
    }

    @Override
    public int getThreadId() {
        return this.threadId;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public OSProcess.State getState() {
        return this.state;
    }

    @Override
    public long getStartMemoryAddress() {
        return this.startMemoryAddress;
    }

    @Override
    public long getContextSwitches() {
        return this.contextSwitches;
    }

    @Override
    public long getMinorFaults() {
        return this.minorFaults;
    }

    @Override
    public long getMajorFaults() {
        return this.majorFaults;
    }

    @Override
    public long getKernelTime() {
        return this.kernelTime;
    }

    @Override
    public long getUserTime() {
        return this.userTime;
    }

    @Override
    public long getUpTime() {
        return this.upTime;
    }

    @Override
    public long getStartTime() {
        return this.startTime;
    }

    @Override
    public int getPriority() {
        return this.priority;
    }

    @Override
    public boolean updateAttributes() {
        String psCommand = "ps -awwxo tdname,lwp,state,etimes,systime,time,tdaddr,nivcsw,nvcsw,majflt,minflt,pri -H -p "
                + getOwningProcessId();
        // there is no switch for thread in ps command, hence filtering.
        List<String> threadList = Executor.runNative(psCommand);
        for (String psOutput : threadList) {
            String[] split = RegEx.SPACES.split(psOutput.trim());
            if (split.length > 1 && this.getThreadId() == Builder.parseIntOrDefault(split[1], 0)) {
                return updateAttributes(split);
            }
        }
        this.state = OSProcess.State.INVALID;
        return false;
    }

    private boolean updateAttributes(String[] split) {
        if (split.length != 12) {
            this.state = OSProcess.State.INVALID;
            return false;
        }
        this.name = split[0];
        this.threadId = Builder.parseIntOrDefault(split[1], 0);
        switch (split[2].charAt(0)) {
            case 'R':
                this.state = OSProcess.State.RUNNING;
                break;
            case 'I':
            case 'S':
                this.state = OSProcess.State.SLEEPING;
                break;
            case 'D':
            case 'L':
            case 'U':
                this.state = OSProcess.State.WAITING;
                break;
            case 'Z':
                this.state = OSProcess.State.ZOMBIE;
                break;
            case 'T':
                this.state = OSProcess.State.STOPPED;
                break;
            default:
                this.state = OSProcess.State.OTHER;
                break;
        }
        // Avoid divide by zero for processes up less than a second
        long elapsedTime = Builder.parseDHMSOrDefault(split[3], 0L); // etimes
        this.upTime = elapsedTime < 1L ? 1L : elapsedTime;
        long now = System.currentTimeMillis();
        this.startTime = now - this.upTime;
        this.kernelTime = Builder.parseDHMSOrDefault(split[4], 0L); // systime
        this.userTime = Builder.parseDHMSOrDefault(split[5], 0L) - this.kernelTime; // time
        this.startMemoryAddress = Builder.hexStringToLong(split[6], 0L);
        long nonVoluntaryContextSwitches = Builder.parseLongOrDefault(split[7], 0L);
        long voluntaryContextSwitches = Builder.parseLongOrDefault(split[8], 0L);
        this.contextSwitches = voluntaryContextSwitches + nonVoluntaryContextSwitches;
        this.majorFaults = Builder.parseLongOrDefault(split[9], 0L);
        this.minorFaults = Builder.parseLongOrDefault(split[10], 0L);
        this.priority = Builder.parseIntOrDefault(split[11], 0);
        return true;
    }

}
