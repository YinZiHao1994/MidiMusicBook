package yin.source.com.midimusicbook.midi.baseBean;

/**
 * Created by yin on 2017/6/13.
 */

public enum TimbreEnum {


    ONE(1);

    private int sign;

    TimbreEnum(int sign) {
        this.sign = sign;
    }

    public int getSign() {
        return sign;
    }
}
