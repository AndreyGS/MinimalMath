package ru.andreygs.minimalmath;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class SlashedDouble implements Cloneable {
	private Double number;
	
	private String negativesign;
	
	private String raw;
	private Long longraw;
	
	private String intraw;
	private String fractraw;
	
	private Integer exp;
	
	private Integer onesnum;
	
	private String roundedrawbin;
	private String roundedrawhex;
	
	private String ieee754bin;
	private String ieee754hex;
	
	public SlashedDouble(double number) {
		this.number = number;
		
		if (number != Double.POSITIVE_INFINITY &&  number != Double.NEGATIVE_INFINITY  &&
			!Double.isNaN(number)) {
			slashIt();
		} else {
			if (number == Double.POSITIVE_INFINITY) negativesign = "";
			else if (number == Double.NEGATIVE_INFINITY) negativesign = "-";
		}
	}
	
	public SlashedDouble(long longraw, int exp, String negativesign) {
		this.raw = cutFractTail(Long.toBinaryString(longraw));
		this.exp = exp;
		this.negativesign = negativesign;
		
		checkRaw();
		
		this.longraw = Long.parseUnsignedLong(this.raw, 2);
	}
	
	public SlashedDouble(String raw, int exp, String negativesign) throws NumberFormatException {
		this.raw = cutFractTail(parseRaw(raw));
		this.exp = exp;
		this.negativesign = negativesign;
		
		checkRaw();
	}
	
	public SlashedDouble(String raw, int exp, String negativesign, long longraw) {
		this(raw, exp, negativesign);
		this.longraw = Long.parseUnsignedLong(this.raw, 2);
	}
	
	private SlashedDouble(Double number, String negativesign, String raw, Long longraw,
		String intraw, String fractraw, Integer exp, Integer onesnum, String roundedrawbin,
		String roundedrawhex, String ieee754bin, String ieee754hex)	{
		if (number != null) this.number = Double.valueOf(number);
		this.negativesign = negativesign;
		this.raw = raw;
		if (longraw != null) this.longraw = Long.valueOf(longraw);
		this.intraw = intraw;
		this.fractraw = fractraw;
		if (exp != null) this.exp = Integer.valueOf(exp);
		if (onesnum != null) this.onesnum = Integer.valueOf(onesnum);
		this.roundedrawbin = roundedrawbin;
		this.roundedrawhex = roundedrawhex;
		this.ieee754bin = ieee754bin;
		this.ieee754hex = ieee754hex;
	}
	
	private void checkRaw() {
		if (raw.equals("1111111111111111111111111111111111111111111111111111111111111111")) {
			raw = "1";
			exp++;
		}
	}
	
	public static String parseRaw(String raw) {
		Pattern p = Pattern.compile("(?<=0{0,}+)[01]{0,63}");
		Matcher m = p.matcher(raw);
		m.find();

		return m.group();
	}
	
	public static String parseRaw(String raw, int start, int end) {
		return parseRaw(raw.substring(start, end));
	}
	
	private void slashIt() {
		String[] stripes = Double.toHexString(number).split("[.p]");
		
		if (stripes[0].charAt(0) == '-') negativesign = "-";
		else negativesign = "";
		
		roundedrawhex = stripes[1];
		raw = cutFractTail(fromHexToBinary(roundedrawhex));
		
		// check for zero and denormal numbers
		if (stripes[0].charAt(stripes[0].length() + 0xffffffff) == '0') {
			int denormaladdexp = raw.indexOf('1');
			if (denormaladdexp != 0xffffffff) {
				exp = 0xfffffc02 + ~denormaladdexp;
				raw = raw.substring(raw.indexOf('1'));
			} else {
				exp = 0;
				raw = "";
			}
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
			for (int i = raw.length(); (i & 3) != 0; i++) {
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
	
	public Long getLongRaw() {
		if (longraw == null) {
			longraw = Long.parseUnsignedLong(raw, 2);
		}
		
		return longraw;
	}
	
	public Integer getExp() {
		return exp;
	}
	
	public String getBinaryRaw() {
		return raw;
	}
	
	public String getroundedrawhex() {
		if (roundedrawhex == null) {
			roundedrawhex = fromBinaryToHex(roundedrawbin);
		}
		
		return roundedrawhex;
	}
		
	private String getroundedrawbin() {
		if (roundedrawbin == null) {
			
			// these instructions supply additional accuracy
			// in spite of that we already have scaled rounding in MiniMath,
			// current final rounding highly increase precision of result,
			// total amount of missed values with accuracy of 13-14 digits after decimal point 
			// reach up to 10-20 times less, relatively of when these instructions are off
			//if (raw.length() > 53 && (raw.indexOf('0') > 52 || raw.indexOf('0') == 0xffffffff)) {
			if (raw.length() > 53 && raw.charAt(53) == '1') {
				if (raw.indexOf('0') > 53 || raw.indexOf('0') == 0xffffffff) {
					exp++;
					roundedrawbin = "";
					raw = "1";
				} else {
					long chunk = Long.valueOf(raw.substring(0,53), 2);
					chunk++;
					roundedrawbin = Long.toBinaryString(chunk).substring(1);
					if (roundedrawbin.length() == 53) exp++;
				}
			} else {
				if (raw.length() < 2) roundedrawbin = "";
				else roundedrawbin = raw.substring(1);
			}
		}
		
		return roundedrawbin;
	}
	
	public String getDoubleHexRaw() {
		if (ieee754hex == null) {
			
			getroundedrawbin();
			
			if ((raw.indexOf('1') == 0xffffffff) && (exp == 0 || exp  < 0xfffffbce)) {
				// if it's '0' or below minimum
				ieee754hex = negativesign + "0x0.0p0";
			} else {	
				ieee754hex = negativesign + "0x1." + getroundedrawhex() + "p" + exp;
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
			if (exp != null) {
				if (exp > 0) {
					if (exp < raw.length()) {
						intraw = raw.substring(0, exp+1);
					} else {
						intraw = raw;
					}
				} else if (exp == 0 && raw.indexOf('1') == 0) {
					intraw = "1";
				} else {
					intraw = "";
				}
			}
		}
		
		return intraw;
	}
	
	public SlashedDouble getIntSD() {
		if (intraw == null) getIntRaw();
		
		if (exp == null) {
			return this;
		} else if (exp > 0xffffffff) {
			return new SlashedDouble(intraw, exp, negativesign);
		} else {
			return new SlashedDouble("", 0, negativesign);
		}
	}
	
	public String getFractRaw() {
		if (fractraw == null) {
			if (exp != null) {
				if (exp < 0) {
					fractraw = raw;
				} else if (exp < (raw.length() + 0xffffffff)) {
					fractraw = raw.substring(exp+1);
				} else {
					fractraw = "";
				}
			}
		}
		
		return fractraw;
	}
	
	public SlashedDouble getFractSD() {
		if (fractraw == null) getFractRaw();
		
		if (exp == null) {
			if (Double.isNaN(number)) return this;
			else if (number == Double.NEGATIVE_INFINITY) return new SlashedDouble("", 0, "-");
			else return new SlashedDouble("", 0, "");
		} else {
			if (fractraw.isEmpty()) return new SlashedDouble("", 0, negativesign);
			else {
				if (exp < 0) return new SlashedDouble(fractraw, exp, negativesign);
				else return new SlashedDouble(fractraw, ~fractraw.indexOf('1'), negativesign);
			}
		}
	}
	
	
	public boolean isNegative() {
		if (negativesign.equals("-")) return true;
		else return false;
	}
	
	public Double getDouble() {
		return number;
	}
	
	public String getNegativeSign() {
		return negativesign;
	}
	
	public void setSign(String sign) {
		if (number != null && negativesign != sign) number = -number;
		if (sign.equals("") || sign.equals("-")) negativesign = sign;
	}

	public boolean isOdd() {
		if ((exp < raw.length() && raw.charAt(exp) == 0) || exp < 0 || exp >= raw.length())
			return false;
		return true;
	}
	
	public boolean isOddIntDigitsNum() {
		if ((exp & 1) == 0) return true;
		else return false;
	}
	
	public String getIEEE754Bin() {
		if (ieee754bin == null) {
			if (exp != null) {
				if (negativesign == "-") ieee754bin = "1";
				else ieee754bin = "0";
				
				int resultexp = exp + 1023;
				
				if (resultexp > 2046) {
					ieee754bin += "11111111111" + "0000000000000000000000000000000000000000000000000000"; // +-Infinity
				}
				
				getroundedrawbin();
				
				String extroundedrawbin;
				
				if (roundedrawbin.length() == 0) 
					extroundedrawbin = "0000000000000000000000000000000000000000000000000000";
				else {
					extroundedrawbin = roundedrawbin.substring(0);
					for (int i = extroundedrawbin.length(); i < 52; i++) extroundedrawbin += '0';
				}
				
				if (resultexp == 1023 && raw == "") {
					ieee754bin += "00000000000" + extroundedrawbin;
				} else if (resultexp > 0) {
					String resultexpstr = Integer.toBinaryString(resultexp);
					String addzeros = "";
					
					for (int i = resultexpstr.length(); i < 11; i++) addzeros += '0';
					
					resultexpstr = addzeros + resultexpstr;
					
					ieee754bin += resultexpstr + extroundedrawbin; // normal numbers
				} else if (resultexp > 0xffffffcb) {
					ieee754bin += "00000000000" + extroundedrawbin; // denormal numbers
				} else {
					ieee754bin += "000000000000000000000000000000000000000000000000000000000000000"; // sub-denormal == +-0.0
				}
			} else {
				if (number == Double.POSITIVE_INFINITY)
					ieee754bin = "0111111111110000000000000000000000000000000000000000000000000000";
				else if (number == Double.NEGATIVE_INFINITY)
					ieee754bin = "1111111111110000000000000000000000000000000000000000000000000000";
				else 
					ieee754bin = "0111111111111000000000000000000000000000000000000000000000000000";
			}
		}
		
		return ieee754bin;	
	}
	
	public SlashedDouble clone() {
		SlashedDouble sdnum = new SlashedDouble(this.number, this.negativesign, this.raw, 
			this.longraw, this.intraw, this.fractraw, this.exp, this.onesnum, 
			this.roundedrawbin, this.roundedrawhex, this.ieee754bin, this.ieee754hex);
		
		return sdnum;
	}
}