package ru.andreygs.minimalmath;

import static java.lang.System.*;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class MiniMath {
	private static int[] intPwr = 
	{
		1073741824, 536870912, 268435456, 134217728, 67108864, 
		33554432, 16777216, 8388608, 4194304, 2097152, 1048576, 524288, 
		262144, 131072, 65536, 32768, 16384, 8192, 4096, 2048,
		1024, 512, 256, 128, 64, 32, 16, 8, 4, 2
	};
	
	public static Double plus(String arg1, String arg2) {
		if (arg1 == null || arg2 == null) return null;
		
		double a1, a2;
		try {
			a1 = Double.parseDouble(arg1);
			a2 = Double.parseDouble(arg2);
			return a1+a2;
		} catch(NumberFormatException e) {
			return null;
		}
	}
	
	public static Double pow(double number, double power) {
		if (number == 1) return number;
		else if (power == 0) return 1.0;
		else if (number == 0) return 0.0;
		
		SlashedDouble sdnum = new SlashedDouble(number);
		SlashedDouble sdpow = new SlashedDouble(power, true);
		
		return intPower(sdnum, sdpow).getIEEE754();
	}
	
	private static SlashedDouble intPower(SlashedDouble number, SlashedDouble power) {
		int ipwr = (int) power.getIntMantissaInteger();
		SlashedDouble result = new SlashedDouble(1.0);
		for (int i = 0; i < intPwr.length; i++) {
			if ((ipwr & intPwr[i]) == intPwr[i]) {
				result = innerMult(number, result, "");
			}
			result = innerMult(result, result, "");
		}
		if ((ipwr & 1) == 1) {
			result = innerMult(number, result, "");
		}
		if (number.isNegative() && ipwr % 2 == 1) result.setSign("-");
		return result;
	}
	
	public static Double mult(double number1, double number2) {
		if (number1 == 1) return number2;
		if (number2 == 1) return number1;
		if (number1 == 0 || number2 == 0) return 0.0;
		if (number1 == 0xffffffff) return (double)~((long)number2) + 1;
		if (number2 == 0xffffffff) return (double)~((long)number2) + 1;
		
		SlashedDouble sd1 = new SlashedDouble(number1);
		SlashedDouble sd2 = new SlashedDouble(number2);
		String
			negativesign;
		if ((number1 >= 0 && number2 >= 0) || (number1 < 0 && number2 < 0)) {
			negativesign = "";
		} else {
			negativesign = "-";
		}
		return innerMult(sd1, sd2, negativesign).getIEEE754();
	}
	
	private static SlashedDouble innerMult(SlashedDouble number1, SlashedDouble number2, String negativesign) {
		long result = 0l, unit;
		String num2raw;
		if (number1.onesEnum() < number2.onesEnum()) {
			unit = number2.getLongMantissa();
			num2raw = number1.getBinaryRaw();
		} else {
			unit = number1.getLongMantissa();
			num2raw = number2.getBinaryRaw();
		}

		int biasresult = 0; 

		for (int i = num2raw.length() + 0xffffffff, shift = 0, check, spaceneed, leadzeroes; i > 0xffffffff; i+= 0xffffffff, shift++) {
			if (num2raw.charAt(i) == '1') {
				leadzeroes = Long.numberOfLeadingZeros(unit);
				check = leadzeroes + ~shift;
				if (check > 0) {
					unit <<= shift;
				} else {
					if (leadzeroes == 0) {
						unit >>>= 1;
						spaceneed = ~check + 1;
						if (result > 0) result >>>= 1;
						biasresult += shift + 1;
					} else {
						unit <<= leadzeroes + 0xffffffff;
						spaceneed = ~check + 1;
						result >>>= spaceneed;
						biasresult += spaceneed;
					}
					//10010010000000001001011101000111001100110101100010100010101100
					//100100100000000010010111010001110011001101011000101000101011000
					//100100100000000010010111010001110011001101011000101000101011000
					/*
					// this is a unnecessary rounding codition, but in some cases it can bring
					// some additionally accuracy
					spaceneed = ~check + 1;
					result >>>= spaceneed;
					biasresult += spaceneed;
					if (Long.numberOfTrailingZeros(result) == 0) {
						result >>>= 1;
						result++;
					} else {
						result >>>= 1;
					}
					biasresult += spaceneed;
					*/
					
					/*
					// this is another version of shifting statements
					// it was made for propose of theroreticaly accuracy increase
					unit <<= leadzeroes + 0xffffffff;
					if (check < 0) {
						spaceneed = ~check + 1;
						tempresult >>>= spaceneed;
						biasresult += spaceneed;
					}
					
					if ((tempresult + unit) > 0xffffffff) {
						unit >>>= 1;
						tempresult >>>= 1;
						biasresult++;
					}
					*/
				}
				result += unit;
				shift = 0;
			}
		}
		String mantissa = Long.toBinaryString(result);
		int resultexp = getExponent(mantissa, number1.getExp(), number2.getExp(), number1.getBinaryRaw(), number2.getBinaryRaw(), biasresult);
		//out.println(getIEEE754(mantissa, resultexp, false));
		out.println(resultexp);
		SlashedDouble sd = new SlashedDouble(mantissa, resultexp, negativesign, result);
		return sd;
	}
	
	private static int getExponent(String fraction, int startexp1, int startexp2, String num1raw, String num2raw, int biasresult) {
		int fractdigitsnum = num1raw.length() + num2raw.length() + 0xfffffffe;
		out.println(fraction.length());
		out.println(~fractdigitsnum);
		out.println(startexp1);
		out.println(startexp2);
		out.println(biasresult);
		return fraction.length() + ~fractdigitsnum + startexp1 + startexp2 + biasresult;
	}
	
	public static void main(String[] args) {
		//System.out.println(Double.toHexString(1));
		//System.out.println(Double.toHexString(0));
		//System.out.println(pow(234.532, 40));
		//System.out.println(Math.pow(234.532, 40));
		
		//double d1 = Math.random()*100, d2 = Math.floor(Math.random()*100);
		//System.out.println(d1);
		//System.out.println(d2);
		//System.out.println(pow(36.408808470482136, 23.0));
		//System.out.println(Math.pow(36.408808470482136, 23.0));
		/*
		out.println(mult(4093215996289808.0, 36.408808470482136));
		out.println(4093215996289808.0 * 36.408808470482136);
		
		double d1 = Math.random()*100, d2 = Math.floor(Math.random()*100);
		System.out.println(d1);
		System.out.println(d2);
			System.out.println(pow(d1, d2));
			System.out.println(Math.pow(d1, d2));
		
		System.out.println(pow(96.66513193776815, 21.0));
		System.out.println(Math.pow(96.66513193776815, 21.0));
		*//*
		for (int i = 0; i < 10; i++) {
			double d1 = Math.random()*1, d2 = Math.floor(Math.random()*100);
			System.out.println(d1 + ";;;" + d2);
			System.out.println(pow(d1, d2));
			System.out.println(Math.pow(d1, d2));
		}
		out.println(Math.pow(0,0));
		*/
		
		out.println(Double.longBitsToDouble(Long.parseUnsignedLong("1111111111110000000000001000000000000000100100000100000000000000", 2)));
		out.println(Long.toBinaryString(Double.doubleToLongBits(-1.0)));
		//1 011111111110000000000000000000000000000000000000000000000000000
		out.println(Long.toBinaryString(Double.doubleToLongBits(Double.MAX_EXPONENT)));
		//0 100 0000 1000 1111111110000000000000000000000000000000000000000000
		out.println(Long.toBinaryString(Double.doubleToLongBits(Double.MAX_VALUE)));
		//0 111 1111 1110 1111111111111111111111111111111111111111111111111111
		out.println(Integer.valueOf("11111111110", 2));
		
		//out.println(Double.valueOf(0x1.0p+1024));
		out.println(Long.toBinaryString(Double.doubleToLongBits(Double.MIN_EXPONENT)));
		out.println(Long.toBinaryString(Double.doubleToLongBits(Double.MIN_NORMAL)));
		out.println(Long.toBinaryString(Double.doubleToRawLongBits(Double.MIN_VALUE)));
		
		// Infinity 	0 11111111111 0000000000000000000000000000000000000000000000000000
		// -Infinity	1 11111111111 0000000000000000000000000000000000000000000000000000
		// signalNaN	0 11111111111 0000000000000000000000000000000000000000000000000001
		// quietNaN		0 11111111111 1000000000000000000000000000000000000000000000000001
		// NaN			* 11111111111 ****************************************************
		// all those uphere are NaNs except these Infinities
		
		Pattern p = Pattern.compile("[01]+");
		Matcher m = p.matcher("asdasd0001111000qweqwe");
		m.find();
		System.out.println(m.group());
		
		out.println(Double.valueOf(0x1.0p-1043));
		out.println(Double.valueOf(0x1.0p-10));
		out.println(mult(Double.valueOf(0x1.fadp-1043),Double.valueOf(0x1.0p-1)) + "!!!");
		out.println((Double.valueOf(0x1.fadp-1043)*Double.valueOf(0x1.0p-1)) + "!!!");
		out.println(Double.toHexString(0x1.0p-1043));
		out.println(0xfffffc02);
		//out.println(Long.toBinaryString(Double.doubleToLongBits(Double.valueOf(0x1.0p-10))));
		
	}
	
	
	
	
	
	public static String getStringIEEE754(String number, int exp, boolean isnegative) {
		String ieee754;
		if (isnegative) ieee754 = "1";
		else ieee754 = "0";
		exp += 1023;
		ieee754 += Integer.toBinaryString(exp) + number.substring(1);
		if (ieee754.length() < 64) {
			for (int i = ieee754.length(); i < 64; i++) {
				ieee754 += "0";
			}
		} else {
			ieee754 = ieee754.substring(0, 64);
		}
		return ieee754;	
	}
	
	public static double getIEEE754(String number, int exp) {
		return getIEEE754(number, exp, false);	
	}
	
	public static double getIEEE754(String number, int exp, boolean isnegative) {
		if (number.indexOf('1') == 0xffffffff && exp == 0) return 0.0;
		String ieee754 = "";
		if (isnegative) ieee754 = "-";
		ieee754 += "0x1." + fromBinaryToHex(number.substring(1)) + "p" + exp;
		return Double.valueOf(ieee754);	
	}
	
	public static String fromBinaryToHex(String number) {
		if (number.equals("")) return "0";
		String raw = "", digit;
		if (number.length() < 52) {
			for (int i = number.length(); i % 4 != 0; i++) {
				number += "0";
			}
		}
		for (int i = 0; i < 52 && i < number.length(); i += 4) {
			digit = number.substring(i, i+4);
			switch(digit) {
				case "0000": raw += "0"; break;
				case "0001": raw += "1"; break;
				case "0010": raw += "2"; break;
				case "0011": raw += "3"; break;
				case "0100": raw += "4"; break;
				case "0101": raw += "5"; break;
				case "0110": raw += "6"; break;
				case "0111": raw += "7"; break;
				case "1000": raw += "8"; break;
				case "1001": raw += "9"; break;
				case "1010": raw += "a"; break;
				case "1011": raw += "b"; break;
				case "1100": raw += "c"; break;
				case "1101": raw += "d"; break;
				case "1110": raw += "e"; break;
				default: raw += "f"; break;
			}
		}
		return raw;
	}
}