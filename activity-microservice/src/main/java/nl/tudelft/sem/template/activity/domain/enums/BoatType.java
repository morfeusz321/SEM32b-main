package nl.tudelft.sem.template.activity.domain.enums;

public enum BoatType {
    C4(1),
    FOUR_PLUS(2),
    EIGHT_PLUS(3);

    public final int label;

    private BoatType(int label) {
        this.label = label;
    }

}
