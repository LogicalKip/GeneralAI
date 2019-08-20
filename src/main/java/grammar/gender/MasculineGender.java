package grammar.gender;

public class MasculineGender extends Gender {
    private MasculineGender() {
    }

    private static MasculineGender instance;

    public static MasculineGender getInstance() {
        if (instance == null) {
            instance = new MasculineGender();
        }
        return instance;
    }
}
