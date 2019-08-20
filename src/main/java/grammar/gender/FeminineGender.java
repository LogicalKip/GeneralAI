package grammar.gender;

public class FeminineGender extends Gender {
    private FeminineGender() {
    }

    private static FeminineGender instance;

    public static FeminineGender getInstance() {
        if (instance == null) {
            instance = new FeminineGender();
        }
        return instance;
    }
}
