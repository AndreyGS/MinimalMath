package ru.andreygs.minimalmath;

import static java.lang.System.*;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
//import java.

public class MiniMath {
	private static long[] intPwr = 
	{
		1073741824l, 536870912l, 268435456l, 134217728l, 67108864l, 
		33554432l, 16777216l, 8388608l, 4194304l, 2097152l, 1048576l, 524288l, 
		262144l, 131072l, 65536l, 32768l, 16384l, 8192l, 4096l, 2048l,
		1024l, 512l, 256l, 128l, 64l, 32l, 16l, 8l, 4l, 2l
	};
	
	public static Double pow(double number, double power) {	
		return pow(new SlashedDouble(number), new SlashedDouble(power)).getIEEE754();
	}
	
	public static SlashedDouble pow(SlashedDouble number, SlashedDouble power) {
		double numbernum = number.getDouble(), powernum = power.getDouble();
		
		if (power.getFractRaw().length() > 0 && number.isNegative()) 
			return new SlashedDouble(Double.NaN);
		
		if (Double.isNaN(powernum) || Double.isNaN(numbernum))
			return new SlashedDouble(Double.NaN);
		else if (numbernum == 1) return number;
		else if (powernum == 0.0) return new SlashedDouble(1.0);
		else if (powernum == 1.0) return number;
		else if (numbernum == 0.0 && !number.isNegative()) return new SlashedDouble(0.0);
		else if (numbernum == -0.0) return new SlashedDouble(-0.0);
		else if (numbernum == Double.POSITIVE_INFINITY || powernum == Double.POSITIVE_INFINITY) {
			return new SlashedDouble(Double.POSITIVE_INFINITY);
		} else if (numbernum == Double.NEGATIVE_INFINITY) {
			if (!power.isOdd()) return new SlashedDouble(Double.POSITIVE_INFINITY);
			else return new SlashedDouble(Double.NEGATIVE_INFINITY);
		}
		
		SlashedDouble ipwr;
		
		if (power.getExp() > 0xffffffff) {
			if (power.getExp() > 30) {
				if ((power.isNegative() && number.getExp() < 0) ||
					!power.isNegative() && number.getExp() >= 0) {
					if (power.isOdd() && number.isNegative()) 
						return new SlashedDouble(Double.NEGATIVE_INFINITY);
					else return new SlashedDouble(Double.POSITIVE_INFINITY);
				} else {
					if (power.isOdd() && number.isNegative())
						return new SlashedDouble(-0.0);
					return new SlashedDouble(0.0);
				}
			}
			
			ipwr = intPower(number, power);
			
			if (ipwr.getDouble() != null) {
				if (power.isNegative()) return getOppositeExtremum(ipwr);
				else return ipwr;
			}
		} else {
			ipwr = new SlashedDouble(1.0);
		}

		SlashedDouble fpwr;
		
		if (power.getFractRaw().length() > 0) {
			fpwr = fractPower(number, power);
		} else {
			fpwr = new SlashedDouble(1.0);
		}
		
		if (fpwr.getDouble() != null && fpwr.getDouble() != 1.0) {
			if (power.isNegative()) return getOppositeExtremum(fpwr);
			else return fpwr;
		}
		
		SlashedDouble finalproduct = innerMult(ipwr, fpwr, ipwr.getNegativeSign());
		
		if (power.isNegative()) {
			
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
		return mult(new SlashedDouble(number1), new SlashedDouble(number2)).getIEEE754();
	}
	
	public static SlashedDouble mult(SlashedDouble number1, SlashedDouble number2) {
		double factor1 = number1.getDouble(), factor2 = number2.getDouble();
		
		if (Double.isNaN(factor1) || Double.isNaN(factor2))	
			return new SlashedDouble(Double.NaN);
		else if (factor1 == 1) {
			return number2;
		} else if (factor2 == 1) {
			return number1;
		} else if (factor1 == 0xffffffff) {
			return new SlashedDouble(-factor2);
		} else if (factor2 == 0xffffffff) {
			return new SlashedDouble(-factor1);
		} else if (factor1 == 0.0 && !number1.isNegative()) {
			if (factor2 == Double.NEGATIVE_INFINITY || factor2 == Double.POSITIVE_INFINITY)
				return new SlashedDouble(Double.NaN);
			else if (number2.isNegative()) return new SlashedDouble(-0.0);
			else return new SlashedDouble(0.0);
		} else if (factor1 == -0.0) {
			if (factor2 == Double.NEGATIVE_INFINITY || factor2 == Double.POSITIVE_INFINITY) 
				return new SlashedDouble(Double.NaN);
			else if (number2.isNegative()) return new SlashedDouble(0.0);
			else return new SlashedDouble(-0.0);
		} else if (factor1 == Double.POSITIVE_INFINITY) {
			if (factor2 == 0.0) return new SlashedDouble(Double.NaN);
			else if (number2.isNegative()) return new SlashedDouble(Double.NEGATIVE_INFINITY);
			else return number1;
		} else if (factor1 == Double.NEGATIVE_INFINITY) {
			if (factor2 == 0.0) return new SlashedDouble(Double.NaN);
			else if (number2.isNegative()) return new SlashedDouble(Double.POSITIVE_INFINITY);
			else return number1;
		} 
		
		String negativesign = getPairSign(number1, number2);
		
		SlashedDouble result = innerMult(number1, number2, negativesign);
		result = checkExponentExtremum(result, 1024, -1075);
		
		if (result.getDouble() != null) {
			if (negativesign.length() > 0) result.setSign("-");
		}
		
		return result;
	}
	
	private static String getPairSign(SlashedDouble number1, SlashedDouble number2) {
		if ((!number1.isNegative() && !number2.isNegative()) || 
			(number1.isNegative() && number2.isNegative())) return "";
		
		return "-";
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

		return new SlashedDouble(raw, resultexp, negativesign, result);
	}

	private static int getMultExponent(String raw, int exp1, int exp2, String num1raw, String num2raw, int biasresult) {
		int fractdigitsnum = num1raw.length() + num2raw.length() + 0xfffffffe;

		return raw.length() + ~fractdigitsnum + exp1 + exp2 + biasresult;
	}

	private static SlashedDouble fractPower(SlashedDouble number, SlashedDouble power) {
		String powerraw = power.getFractRaw();
		int powerexp = power.getExp();
		SlashedDouble result = new SlashedDouble(1.0);
		
		for (int i = 0xffffffff; i > powerexp; i--) {
			number = innerRoot(number);
			if (getFractPowerResult(number)) return result;
		}
		
		for (int i = 0; i < powerraw.length(); i++) {
			number = innerRoot(number);
			if (getFractPowerResult(number)) return result;
			
			if (powerraw.charAt(i) == '1') {
				result = innerMult(number, result, "");
				if (getFractPowerResult(number)) return result;
			}
		}
		
		return result;
	}
	
	// here we use boolean type of returning value instead of SlashedDouble like it was
	// in getIntPowerResult(), because of optimization of required computations
	private static boolean getFractPowerResult(SlashedDouble number) {
		if (number.getExp() == 0) {
			if (number.getBinaryRaw().equals("1")) return true;
		}
		
		return false;
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
	
	private static int getRootExponent(int exp) {
		if (exp == 0xffffffff || exp == 0x00000000) {
			return exp;
		} else {
			return exp >> 1;
		}
	}
	
	public static Double division(double dividend, double divisor) {
		return division(new SlashedDouble(dividend), new SlashedDouble(divisor)).getIEEE754();
	}
	
	public static SlashedDouble division(SlashedDouble dividend, SlashedDouble divisor) {
		SlashedDouble check = divisionPreCheck(dividend, divisor);
		if (check != null) return check;
		
		String negativesign = getPairSign(dividend, divisor);

		SlashedDouble result = innerDiv(dividend, divisor, negativesign, 0);
		result = checkExponentExtremum(result, 1024, -1075);
		
		if (result.getDouble() != null) {
			if (negativesign.length() > 0) result.setSign("-");
		}
		
		return result;
	}
	
	public static Double div(double dividend, double divisor) {
		return div(new SlashedDouble(dividend), new SlashedDouble(divisor)).getIEEE754();
	}
	
	public static SlashedDouble div(SlashedDouble dividend, SlashedDouble divisor) {
		String negativesign = getPairSign(dividend, divisor);
		
		if (dividend.getExp() < divisor.getExp())
			return new SlashedDouble("", 0, negativesign);
		
		SlashedDouble check = divisionPreCheck(dividend, divisor);
		if (check != null) return check;

		SlashedDouble result = innerDiv(dividend, divisor, negativesign, 1).getIntSD();
		result = checkExponentExtremum(result, 1024, -1075);
		
		if (result.getDouble() != null) {
			if (negativesign.length() > 0) result.setSign("-");
		}
		
		return result;
	}
	
	public static Double floorDiv(double dividend, double divisor) {
		return floorDiv(new SlashedDouble(dividend), new SlashedDouble(divisor)).getIEEE754();
	}
	
	public static SlashedDouble floorDiv(SlashedDouble dividend, SlashedDouble divisor) {
		SlashedDouble check = divisionPreCheck(dividend, divisor);
		if (check != null) return check;
		
		String negativesign = getPairSign(dividend, divisor);
		
		if (dividend.getExp() < divisor.getExp()) {
			if (negativesign.length() > 0) return new SlashedDouble("1", 0, negativesign);
			return new SlashedDouble("", 0, negativesign);
		}
		
		SlashedDouble result = innerDiv(dividend, divisor, negativesign, 2).getIntSD();;
		result = checkExponentExtremum(result, 1024, -1075);
		
		if (result.getDouble() != null) {
			if (negativesign.length() > 0) result.setSign("-");
		}
		
		return result;
	}
	
	private static SlashedDouble divisionPreCheck(SlashedDouble dividend, SlashedDouble divisor) {
		double dividendnum = dividend.getDouble(), divisornum = divisor.getDouble();
		
		if (Double.isNaN(divisornum) || Double.isNaN(dividendnum)) 
			return new SlashedDouble(Double.NaN);
		else if (divisornum == 1.0) return dividend;
		else if (divisornum == -1.0) return new SlashedDouble(-dividendnum);
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
		
		return null;
	}
	
	public static Double divisionRemainder(double dividend, double divisor) {
		return divisionRemainder(new SlashedDouble(dividend), new SlashedDouble(divisor)).getIEEE754();
	}
	
	public static SlashedDouble divisionRemainder(SlashedDouble dividend, SlashedDouble divisor) {
		SlashedDouble check = remainderPreCheck(dividend, divisor);
		if (check != null) return check;

		String negativesign;
		if (dividend.isNegative()) negativesign = "-";
		else negativesign = "";
		
		if (divisor.getDouble() == Double.POSITIVE_INFINITY || 
			divisor.getDouble() == Double.NEGATIVE_INFINITY || 
			dividend.getExp() < divisor.getExp())
			return dividend;
		
		SlashedDouble result = innerDiv(dividend, divisor, negativesign, 3);
		
		// here we don't need to additional check for overflowing,
		// for the reason that we already had to check input operands
		// and if they passed, than here must be a valid value
		
		return result;
	}
	
	
	
	private static SlashedDouble remainderPreCheck(SlashedDouble dividend, SlashedDouble divisor) {
		double dividendnum = dividend.getDouble(), divisornum = divisor.getDouble();
		
		if (Double.isNaN(divisornum) || Double.isNaN(dividendnum)) 
			return new SlashedDouble(Double.NaN);
		else if (divisornum == 1.0 || divisornum == -1.0)
			return new SlashedDouble(0.0);
		else if (divisornum == 0.0 && dividendnum == 0.0)
			return new SlashedDouble(Double.NaN);
		else if (dividendnum == 0.0)
			return new SlashedDouble(0.0);
		
		return null;
	}
	
	// if (featuresign == 0) - returns result of full division
	// if (featuresign == 1) - returns result of integer division
	// if (featuresign == 2) - returns result of integer division toward negative infinity rounding
	// if (featuresign == 3) - returns remainder of division
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
		if (featuresign > 0 && stepnum < 0) stepnum = 0;
		
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
		
		if (featuresign < 3) {
			resultexp = getDivisionExponent(dividend.getExp(), divisor.getExp(), initialzero);	
		} else {
			resultexp = getRemainderExponent(dividend.getExp(), dividendraw);
		}
		
		if (featuresign < 3) {
			// if we perform floorDiv and result is negative and there is a remainder
			// we need to handle it by substracion of -1 to the final result before returning it
			if (featuresign == 2 && negativesign.length() > 0 
				&& dividendraw.indexOf('1') > 0xffffffff) {
				return new SlashedDouble(
					new SlashedDouble(quotient, resultexp, negativesign)
						.getIEEE754() + 0xffffffff);
			}
			return new SlashedDouble(quotient, resultexp, negativesign);
		} else {
			return new SlashedDouble(dividendraw, resultexp, negativesign);
		}
	}
	
	private static int getDivisionExponent(int dividendexp, int divisorexp, int initialzero) {
		return dividendexp + ~divisorexp + ~initialzero + 2;
	}
	
	private static int getRemainderExponent(int dividendexp, String dividendraw) {
		int remainderstart = dividendraw.indexOf('1');
		
		if (remainderstart == 0xffffffff) return 0;
		else return dividendexp + ~dividendraw.indexOf('1') + 1;
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
	
	public static Double substract(double minuend, double subtrahend) {
		return substract(new SlashedDouble(minuend), new SlashedDouble(subtrahend)).getIEEE754();
	}
	
	public static SlashedDouble substract(SlashedDouble minuend, SlashedDouble subtrahend) {
		double minuendnum = minuend.getDouble(), subtrahendnum = subtrahend.getDouble();
		
		if (Double.isNaN(minuendnum) || Double.isNaN(subtrahendnum))
			return new SlashedDouble(Double.NaN);
		else if (minuendnum == Double.POSITIVE_INFINITY) {
			if (subtrahendnum == Double.POSITIVE_INFINITY) return new SlashedDouble(Double.NaN);
			else return minuend;
		} else if (minuendnum == Double.NEGATIVE_INFINITY) {
			if (subtrahendnum == Double.NEGATIVE_INFINITY) return new SlashedDouble(Double.NaN);
			else return minuend;
		} else if (subtrahendnum == Double.POSITIVE_INFINITY)
			return new SlashedDouble(Double.NEGATIVE_INFINITY);
		else if (subtrahendnum == Double.NEGATIVE_INFINITY)
			return new SlashedDouble(Double.POSITIVE_INFINITY);
		
		SlashedDouble result = innerSub(minuend, subtrahend);
		result = checkExponentExtremum(result, 1024, -1075);
		
		return result;
	}
	
	private static SlashedDouble innerSub(SlashedDouble minuend, SlashedDouble subtrahend) {
		SlashedDouble result;
		/*
		if (subtrahend.getExp() + 63 < minuend.getExp()) return minuend;
		else if (minuend.getExp() + 63 < subtrahend.getExp()) {
			result = minuend.clone();
			if (result.isNegative) result.setSign("");
			else result.setSign("-");
			return result;
		}*/
		
		String minraw, subraw;
		int minexp, subexp;
		boolean changesign;
		if (subtrahend.getExp() > minuend.getExp()) {
			minraw = subtrahend.getBinaryRaw(); subraw = minuend.getBinaryRaw();
			minexp = subtrahend.getExp(); subexp = minuend.getExp();
			changesign = true;
		} else {
			minraw = minuend.getBinaryRaw(); subraw = subtrahend.getBinaryRaw();
			minexp = minuend.getExp(); subexp = subtrahend.getExp();
			changesign = false;
		}
		
		String prezeros = "";
		int expdiff = minexp + ~subexp + 1;
		for (int i = 0; i < expdiff; i++) prezeros += '0';
		subraw = prezeros + subraw;
		
		int len;
		if (subraw.length() > 64) { subraw = subraw.substring(0, 64); len = 64; }
		else len = subraw.length();
		for (int i = minraw.length(); i < len; i++) minraw += '0';
		
		long minlong = Long.parseUnsignedLong(minraw, 2);
		long sublong = Long.parseUnsignedLong(subraw, 2);
		
		return null;
		
	}
	
	public static void main(String[] args) {
		
		//double result1 = pow(-1.536411651123631E-11, -9.13352388E8), result2 = Math.pow(-1.536411651123631E-11, -9.13352388E8);
		//double result1 = pow(7.667063751883245E30, 0.9989203174209581), result2 = Math.pow(7.667063751883245E30, 0.9989203174209581);
		
		
		/*
		out.println(Double.NEGATIVE_INFINITY - Double.NEGATIVE_INFINITY);
		//0
		out.println(Math.floorMod(7,-1));
		//0
		out.println(7 % 1);
		//0
		out.println(Math.floorMod(7, 1));
		out.println(-7 % -1);
		//0
		out.println(Math.floorMod(-7,-1));
		//0
		out.println(-7 % 1);
		//0
		out.println(Math.floorMod(-7, 1));
		//0
		out.println(1 % 7);
		//1
		out.println(Math.floorMod(1,7));
		//1
		out.println(-1 % 7);
		//-1
		out.println(Math.floorMod(-1, 7));
		//6
		out.println(-4.0 % -4.0);
		out.println(divisionRemainder(-11.034, 11.034));
		//0
		out.println(Math.floorMod(0, 5));
		out.println(floorDiv(0, -5) + "!");
		out.println(Math.floorDiv(0, -5) + "!");
		out.println(7 % 0);
		//0
		//out.println(Math.floorMod(7, 0));
		//0
		//out.println(SlashedDouble.parseRaw("sdvsd110101sfdsd", 6, 15));
		//out.println(result1);
		//out.println(result2);
		//0.12537798823403662
		*/
		double factor1 = 100000000000000000000000000000000000000000.0, factor2;
		int counter = 0;
		for (int i = 0; i < 80; i++) {
			factor1 = factor1 / 10; out.println(factor1 + " power random factor");
			out.println("");
			factor2 = 100000000000000000000000000000000000000000.0;
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

		
		factor1 = 100000000000000000000000000000000000000000.0;

		counter = 0;
		for (int i = 0; i < 60; i++) {
			factor1 = factor1 / 10; out.println(factor1 + " power random factor");
			out.println("");
			factor2 = 100000000000000000000000000000000000000000.0;
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