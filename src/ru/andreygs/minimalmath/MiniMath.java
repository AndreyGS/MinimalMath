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
		if (sdpow.getFractRaw().length() > 0 && number < 0) return Double.NaN;
		
		SlashedDouble ipwr;
		
		if (sdpow.getIntRaw().length() > 0) {
			ipwr = intPower(sdnum, sdpow);
			if (ipwr.isDone()) return ipwr.getIEEE754();
		} else {
			ipwr = new SlashedDouble(1.0);
		}
		if (ipwr.getIEEE754() == Double.POSITIVE_INFINITY || ipwr.getIEEE754() == Double.NEGATIVE_INFINITY)
			return ipwr.getIEEE754();
			
		SlashedDouble fpwr;
		
		if (sdpow.getFractRaw().length() > 0) {
			fpwr = fractPower(sdnum, sdpow);
		} else {
			fpwr = new SlashedDouble(1.0);
		}
		
		if (fpwr.getIEEE754() == Double.POSITIVE_INFINITY || fpwr.getIEEE754() == Double.NEGATIVE_INFINITY || fpwr.getIEEE754() == Double.NaN || ipwr.getIEEE754() == 1.0) {
			return fpwr.getIEEE754();
		} else if (fpwr.getIEEE754() == 1.0) {
			return ipwr.getIEEE754();
		}
			
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
				//out.println(result.getIEEE754());
				if (result.isDone()) {
					if (isNegative(number, power)) return new SlashedDouble(-number.getIEEE754(), 1);
					return result;
				}
			}
			result = innerMult(result, result, "");
			if (result.isDone()) {
				if (isNegative(number, power)) return new SlashedDouble(-number.getIEEE754(), 1);
				return result;
			}
			//out.println(result.getIEEE754());
		}
		
		if ((ipwr & 1) == 1) {
			result = innerMult(number, result, "");
		}

		if (isNegative(number, power)) result.setSign("-");
		
		return result;
	}
	
	private static boolean isNegative(SlashedDouble number, SlashedDouble power) {
		if (number.isNegative() && power.onesEnum() % 2 == 1) return true;
		
		return false;
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

		for (int i = num2raw.length() + 0xffffffff, shift = 0, check, spaceneed, leadzeroes, carry; i > 0xffffffff; i+= 0xffffffff, shift++) {
			String test;
			if (num2raw.charAt(i) == '1') {
				leadzeroes = Long.numberOfLeadingZeros(unit);
				check = leadzeroes + ~shift;
				if (check > 0xffffffff) {
					unit <<= shift;
				} else {
					carry = 0;
					if (leadzeroes == 0) {
						unit >>>= 1;
						unit++;
						spaceneed = ~check + 1;
						if (result > 0) {
							if (Long.numberOfTrailingZeros(result) == 0) carry = 1;
							result >>>= 1;
							result += carry;
						}
						biasresult += shift + 1;
					} else {
						unit <<= leadzeroes + 0xffffffff;
						spaceneed = ~check + 1;
						test = Long.toBinaryString(result);
						if (test.length() >= spaceneed) {
							if (test.charAt(test.length()+check) == '1') carry = 1;
						}
						result >>>= spaceneed;
						result += carry;
						biasresult += spaceneed;
					}
				}
				result += unit;
				shift = 0;
			}
		}
		String mantissa = Long.toBinaryString(result);
		int resultexp = getMultExponent(mantissa, number1.getExp(), number2.getExp(), number1.getBinaryRaw(), number2.getBinaryRaw(), biasresult);
		if (resultexp == 0 && 
			((number1.getBinaryRaw().length() > 62 && number1.getBinaryRaw().substring(0, 63).equals( "111111111111111111111111111111111111111111111111111111111111111")) || (number2.getBinaryRaw().length() > 62 && number2.getBinaryRaw().substring(0, 62).equals( "111111111111111111111111111111111111111111111111111111111111111")))) {
			if (negativesign.equals("-")) return new SlashedDouble(-1.0, 1);
			else return new SlashedDouble(1.0, 1); 
		} else if (resultexp > 1024) {
			if (negativesign.equals("-")) return new SlashedDouble(Double.NEGATIVE_INFINITY, 1);
			else return new SlashedDouble(Double.POSITIVE_INFINITY, 1);
		} else if (resultexp < -1075) {
			if (negativesign.equals("-")) return new SlashedDouble(-0.0, 1);
			else return new SlashedDouble(0.0, 1);
		} else {
			return new SlashedDouble(mantissa, resultexp, negativesign, result);
		}
	}
	
	private static int getMultExponent(String fraction, int exp1, int exp2, String num1raw, String num2raw, int biasresult) {
		int fractdigitsnum = num1raw.length() + num2raw.length() + 0xfffffffe;
		return fraction.length() + ~fractdigitsnum + exp1 + exp2 + biasresult;
	}
	
	private static int getRootExponent(int exp) {
		if (exp == 0xffffffff || exp == 0x00000000) {
			return exp;
		} else {
			if (exp % 2 == -1) return exp/2 + 0xffffffff;
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
		if ((number.getIntRaw().length() % 2 == 0 && number.getIntRaw().length() > 0) ||
			(number.getIntRaw().length() == 0 && ((~number.getExp() + 1) % 2 == 1))) {
			if (numraw.substring(0, 2).equals("10")) {
				numraw = "01" + numraw.substring(2);
			} else {
				//out.println("!");
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
		//out.println(l1-l2);
		
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
		/*	
		double number = 23423;
		out.println(Long.toBinaryString(Double.doubleToLongBits(0.028132614678155754)));
		out.println(Long.toBinaryString(Double.doubleToLongBits(0.5571573247888875)));
		double result1 = pow(0.028132614678155754, 0.5571573247888875), result2 = Math.pow(0.028132614678155754, 0.5571573247888875);
		out.println(result1);
		out.println(result2);
		*/
		//out.println(pow(0.028132614678155754, 0.5));
		//out.println(Math.pow(0.028132614678155754, 0.5));
		// 1111111001 1100 1100 1110 1100 1011 1100 1111 1111 1001 0111 1111 0000 0000 number
		// 1111111110 0001 1101 0100 0011 1011 1001 1001 0001 0110 0011 1110 0111 0000 power
		
		// 		    1 1100 1100 1110 1100 1011 1100 1111 1111 1001 0111 1111 raw
		//          1 1110 0101 1100 1010 1010 0110 0010 1000 0100 0010 1010 0011 0100 00100110
		//1111111100    0101 0111 1000 0001 1010 1100 0010 0111 0000 1100 1111 1101 1111
		//out.println(Long.toBinaryString(Double.doubleToLongBits(0.16772779936002188)));
		//out.println(mult(1237.1208424536674, 1530467.9788332717));
		//out.println(1237.1208424536674 * 1530467.9788332717);
		double factor = 1;
		
		for (int i = 0; i < 26000; i++) {
			if (i % 1000 == 0) factor = factor / 10;
			double num = Math.random()*1000000000000l, power = Math.random()*10000000000000.0;
			//power = power - Math.floor(power);
			if (i % 2 == 0) num = -num;
			double result1 = pow(num, power), result2 = Math.pow(num, power);
		
			if (result1 != result2 && (!Double.isNaN(result1) && !Double.isNaN(result1))) {
				if (Double.toString(result1).length() < 14 || Double.toString(result2).length() < 14 || !Double.toString(result1).substring(0, 14).equals(Double.toString(result2).substring(0, 14))) {
				out.println(num + "!");
				out.println(power);
				out.println(result1);
				out.println(result2);
				}
			}
		}


		//double db = Double.POSITIVE_INFINITY;
		//SlashedDouble sd1 = new SlashedDouble(-db, 1);
		//out.println(sd1.getIEEE754());
		double result1 = pow(-6.55437551659453E11, 8.47902452279E11), result2 = Math.pow(0.00000021988872393374094, 0.964674847343474);
		out.println(result1);
		//out.println(result2);
		//out.println(Long.toBinaryString(Double.doubleToLongBits(0.00000021988872393374094)));
		//out.println(Long.toBinaryString(Double.doubleToLongBits(0.964674847343474)));
		//1111101000 1101 1000 0011 0101 0001 1010 1011 1001 0101 1110 0010 0101 0101
		//1111111110 1110 1101 1110 1001 1101 1100 1001 0001 0011 1010 0101 1111 1111
		//double result1 = pow(2.7199211094562027E11, 5.675769560298457E13), result2 = Math.pow(2.7199211094562027E11, 5.675769560298457E13);
		//out.println(innerRoot(new SlashedDouble(Long.parseUnsignedLong("1111111111111111111111111111111111111111111111111111110011100110", 2), -1, "", true)).getBinaryRaw());
		//11111111111111111111111111111111111111111111111111111 00111001110
		//double result1 = pow(0.9999999999999999, 0.5);
		//0,0020548474289602383
		//out.println(result1);
		//out.println(result2);
		//out.println(Long.toBinaryString(Double.doubleToLongBits(0.0020548474289602383)));
		//out.println(Long.toBinaryString(Double.doubleToLongBits(4.315309092478779E-18)));
		//11111111101000000000000000000000000000000000000000000000000000
		//11111111110000000000000000000000000000000000000000000000000000
		
	    //1111110110 0000 1101 0101 0101 0011 1101 0000 0010 1110 0101 0001 0001 1010
	    //1111000101 0011 1110 0110 1001 1110 0010 0101 0011 0111 1000 1100 0011 1110
		//-59
	}/*
	2.7199211094562027E11!
5.675769560298457E13
0.0
Infinity
5.4789758999206104E11!
1.0479925172062943E13
0.0
Infinity
9.7585971818138E11!
9.04096518638956E13
0.0
Infinity
8.910836239509121E11!
8.414588627461539E13
0.0
Infinity
6.548181144538467E11!
4.232912010213882E13
0.0
Infinity
*/
//0,00000021988872393374094
	/*
0.19623450306083257!
8.964674847343474
1.7408176330618385E-6
4.570472991215036E-7

*/
/*
0.0020548474289602383!
4.315309092478779E-18
0.75
1.0


0.08387399840840858!
8.566001608712682E-20
51.0
1.0

0.011144110734673774!
3.212831640906799E-20
43.0
1.0


0.07732771947689687!
8.124689083499504E-20
65.0
1.0


0.02356777008058071!
4.186495688784098E-20
55.0
1.0


0.025283644197233692!
8.531014963104989E-20
53.0
1.0

*/


	
	
	//0,000030517578125
	
	
	
	
	
	
	
	
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