package pink.zak.giveawaybot.service.time;

public enum TimeIdentifier {

    SECOND(1000, 9223372036854775L, "second", "seconds", "sec", "secs", "s"),
    MINUTE(60000, 153722867000000L, "minute", "minutes", "min", "mins", "m"),
    HOUR(3600000, 2562047780000L, "hour", "hours", "h", "hr", "hrs"),
    DAY(86400000, 106751991000L, "day", "days", "d", "ds"),
    WEEK(604800000, 15250284400L, "week", "weeks", "w", "ws"),
    MONTH(2630016000L, 476571388, "month", "months", "mo", "mos"),
    YEAR(31536000000L, 1305675, "year", "years", "yr", "yrs", "y");

    private final long milliseconds;
    private final long maxAmountOf;
    private final String[] identifiers;

    TimeIdentifier(long milliseconds, long maxAmountOf, String... identifiers) {
        this.milliseconds = milliseconds;
        this.maxAmountOf = maxAmountOf;
        this.identifiers = identifiers;
    }

    public static TimeIdentifier match(String input) {
        for (TimeIdentifier identifier : TimeIdentifier.values()) {
            for (String possibility : identifier.getIdentifiers()) {
                if (possibility.equalsIgnoreCase(input)) {
                    return identifier;
                }
            }
        }
        return null;
    }

    public String[] getIdentifiers() {
        return this.identifiers;
    }

    public long getMilliseconds(long amountOfUnit) {
        if (amountOfUnit > this.maxAmountOf) {
            return this.maxAmountOf * this.milliseconds;
        }
        return amountOfUnit * this.milliseconds;
    }

    public long getMilliseconds() {
        return this.milliseconds;
    }
}
