package muzdima.boringmoney.repository.model;

public class SpinnerItem {
    public long id;
    public String text;
    public boolean isVisible;

    @Override
    public String toString() {
        return text;
    }
}
