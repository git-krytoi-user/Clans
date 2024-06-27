package dev.r1nex.clans.data;

public class Description {
    private int id;
    private final int clanId;
    private String text;

    public Description(int id, int clanId, String text) {
        this.id = id;
        this.clanId = clanId;
        this.text = text;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public int getClanId() {
        return clanId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
