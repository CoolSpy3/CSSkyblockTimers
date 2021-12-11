package com.coolspy3.cssbtmrs;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.TimeZone;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.coolspy3.csmodloader.mod.Entrypoint;
import com.coolspy3.csmodloader.mod.Mod;
import com.coolspy3.csmodloader.network.PacketHandler;
import com.coolspy3.csmodloader.network.SubscribeToPacketStream;
import com.coolspy3.csmodloader.util.Utils;
import com.coolspy3.cspackets.datatypes.MCColor;
import com.coolspy3.cspackets.packets.ClientChatSendPacket;
import com.coolspy3.util.ModUtil;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@Mod(id = "cssbtmrs", name = "CSSkyblockTimers",
        description = "Provides notifications for various Skyblock events in chat.",
        version = "1.0.1",
        dependencies = {"csmodloader:[1.1,2)", "cspackets:[1,2)", "csutils:[1,2)"})
public class CSSBTmrs implements Entrypoint
{

    private ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    private ArrayList<String> fetchurObjects = new ArrayList<>();

    public static double secondsPerMonth = 37200;
    public static double secondsPerYear = secondsPerMonth * 12;
    public static double secondsPerDay = 1200;
    public static double secondsPerHour = 50;
    public static double secondsPerMinute = 5D / 6D;

    public CSSBTmrs()
    {
        fetchurObjects.add("Red Wool");
        fetchurObjects.add("Yellow Stained Glass");
        fetchurObjects.add("a Compass");
        fetchurObjects.add("Mithril");
        fetchurObjects.add("a Firework Rocket");
        fetchurObjects.add("Cheap Coffee or Decent Coffee");
        fetchurObjects.add("a Door");
        fetchurObjects.add("Rabbit's Foot");
        fetchurObjects.add("Superboom TNT");
        fetchurObjects.add("a Pumpkin");
        fetchurObjects.add("Flint and Steel");
        fetchurObjects.add("Nether Quartz Ore");
        fetchurObjects.add("Ender Pearls");
    }

    @Override
    public Entrypoint create()
    {
        return new CSSBTmrs();
    }

    @Override
    public void init(PacketHandler handler)
    {
        executor.scheduleAtFixedRate(this::everyMinute,
                60 - LocalDateTime.now().get(ChronoField.SECOND_OF_MINUTE), 60, TimeUnit.SECONDS);
        handler.register(this);
        handler.register(new QueryTimeCommand("magma", "Magma Boss will spawn",
                "https://hypixel-api.inventivetalent.org/api/skyblock/bosstimer/magma/estimatedSpawn"));
        handler.register(new QueryTimeCommand("da", "Dark Auction will start",
                "https://hypixel-api.inventivetalent.org/api/skyblock/darkauction/estimate"));
        handler.register(new QueryTimeCommand("soj", "Season of Jerry will begin",
                "https://hypixel-api.inventivetalent.org/api/skyblock/winter/estimate"));
        handler.register(new QueryTimeCommand("jw", "Jerry's Workshop will open",
                "https://hypixel-api.inventivetalent.org/api/skyblock/jerryWorkshop/estimate"));
        handler.register(new QueryTimeCommand("ny", "New Year's will begin",
                "https://hypixel-api.inventivetalent.org/api/skyblock/newyear/estimate"));
        handler.register(new QueryTimeCommand("tz", "The Traveling Zoo will open",
                "https://hypixel-api.inventivetalent.org/api/skyblock/zoo/estimate"));
        handler.register(new TimeCommand("cofs", "Cult of the Fallen Star will meet",
                CSSBTmrs::timeUntilCofs));
    }

    @Override
    public void shutdown()
    {
        executor.shutdown();
    }

    @SubscribeToPacketStream
    public boolean onChat(ClientChatSendPacket event)
    {
        if (event.msg.matches("/wif"))
        {
            int i = ZonedDateTime.now(TimeZone.getTimeZone("America/New_York").toZoneId())
                    .getDayOfMonth() % fetchurObjects.size();
            i = i == 0 ? fetchurObjects.size() : i;
            ModUtil.sendMessage(MCColor.AQUA + "He wants " + fetchurObjects.get(i - 1) + "");

            return true;
        }

        return false;
    }

    public static final String format(String str)
    {
        return str.toLowerCase().replaceAll("[^a-z\\s\\-]", "");
    }

    private static String timeUntilCofs()
    {
        SkyblockTime currentTime = getSkyblockTime();
        Duration timeUntil;
        if (currentTime.day > 27)
        {
            timeUntil = Duration.ofSeconds((10 - (currentTime.day - 28)) * (int) secondsPerDay);
        }
        else
        {
            timeUntil = Duration.ofSeconds((7 - (currentTime.day % 7)) * (int) secondsPerDay);
        }
        timeUntil = timeUntil.minusSeconds(currentTime.hour * (int) secondsPerHour);
        timeUntil = timeUntil.minusSeconds(currentTime.minute * (int) secondsPerMinute);
        timeUntil = timeUntil.minusNanos((int) (currentTime.minute
                * (secondsPerMinute - (int) secondsPerMinute) * TimeUnit.SECONDS.toNanos(1)));
        return formatTimeUntil(timeUntil);
    }

    private void everyMinute()
    {
        try
        {
            if (isMidnightEST())
            {
                ModUtil.sendMessage(MCColor.YELLOW + "A new day dawns on Hypixel...");
                ModUtil.sendMessage(MCColor.AQUA + "Check out your daily rewards!");
                ModUtil.sendMessage(MCColor.AQUA
                        + "Fetchur and Puzzler should also have some new quests for you!");
            }
            long currentTimeMillis = System.currentTimeMillis();
            checkTimeUntil("Magma Boss will spawn", currentTimeMillis,
                    "https://hypixel-api.inventivetalent.org/api/skyblock/bosstimer/magma/estimatedSpawn");
            checkTimeUntil("Dark Auction will start", currentTimeMillis,
                    "https://hypixel-api.inventivetalent.org/api/skyblock/darkauction/estimate");
            checkTimeUntil("Season of Jerry will begin", currentTimeMillis,
                    "https://hypixel-api.inventivetalent.org/api/skyblock/winter/estimate");
            checkTimeUntil("Jerry's Workshop will open", currentTimeMillis,
                    "https://hypixel-api.inventivetalent.org/api/skyblock/jerryWorkshop/estimate");
            checkTimeUntil("New Year's will begin", currentTimeMillis,
                    "https://hypixel-api.inventivetalent.org/api/skyblock/newyear/estimate");
            checkTimeUntil("New Year's will end", currentTimeMillis,
                    "https://hypixel-api.inventivetalent.org/api/skyblock/newyear/estimate",
                    TimeUnit.HOURS.toMillis(1));
            checkTimeUntil("The Traveling Zoo will open", currentTimeMillis,
                    "https://hypixel-api.inventivetalent.org/api/skyblock/zoo/estimate");
            SkyblockTime skyblockTime = getSkyblockTime();
            if (skyblockTime != null)
            {
                if (skyblockTime.day % 7 == 6)
                {
                    if (skyblockTime.hour == 18 && skyblockTime.minute == 0)
                    {
                        ModUtil.sendMessage(MCColor.AQUA
                                + "The Cult of the Fallen Star is meeting in 5 minutes!");
                    }
                    if (skyblockTime.hour == 12 && skyblockTime.minute == 0)
                    {
                        ModUtil.sendMessage(MCColor.AQUA
                                + "The Cult of the Fallen Star is meeting in 10 minutes!");
                    }
                }
                if (skyblockTime.day % 7 == 0)
                {
                    if (skyblockTime.day % 7 == 0 && skyblockTime.hour == 0
                            && skyblockTime.minute == 0)
                    {
                        ModUtil.sendMessage(
                                MCColor.AQUA + "The Cult of the Fallen Star is meeting!");
                    }
                    if (skyblockTime.hour == 5 && skyblockTime.minute == 0)
                    {
                        ModUtil.sendMessage(MCColor.AQUA
                                + "The Cult of the Fallen Star meeting is ending in 50 seconds!");
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
        }
    }

    private static SkyblockTime getSkyblockTime()
    {
        JsonObject json = Utils.reporting(() -> {
            try (InputStreamReader reader =
                    openURL("https://hypixel-api.inventivetalent.org/api/skyblock/calendar"))
            {
                return JsonParser.parseReader(reader).getAsJsonObject();
            }
        }, null);

        if (json == null) return null;

        JsonObject lastLog = json.get("lastLog").getAsJsonObject();

        double secondsSinceLastLog =
                (System.currentTimeMillis() / 1000) - lastLog.get("time").getAsDouble();

        // Parsing code from hypixel.inventivetalent.org
        int year = lastLog.get("year").getAsInt();
        int month = lastLog.get("month").getAsInt();
        int day = lastLog.get("day").getAsInt();
        int hour = lastLog.get("hour").getAsInt();
        int minute = lastLog.get("minute").getAsInt();

        int yearDiff = (int) Math.floor(secondsSinceLastLog / secondsPerYear);
        secondsSinceLastLog -= yearDiff * secondsPerYear;
        year += yearDiff; // the only thing without bounds

        int monthDiff = (int) Math.floor(secondsSinceLastLog / secondsPerMonth) % 13;
        secondsSinceLastLog -= monthDiff * secondsPerMonth;
        month = (month + monthDiff) % 13;


        int dayDiff = (int) Math.floor(secondsSinceLastLog / secondsPerDay) % 32;
        secondsSinceLastLog -= dayDiff * secondsPerDay;
        day = (day + dayDiff) % 32;


        int hourDiff = (int) Math.floor(secondsSinceLastLog / secondsPerHour) % 24;
        secondsSinceLastLog -= hourDiff * secondsPerHour;
        hour = (hour + hourDiff) % 24;

        if (hour < 6)
        { // hacky fix for the day rolling over at 6am instead of midnight
            if (day < 31)
            {
                day += 1;
            }
            else
            {
                day = 1;
                month += 1;
            }
        }


        int minuteDiff = (int) Math.floor(secondsSinceLastLog / secondsPerMinute) % 60;
        secondsSinceLastLog -= minuteDiff * secondsPerMinute;
        minute = (minute + minuteDiff) % 60;

        minute = (Math.round(minute / 5) * 5) % 60;

        return new SkyblockTime(year, month, day, hour, minute);
    }

    private static void checkTimeUntil(String event, long currentTimeMillis, String url)
    {
        checkTimeUntil(event, currentTimeMillis, url, 0);
    }

    private static void checkTimeUntil(String event, long currentTimeMillis, String url,
            long offset)
    {
        JsonObject json = Utils.reporting(() -> {
            try (InputStreamReader reader =
                    openURL("https://hypixel-api.inventivetalent.org/api/skyblock/calendar"))
            {
                return JsonParser.parseReader(reader).getAsJsonObject();
            }
        }, null);

        if (json == null) return;

        Duration timeUntil =
                Duration.ofMillis(offset + json.get("estimate").getAsLong() - currentTimeMillis);
        if (timeUntil.getSeconds() >= 600 && timeUntil.getSeconds() < 660)
        {
            ModUtil.sendMessage(MCColor.AQUA + event + " in 10 minutes!");
        }
        if (timeUntil.getSeconds() >= 300 && timeUntil.getSeconds() < 360)
        {
            ModUtil.sendMessage(MCColor.AQUA + event + " in 5 minutes!");
        }
        if (timeUntil.getSeconds() < 60)
        {
            ModUtil.sendMessage(MCColor.AQUA + event + " now!");
        }
    }

    private static boolean isMidnightEST()
    {
        ZonedDateTime est = ZonedDateTime.now(TimeZone.getTimeZone("America/New_York").toZoneId());
        return est.getHour() == 0 && est.getMinute() == 0;
    }

    public static String formatTimeUntil(java.time.Duration time)
    {
        DoubleValue<String, Boolean> out = new DoubleValue<>("", false);
        long days = time.toDays();
        out = tryFormat(out, "Week", days / 7);
        out = tryFormat(out, "Day", days % 7);
        out = tryFormat(out, "Hour", (int) time.toHours() % 24);
        out = tryFormat(out, "Minute", (int) time.toMinutes() % 60);
        out = tryFormat(out, "Second", (int) time.getSeconds() % 60);
        return out.retVal1.isEmpty() ? "0 seconds"
                : out.retVal1.substring(0, out.retVal1.length() - 1);
    }

    public static DoubleValue<String, Boolean> tryFormat(DoubleValue<String, Boolean> initialState,
            String section, long value)
    {
        if (value != 0 || initialState.retVal2)
        {
            return new DoubleValue<>(
                    initialState.retVal1 + value + " " + section + (value == 1 ? "" : "s") + " ",
                    true);
        }
        return initialState;
    }

    public static InputStreamReader openURL(String url) throws IOException
    {
        URLConnection conn = new URL(url).openConnection();
        conn.addRequestProperty("User-Agent", "Mozilla");
        return new InputStreamReader(conn.getInputStream());
    }

}
