package ru.jampire.wclans;

public enum ClanStanding
{
    WAR("WAR", 0), 
    NORMAL("NORMAL", 1), 
    ALLY("ALLY", 2);
    
    private ClanStanding(final String s, final int n) {
    }
    
    public static ClanStanding getByName(final String type) {
        try {
            return Enum.valueOf(ClanStanding.class, type.toUpperCase());
        }
        catch (IllegalArgumentException e) {
            return null;
        }
    }
}
