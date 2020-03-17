package ru.andreygs.minimalmath;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class SlashedDouble {
	
	private String negativesign;
	private String raw;
	private Integer exp;
	
	private Integer onesnum;
	
	private Long mantissa;
	
	private String roundedbin;
	private String roundedhex;
	
	private String ieee754bin;
	private String ieee754hex;
	
	private Double number;
	
	private String intraw;
	private String fractraw;
	
	private Long intmantissalong;
	private Integer intmantissaint;
	private Long fractmantissa;
	
	public SlashedDouble(double number) {
		this.number = number;
		slashIt();
	}
	
	public SlashedDouble(double number, boolean additionalslicing) {
		this(number);
		getIntRaw();
		getFractRaw();
	}
	
	public SlashedDouble(Long mantissa, Integer exp, String negativesign) {
		this.mantissa = mantissa;
		this.exp = exp;
		this.negativesign = negativesign;
	}
	
	public SlashedDouble(Long mantissa, Integer exp, String negativesign, boolean additionalslicing) {
		this(mantissa, exp, negativesign);
		this.raw = cutFractTail(Long.toBinaryString(mantissa));
		getIntRaw();
		getFractRaw();
	}
	
	public SlashedDouble(String raw, Integer exp, String negativesign) throws NumberFormatException {
		this.raw = fetchRaw(raw);
		this.exp = exp;
		this.negativesign = negativesign;
	}
	
	public SlashedDouble(String raw, Integer exp, String negativesign, Long mantissa) {
		this(raw, exp, negativesign);
		this.mantissa = mantissa;
	}
	
	public static String fetchRaw(String raw) {
		Pattern p = Pattern.compile("[01]+");
		Matcher m = p.matcher(raw);
		m.find();
		
		return m.group();
	}
	
	private void slashIt() {
		String[] stripes = Double.toHexString(number).split("[.p]");
		
		if (stripes[0].charAt(0) == '-') negativesign = "-";
		else negativesign = "";
		
		roundedhex = stripes[1];
		raw = cutFractTail(fromHexToBinary(roundedhex));

		if (stripes[0].charAt(stripes[0].length() + 0xffffffff) == '0') {
			for (int i = 0; i < raw.length(); i++) {
				if (raw.charAt(i) == '1') {
					this.exp = Integer.valueOf(stripes[2]) + ~i;
					break;
				}
			}
			raw = raw.substring(raw.indexOf('1'));
		} else {
			exp = Integer.valueOf(stripes[2]);
			raw = "1" + raw;
		}
	}
	
	private static String fromHexToBinary(String hexraw) {
		String raw = "";
		
		for (int i = 0; i < hexraw.length(); i++) {
			char digit = hexraw.charAt(i);
			
			switch(digit) {
				case '0': raw += "0000"; break;
				case '1': raw += "0001"; break;
				case '2': raw += "0010"; break;
				case '3': raw += "0011"; break;
				case '4': raw += "0100"; break;
				case '5': raw += "0101"; break;
				case '6': raw += "0110"; break;
				case '7': raw += "0111"; break;
				case '8': raw += "1000"; break;
				case '9': raw += "1001"; break;
				case 'a': raw += "1010"; break;
				case 'b': raw += "1011"; break;
				case 'c': raw += "1100"; break;
				case 'd': raw += "1101"; break;
				case 'e': raw += "1110"; break;
				default: raw += "1111"; break;
			}
		}
		
		return raw;
	}
	
	public static String fromBinaryToHex(String raw) {
		if (raw.equals("")) return "0";
		
		String hexraw = "", digit;
		
		if (raw.length() < 52) {
			for (int i = raw.length(); i % 4 != 0; i++) {
				raw += "0";
			}
		}
		
		for (int i = 0; i < 52 && i < raw.length(); i += 4) {
			digit = raw.substring(i, i+4);
			
			switch(digit) {
				case "0000": hexraw += "0"; break;
				case "0001": hexraw += "1"; break;
				case "0010": hexraw += "2"; break;
				case "0011": hexraw += "3"; break;
				case "0100": hexraw += "4"; break;
				case "0101": hexraw += "5"; break;
				case "0110": hexraw += "6"; break;
				case "0111": hexraw += "7"; break;
				case "1000": hexraw += "8"; break;
				case "1001": hexraw += "9"; break;
				case "1010": hexraw += "a"; break;
				case "1011": hexraw += "b"; break;
				case "1100": hexraw += "c"; break;
				case "1101": hexraw += "d"; break;
				case "1110": hexraw += "e"; break;
				default: hexraw += "f"; break;
			}
		}
		
		return hexraw;
	}
	
	public static String cutFractTail(String raw) {
		for (int i = raw.length() + 0xffffffff; i > 0xffffffff; i += 0xffffffff) {
			if (raw.charAt(i) == '1') {
				return raw.substring(0, i+1);
			}
		}
		
		return "";
	}
	
	public Long getLongMantissa() {
		if (mantissa == null) {
			mantissa = Long.valueOf(raw, 2);
		}
		
		return mantissa;
	}
	
	public Integer getExp() {
		return exp;
	}
	
	public String getBinaryRaw() {
		return raw;
	}
	
	public String getRoundedHex() {
		if (roundedhex == null) {
			roundedhex = fromBinaryToHex(roundedbin);
		}
		
		return roundedhex;
	}
	
	private String roundingToDoubleRaw() {
		if (roundedbin == null) {
			if (raw.length() > 53) {
				if (raw.charAt(53) == '1') {
					long chunk = Long.valueOf(raw.substring(1,53), 2);
					chunk++;
					roundedbin = "";
					for (int i = 1; raw.charAt(i) == '0'; i++) roundedbin += "0";
					roundedbin += Long.toBinaryString(chunk);
				} else {
					roundedbin = raw.substring(1,53);
				}
			} else {
				roundedbin = raw.substring(1);
				for (int i = roundedbin.length(); i < 52; i++) {
					roundedbin += '0';
				}
			}
		}
		
		return roundedbin;
	} 
	
	public String getDoubleRaw() {
		if (ieee754bin == null) {
			if (negativesign == "-") ieee754bin = "1";
			else ieee754bin = "0";
			
			int resultexp = exp + 1023;
			
			if (resultexp > 2046) {
				ieee754bin += Integer.toBinaryString(resultexp) + "0000000000000000000000000000000000000000000000000000"; // +-Infinity
			} else if (resultexp > 0) {
				ieee754bin += Integer.toBinaryString(resultexp) + roundingToDoubleRaw(); // normal numbers
			} else if (resultexp > 0xffffffcc) {
				ieee754bin += "00000000000" + roundingToDoubleRaw(); // denormal numbers
			} else {
				ieee754bin += "000000000000000000000000000000000000000000000000000000000000000"; // sub-denormal = 0
			}
		}
		
		return ieee754bin;	
	}
	
	public String getDoubleHexRaw() {
		if (ieee754hex == null) {
			if (getDoubleRaw().indexOf('1') == 0xffffffff) {
				if 	(exp == 0 || exp  < 0xfffffc02) { // if it's '0' or below minimum
					ieee754hex = negativesign + "0x0.0p0";
				} else {
					
				}
			} else {
				ieee754hex = negativesign + "0x1." + getRoundedHex() + "p" + exp;
			}
		}
		
		return ieee754hex;
	}
	
	public Double getIEEE754() {
		if (number == null) {
			number = Double.valueOf(getDoubleHexRaw());
		}
		
		return number;	
	}
	
	public Integer onesEnum() {
		if (onesnum == null) {
			int counter = 0;
			
			for (int i = 0; i < getBinaryRaw().length(); i++) {
				if (raw.charAt(i) == '1') counter++;
			}
			
			onesnum = counter;
		}
		
		return onesnum;
	}
	
	public String getIntRaw() {
		if (intraw == null) {
			if (exp < 0) {
				intraw = "";
			} else {
				if (exp < raw.length()) {
					intraw = raw.substring(0, exp+1);
				} else {
					intraw = raw;
					
					for (int i = intraw.length(); i < 64 && i <= exp; i++) {
						intraw += '0';
					}
				}
			}
		}
		
		return intraw;
	}
	
	public String getFractRaw() {
		if (fractraw == null) {
			
			if (getIntRaw().length() < raw.length()) {
				fractraw = raw.substring(getIntRaw().length());
			} else {
				fractraw = "";
			}
			
		}
		return fractraw;
	}
	
	public Long getIntMantissaLong() {
		if (intmantissalong == null) {
			intmantissalong = Long.valueOf(intraw, 2);
		}
		
		return intmantissalong;
	}
	
	public Integer getIntMantissaInteger() {
		if (intmantissaint == null) {
			if (intraw.length() > 32) {
				intmantissaint = Integer.valueOf(intraw.substring(0, 32), 2);
			} else {
				intmantissaint = Integer.valueOf(intraw, 2);
			}
		}
		
		return intmantissaint;
	}
	
	public Long getFractMantissa() {
		if (fractmantissa == null) {
			fractmantissa = Long.valueOf(fractraw, 2);
		}
		
		return fractmantissa;
	}
	
	public boolean isNegative() {
		if (negativesign.equals("-")) return true;
		else return false;
	}
	
	public String getNegativeSign() {
		return negativesign;
	}
	
	public void setSign(String sign) {
		if (sign.equals("") || sign.equals("-")) negativesign = sign;
	}
}