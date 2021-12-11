package com.coolspy3.cssbtmrs;

import java.io.InputStreamReader;
import java.time.Duration;
import java.util.function.Supplier;

import com.coolspy3.csmodloader.util.Utils;
import com.coolspy3.cspackets.datatypes.MCColor;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class QueryTimeCommand extends TimeCommand
{

    public final String url;

    public QueryTimeCommand(String event, String eventName, String url)
    {
        super(event, eventName, new TimeQuery(url));
        this.url = url;
    }

    public static class TimeQuery implements Supplier<String>
    {

        public final String url;

        public TimeQuery(String url)
        {
            this.url = url;
        }

        @Override
        public String get()
        {
            return Utils.reporting(() -> {
                JsonObject json;
                try (InputStreamReader reader = CSSBTmrs.openURL(url))
                {
                    json = JsonParser.parseReader(reader).getAsJsonObject();
                }
                Duration timeUntil = Duration
                        .ofMillis(json.get("estimate").getAsLong() - System.currentTimeMillis());
                return CSSBTmrs.formatTimeUntil(timeUntil);
            }, MCColor.RED + "<ERROR>");
        }

    }

}
