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
		if (power == Double.NaN || number == Double.NaN) {
			return Double.NaN;
		} else if (number == Double.POSITIVE_INFINITY || power == Double.POSITIVE_INFINITY) {
			return Double.POSITIVE_INFINITY;
		} else if (number == Double.NEGATIVE_INFINITY) {
			if ((int) power % 2 == 0) return Double.POSITIVE_INFINITY;
			else return Double.NEGATIVE_INFINITY;
		} else if (power == Double.NEGATIVE_INFINITY) {
			if (number >= 0) return 0.0;
			else if (number < 0) return -0.0;
		} else if (number == 1) return number;
		else if (power == 0) return 1.0;
		else if (number == 0) return 0.0;
		
		SlashedDouble sdnum = new SlashedDouble(number);
		SlashedDouble sdpow = new SlashedDouble(power, true);

		SlashedDouble ipwr;
		
		if (sdpow.getIntRaw().length() > 0) {
			ipwr = intPower(sdnum, sdpow);
		} else {
			ipwr = new SlashedDouble(1.0);
		}
		if (ipwr.getIEEE754() == Double.POSITIVE_INFINITY || ipwr.getIEEE754() == Double.NEGATIVE_INFINITY)
			return ipwr.getIEEE754();
			
		SlashedDouble fpwr;
		
		if (sdpow.getFractRaw().length() > 0) {
			if (number < 0) return Double.NaN;
			fpwr = fractPower(sdnum, sdpow);
		} else {
			fpwr = new SlashedDouble(1.0);
		}
		
		if (fpwr.getIEEE754() == Double.POSITIVE_INFINITY || fpwr.getIEEE754() == Double.NEGATIVE_INFINITY)
			return fpwr.getIEEE754();
			
		return innerMult(ipwr, fpwr, ipwr.getNegativeSign()).getIEEE754();
	}
	
	private static SlashedDouble intPower(SlashedDouble number, SlashedDouble power) {
		int ipwr = (int) power.getIntMantissaInteger();
		SlashedDouble result = new SlashedDouble(1.0);
		
		if (number.getIEEE754() == -1) {
			if (ipwr % 2 == 0) return result;
			else return new SlashedDouble(-1.0);
		}
		
		for (int i = 0; i < intPwr.length; i++) {
			if ((ipwr & intPwr[i]) == intPwr[i]) {
				result = innerMult(number, result, "");
			}
			result = innerMult(result, result, "");
		}
		
		if ((ipwr & 1) == 1) {
			result = innerMult(number, result, "");
		}

		if (number.isNegative() && (ipwr % 2 == 1))	result.setSign("-");
		
		return result;
	}
	
	public static Double mult(double number1, double number2) {
		if (number1 == Double.NaN || number2 == Double.NaN) {
			return Double.NaN;
		} else if (number1 == Double.POSITIVE_INFINITY) {
			if (number2 == Double.NEGATIVE_INFINITY) return Double.NEGATIVE_INFINITY;
			else if (number2 == 0.0 || number2 == -0.0) return Double.NaN;
			else return Double.POSITIVE_INFINITY;
		} else if (number1 == Double.NEGATIVE_INFINITY) {
			if (number2 == Double.NEGATIVE_INFINITY) return Double.POSITIVE_INFINITY;
			else if (number2 == 0.0 || number2 == -0.0) return Double.NaN;
			else return Double.NEGATIVE_INFINITY;
		} else if (number1 == 0.0) {
			if (number2 == Double.NEGATIVE_INFINITY || number2 == Double.POSITIVE_INFINITY) return Double.NaN;
			else if (number2 == -0.0) return -0.0;
			else return 0.0;
		} else if (number1 == -0.0) {
			if (number2 == Double.NEGATIVE_INFINITY || number2 == Double.POSITIVE_INFINITY) return Double.NaN;
			else if (number2 == -0.0) return 0.0;
			else return -0.0;
		} else if (number1 == 1) {
			return number2;
		} else if (number2 == 1) {
			return number1;
		} else if (number1 == 0xffffffff) {
			return -number2;
		} else if (number2 == 0xffffffff) {
			return -number1;
		}
		
		SlashedDouble sd1 = new SlashedDouble(number1);
		SlashedDouble sd2 = new SlashedDouble(number2);
		String negativesign;
		
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
				}
				result += unit;
				shift = 0;
			}
		}
		String mantissa = Long.toBinaryString(result);
		int resultexp = getMultExponent(mantissa, number1.getExp(), number2.getExp(), number1.getBinaryRaw(), number2.getBinaryRaw(), biasresult);
		return new SlashedDouble(mantissa, resultexp, negativesign, result);
	}
	
	private static int getMultExponent(String fraction, int exp1, int exp2, String num1raw, String num2raw, int biasresult) {
		int fractdigitsnum = num1raw.length() + num2raw.length() + 0xfffffffe;
		return fraction.length() + ~fractdigitsnum + exp1 + exp2 + biasresult;
	}
	
	private static int getRootExponent(int exp) {
		if (exp == 0xffffffff || exp == 0x00000000) {
			return exp;
		} else {
			return (exp/2);
		}
	}
	
	private static SlashedDouble fractPower(SlashedDouble number, SlashedDouble degree) {
		String degreeraw = degree.getFractRaw();
		int degreeexp = degree.getExp();
		SlashedDouble result = new SlashedDouble(1.0);
		for (int i = 0xffffffff; i > degreeexp; i--) {
			number = innerRoot(number);
		} 
		for (int i = 0, counter = 1; i < degreeraw.length(); i++, counter++) {
			number = innerRoot(number);
			if (degreeraw.charAt(i) == '1') {
				result = innerMult(number, result, "");
			}
		}
		return result;
	}
	
	private static SlashedDouble innerRoot(SlashedDouble number) {
		String numraw = number.getBinaryRaw(), residualstr;
		long minuend = 0l, subtrahend, residual;
		if (number.getIntRaw().length() % 2 == 0) {
			if (numraw.substring(0, 2).equals("10")) {
				numraw = "01" + numraw.substring(2);
			} else {
				numraw = "10" + numraw.substring(2);
			}
		} else {
			numraw = "00" + numraw.substring(1);
		}
		Long result = 1l;
		String subzeros = "";
		for (int i = numraw.length(); i < 127; i++) {
			numraw += "0";
		}
		for (int left = 0, right = 4; left < 60; left++, right += 2) {
			result <<= 1;
			if (numraw.charAt(left) == '1' || numraw.charAt(left+1) == '1') {
				minuend = Long.parseLong(numraw.substring(left, right), 2);
				subtrahend = result << 1;
				residual = minuend + ~subtrahend;
				if (residual >= 0) {
					residualstr = Long.toBinaryString(residual);
					subzeros = "";
					for (int i = 0, len = right + ~residualstr.length() + 1; i < len; i++) {
						subzeros += "0";
					}
					numraw = subzeros + residualstr + numraw.substring(right);
					result++;
				}
			}
		}
		
		// next "if's" are made for additional precission
		result <<= 1;
		//out.println(numraw);
		if (numraw.charAt(60) == '1') {
			minuend = Long.parseUnsignedLong(numraw.substring(60, 124), 2);
			subtrahend = result << 1;
			residual = minuend + ~subtrahend;
			residualstr = Long.toBinaryString(residual);
			subzeros = "";
			for (int i = residualstr.length(); i < 124; i++) {
				subzeros += "0";
			}
			numraw = subzeros + residualstr + numraw.substring(124);
			result++;
		} else if (numraw.charAt(61) == '1') {
			minuend = Long.parseLong(numraw.substring(60, 124), 2);
			subtrahend = result << 1;
			residual = minuend + ~subtrahend;
			if (residual >= 0) {
				residualstr = Long.toBinaryString(residual);
				subzeros = "";
				for (int i = residualstr.length(); i < 124; i++) {
					subzeros += "0";
				}
				numraw = subzeros + residualstr + numraw.substring(124);
				result++;
			}
		}	
		result <<= 1;
		//out.println(numraw);
		if (numraw.charAt(60) == '1') {
			minuend = Long.parseUnsignedLong(numraw.substring(60, 124), 2);
			subtrahend = result >>> 1;
			residual = minuend + ~subtrahend;
			residualstr = Long.toBinaryString(residual);
			subzeros = "";
			for (int i = residualstr.length(); i < 124; i++) {
				subzeros += "0";
			}
			numraw = subzeros + residualstr + numraw.substring(124);
			result++;
		} else if (numraw.charAt(61) == '1') {
			minuend = Long.parseUnsignedLong(numraw.substring(61, 125), 2);
			subtrahend = result;
			residual = minuend + ~subtrahend;
			residualstr = Long.toBinaryString(residual);
			subzeros = "";
			for (int i = residualstr.length(); i < 125; i++) {
				subzeros += "0";
			}
			numraw = subzeros + residualstr + numraw.substring(125);
			result++;
		} else if (numraw.charAt(62) == '1') {
			minuend = Long.parseUnsignedLong(numraw.substring(62, 126), 2);
			subtrahend = result << 1;
			residual = minuend + ~subtrahend;
			if (residual >= 0) {
				residualstr = Long.toBinaryString(residual);
				subzeros = "";
				for (int i = residualstr.length(); i < 126; i++) {
					subzeros += "0";
				}
				numraw = subzeros + residualstr + numraw.substring(126);
				result++;
			}
			residualstr = Long.toBinaryString(residual);
		}
		result <<= 1;
		if (numraw.charAt(60) == '1' || numraw.charAt(61) == '1' || numraw.charAt(62) == '1') {
			result++;
		} else if (numraw.charAt(63) == '1') {
			minuend = Long.parseUnsignedLong(numraw.substring(63, 127), 2);
			subtrahend = result;
			residual = minuend + ~subtrahend;
			if (residual >= 0) {
				result++;
			}
		} 
		int resultexp = getRootExponent(number.getExp());
		return new SlashedDouble(result, resultexp, "", true);
	}
	
	public static void main(String[] args) {
		//out.println(pow(15, 0.5));
		long l1 = Long.parseUnsignedLong("1111111111111111111111111111111111111111111111111111111111111111",2),
			l2 = Long.parseLong("-011111111111111111111111111111111111111111111111111111111111110",2);
		out.println(l1-l2);
		
		SlashedDouble sd = new SlashedDouble(4.0, true);
		
		/*
		double num = Math.random()*10000, power = Math.floor(Math.random()*50);
			num = -num;
			double result1 = pow(num, power), result2 = Math.pow(num, power);
			String[] stripes = Double.toHexString(num).split("[.p]");
			out.println(stripes[0]);
			out.println(power);
			out.println(result1);
				out.println(result2);
			*/
			
		for (int i = 0; i < 1000; i++) {
			double num = Math.random()*10000, power = Math.floor(Math.random()*50);
			if (i % 2 == 0) num = -num;
			double result1 = pow(num, power), result2 = Math.pow(num, power);
			if (result1 != result2) {
				out.println(result1);
				out.println(result2);
			}
		}
	}
	

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/* For testing proposes
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
	*/
}