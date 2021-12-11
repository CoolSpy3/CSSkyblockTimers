package com.coolspy3.cssbtmrs;

import java.util.function.Supplier;

import com.coolspy3.csmodloader.network.SubscribeToPacketStream;
import com.coolspy3.cspackets.datatypes.MCColor;
import com.coolspy3.cspackets.packets.ClientChatSendPacket;
import com.coolspy3.util.ModUtil;

public class TimeCommand
{

    public final String regex;
    public final String eventName;
    protected final Supplier<String> timeSupplier;

    public TimeCommand(String event, String eventName, Supplier<String> timeSupplier)
    {
        regex = "/" + event + "tmr( .*)?";
        this.eventName = eventName;
        this.timeSupplier = timeSupplier;
    }

    @SubscribeToPacketStream
    public boolean register(ClientChatSendPacket event)
    {
        if (event.msg.matches(regex))
        {
            ModUtil.executeAsync(() -> {
                ModUtil.sendMessage(MCColor.AQUA + eventName + " in " + timeSupplier.get());
            });

            return true;
        }

        return false;
    }

}
