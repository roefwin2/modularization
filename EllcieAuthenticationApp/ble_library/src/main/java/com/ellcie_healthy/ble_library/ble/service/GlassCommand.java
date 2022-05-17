package com.ellcie_healthy.ble_library.ble.service;

import androidx.annotation.NonNull;

import com.ellcie_healthy.ble_library.ble.profile.command.data.cmd.CommandResponse;

public class GlassCommand {
    private final CommandResponse cmd;
    private final int timeout;
    private GlassCommandCallback cmdCallback;
    private PreGlassCommandCallback preCmdCallback;

    public GlassCommand(@NonNull CommandResponse cmd, int timeout, PreGlassCommandCallback preCmdCallback, GlassCommandCallback cmdCallback) {
        this.cmd = cmd;
        this.cmdCallback = cmdCallback;
        this.preCmdCallback = preCmdCallback;
        this.timeout = timeout;
    }

    public GlassCommand(@NonNull CommandResponse cmd, int timeout) {
        this(cmd, timeout, null, null);
    }

    public GlassCommand(@NonNull CommandResponse cmd, int timeout, PreGlassCommandCallback preCmdCallback) {
        this(cmd, timeout, preCmdCallback, null);
    }

    public GlassCommand(@NonNull CommandResponse cmd, int timeout, GlassCommandCallback cmdCallback) {
        this(cmd, timeout, null, cmdCallback);
    }

    public CommandResponse getCmd() {
        return cmd;
    }

    public GlassCommandCallback getCmdCallback() {
        return cmdCallback;
    }

    public void setCmdCallback(GlassCommandCallback cmdCallback) {
        this.cmdCallback = cmdCallback;
    }

    public PreGlassCommandCallback getPreCmdCallback() {
        return preCmdCallback;
    }

    public void setPreCmdCallback(PreGlassCommandCallback preCmdCallback) {
        this.preCmdCallback = preCmdCallback;
    }

    public int getTimeout() {
        return timeout;
    }

    public interface GlassCommandCallback {
        void onCommandResponse(CommandResponse response);
    }

    public interface PreGlassCommandCallback {
        void beforeCommandWrite();
    }
}