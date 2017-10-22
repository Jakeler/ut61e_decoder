package com.jake;


/**
 * Created by Jake on 11.08.16.
 */
public class UT61e_decoder {
    public byte[] raw;
    public double value;
    public int mode;
    private int unit, type, info;
    public String unit_str;

    // Mode constants
    public final int MODE_VOLTAGE = 0xB;
    public final int MODE_CURRENT_A = 0x0;
    public final int MODE_CURRENT_mA = 0xF;
    public final int MODE_CURRENT_µA = 0xD;
    public final int MODE_RESISTANCE = 0x3;
    public final int MODE_FREQ = 0x2;
    public final int MODE_CAPACITANCE = 0x6;
    public final int MODE_DIODE = 0x1;
    public final int MODE_CONTINUITY = 0x5;
    private final int MODE_DUTY = 0x8;

    //Type constants
    private final int DC = 0x8; //byte 10
    private final int AC = 0x4;
    private final int AUTO = 0x2;

    private final int HZ = 0x1;
    //Info constants
    private final int OL = 0x1; // byte 7
    private final int LOWBAT = 0x2;
    private final int NEG = 0x4;

    private final int UL = 0x8; // byte 9

    //Unit constants
    private final String[][] units = new String[16][];
    private final int[][] units_div = new int[16][];

    public UT61e_decoder() {
        units[MODE_VOLTAGE] = new String[]{"V", "V", "V", "V", "mV"};
        units[MODE_CURRENT_A] = new String[]{"A"};
        units[MODE_CURRENT_mA] = new String[]{"mA", "mA"};
        units[MODE_CURRENT_µA] = new String[]{"µA", "µA"};
        units[MODE_RESISTANCE] = new String[]{"Ω", "kΩ", "kΩ", "kΩ", "MΩ", "MΩ", "MΩ"};
        units[MODE_FREQ] = new String[]{"Hz", "Hz", "", "kHz", "kHz", "MHz", "MHz", "MHz"};
        units[MODE_CAPACITANCE] = new String[]{"nF", "nF", "µF", "µF", "µF", "mF", "mF", "mF"};
        units[MODE_DIODE] = new String[]{"V"};
        units[MODE_CONTINUITY] = new String[]{"Ω"};
        units[MODE_DUTY] = new String[]{"%", "%"};

        units_div[MODE_VOLTAGE] = new int[]{10000, 1000, 100, 10, 100};
        units_div[MODE_CURRENT_A] = new int[]{10000, 1000, 100, 10, 100};
        units_div[MODE_CURRENT_mA] = new int[]{1000, 100};
        units_div[MODE_CURRENT_µA] = new int[]{100, 10};
        units_div[MODE_RESISTANCE] = new int[]{100, 10000, 1000, 100, 10000, 1000, 100};
        units_div[MODE_FREQ] = new int[]{100, 10, 1, 1000, 100, 10000, 1000, 100};
        units_div[MODE_CAPACITANCE] = new int[]{1000, 100, 10000, 1000, 100, 10000, 1000, 100};
        units_div[MODE_DIODE] = new int[]{10000};
        units_div[MODE_CONTINUITY] = new int[]{100};
        units_div[MODE_DUTY] = new int[]{10, 10};



    }

    public boolean parse(byte[] input) {

        if (!checkLength(input)) {
            System.err.print("decoder: wrong input length");
            return false;
        } else if (!checkParity(input, true)) {
            System.err.print("decoder: parity check failed");
            return false;
        }
        try {
            set_info();
            set_mode();
            calcValue();
        } catch (NullPointerException e) {
            System.err.print("decoder: mode/unit not found");
            return false;
        }

        return true;
    }

    private boolean checkParity(byte[] input, boolean odd) {
        boolean correct = true;
        raw = new byte[input.length];
        for (int i = 0; i<input.length; i++) {
            byte in = input[i];
            in ^= in >> 4;
            in ^= in >> 2;
            in ^= in >> 1;
            correct = ((in & 1) == 1) == odd? correct : false;

            raw[i] = (byte)(input[i] & 0x7F);
        }
        return correct;
    }

    private boolean checkLength(byte[] input) {
        return input.length == 14;
    }


    private void set_mode() {
        mode = raw[6] & 0x0F;
        if (isFreq()) mode = MODE_FREQ;
        if (isDuty()) mode = MODE_DUTY;
        unit = raw[0] & 0x7;
        unit_str = units[mode][unit];
    }

    private void set_info() {
        type = raw[10] & 0x0F;
        info = raw[7] & 0x0F;
    }

    private void calcValue() {
        int factor = 10000;
        for (int i = 1; i<=5; i++) {
            value += (raw[i] & 0xF) * factor;
            factor /= 10;
        }
        value /= units_div[mode][unit];
        if (isNeg()) value *= -1;
    }

    public boolean isNeg() {
        return (info & NEG) == NEG;
    }

    public boolean isOL() {
        return (info & OL) == OL;
    }

    public boolean isUL() {
        return (raw[9] & UL) == UL;
    }

    public boolean isFreq() {
        return (type & HZ) == HZ;
    }

    public boolean isDuty() {
        return (info & MODE_DUTY) == MODE_DUTY || info == MODE_DUTY;
    }

    public boolean isDC() {
        return (type & DC) == DC;
    }

    public boolean isAC() {
        return (type & AC) == AC;
    }

    @Override
    public String toString() {
        return String.format("%.4f", value) + " " + unit_str;
    }

    public String toCSVString() {
        String seperator = ";";
        String out = String.format("%.4f", value) + seperator;
        out += unit_str + seperator;
        out += (isDC()? "DC":"") + (isAC()? "DC":"")+ (isFreq()? "Freq.":"")+ (isDuty()? "Duty":"") + seperator;
        out += (isOL()? "OL":"") + (isUL()? "UL":"");
        return out;
    }

    public static String csvHeader = "Value;Unit;Type;Overloaded";
}
