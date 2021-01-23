package ru.jampire.wclans;

public class Member
{
    private String name;
    private boolean isModer;
    
    public Member(final String name, final boolean isModer) {
        this.name = name;
        this.isModer = isModer;
    }
    
    public String getName() {
        return this.name;
    }
    
    public boolean isModer() {
        return this.isModer;
    }
    
    public void setModer(final boolean isModer) {
        this.isModer = isModer;
    }
}
