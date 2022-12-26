package nl.tudelft.sem.template.activity.domain.enums;

public enum Level {
    PROFESSIONAL(1),
    AMATEUR(2);
    public final int label;

    private Level(int label) {
        this.label = label;
    }
}
