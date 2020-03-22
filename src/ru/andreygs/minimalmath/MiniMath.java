package ru.andreygs.minimalmath;

import static java.lang.System.*;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class MiniMath {
	private static long[] intPwr = 
	{
		1073741824l, 536870912l, 268435456l, 134217728l, 67108864l, 
		33554432l, 16777216l, 8388608l, 4194304l, 2097152l, 1048576l, 524288l, 
		262144l, 131072l, 65536l, 32768l, 16384l, 8192l, 4096l, 2048l,
		1024l, 512l, 256l, 128l, 64l, 32l, 16l, 8l, 4l, 2l
	};
	
	public static Double pow(double number, double power) {
		SlashedDouble sdnum = new SlashedDouble(number);
		SlashedDouble sdpow = new SlashedDouble(power, true);
		
		return pow(sdnum, sdpow).getIEEE754();
	}
	
	public static SlashedDouble pow(SlashedDouble sdnum, SlashedDouble sdpow) {
		double number = sdnum.getDouble(), power = sdpow.getDouble();
		
		if (sdpow.getFractRaw().length() > 0 && number < 0) return new SlashedDouble(Double.NaN);
		
		if (Double.isNaN(power) || Double.isNaN(number)) {
			return new SlashedDouble(Double.NaN);
		} else if (number == Double.POSITIVE_INFINITY || power == Double.POSITIVE_INFINITY) {
			return new SlashedDouble(Double.POSITIVE_INFINITY);
		} else if (number == Double.NEGATIVE_INFINITY) {
			if ((int) power % 2 == 0) return new SlashedDouble(Double.POSITIVE_INFINITY);
			else return new SlashedDouble(Double.NEGATIVE_INFINITY);
		} else if (power == Double.NEGATIVE_INFINITY) {
			if (number >= 0) return new SlashedDouble(0.0);
			else if (number < 0) return new SlashedDouble(-0.0);
		} else if (number == 1) return sdnum;
		else if (power == 0.0) return new SlashedDouble(1.0);
		else if (power == 1.0) return sdnum;
		else if (number == 0.0) return new SlashedDouble(0.0);
		else if (number == -0.0) return new SlashedDouble(-0.0);
		
		SlashedDouble ipwr;
		
		if (sdpow.getExp() > 0xffffffff) {
			if (sdpow.getExp() > 30) {
				if (sdnum.getExp() < 0) {
					if (number < 0) return new SlashedDouble(-0.0);
					else return new SlashedDouble(0.0);
				} else {
					if (number < 0 && sdpow.isOdd()) return new SlashedDouble(Double.NEGATIVE_INFINITY);
					else return new SlashedDouble(Double.POSITIVE_INFINITY);
				}
			}
			ipwr = intPower(sdnum, sdpow);
			
			if (ipwr.isDone()) return ipwr;
		} else {
			ipwr = new SlashedDouble(1.0);
		}
		if (ipwr.getIEEE754() == Double.POSITIVE_INFINITY || ipwr.getIEEE754() == Double.NEGATIVE_INFINITY)
			return ipwr;
			
		SlashedDouble fpwr;
		
		if (sdpow.getFractRaw().length() > 0) {
			fpwr = fractPower(sdnum, sdpow);
		} else {
			fpwr = new SlashedDouble(1.0);
		}
		
		if (fpwr.getIEEE754() == Double.POSITIVE_INFINITY || fpwr.getIEEE754() == Double.NEGATIVE_INFINITY || fpwr.getIEEE754() == Double.NaN) {
			return fpwr;
		} else if (fpwr.getIEEE754() == 1.0) {
			return ipwr;
		} else if (fpwr.getIEEE754() == 0.0) {
			return new SlashedDouble(0.0);
		}
		
		return innerMult(ipwr, fpwr, ipwr.getNegativeSign());
	}
	
	private static SlashedDouble intPower(SlashedDouble number, SlashedDouble power) {
		long ipwr = power.getIntSD().getIEEE754().longValue();
		//out.println(power.getIntSD().getBinaryRaw());
		SlashedDouble result = new SlashedDouble(1.0);

		if (number.getIEEE754() == -1) {
			if (ipwr % 2 == 0) return result;
			else return new SlashedDouble(-1.0);
		}
		
		for (int i = 0; i < intPwr.length; i++) {
			if ((ipwr & intPwr[i]) == intPwr[i]) {
				result = innerMult(number, result, "");
				if (result.isDone()) {
					if (isNegative(number, power)) return new SlashedDouble(-result.getIEEE754(), 1);
					return result;
				}
			}
			result = innerMult(result, result, "");
			if (result.isDone()) {
				if (isNegative(number, power)) return new SlashedDouble(-result.getIEEE754(), 1);
				return result;
			}
		}
		
		if ((ipwr & 1l) == 1l) {
			result = innerMult(number, result, "");
		}

		if (isNegative(number, power)) result.setSign("-");
		
		return result;
	}
	
	private static boolean isNegative(SlashedDouble number, SlashedDouble power) {
		if (number.isNegative() && power.isOdd()) return true;
		
		return false;
	}
	
	public static Double mult(double number1, double number2) {
		SlashedDouble sdnum1 = new SlashedDouble(number1);
		SlashedDouble sdnum2 = new SlashedDouble(number2);
		
		
		return mult(sdnum1, sdnum2).getIEEE754();
	}
	
	public static SlashedDouble mult(SlashedDouble sdnum1, SlashedDouble sdnum2) {
		double number1 = sdnum1.getDouble(), number2 = sdnum2.getDouble();
		
		if (Double.isNaN(number1) || Double.isNaN(number2)) {
			return new SlashedDouble(Double.NaN);
		} else if (number1 == Double.POSITIVE_INFINITY) {
			if (number2 == Double.NEGATIVE_INFINITY) return new SlashedDouble(Double.NEGATIVE_INFINITY);
			else if (number2 == 0.0 || number2 == -0.0) return new SlashedDouble(Double.NaN);
			else return new SlashedDouble(Double.POSITIVE_INFINITY);
		} else if (number1 == Double.NEGATIVE_INFINITY) {
			if (number2 == Double.NEGATIVE_INFINITY) return new SlashedDouble(Double.POSITIVE_INFINITY);
			else if (number2 == 0.0 || number2 == -0.0) return new SlashedDouble(Double.NaN);
			else return new SlashedDouble(Double.NEGATIVE_INFINITY);
		} else if (number1 == 0.0) {
			if (number2 == Double.NEGATIVE_INFINITY || number2 == Double.POSITIVE_INFINITY) return new SlashedDouble(Double.NaN);
			else if (number2 == -0.0) return new SlashedDouble(-0.0);
			else return new SlashedDouble(0.0);
		} else if (number1 == -0.0) {
			if (number2 == Double.NEGATIVE_INFINITY || number2 == Double.POSITIVE_INFINITY) return new SlashedDouble(Double.NaN);
			else if (number2 == -0.0) return new SlashedDouble(0.0);
			else return new SlashedDouble(-0.0);
		} else if (number1 == 1) {
			return sdnum2;
		} else if (number2 == 1) {
			return sdnum1;
		} else if (number1 == 0xffffffff) {
			return new SlashedDouble(-number2);
		} else if (number2 == 0xffffffff) {
			return new SlashedDouble(-number1);
		}
		
		String negativesign;
		
		if ((number1 >= 0 && number2 >= 0) || (number1 < 0 && number2 < 0)) {
			negativesign = "";
		} else {
			negativesign = "-";
		}
		
		return innerMult(sdnum1, sdnum2, negativesign);
	}
	
	private static SlashedDouble innerMult(SlashedDouble number1, SlashedDouble number2, String negativesign) {
		long result = 0l, unit;
		String num2raw;
		
		if (number1.onesEnum() < number2.onesEnum()) {
			unit = number2.getLongRaw();
			num2raw = number1.getBinaryRaw();
		} else {
			unit = number1.getLongRaw();
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
						if (Long.numberOfTrailingZeros(unit) == 0) carry = 1;
						unit >>>= 1;
						unit += carry;
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

		String raw = Long.toBinaryString(result);
		
		int resultexp = getMultExponent(raw, number1.getExp(), number2.getExp(), number1.getBinaryRaw(), number2.getBinaryRaw(), biasresult);

		if (resultexp == 0) {
			if ((number1.getBinaryRaw().length() > 63 && number1.getBinaryRaw().substring(0, 64).equals( "111111111111111111111111111111111111111111111111111111111111111")) || (number2.getBinaryRaw().length() > 63 && number2.getBinaryRaw().substring(0, 64).equals( "111111111111111111111111111111111111111111111111111111111111111"))) {
				if (negativesign.equals("-")) return new SlashedDouble(-1.0, 1);
				else return new SlashedDouble(1.0, 1); 
			}
				/*else if (raw.equals("1") && number1.getExp() < 0 && number2.getExp() > 0) { 
				if (negativesign.equals("-")) return new SlashedDouble(-0.0, 1);
				else return new SlashedDouble(0.0, 1);
			}*/
		} else if (resultexp > 1024) {
			if (negativesign.equals("-")) return new SlashedDouble(Double.NEGATIVE_INFINITY, 1);
			else return new SlashedDouble(Double.POSITIVE_INFINITY, 1);
		} else if (resultexp < -1075) {
			if (negativesign.equals("-")) return new SlashedDouble(-0.0, 1);
			else return new SlashedDouble(0.0, 1);
		}
		
		return new SlashedDouble(raw, resultexp, negativesign, result);
	}

	private static int getMultExponent(String raw, int exp1, int exp2, String num1raw, String num2raw, int biasresult) {
		int fractdigitsnum = num1raw.length() + num2raw.length() + 0xfffffffe;

		return raw.length() + ~fractdigitsnum + exp1 + exp2 + biasresult;
	}
	
	private static int getRootExponent(int exp) {
		if (exp == 0xffffffff || exp == 0x00000000) {
			return exp;
		} else {
			if (exp % 2 == -1) return exp/2 + 0xffffffff;
			else return exp/2;
		}
	}

	private static SlashedDouble fractPower(SlashedDouble number, SlashedDouble power) {
		String powerraw = power.getFractRaw();
		int powerexp = power.getExp();
		SlashedDouble result = new SlashedDouble(1.0);
		//double test = 0.015719639933614228;
		//double tresult = 1.0;
		
		for (int i = 0xffffffff; i > powerexp; i--) {
			number = innerRoot(number);
			//out.println(number.getIEEE754() + "*");
			//test = Math.pow(test, 0.5);
			//out.println(test);
			if (number.isDone()) return number;
		} 
		for (int i = 0; i < powerraw.length(); i++) {
			number = innerRoot(number);
			//out.println(number.getIEEE754() + "*");
			//test = Math.pow(test, 0.5);
			//out.println(test);
			if (powerraw.charAt(i) == '1') {
				result = innerMult(number, result, "");
				//out.println(result.getIEEE754() + "&");
				//tresult = tresult * test;
				//out.println(tresult);
				if (number.isDone()) return result;
			}
		}
		
		return result;
	}
	
	private static SlashedDouble innerRoot(SlashedDouble number) {
		String numraw = number.getBinaryRaw(), residualstr;
		long minuend = 0l, subtrahend, residual;
		if (!number.isOddIntDigitsNum()) {
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
		
		/*
		String resultraw = Long.toBinaryString(result);
		if (resultexp == 0 && 
			(resultraw.length() > 63 && resultraw.substring(0, 64).equals( "111111111111111111111111111111111111111111111111111111111111111"))) {
			return new SlashedDouble(1.0, 1);
		} else if (resultexp < -1075) {
			return new SlashedDouble(0.0, 1);
		} else { */
			return new SlashedDouble(result, resultexp, "", true);
		//}
	}
	
	public static Double floor(double number) {
		SlashedDouble sdnum = new SlashedDouble(number);
		
		return floor(sdnum).getIEEE754(); 
	}
	
	public static SlashedDouble floor(SlashedDouble sdnum) {
		double number = sdnum.getIntSD().getIEEE754();
		//out.println(number + "!");
		if (Double.isNaN(number) || number == Double.POSITIVE_INFINITY || 
			number == Double.NEGATIVE_INFINITY || sdnum.getNegativeSign().isEmpty())
			return sdnum.getIntSD();
		else {
			//out.println(number + "!");
			if (sdnum.getFractRaw().length() > 0) return new SlashedDouble(number + 0xffffffff);
			else return sdnum.getIntSD();
		}
		/*
		if (Double.isNaN(number) || number == Double.POSITIVE_INFINITY || 
			number == Double.NEGATIVE_INFINITY )
			return sdnum;
		
		String negativesign = sdnum.getNegativeSign();
		String raw = sdnum.getBinaryRaw();
		
		if (sdnum.getExp() < 0) {
			if (negativesign.equals("")) return new SlashedDouble(0.0);
			else return new SlashedDouble(-1.0);
		} else if (sdnum.getExp() >= (raw.length() + 0xffffffff)) {
			return sdnum;
		} else {
			if (negativesign.equals("")) 
				return new SlashedDouble(raw.substring(0, sdnum.getExp()+1), sdnum.getExp(), "");
			else {
				long longraw = Long.parseLong(raw.substring(0, sdnum.getExp()+1), 2);
				longraw++;
				return new SlashedDouble(Long.toBinaryString(longraw), sdnum.getExp(), "");
			}	
		}*/
	}
	
	// this method returns value of fractional part that is closest to zero
	public static Double fraction(double number) {
		SlashedDouble sdnum = new SlashedDouble(number);
		
		return fraction(sdnum).getIEEE754(); 
	}
	
	public static SlashedDouble fraction(SlashedDouble sdnum) {

		return sdnum.getFractSD();
		/*
		if (Double.isNaN(number) || sdnum.getExp() < 0)
			return sdnum;
		else if (number == Double.POSITIVE_INFINITY || number == Double.NEGATIVE_INFINITY || 
			number == 0.0 || number == -0.0 || sdnum.getExp() >= (sdnum.getBinaryRaw().length() + 0xffffffff))
			return new SlashedDouble(0.0);
		else {
			return new SlashedDouble(sdnum.getBinaryRaw().substring(sdnum.getExp()+1), 0, "");
		}*/
	}
	
	public static void main(String[] args) {
		
		//double result1 = pow(2.416702254629639E-36, 6.726006230511679E-25), result2 = Math.pow(2.416702254629639E-36, 6.726006230511679E-25);
		//double result1 = pow(7.667063751883245E30, 0.9989203174209581), result2 = Math.pow(7.667063751883245E30, 0.9989203174209581);

		//out.println(result1);
		//out.println(result2);
		//0.12537798823403662

//2.416702254629639E-36!
//6.726006230511679E-25
//0.0
//1.0



		//out.println(Math.pow(1.723877541351934E-8, 2));
		//out.println(pow(1.723877541351934E-8, 2));
		//out.println(Long.toBinaryString(Double.doubleToLongBits(-8.26433735220462E35)) + "$$$");
		//out.println(Long.toBinaryString(Double.doubleToLongBits(result1)));
		//out.println(Long.toBinaryString(Double.doubleToLongBits(result2)));
		//out.println(Math.pow(7.667063751883245E30, 0.25));
		//out.println(Math.pow(7.667063751883245E30, 0.125));
		//out.println(Math.pow(7.667063751883245E30, 0.0625));
		//out.println(Math.round)
		
		
		//out.println(Long.toBinaryString(Double.doubleToLongBits(result1)));
		//out.println(Long.toBinaryString(Double.doubleToLongBits(result2)) + "!!!!");
		
		double factor1 = 1000000000000000000000000000000000000000.0, factor2 = 1000000000000000000000000000000000000000.0;
		int counter = 0;
		for (int i = 0; i < 80; i++) {
			factor1 = factor1 / 10; out.println(factor1 + " power random factor");
			out.println("");
			factor2 = 1000000000000000000000000000000000000000.0;
			for (int j = 0; j < 8000; j++) {
				if (j % 100 == 0) factor2 = factor2 / 10;
				double num = Math.random()*factor2, power = Math.floor(Math.random()*factor1);
				if (j % 2 == 0) num = -num;
				double result1 = pow(num, power), result2 = Math.pow(num, power);
				if (result1 != result2 && (!Double.isNaN(result1) && !Double.isNaN(result1))) {
					if ((Double.toString(result1).length() < 14 || Double.toString(result2).length() < 14 || !Double.toString(result1).substring(0, 14).equals(Double.toString(result2).substring(0, 14))) && !(result1 == 1.0 && result2 == 1.0000000000000002)) {
					out.println(num + "!");
					out.println(power);
					out.println(result1);
					out.println(result2);
					out.println(Long.toBinaryString(Double.doubleToLongBits(power)));
					out.println(Double.toHexString(power));
					counter++;
					}
				}
			}
		}
		
		out.println(counter + " results of integer number powers that have missed accuracy");



		factor1 = 1000000000000000000000000000000000000000.0;
		factor2 = 1000000000000000000000000000000000000000.0;
		counter = 0;
		for (int i = 0; i < 80; i++) {
			factor1 = factor1 / 10; out.println(factor1 + " power random factor");
			out.println("");
			factor2 = 1000000000000000000000000000000000000000.0;
			for (int j = 0; j < 8000; j++) {
				if (j % 100 == 0) factor2 = factor2 / 10;
				double num = Math.random()*factor2, power = Math.random()*factor1;
				if (j % 2 == 0) num = -num;
				double result1 = pow(num, power), result2 = Math.pow(num, power);
				if (result1 != result2 && (!Double.isNaN(result1) && !Double.isNaN(result1))) {
					if ((Double.toString(result1).length() < 14 || Double.toString(result2).length() < 14 || !Double.toString(result1).substring(0, 14).equals(Double.toString(result2).substring(0, 14))) && !(result1 == 1.0 && result2 == 1.0000000000000002)) {
					out.println(num + "!");
					out.println(power);
					out.println(result1);
					out.println(result2);
					out.println(Long.toBinaryString(Double.doubleToLongBits(power)));
					out.println(Double.toHexString(power));
					counter++;
					}
				}
			}
		}
		
		out.println(counter + " results of real number powers that have missed accuracy");
		
		
		
		/*
		out.println(Double.toHexString(Double.MIN_NORMAL - Double.MIN_VALUE));
		double rnd = Math.random() * 6565.0;
		double rnd2 = Math.random() * 12412.0;
		SlashedDouble sd1 = new SlashedDouble(rnd);
		SlashedDouble sd2 = new SlashedDouble(rnd2);
		out.println(mult(rnd1, rnd2));
		out.println(rnd * rnd2);
		
		out.println(floor(0.0));
		out.println(Math.floor(0.0));
		out.println(floor(-0.0));
		out.println(Math.floor(-0.0));
		out.println(floor(Double.POSITIVE_INFINITY));
		out.println(Math.floor(Double.POSITIVE_INFINITY));
		out.println(floor(Double.NEGATIVE_INFINITY));
		out.println(Math.floor(Double.NEGATIVE_INFINITY));
		out.println(floor(Double.NaN));
		out.println(Math.floor(Double.NaN));
		out.println(floor(234234787897892.57897889890));
		out.println(Math.floor(234234787897892.57897889890));
		out.println(floor(-0.2));
		out.println(Math.floor(-0.2));
		
		
		
		out.println(fraction(0.0));
		out.println(fraction(-0.0));
		out.println(fraction(Double.POSITIVE_INFINITY));
		out.println(fraction(Double.NEGATIVE_INFINITY));
		out.println(fraction(Double.NaN));
		out.println(fraction(43534.534580));
		//out.println(Double.toHexString(234234.578));
		out.println(fraction(-0.2));
		out.println(fraction(1.0));
		out.println(fraction(-1.0));
		*/
	}
	
	/*
	private static double roundResult(SlashedDouble number) throws NullPointerException {
		String raw = number.getBinaryRaw();
		if (raw == null || raw.length() < 54) return number.getIEEE754();
		
		SlashedDouble a = new SlashedDouble(raw.substring(0, 53), number.getExp(), "");
		long longraw = Long.parseUnsignedLong(raw.substring(0, 53), 2);
		longraw++;
		String longb = Long.toBinaryString(longraw);
		int expb = number.getExp();
		if (longb.length() == 54) {
			longb = "1";
			expb++;
		}
		
		SlashedDouble b = new SlashedDouble(longb, expb, "");
		SlashedDouble square = innerMult(number, number, "");
		SlashedDouble product = innerMult(a, b, "");
		String rawsquare = square.getBinaryRaw();
		String rawproduct = product.getBinaryRaw();
		
		if (square.isDone()) {
			if ((square.getIEEE754() == Double.POSITIVE_INFINITY &&  b.getIEEE754() != Double.POSITIVE_INFINITY) || square.getIEEE754() == 0.0 || square.getIEEE754() == -0.0) {
				b.setSign(number.getNegativeSign());
				return b.getIEEE754();
			} else if (b.getIEEE754() == Double.POSITIVE_INFINITY) {
				a.setSign(number.getNegativeSign());
				return a.getIEEE754();
			}
		}
		
		
		for (int i = rawsquare.length(); i < 64; i++) rawsquare += '0';
		for (int i = rawproduct.length(); i < 64; i++) rawproduct += '0';
		long longrawsquare = Long.parseUnsignedLong(rawsquare, 2);
		long longrawproduct = Long.parseUnsignedLong(rawproduct, 2);
		
		if (longrawsquare > longrawproduct || square.getExp() > product.getExp()) {
			b.setSign(number.getNegativeSign());
			return b.getIEEE754();
		} else {
			a.setSign(number.getNegativeSign());
			return a.getIEEE754();
		}
	}*/
}