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
		SlashedDouble sdpow = new SlashedDouble(power);
		
		return pow(sdnum, sdpow).getIEEE754();
	}
	
	public static SlashedDouble pow(SlashedDouble sdnum, SlashedDouble sdpow) {
		double number = sdnum.getDouble(), power = sdpow.getDouble();
		
		if (sdpow.getFractRaw().length() > 0 && number < 0) return new SlashedDouble(Double.NaN);
		
		if (Double.isNaN(power) || Double.isNaN(number))
			return new SlashedDouble(Double.NaN);
		else if (number == 1) return sdnum;
		else if (power == 0.0) return new SlashedDouble(1.0);
		else if (power == 1.0) return sdnum;
		else if (number == 0.0 && !sdnum.isNegative()) return new SlashedDouble(0.0);
		else if (number == -0.0) return new SlashedDouble(-0.0);
		else if (number == Double.POSITIVE_INFINITY || power == Double.POSITIVE_INFINITY) {
			return new SlashedDouble(Double.POSITIVE_INFINITY);
		} else if (number == Double.NEGATIVE_INFINITY) {
			if (!sdpow.isOdd()) return new SlashedDouble(Double.POSITIVE_INFINITY);
			else return new SlashedDouble(Double.NEGATIVE_INFINITY);
		}
		
		power = abs(power);
		
		SlashedDouble ipwr;
		
		if (sdpow.getExp() > 0xffffffff) {
			if (sdpow.getExp() > 30) {
				if ((sdpow.isNegative() && sdnum.getExp() < 0) ||
					!sdpow.isNegative() && sdnum.getExp() >= 0) {
					if (sdpow.isOdd() && sdnum.isNegative()) 
						return new SlashedDouble(Double.NEGATIVE_INFINITY);
					else return new SlashedDouble(Double.POSITIVE_INFINITY);
				} else {
					if (sdpow.isOdd() && sdnum.isNegative())
						return new SlashedDouble(-0.0);
					return new SlashedDouble(0.0);
				}
			}
			
			ipwr = intPower(sdnum, sdpow);
			
			if (ipwr.getDouble() != null) {
				if (sdpow.isNegative()) return getOppositeExtremum(ipwr);
				else return ipwr;
			}
		} else {
			ipwr = new SlashedDouble(1.0);
		}

		SlashedDouble fpwr;
		
		if (sdpow.getFractRaw().length() > 0) {
			fpwr = fractPower(sdnum, sdpow);
		} else {
			fpwr = new SlashedDouble(1.0);
		}
		
		if (fpwr.getDouble() != null && fpwr.getDouble() != 1.0) {
			if (sdpow.isNegative()) return getOppositeExtremum(fpwr);
			else return fpwr;
		}
		
		SlashedDouble finalproduct = innerMult(ipwr, fpwr, ipwr.getNegativeSign());
		
		if (sdpow.isNegative()) {
			
			return innerDiv(new SlashedDouble(1.0), finalproduct, ipwr.getNegativeSign(), 0);
		}
		else return finalproduct;
	}
	
	private static SlashedDouble getOppositeExtremum(SlashedDouble number) {
		if (number.getDouble() != null) {
			if (number.getDouble() == Double.POSITIVE_INFINITY)
				return new SlashedDouble(0.0);
			else if (number.getDouble() == Double.NEGATIVE_INFINITY)
				return new SlashedDouble(-0.0);
			else if (number.getDouble() == 0 && !number.isNegative())
				return new SlashedDouble(Double.POSITIVE_INFINITY);
			else if (number.getDouble() == 0 && number.isNegative())
				return new SlashedDouble(Double.NEGATIVE_INFINITY);
			else
				return new SlashedDouble(Double.NaN);
		} else {
			return new SlashedDouble(Double.NaN);
		}
	}
	
	private static SlashedDouble intPower(SlashedDouble number, SlashedDouble power) {
		long ipwr = abs(power).getIntSD().getIEEE754().longValue();
		//out.println(power.getIntSD().getBinaryRaw());
		SlashedDouble result = new SlashedDouble(1.0);

		if (number.getIEEE754() == -1) {
			if ((ipwr & 1) == 0) return result;
			else return new SlashedDouble(-1.0);
		}
		
		for (int i = 0; i < intPwr.length; i++) {
			if ((ipwr & intPwr[i]) == intPwr[i]) {
				result = innerMult(number, result, "");
				result = getIntPowerResult(result, power);
				
				if (result.getDouble() != null) {
					if (isNegative(number, power)) result.setSign("-");
					return result;
				}
			}
			result = innerMult(result, result, "");
			result = getIntPowerResult(result, power);
			
			if (result.getDouble() != null) {
				if (isNegative(number, power)) result.setSign("-");
				return result;
			}
		}
		
		if ((ipwr & 1l) == 1l) {
			result = innerMult(number, result, "");
			result = getIntPowerResult(result, power);
		}
		
		if (result.getDouble() != null) {
			if (isNegative(number, power)) result.setSign("-");
			return result;
		}
		
		if (isNegative(number, power)) result.setSign("-");
		
		return result;
	}
	
	private static SlashedDouble getIntPowerResult(SlashedDouble result, SlashedDouble power) {
		// this check is for extremum values
		if (result.getExp() > 1024 || result.getExp() < -1075) {
			// if power is negative there can be an exponent
			// of denormal numbers, and to hold it correctly
			// we must to accept powers over 1024 
			if (power.isNegative()) result = checkExponentExtremum(result, 1075, -1075);
			else result = checkExponentExtremum(result, 1024, -1075);
		}
		
		return result;
	}
	
	private static SlashedDouble checkExponentExtremum(SlashedDouble number, int max, int min) {
		// here we only find the abs(extremum) without actual sign
		// correct sign will be applied in the calling function
		if (number.getExp() > max) {
			return new SlashedDouble(Double.POSITIVE_INFINITY);
		} else if (number.getExp() < min) {
			return new SlashedDouble(0.0);
		} else {
			return number;
		}
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
		
		if (Double.isNaN(number1) || Double.isNaN(number2))	
			return new SlashedDouble(Double.NaN);
		else if (number1 == 1) {
			return sdnum2;
		} else if (number2 == 1) {
			return sdnum1;
		} else if (number1 == 0xffffffff) {
			return new SlashedDouble(-number2);
		} else if (number2 == 0xffffffff) {
			return new SlashedDouble(-number1);
		} else if (number1 == 0.0 && !sdnum1.isNegative()) {
			if (number2 == Double.NEGATIVE_INFINITY || number2 == Double.POSITIVE_INFINITY)
				return new SlashedDouble(Double.NaN);
			else if (sdnum2.isNegative()) return new SlashedDouble(-0.0);
			else return new SlashedDouble(0.0);
		} else if (number1 == -0.0) {
			if (number2 == Double.NEGATIVE_INFINITY || number2 == Double.POSITIVE_INFINITY) 
				return new SlashedDouble(Double.NaN);
			else if (sdnum2.isNegative()) return new SlashedDouble(0.0);
			else return new SlashedDouble(-0.0);
		} else if (number1 == Double.POSITIVE_INFINITY) {
			if (number2 == 0.0) return new SlashedDouble(Double.NaN);
			else if (sdnum2.isNegative()) return new SlashedDouble(Double.NEGATIVE_INFINITY);
			else return sdnum1;
		} else if (number1 == Double.NEGATIVE_INFINITY) {
			if (number2 == 0.0) return new SlashedDouble(Double.NaN);
			else if (sdnum2.isNegative()) return new SlashedDouble(Double.POSITIVE_INFINITY);
			else return sdnum1;
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
/*
		if (resultexp > 1024) {
			if (negativesign.equals("-")) return new SlashedDouble(Double.NEGATIVE_INFINITY);
			else return new SlashedDouble(Double.POSITIVE_INFINITY);
		} else if (resultexp < -1075) {
			if (negativesign.equals("-")) return new SlashedDouble(-0.0);
			else return new SlashedDouble(0.0);
		}
		*/
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
			return exp >> 1;
		}
	}

	private static SlashedDouble fractPower(SlashedDouble number, SlashedDouble power) {
		String powerraw = power.getFractRaw();
		int powerexp = power.getExp();
		SlashedDouble result = new SlashedDouble(1.0);
		
		for (int i = 0xffffffff; i > powerexp; i--) {
			number = innerRoot(number);
		} 
		for (int i = 0; i < powerraw.length(); i++) {
			number = innerRoot(number);
			if (powerraw.charAt(i) == '1') {
				result = innerMult(number, result, "");
				if (result.getDouble() != null) return result;
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
		
		return new SlashedDouble(result, resultexp, "");
	}
	
	public static Double div(double dividend, double divisor) {
		SlashedDouble sddividend = new SlashedDouble(dividend);
		SlashedDouble sddivisor = new SlashedDouble(divisor);
		
		return div(sddividend, sddivisor).getIEEE754();
	}
	
	public static SlashedDouble div(SlashedDouble dividend, SlashedDouble divisor) {
		double dividendnum = dividend.getDouble(), divisornum = divisor.getDouble();
		
		if (divisornum == 1.0) return dividend;
		else if (divisornum == -1.0) return new SlashedDouble(-dividendnum);
		else if (Double.isNaN(divisornum) || Double.isNaN(dividendnum)) return new SlashedDouble(Double.NaN);
		else if (divisornum == 0.0) {
			if (dividendnum == 0.0) return new SlashedDouble(Double.NaN);
			else {
				if (divisor.isNegative() && dividend.isNegative() || 
					!divisor.isNegative() && !dividend.isNegative()) {
					return new SlashedDouble(Double.POSITIVE_INFINITY);
				} else {
					return new SlashedDouble(Double.NEGATIVE_INFINITY);
				}
			}
		} 
		else if (divisornum == Double.POSITIVE_INFINITY) {
			if (dividendnum == Double.POSITIVE_INFINITY || dividendnum == Double.NEGATIVE_INFINITY)
				return new SlashedDouble(Double.NaN);
			else if (dividend.isNegative()) return new SlashedDouble(-0.0);
			else return new SlashedDouble(0.0);
		} else if (divisornum == Double.NEGATIVE_INFINITY) {
			if (dividendnum == Double.POSITIVE_INFINITY || dividendnum == Double.NEGATIVE_INFINITY)
				return new SlashedDouble(Double.NaN);
			else if (dividend.isNegative()) return new SlashedDouble(0.0);
			else return new SlashedDouble(-0.0);
		} else if (dividendnum == 0.0 && !dividend.isNegative()) {
			if (divisor.isNegative()) return new SlashedDouble(-0.0);
			else return new SlashedDouble(0.0);
		} else if (dividendnum == -0.0) {
			if (divisor.isNegative()) return new SlashedDouble(0.0);
			else return new SlashedDouble(-0.0);
		} else if (dividendnum == Double.POSITIVE_INFINITY) {
			if (divisor.isNegative()) return new SlashedDouble(Double.NEGATIVE_INFINITY);
			else return dividend;
		} else if (dividendnum == Double.NEGATIVE_INFINITY) {
			if (divisor.isNegative()) return new SlashedDouble(Double.POSITIVE_INFINITY);
			else return dividend;
		}
		
		String negativesign;
		
		if ((dividendnum >= 0 && divisornum >= 0) || (dividendnum < 0 && divisornum < 0)) {
			negativesign = "";
		} else {
			negativesign = "-";
		}
		
		return innerDiv(dividend, divisor, negativesign, 0);
	}
	
	// if (featuresign == 0) - returns result of full division
	// if (featuresign == 1) - returns result of integer division
	// if (featuresign == 2) - returns remainder of division
	private static SlashedDouble innerDiv(SlashedDouble dividend, SlashedDouble divisor, String negativesign, int featuresign) {
		String dividendraw = dividend.getBinaryRaw();
		String checksorraw = divisor.getBinaryRaw();
		long divisorlong;
		
		// for simplifying calculation we only use 63 bits of maximum 64 in divisor
		// and here we cutting off the excess bit and then making simple rounding
		if (checksorraw.length() == 64) {
			checksorraw = checksorraw.substring(0, 63);
			divisorlong = Long.parseLong(checksorraw);
			if (checksorraw.charAt(63) == '1') divisorlong++;
		} else {
			divisorlong = divisor.getLongRaw();
		}
		
		int divisorlen = checksorraw.length();
		
		// calculating number of required steps
		int stepnum;
		if (featuresign == 0) stepnum = 63;
		else stepnum = dividend.getExp() + ~divisor.getExp() + 1;
		
		// adding zeros to dividend for necessary length
		int dividendlen = divisorlen + stepnum;
		for (int i = dividendraw.length(); i < dividendlen; i++) dividendraw += '0';
		
		long minuend, residual;
		
		// initialzero is a tag of result of first substraction
		int initialzero;
		String quotient = "", residualstr, subzeros;
		
		// first step holds a special case and it is a 'zero' step
		minuend = Long.parseUnsignedLong(dividendraw.substring(0, divisorlen), 2);
		residual = minuend + ~divisorlong + 1;
		if (residual > 0xffffffff) {
			residualstr = Long.toBinaryString(residual);
			subzeros = "";
			for (int i = 0, len = divisorlen + ~residualstr.length() + 1; i < len; i++) {
				subzeros += "0";
			}
			dividendraw = subzeros + residualstr + dividendraw.substring(divisorlen);
			quotient += '1';
			initialzero = 0;
		} else {
			initialzero = 1;
		}
		
		// following steps will produce rest of result
		for (int left = 0, right = divisorlen+1; left < stepnum; left++, right++) {
			minuend = Long.parseUnsignedLong(dividendraw.substring(0, right), 2);
			residual = minuend + ~divisorlong + 1;
			if (residual > 0xffffffff) {
				residualstr = Long.toBinaryString(residual);
				subzeros = "";
				for (int i = 0, len = right + ~residualstr.length() + 1; i < len; i++) {
					subzeros += "0";
				}
				dividendraw = subzeros + residualstr + dividendraw.substring(right);
				quotient += '1';
			} else {
				quotient += '0';
			}
		}

		int resultexp;
		
		if (featuresign == 0) {
			resultexp = getDivisionExponent(dividend.getExp(), divisor.getExp(), initialzero);	
		} else {
			resultexp = getRemainderExponent(dividend.getExp(), dividendraw);
		}
		
		if (resultexp > 1024) {
			if (negativesign.equals("-")) return new SlashedDouble(Double.NEGATIVE_INFINITY);
			else return new SlashedDouble(Double.POSITIVE_INFINITY);
		} else if (resultexp < -1075) {
			if (negativesign.equals("-")) return new SlashedDouble(-0.0);
			else return new SlashedDouble(0.0);
		}
		
		if (featuresign == 0) {
			return new SlashedDouble(quotient, resultexp, negativesign);
		} else {
			return new SlashedDouble(dividendraw, resultexp, negativesign);
		}
	}
	
	private static int getDivisionExponent(int dividendexp, int divisorexp, int initialzero) {
		return dividendexp + ~divisorexp + ~initialzero + 2;
	}
	
	private static int getRemainderExponent(int dividendexp, String dividendraw) {
		return dividendexp + ~dividendraw.indexOf('1') + 1;
	}
	
	public static Double abs(double number) {
		SlashedDouble sdnum = new SlashedDouble(number);
		
		return abs(sdnum).getIEEE754();
	}
	
	public static SlashedDouble abs(SlashedDouble number) {
		SlashedDouble sdnum = number.clone();
		sdnum.setSign("");
		
		return sdnum;
	}
	
	public static Double floor(double number) {
		SlashedDouble sdnum = new SlashedDouble(number);
		
		return floor(sdnum).getIEEE754(); 
	}
	
	public static SlashedDouble floor(SlashedDouble sdnum) {
		double number = sdnum.getIntSD().getIEEE754();
		
		if (Double.isNaN(number) || number == Double.POSITIVE_INFINITY || 
			number == Double.NEGATIVE_INFINITY || sdnum.getNegativeSign().isEmpty())
			return sdnum.getIntSD();
		else {
			if (sdnum.getFractRaw().length() > 0) return new SlashedDouble(number + 0xffffffff);
			else return sdnum.getIntSD();
		}
	}
	
	public static Double ceil(double number) {
		SlashedDouble sdnum = new SlashedDouble(number);
		
		return ceil(sdnum).getIEEE754(); 
	}
	
	public static SlashedDouble ceil(SlashedDouble sdnum) {
		double number = floor(sdnum).getIEEE754();
		
		if (sdnum.getFractRaw() != null && sdnum.getFractRaw().length() > 0) {
			++number;
			if (number == 0.0) return new SlashedDouble("", 0, "-");
		} 
		
		return new SlashedDouble(number);
	}
	
	// this method returns value of fractional part that is closest to zero
	public static Double fraction(double number) {
		SlashedDouble sdnum = new SlashedDouble(number);
		
		return fraction(sdnum).getIEEE754(); 
	}
	
	public static SlashedDouble fraction(SlashedDouble sdnum) {
		return sdnum.getFractSD();
	}
	
	public static void main(String[] args) {
		
		//double result1 = pow(-1.536411651123631E-11, -9.13352388E8), result2 = Math.pow(-1.536411651123631E-11, -9.13352388E8);
		//double result1 = pow(7.667063751883245E30, 0.9989203174209581), result2 = Math.pow(7.667063751883245E30, 0.9989203174209581);

		//out.println(result1);
		//out.println(result2);
		//0.12537798823403662
		
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
				if (j % 3 == 0) power = -power;
				double result1 = pow(num, power), result2 = Math.pow(num, power);
				if (result1 != result2 && (!Double.isNaN(result1) && !Double.isNaN(result1))) {
					if ((Double.toString(result1).length() < 16 || Double.toString(result2).length() < 16 || !Double.toString(result1).substring(0, 16).equals(Double.toString(result2).substring(0, 16)))) {
					out.println(num + "!");
					out.println(power);
					out.println(result1);
					out.println(result2);
					out.println(Long.toBinaryString(Double.doubleToLongBits(result1)));
					out.println(Double.toHexString(result2));
					counter++;
					}
				}
			}
		}
		
		out.println(counter + " results in integer number powers test that have missed accuracy");


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
				if (j % 3 == 0) power = -power;
				double result1 = pow(num, power), result2 = Math.pow(num, power);
				if (result1 != result2 && (!Double.isNaN(result1) && !Double.isNaN(result1))) {
					if ((Double.toString(result1).length() < 16 || Double.toString(result2).length() < 16 || !Double.toString(result1).substring(0, 16).equals(Double.toString(result2).substring(0, 16))) && !(result1 == 1.0 && result2 == 1.0000000000000002) && (result1 == 1.0000000000000002 && result2 == 1.0) && !(result1 == 0.9999999999999999 && result2 == 1.0) && !(result1 == 1.0 && result2 == 0.9999999999999999)) {
					out.println(num + "!");
					out.println(power);
					out.println(result1);
					out.println(result2);
					out.println(Long.toBinaryString(Double.doubleToLongBits(result1)));
					out.println(Double.toHexString(result2));
					counter++;
					}
				}
			}
		}
		
		out.println(counter + " results in real number powers test that have missed accuracy");
		
		
		/*
		
		*//*
		out.println(0.0/0.0);
		out.println(div(0.0,0.0));
		out.println(0.0/-0.0);
		out.println(div(0.0,-0.0));
		out.println(0.0/Double.POSITIVE_INFINITY);
		out.println(div(0.0,Double.POSITIVE_INFINITY));
		out.println(0.0/Double.NEGATIVE_INFINITY);
		out.println(div(0.0,Double.NEGATIVE_INFINITY));
		out.println(2/1);
		out.println(div(2,1));
		out.println(2/-1);
		out.println(div(2,-1));
		out.println(-2/1);
		out.println(div(-2,1));
		out.println(-2/-1);
		out.println(div(-2,-1));
		out.println(Double.POSITIVE_INFINITY/-5);
		out.println(div(Double.POSITIVE_INFINITY,-5));
		out.println(Double.POSITIVE_INFINITY/5);
		out.println(div(Double.POSITIVE_INFINITY,5));
		out.println(Double.NEGATIVE_INFINITY/-5);
		out.println(div(Double.NEGATIVE_INFINITY,-5));
		out.println(Double.NEGATIVE_INFINITY/5);
		out.println(div(Double.NEGATIVE_INFINITY,5));
		out.println(0.0/-1);
		out.println(div(0.0,-1));
		out.println(Double.NaN/1);
		out.println(div(Double.NaN,1));
		*/
		/*0.99999999612078
		out.println(0.0*0.0);
		out.println(mult(0.0,0.0));
		out.println(0.0*-0.0);
		out.println(mult(0.0,-0.0));
		out.println(0.0*Double.POSITIVE_INFINITY);
		out.println(mult(0.0,Double.POSITIVE_INFINITY));
		out.println(0.0*Double.NEGATIVE_INFINITY);
		out.println(mult(0.0,Double.NEGATIVE_INFINITY));
		out.println(2*1);
		out.println(mult(2,1));
		out.println(2*-1);
		out.println(mult(2,-1));
		out.println(-2*1);
		out.println(mult(-2,1));
		out.println(-2*-1);
		out.println(mult(-2,-1));
		out.println(Double.POSITIVE_INFINITY*-5);
		out.println(mult(Double.POSITIVE_INFINITY,-5));
		out.println(Double.POSITIVE_INFINITY*5);
		out.println(mult(Double.POSITIVE_INFINITY,5));
		out.println(Double.NEGATIVE_INFINITY*-5);
		out.println(mult(Double.NEGATIVE_INFINITY,-5));
		out.println(Double.NEGATIVE_INFINITY*5);
		out.println(mult(Double.NEGATIVE_INFINITY,5));
		out.println(0.0*-1);
		out.println(mult(0.0,-1));
		out.println(mult(Double.NaN,1));
		out.println(Double.POSITIVE_INFINITY*-0.0);
		out.println(mult(Double.POSITIVE_INFINITY,-0.0));
		
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
		
		if (square.getDouble() != null) {
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