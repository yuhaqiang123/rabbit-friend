package testcase;

/**
 * Created by yuhaiqiang on 2018/7/12.
 *
 * @description
 */
public class TestEnum {
    public static void main(String[] args) {
        System.out.println(RawEnum.READY);
    }

}

enum RawEnum {
    READY(0),
    STOP(1);

    private final int value;

    RawEnum(int val) {
        this.value = val;
    }

    int getValue() {
        return this.value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
