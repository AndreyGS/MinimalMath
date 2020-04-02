package ru.andreygs.minimalmath;

import static java.lang.System.*;

/**
 * The {@code MiniMath} class is implementing some of the
 * <i>{@code java.lang.Math}</i> class methods and also offer analogues
 * of basic mathematical operations, in some kind of minimalistic manner.
 * It works with surrogate number holding type - {@code SlicedDouble}.  
 * Using of this type derived from the technique in which MiniMath methods
 * are working.
 *
 * <p>There are two options to work with this class.
 * <ul>
 *   <li>you can directly apply {@code double} values to all of it public
 *   methods.</li>
 *   <li>or, you may wish to try it with {@code SlicedDouble} class,
 *   which can handle an intermidiate values that have much more wider
 *   limits that {@code double} type and as a result you can get more
 *   accurate result in the end if in some operations your value will
 *   intersect {@code double} limit borders in a few times.</li>
 *</ul>
 *
 * <p>For the values that are in range of {@code double} type the accuracy
 * of calculating is over 99.999% for 12 digits after decimal point,
 * in comparison with embedded functions.
 *
 * <p>But the main goal of this class is in its implemention. The only
 * built-in operators that was used are summing, bitwise and conditional.
 * More of it, the summing method is also exists here - mainly for
 * working with {@code SlicedDouble} type directly.
 *
 * @author Andrey Grabov-Smetankin
 */
public class MiniMath {
	
	/**
	 * This class does not presume to use any instantiation 
	 */
	private MiniMath() {}
	
	/**
	 * This array is holding the powers of two for instructions in
	 * {@code intPower()} and {@code intPowerNoLimits()} methods
	 */
	private static long[] intPwr = 
	{
		1073741824l, 536870912l, 268435456l, 134217728l, 67108864l, 
		33554432l, 16777216l, 8388608l, 4194304l, 2097152l, 1048576l, 524288l, 
		262144l, 131072l, 65536l, 32768l, 16384l, 8192l, 4096l, 2048l,
		1024l, 512l, 256l, 128l, 64l, 32l, 16l, 8l, 4l, 2l
	};
	
	/**
	 * Returns the value of the first argument raised to the power of the
     * second argument. The information about special cases is the same
	 * as in {@code java.lang.Math} class method.
	 *
	 * @param   number(n)  the base.
     * @param   power(p)   the exponent.
     * @return  the value {@code n}<sup>{@code p}</sup>.
	 */
	public static Double pow(double number, double power) {	
		return pow(new SlicedDouble(number), new SlicedDouble(power)).getIEEE754();
	}
	
	/**
	 * Returns the value of the first argument raised to the power of the
     * second argument. The information about special cases is the same
	 * as in {@code java.lang.Math} class method.
	 *
	 * <p><b>Important.</b>
	 * <p>This method works directly with {@code SlicedDouble} type of inputs.
	 * If the arguments are not holding compilated {@code double} numbers
	 * them will be computed, so if you do not wish to lose advantages of
	 * using {@code SlicedDouble} type, you should not use this method.
	 * Instead you may use separately {@code intPowerNoLimits()} and
	 * {@code fractPower()} methods.
	 *
	 * @param   number(n)  the base.
     * @param   power(p)   the exponent.
     * @return  the value {@code n}<sup>{@code p}</sup>.
	 */
	public static SlicedDouble pow(SlicedDouble number, SlicedDouble power) {
		double numbernum = number.getIEEE754(), powernum = power.getIEEE754();
		
		if (power.getFractRaw().length() > 0 && number.isNegative()) 
			return new SlicedDouble(Double.NaN);
		
		if (Double.isNaN(powernum) || Double.isNaN(numbernum))
			return new SlicedDouble(Double.NaN);
		else if (numbernum == 1) return number;
		else if (powernum == 0.0) return new SlicedDouble(1.0);
		else if (powernum == 1.0) return number;
		else if (numbernum == 0.0 && !number.isNegative()) return new SlicedDouble(0.0);
		else if (numbernum == -0.0) return new SlicedDouble(-0.0);
		else if (numbernum == Double.POSITIVE_INFINITY || powernum == Double.POSITIVE_INFINITY) {
			return new SlicedDouble(Double.POSITIVE_INFINITY);
		} else if (numbernum == Double.NEGATIVE_INFINITY) {
			if (!power.isOdd()) return new SlicedDouble(Double.POSITIVE_INFINITY);
			else return new SlicedDouble(Double.NEGATIVE_INFINITY);
		}
		
		SlicedDouble ipwr;
		
		if (power.getExp() > 0xffffffff) {
			if (power.getExp() > 30) {
				if ((power.isNegative() && number.getExp() < 0) ||
					!power.isNegative() && number.getExp() >= 0) {
					if (power.isOdd() && number.isNegative()) 
						return new SlicedDouble(Double.NEGATIVE_INFINITY);
					else return new SlicedDouble(Double.POSITIVE_INFINITY);
				} else {
					if (power.isOdd() && number.isNegative())
						return new SlicedDouble(-0.0);
					return new SlicedDouble(0.0);
				}
			}
			
			ipwr = intPower(number, power);
			
			if (ipwr.getDouble() != null) {
				if (power.isNegative()) return getOppositeExtremum(ipwr);
				else return ipwr;
			}
		} else {
			ipwr = new SlicedDouble(1.0);
		}

		SlicedDouble fpwr;
		
		if (power.getFractRaw().length() > 0) {
			fpwr = fractPower(number, power);
		} else {
			fpwr = new SlicedDouble(1.0);
		}
		
		if (fpwr.getDouble() != null && fpwr.getDouble() != 1.0) {
			if (power.isNegative()) return getOppositeExtremum(fpwr);
			else return fpwr;
		}
		
		SlicedDouble finalproduct = innerMult(ipwr, fpwr, ipwr.getNegativeSign());
		
		if (power.isNegative()) {
			
			return innerDiv(new SlicedDouble(1.0), finalproduct, ipwr.getNegativeSign(), 0);
		}
		else return finalproduct;
	}
	
	/**
	 * This is auxiliary method for getting result of {@code pow} for
	 * the cases when result value is over the {@code double} limits
	 * and the power is negative.
	 */
	private static SlicedDouble getOppositeExtremum(SlicedDouble number) {
		if (number.getDouble() != null) {
			if (number.getDouble() == Double.POSITIVE_INFINITY)
				return new SlicedDouble(0.0);
			else if (number.getDouble() == Double.NEGATIVE_INFINITY)
				return new SlicedDouble(-0.0);
			else if (number.getDouble() == 0 && !number.isNegative())
				return new SlicedDouble(Double.POSITIVE_INFINITY);
			else if (number.getDouble() == 0 && number.isNegative())
				return new SlicedDouble(Double.NEGATIVE_INFINITY);
			else
				return new SlicedDouble(Double.NaN);
		} else {
			return new SlicedDouble(Double.NaN);
		}
	}
	
	/**
	 * Returns the number after raising it to the specified integer number.
	 */
	private static SlicedDouble intPower(SlicedDouble number, SlicedDouble power) {
		long ipwr = abs(power).getIntSD().getIEEE754().longValue();
		SlicedDouble result = new SlicedDouble(1.0);

		if (number.getIEEE754() == -1) {
			if ((ipwr & 1) == 0) return result;
			else return new SlicedDouble(-1.0);
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
	
	/**
	 * Returns whether or not number raised to the giving power will be negative
	 */
	private static boolean isNegative(SlicedDouble number, SlicedDouble power) {
		if (number.isNegative() && power.isOdd()) return true;
		
		return false;
	}
	
	/**
	 * This method checks intermidiate results in {@code intPower()}
	 */
	private static SlicedDouble getIntPowerResult(SlicedDouble result, SlicedDouble power) {
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
	
	/**
	 * This is accessory method that checks exponent against {@code double}
	 * limitations in methods that needs it to supply correct result.
	 */
	private static SlicedDouble checkExponentExtremum(SlicedDouble number, int max, int min) {
		// here we only find the abs(extremum) without actual sign
		// correct sign will be applied in the calling function
		if (number.getExp() > max) {
			return new SlicedDouble(Double.POSITIVE_INFINITY);
		} else if (number.getExp() < min) {
			return new SlicedDouble(0.0);
		} else {
			return number;
		}
	}
	
	/**
	 * Returns the number after raising it to the integer part of
	 * the specified power.
	 *
	 * <p>It can be used in a sheaf with {@code fractPower()} and {@code innerMult()}
	 * methods to produce the same result as with using {@code pow()} method,
	 * but without limitation of {@code double} type. The only limit that can happen
	 * is an overflow of {@code integer} that holds an exponent if your
	 * value will be too small or too large. But you can simply handle it by
	 * checking inputs and outputs if such scenario may take a place.
	 *
	 * <p>For example:<br>
	 *
	 * {@code SlicedDouble number = new SlicedDouble(3432543.23423);}<br>
	 * {@code SlicedDouble power = new SlicedDouble(-33.23423);}<br><br>
	 *
	 * {@code SlicedDouble intpowresult, fractpowresult, multresult, divresult;}<br><br>
	 *
	 * {@code intpowresult = MiniMath.intPowerNoLimits(number, power);}<br>
	 * {@code fractpowresult = MiniMath.fractPower(number, power);}<br>
	 * {@code multresult = MiniMath.innerMult(intpowresult, fractpowresult, intpowresult.getNegativeSign());}<br>
	 * {@code divresult = MiniMath.innerDiv(new SlicedDouble(1.0), multresult, multresult.getNegativeSign(), 0);}<br>
	 *
	 * <p>That would be striclty as if you were using 
	 * {@code MiniMath.pow(3432543.23423, -33.23423)} but in the true
	 * {@code SlicedDouble} format.
	 *
	 * @param   number(n)  the base.
     * @param   power(p)   the integer part of the exponent.
     * @return  the value {@code n}<sup>{@code p}</sup>.
	 */
	public static SlicedDouble intPowerNoLimits(SlicedDouble number, SlicedDouble power) {
		long ipwr = abs(power).getIntSD().getIEEE754().longValue();
		SlicedDouble result = new SlicedDouble(1.0);

		if (number.getIEEE754() == -1) {
			if ((ipwr & 1) == 0) return result;
			else return new SlicedDouble(-1.0);
		}
		
		for (int i = 0; i < intPwr.length; i++) {
			if ((ipwr & intPwr[i]) == intPwr[i]) {
				result = innerMult(number, result, "");
			}
			result = innerMult(result, result, "");
		}
		
		if ((ipwr & 1l) == 1l) {
			result = innerMult(number, result, "");
		}
		
		if (isNegative(number, power)) result.setSign("-");
		
		return result;
	}

	/**
	 * Return {@code double} value of multiplication inputs
	 *
	 * @param number1 first multiplyer
	 * @param number2 second multiplyer
	 *
	 * @return {@code number1*number2}
	 */
	public static Double mult(double number1, double number2) {	
		return mult(new SlicedDouble(number1), new SlicedDouble(number2)).getIEEE754();
	}
	
	/**
	 * Return {@code SlicedDouble} object that holds value of multiplication 
	 * values that inputs holds.
	 *
	 * <p><b>Important.</b>
	 * <p>This method works directly with {@code SlicedDouble} type of inputs.
	 * If the arguments are not holding compilated {@code double} numbers
	 * them will be computed, so if you do not wish to lose advantages of
	 * using {@code SlicedDouble} type, you should not use this method.
	 * Instead you may use {@code innerMult} method.
	 *
	 * @param number1 the first multiplyer
	 * @param number2 the second multiplyer
	 *
	 * @return the SlicedDouble that holds in value {@code number1*number2}
	 */
	public static SlicedDouble mult(SlicedDouble number1, SlicedDouble number2) {
		double factor1 = number1.getIEEE754(), factor2 = number2.getIEEE754();
		
		if (Double.isNaN(factor1) || Double.isNaN(factor2))	
			return new SlicedDouble(Double.NaN);
		else if (factor1 == 1) {
			return number2;
		} else if (factor2 == 1) {
			return number1;
		} else if (factor1 == 0xffffffff) {
			return new SlicedDouble(-factor2);
		} else if (factor2 == 0xffffffff) {
			return new SlicedDouble(-factor1);
		} else if (factor1 == 0.0 && !number1.isNegative()) {
			if (factor2 == Double.NEGATIVE_INFINITY || factor2 == Double.POSITIVE_INFINITY)
				return new SlicedDouble(Double.NaN);
			else if (number2.isNegative()) return new SlicedDouble(-0.0);
			else return new SlicedDouble(0.0);
		} else if (factor1 == -0.0) {
			if (factor2 == Double.NEGATIVE_INFINITY || factor2 == Double.POSITIVE_INFINITY) 
				return new SlicedDouble(Double.NaN);
			else if (number2.isNegative()) return new SlicedDouble(0.0);
			else return new SlicedDouble(-0.0);
		} else if (factor1 == Double.POSITIVE_INFINITY) {
			if (factor2 == 0.0) return new SlicedDouble(Double.NaN);
			else if (number2.isNegative()) return new SlicedDouble(Double.NEGATIVE_INFINITY);
			else return number1;
		} else if (factor1 == Double.NEGATIVE_INFINITY) {
			if (factor2 == 0.0) return new SlicedDouble(Double.NaN);
			else if (number2.isNegative()) return new SlicedDouble(Double.POSITIVE_INFINITY);
			else return number1;
		} 
		
		String negativesign = getPairSign(number1, number2);
		
		SlicedDouble result = innerMult(number1, number2, negativesign);
		result = checkExponentExtremum(result, 1024, -1075);
		
		if (result.getDouble() != null) {
			if (negativesign.length() > 0) result.setSign("-");
		}
		
		return result;
	}
	
	/**
	 * Returns result sign of multiplication or division
	 */
	private static String getPairSign(SlicedDouble number1, SlicedDouble number2) {
		if ((!number1.isNegative() && !number2.isNegative()) || 
			(number1.isNegative() && number2.isNegative())) return "";
		
		return "-";
	}
	
	/**
	 * Return {@code SlicedDouble} object that holds value of multiplication 
	 * of values that inputs holds.
	 *
	 * <p>It can be used in a sheaf with {@code fractPower()} and {@code innerMult()}
	 * methods to produce the same result as with using {@code pow()} method,
	 * but without limitation of {@code double} type. The only limit that can happen
	 * is an overflow of {@code integer} that holds an exponent if your
	 * value will be too small or too large. But you can simply handle it by
	 * checking inputs and outputs if such scenario may take a place.
	 *
	 * <p>For example:<br>
	 *
	 * {@code SlicedDouble number = new SlicedDouble(3432543.23423);}<br>
	 * {@code SlicedDouble power = new SlicedDouble(-33.23423);}<br><br>
	 *
	 * {@code SlicedDouble intpowresult, fractpowresult, multresult, divresult;}<br><br>
	 *
	 * {@code intpowresult = MiniMath.intPowerNoLimits(number, power);}<br>
	 * {@code fractpowresult = MiniMath.fractPower(number, power);}<br>
	 * {@code multresult = MiniMath.innerMult(intpowresult, fractpowresult, intpowresult.getNegativeSign());}<br>
	 * {@code divresult = MiniMath.innerDiv(new SlicedDouble(1.0), multresult, multresult.getNegativeSign(), 0);}<br>
	 *
	 * <p>That would be striclty as if you were using 
	 * {@code MiniMath.pow(3432543.23423, -33.23423)} but in the true
	 * {@code SlicedDouble} format.
	 *
	 * @param number1 		the first multiplyer
	 * @param number2 		the second multiplyer
	 * @param negativesign	the result sign
	 *
	 * @return the SlicedDouble that holds in value {@code number1*number2} with the sign that was supplied
	 */
	public static SlicedDouble innerMult(SlicedDouble number1, SlicedDouble number2, String negativesign) {
		
		// this is a check of getting Double.NaN after intermidiate operations
		// with pure SlicedDouble. When using one-time instructions that
		// do not include chains this check is not take any valuable part.
		if (number1.getDouble() != null && Double.isNaN(number1.getDouble()))
			return number1;
		else if (number2.getDouble() != null && Double.isNaN(number2.getDouble()))
			return number2;
		
		long product = 0l, unit;
		String num2raw;
		
		if (number1.onesEnum() < number2.onesEnum()) {
			unit = number2.getLongRaw();
			num2raw = number1.getBinaryRaw();
		} else {
			unit = number1.getLongRaw();
			num2raw = number2.getBinaryRaw();
		}
		
		int biasproduct = 0; 

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
						if (product > 0) {
							if (Long.numberOfTrailingZeros(product) == 0) carry = 1;
							product >>>= 1;
							product += carry;
						}
						biasproduct += shift + 1;
					} else {
						unit <<= leadzeroes + 0xffffffff;
						spaceneed = ~check + 1;
						test = Long.toBinaryString(product);
						if (test.length() >= spaceneed) {
							if (test.charAt(test.length()+check) == '1') carry = 1;
						}
						product >>>= spaceneed;
						product += carry;
						biasproduct += spaceneed;
					}
				}
				product += unit;
				shift = 0;
			}
		}

		String raw = Long.toBinaryString(product);
		
		int productexp = getMultExponent(raw, number1.getExp(), number2.getExp(), number1.getBinaryRaw(), number2.getBinaryRaw(), biasproduct);

		return new SlicedDouble(raw, productexp, negativesign);
	}
	
	/**
	 * Auxiliary method to get multiplication result exponent
	 */
	private static int getMultExponent(String raw, int exp1, int exp2, String num1raw, String num2raw, int biasproduct) {
		int fractdigitsnum = num1raw.length() + num2raw.length() + 0xfffffffe;

		return raw.length() + ~fractdigitsnum + exp1 + exp2 + biasproduct;
	}
	
	/**
	 * Returns the number after raising it to the fractional part of
	 * the specified power.
	 *
	 * <p>It can be used in a sheaf with {@code intPower()} and {@code innerMult()}
	 * methods to produce the same result as with using {@code pow()} method,
	 * but without limitation of {@code double} type except those that is
	 * too do not extract roots from negative numbers. The only limit that can happen
	 * is an overflow of {@code integer} that holds an exponent if your
	 * value will be too small or too large. But you can simply handle it by
	 * checking inputs and outputs if such scenario may take a place.
	 *
	 * <p>For example:<br>
	 *
	 * {@code SlicedDouble number = new SlicedDouble(3432543.23423);}<br>
	 * {@code SlicedDouble power = new SlicedDouble(-33.23423);}<br><br>
	 *
	 * {@code SlicedDouble intpowresult, fractpowresult, multresult, divresult;}<br><br>
	 *
	 * {@code intpowresult = MiniMath.intPowerNoLimits(number, power);}<br>
	 * {@code fractpowresult = MiniMath.fractPower(number, power);}<br>
	 * {@code multresult = MiniMath.innerMult(intpowresult, fractpowresult, intpowresult.getNegativeSign());}<br>
	 * {@code divresult = MiniMath.innerDiv(new SlicedDouble(1.0), multresult, multresult.getNegativeSign(), 0);}
	 *
	 * <p>That would be striclty as if you were using 
	 * {@code MiniMath.pow(3432543.23423, -33.23423)} but in the true
	 * {@code SlicedDouble} format.
	 *
	 * @param   number(n)  the base.
     * @param   power(p)   the fractional part of the exponent.
     * @return  the value {@code n}<sup>{@code p}</sup>.
	 */
	public static SlicedDouble fractPower(SlicedDouble number, SlicedDouble power) {
		if (!number.getNegativeSign().isEmpty()) return new SlicedDouble(Double.NaN);
		else if (number.getDouble() != null && Double.isNaN(number.getDouble()))
			return number;
		else if (power.getDouble() != null && Double.isNaN(power.getDouble()))
			return power;

		String powerraw = power.getFractRaw();
		int powerexp = power.getExp();
		SlicedDouble result = new SlicedDouble(1.0);
		
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
	
	// here we use boolean type of returning value instead of SlicedDouble like it was
	// in getIntPowerResult(), because of optimization of required computations
	/**
	 * Auxiliary method to check if number is collapsed to '1'
	 */
	private static boolean getFractPowerResult(SlicedDouble number) {
		if (number.getExp() == 0) {
			if (number.getBinaryRaw().equals("1")) return true;
		}
		
		return false;
	}
	
	/**
	 * Return a square root of input number.
	 *
	 * It's named so and not {@code sqrt} for example as similar method
	 * in {@code Math} class because it works little different
	 * in part that it do not rounding final result, and for that to
	 * meet names of other similar methods in current class.
	 *
	 * @param number the value
	 * @return the positive square root of {@code number}.
     * 			If the argument is NaN or less than zero, the result is NaN.
	 */
	public static SlicedDouble innerRoot(SlicedDouble number) {
		
		// this ia a check of passing Double.NaN or negative values
		// after intermidiate operations with pure SlicedDouble.
		// When using one-time instructions that do not include chains
		// this check is not take any valuable part.
		if (!number.getNegativeSign().isEmpty() || 
			(number.getDouble() != null && Double.isNaN(number.getDouble()))) 
			return new SlicedDouble(Double.NaN);
		
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
		
		return new SlicedDouble(result, resultexp, "");
	}
	
	/**
	 * Auxiliary method to get root result exponent
	 */
	private static int getRootExponent(int exp) {
		if (exp == 0xffffffff || exp == 0x00000000) {
			return exp;
		} else {
			return exp >> 1;
		}
	}
	
	/**
	 * Returns result of the divison.
	 *
	 * <p><b>Caution:</b>If the divisor is '0' there is no exception,
	 * instead you will get an Infinity, positive or negative.
	 *
	 * @param dividend	the dividend number
	 * @param divisor	the divisor number
	 * @return quotient
	 */
	public static Double division(double dividend, double divisor) {
		return division(new SlicedDouble(dividend), new SlicedDouble(divisor)).getIEEE754();
	}
	/**
	 * Returns result of the divison
	 *
	 * <p><b>Important.</b>
	 * <p>This method works directly with {@code SlicedDouble} type of inputs.
	 * If the arguments are not holding compilated {@code double} numbers
	 * them will be computed, so if you do not wish to lose advantages of
	 * using {@code SlicedDouble} type, you should not use this method.
	 * Instead you may use separately {@code innerDiv()} with '0' featuresign.
	 *
	 * <p><b>Caution:</b>If the divisor is '0' there is no exception,
	 * instead you will get an Infinity, positive or negative.
	 *
	 * @param dividend	the dividend number
	 * @param divisor	the divisor number
	 * @return quotient
	 */
	public static SlicedDouble division(SlicedDouble dividend, SlicedDouble divisor) {
		SlicedDouble check = divisionPreCheck(dividend, divisor);
		if (check != null) return check;
		
		String negativesign = getPairSign(dividend, divisor);

		SlicedDouble result = innerDiv(dividend, divisor, negativesign, 0);
		result = checkExponentExtremum(result, 1024, -1075);
		
		if (result.getDouble() != null) {
			if (negativesign.length() > 0) result.setSign("-");
		}
		
		return result;
	}
	
	/**
	 * Returns integer result of the divison
	 *
	 * <p><b>Caution:</b>If the divisor is '0' there is no exception,
	 * instead you will get an Infinity, positive or negative.
	 *
	 * @param dividend	the dividend number
	 * @param divisor	the divisor number
	 * @return integer quotient
	 */
	public static Double div(double dividend, double divisor) {
		return div(new SlicedDouble(dividend), new SlicedDouble(divisor)).getIEEE754();
	}
	
	/**
	 * Returns integer result of the divison
	 *
	 * <p><b>Important.</b>
	 * <p>This method works directly with {@code SlicedDouble} type of inputs.
	 * If the arguments are not holding compilated {@code double} numbers
	 * them will be computed, so if you do not wish to lose advantages of
	 * using {@code SlicedDouble} type, you should not use this method.
	 * Instead you may use separately {@code innerDiv()} with '1' featuresign.
	 *
	 * <p><b>Caution:</b>If the divisor is '0' there is no exception,
	 * instead you will get an Infinity, positive or negative.
	 *
	 * @param dividend	the dividend number
	 * @param divisor	the divisor number
	 * @return integer quotient
	 */
	public static SlicedDouble div(SlicedDouble dividend, SlicedDouble divisor) {
		String negativesign = getPairSign(dividend, divisor);
		
		if (dividend.getExp() < divisor.getExp())
			return new SlicedDouble("", 0, negativesign);
		
		SlicedDouble check = divisionPreCheck(dividend, divisor);
		if (check != null) return check;

		SlicedDouble result = innerDiv(dividend, divisor, negativesign, 1).getIntSD();
		result = checkExponentExtremum(result, 1024, -1075);
		
		if (result.getDouble() != null) {
			if (negativesign.length() > 0) result.setSign("-");
		}
		
		return result;
	}
	
	/**
	 * Returns the largest (closest to positive infinity) integer value
	 * that is less than or equal to the algebraic quotient
	 *
	 * <p><b>Caution:</b>If the divisor is '0' there is no exception,
	 * instead you will get an Infinity, positive or negative. Because we
	 * are using double arithmetic, not integer. And if divisor is also 0
	 * the result would be Double.NaN
	 *
	 * @param dividend	the dividend number
	 * @param divisor	the divisor number
	 * @return the largest (closest to positive infinity) integer value
	 * 			that is less than or equal to the algebraic quotient
	 */
	public static Double floorDiv(double dividend, double divisor) {
		return floorDiv(new SlicedDouble(dividend), new SlicedDouble(divisor)).getIEEE754();
	}
	
	/**
	 * Returns the largest (closest to positive infinity) integer value
	 * that is less than or equal to the algebraic quotient
	 *
	 * <p><b>Important.</b>
	 * <p>This method works directly with {@code SlicedDouble} type of inputs.
	 * If the arguments are not holding compilated {@code double} numbers
	 * them will be computed, so if you do not wish to lose advantages of
	 * using {@code SlicedDouble} type, you should not use this method.
	 * Instead you may use separately {@code innerDiv()} with '2' featuresign.
	 *
	 * <p><b>Caution:</b>If the divisor is '0' there is no exception,
	 * instead you will get an Infinity, positive or negative. Because we
	 * are using double arithmetic, not integer. And if divisor is also 0
	 * the result would be Double.NaN
	 *
	 * @param dividend	the dividend number
	 * @param divisor	the divisor number
	 * @return the largest (closest to positive infinity) integer value
	 * 			that is less than or equal to the algebraic quotient
	 */
	public static SlicedDouble floorDiv(SlicedDouble dividend, SlicedDouble divisor) {
		SlicedDouble check = divisionPreCheck(dividend, divisor);
		if (check != null) return check;
		
		String negativesign = getPairSign(dividend, divisor);
		
		if (dividend.getExp() < divisor.getExp()) {
			if (negativesign.length() > 0) return new SlicedDouble("1", 0, negativesign);
			return new SlicedDouble("", 0, negativesign);
		}
		
		SlicedDouble result = innerDiv(dividend, divisor, negativesign, 2).getIntSD();
		result = checkExponentExtremum(result, 1024, -1075);
		
		if (result.getDouble() != null) {
			if (negativesign.length() > 0) result.setSign("-");
		}
		
		return result;
	}
	
	/**
	 * Auxiliary method to precheck of division components
	 */
	private static SlicedDouble divisionPreCheck(SlicedDouble dividend, SlicedDouble divisor) {
		double dividendnum = dividend.getIEEE754(), divisornum = divisor.getIEEE754();
		
		if (Double.isNaN(divisornum) || Double.isNaN(dividendnum)) 
			return new SlicedDouble(Double.NaN);
		else if (divisornum == 1.0) return dividend;
		else if (divisornum == -1.0) return new SlicedDouble(-dividendnum);
		else if (divisornum == 0.0) {
			if (dividendnum == 0.0) return new SlicedDouble(Double.NaN);
			else {
				if (divisor.isNegative() && dividend.isNegative() || 
					!divisor.isNegative() && !dividend.isNegative()) {
					return new SlicedDouble(Double.POSITIVE_INFINITY);
				} else {
					return new SlicedDouble(Double.NEGATIVE_INFINITY);
				}
			}
		} 
		else if (divisornum == Double.POSITIVE_INFINITY) {
			if (dividendnum == Double.POSITIVE_INFINITY || dividendnum == Double.NEGATIVE_INFINITY)
				return new SlicedDouble(Double.NaN);
			else if (dividend.isNegative()) return new SlicedDouble(-0.0);
			else return new SlicedDouble(0.0);
		} else if (divisornum == Double.NEGATIVE_INFINITY) {
			if (dividendnum == Double.POSITIVE_INFINITY || dividendnum == Double.NEGATIVE_INFINITY)
				return new SlicedDouble(Double.NaN);
			else if (dividend.isNegative()) return new SlicedDouble(0.0);
			else return new SlicedDouble(-0.0);
		} else if (dividendnum == 0.0 && !dividend.isNegative()) {
			if (divisor.isNegative()) return new SlicedDouble(-0.0);
			else return new SlicedDouble(0.0);
		} else if (dividendnum == -0.0) {
			if (divisor.isNegative()) return new SlicedDouble(0.0);
			else return new SlicedDouble(-0.0);
		} else if (dividendnum == Double.POSITIVE_INFINITY) {
			if (divisor.isNegative()) return new SlicedDouble(Double.NEGATIVE_INFINITY);
			else return dividend;
		} else if (dividendnum == Double.NEGATIVE_INFINITY) {
			if (divisor.isNegative()) return new SlicedDouble(Double.POSITIVE_INFINITY);
			else return dividend;
		}
		
		return null;
	}
	
	/**
	 * Returns the nearest integer (closest to zero) that is 
	 * that is less than or equal to the algebraic quotient if the divisor
	 * is positive, and larger than or equal if the divisor is negative.
	 *
	 * <p><b>Caution:</b>If the divisor is '0' there is no exception,
	 * instead if the divisor is also 0 you will get Double.NaN as the result.
	 * Or if divisor is not 0, you will get 0.0 as a result.
	 *
	 * @param dividend	the dividend number
	 * @param divisor	the divisor number
	 * @return division remainder closest to zero
	 */
	public static Double divisionRemainder(double dividend, double divisor) {
		return divisionRemainder(new SlicedDouble(dividend), new SlicedDouble(divisor)).getIEEE754();
	}
	
	/**
	 * Returns the nearest integer (closest to zero) that is 
	 * that is less than or equal to the algebraic quotient if the divisor
	 * is positive, and larger than or equal if the divisor is negative.
	 *
	 * <p><b>Important.</b>
	 * <p>This method works directly with {@code SlicedDouble} type of inputs.
	 * If the arguments are not holding compilated {@code double} numbers
	 * them will be computed, so if you do not wish to lose advantages of
	 * using {@code SlicedDouble} type, you should not use this method.
	 * Instead you may use separately {@code innerDiv()} with '3' featuresign.
	 *
	 * <p><b>Caution:</b>If the divisor is '0' there is no exception,
	 * instead if the divisor is also 0 you will get Double.NaN as the result.
	 * Or if divisor is not 0, you will get 0.0 as a result.
	 *
	 * @param dividend	the dividend number
	 * @param divisor	the divisor number
	 * @return division remainder closest to zero
	 */
	public static SlicedDouble divisionRemainder(SlicedDouble dividend, SlicedDouble divisor) {
		SlicedDouble check = remainderPreCheck(dividend, divisor);
		if (check != null) return check;

		String negativesign;
		if (dividend.isNegative()) negativesign = "-";
		else negativesign = "";
		
		if (divisor.getIEEE754() == Double.POSITIVE_INFINITY || 
			divisor.getIEEE754() == Double.NEGATIVE_INFINITY || 
			dividend.getExp() < divisor.getExp())
			return dividend;
		
		SlicedDouble result = innerDiv(dividend, divisor, negativesign, 3);
		
		// here we don't need to additional check for overflowing,
		// for the reason that we already had to check input operands
		// and if they passed, than here must be a valid value
		
		return result;
	}
	
	/**
	 * Auxiliary method to precheck of division remainder operation components
	 */
	private static SlicedDouble remainderPreCheck(SlicedDouble dividend, SlicedDouble divisor) {
		double dividendnum = dividend.getIEEE754(), divisornum = divisor.getIEEE754();
		
		if (Double.isNaN(divisornum) || Double.isNaN(dividendnum)) 
			return new SlicedDouble(Double.NaN);
		else if (divisornum == 1.0 || divisornum == -1.0)
			return new SlicedDouble(0.0);
		else if (dividendnum == 0.0 && divisornum == 0.0)
			return new SlicedDouble(Double.NaN);
		else if (dividendnum == 0.0 || divisornum == 0.0)
			return new SlicedDouble(0.0);
		
		return null;
	}
	
	/**
	 * Returns the floor modulus of inputs.
	 *
	 * <p>The formula of modulus is:
	 *
	 * <p><i>dividend - (floorDiv(dividend, divisor) * divisor)</i>
	 *
	 * <p>First of all floorDiv is happend and there is a few special
	 * cases that can occur:
	 *
	 * <ul>
	 *	<li>result of floorDiv is Double.NaN than overall result
	 *			is the same.</li>
	 *	<li>result of floorDiv is Double.POSITIVE_INFINITY or
	 *			Double.NEGATIVE_INFINITY than if <i>dividend</i> is
	 *			Double.POSITIVE_INFINITY or	Double.NEGATIVE_INFINITY than
	 *			overall result is Double.NaN, and if not - overall result is
	 *			dividend.</li>
	 *	<li>result of floorDiv is 0.0 than overall result is dividend.</li>
	 * </ul>
	 *
	 * <p><b>Caution:</b>If the divisor is '0' there is no exception,
	 * instead if the divisor is also 0 you will get Double.NaN as the result.
	 *
	 *
	 * @param dividend	the dividend number
	 * @param divisor	the divisor number
	 * @return the floor modulus
	 */
	public static Double floorMod(double dividend, double divisor) {
		return floorMod(new SlicedDouble(dividend), new SlicedDouble(divisor)).getIEEE754();
	}
	
	/**
	 * Returns the floor modulus of inputs.
	 *
	 * <p>The formula of modulus is:
	 *
	 * <p><i>dividend - (floorDiv(dividend, divisor) * divisor)</i>
	 *
	 * <p>First of all floorDiv is happend and there is a few special
	 * cases that can occur:
	 *
	 * <ul>
	 *	<li>result of floorDiv is Double.NaN than overall result
	 *			is the same.</li>
	 *	<li>result of floorDiv is Double.POSITIVE_INFINITY or
	 *			Double.NEGATIVE_INFINITY than if <i>dividend</i> is
	 *			Double.POSITIVE_INFINITY or	Double.NEGATIVE_INFINITY than
	 *			overall result is Double.NaN, and if not - overall result is
	 *			dividend.</li>
	 *	<li>result of floorDiv is 0.0 than overall result is dividend.</li>
	 * </ul>
	 *
	 * <p><b>Important.</b>
	 * <p>This method works directly with {@code SlicedDouble} type of inputs.
	 * If the arguments are not holding compilated {@code double} numbers
	 * them will be computed, so if you do not wish to lose advantages of
	 * using {@code SlicedDouble} type, you should not use this method.
	 * Instead you may use it like that:<br>
	 *
	 * {@code SlicedDouble dividend = new SlicedDouble(345.34);}<br>
	 * {@code SlicedDouble divisor = new SlicedDouble(23.53);}<br>
	 * {@code SlicedDouble floordiv = MiniMath.innerDiv(dividend, divisor, "", 2).getIntSD();}<br>
	 * {@code SlicedDouble innermult = MiniMath.innerMult(floordiv, divisor, floordiv.getNegativeSign());}<br>
	 * {@code SlicedDouble innersub = MiniMath.innerSub(dividend, innermult);}
	 *
	 *
	 * <p>That would be striclty as if you were using 
	 * {@code MiniMath.floorMod(345.34, 23.53)} but in the true
	 * {@code SlicedDouble} format.
	 *
	 * <p><b>Caution:</b>If the divisor is '0' there is no exception,
	 * instead if the divisor is also '0' you will get Double.NaN as the result.
	 *
	 *
	 * @param dividend	the dividend number
	 * @param divisor	the divisor number
	 * @return the floor modulus
	 */
	public static SlicedDouble floorMod(SlicedDouble dividend, SlicedDouble divisor) {
		SlicedDouble result = floorDiv(dividend, divisor);
		
		if (result.getDouble() != null) {
			if (result.getDouble() == Double.NaN) return result;
			else if (result.getDouble() == Double.POSITIVE_INFINITY || 
				result.getDouble() == Double.NEGATIVE_INFINITY) {
				if (dividend.getDouble() == Double.POSITIVE_INFINITY || dividend.getDouble() == Double.NEGATIVE_INFINITY)
					return new SlicedDouble(Double.NaN);
				else return dividend;
			} else if (result.getDouble() == 0.0) {	
				return dividend;
			}
		}
		
		String negativesign;
		if ((!result.isNegative() && !divisor.isNegative()) ||
			result.isNegative() && divisor.isNegative()) negativesign = "";
		else negativesign = "-";
		
		result = innerMult(divisor, result, negativesign);
		result = checkExponentExtremum(result, 1024, -1075);
		if (result.getDouble() != null) {
			if (result.getDouble() == Double.POSITIVE_INFINITY) {
				if (negativesign.isEmpty()) return new SlicedDouble(Double.NEGATIVE_INFINITY);
				else return result;
			} else {
				if (negativesign.isEmpty()) result.setSign("-");
				else result.setSign("");
			}
		}
		
		return innerSub(dividend, result);
	}
	
	// 
	/**
	 * Returns result of the division of dividend and divisor
	 *
	 * <p>It can be used in a sheaf with {@code intPower()}, {@code fractPower()}
	 * and {@code innerMult()} methods to produce the same result,
	 * as with using {@code pow()} method but without limitation  those that is
	 * of {@code double} type except that it also does not extracting roots 
	 * from negative numbers. The only limit that can happen
	 * is an overflow of {@code integer} that holds an exponent if your
	 * value will be too small or too large. But you can simply handle it by
	 * checking inputs and outputs if such scenario may take a place.
	 *
	 * <p>For example:<br>
	 *
	 * {@code SlicedDouble number = new SlicedDouble(3432543.23423);}<br>
	 * {@code SlicedDouble power = new SlicedDouble(-33.23423);}<br><br>
	 *
	 * {@code SlicedDouble intpowresult, fractpowresult, multresult, divresult;}<br><br>
	 *
	 * {@code intpowresult = MiniMath.intPowerNoLimits(number, power);}<br>
	 * {@code fractpowresult = MiniMath.fractPower(number, power);}<br>
	 * {@code multresult = MiniMath.innerMult(intpowresult, fractpowresult, intpowresult.getNegativeSign());}<br>
	 * {@code divresult = MiniMath.innerDiv(new SlicedDouble(1.0), multresult, multresult.getNegativeSign(), 0);}
	 *
	 * <p>That would be striclty as if you were using 
	 * {@code MiniMath.pow(3432543.23423, -33.23423)} but in the true
	 * {@code SlicedDouble} format.
	 *
	 * <p>The featuresign parameter accept several different values:
	 * <ul> 
	 *	<li>if (featuresign == 0) - returns result of full division;</li>
	 *	<li>if (featuresign == 1) - returns result of integer division<br>
	 *	(exmaple of use: 
	 *	{@code SlicedDouble floordiv = MiniMath.innerDiv(dividend, divisor, "", 1).getIntSD();});</li>
	 *	<li>if (featuresign == 2) - returns result floor division<br>
	 *	(exmaple of use: 
	 *	{@code SlicedDouble floordiv = MiniMath.innerDiv(dividend, divisor, "", 2).getIntSD();});</li>
	 *	<li>if (featuresign == 3) - returns remainder of division;</li>
	 * </ul>
	 *
	 * @param dividend		the dividend number
	 * @param divisor		the divisor number
	 * @param negativesign	the negativesign
	 * @param featuresign	the operation sign
	 * @return the quotient
	 */
	public static SlicedDouble innerDiv(SlicedDouble dividend, SlicedDouble divisor, String negativesign, int featuresign) {
		if (dividend.getDouble() != null && Double.isNaN(dividend.getDouble()))
			return dividend;
		else if (divisor.getDouble() != null && Double.isNaN(divisor.getDouble()))
			return divisor;
		else if (dividend.getBinaryRaw().isEmpty() && divisor.getBinaryRaw().isEmpty()) {
			return new SlicedDouble(Double.NaN);
		}
			
		String dividendraw = dividend.getBinaryRaw();
		String checksorraw = divisor.getBinaryRaw();
		long divisorlong;
		
		// for simplifying calculation we only use 63 bits of maximum 64 in divisor
		// and here we cutting off the excess bit and then making simple rounding
		if (checksorraw.length() == 64) {
			divisorlong = Long.parseLong(checksorraw.substring(0, 63), 2);
			if (checksorraw.charAt(63) == '1') divisorlong++;
			checksorraw = checksorraw.substring(0, 63);
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
		
		// following steps will produce the rest of the result
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
			// if we perform floorDiv and the result is negative and there is a remainder
			// we need to handle it by substracion of -1 to the final result before returning it
			if (featuresign == 2 && negativesign.length() > 0 && resultexp < 53 && 
				dividendraw.indexOf('1') > 0xffffffff) {
				return new SlicedDouble(
					new SlicedDouble(quotient, resultexp, negativesign)
						.getIEEE754() + 0xffffffff);
						
			}
			return new SlicedDouble(quotient, resultexp, negativesign);
		} else {
			return new SlicedDouble(dividendraw, resultexp, negativesign);
		}
	}
	
	/**
	 * Auxiliary method to get quotient exponent
	 */
	private static int getDivisionExponent(int dividendexp, int divisorexp, int initialzero) {
		return dividendexp + ~divisorexp + ~initialzero + 2;
	}
	
	/**
	 * Auxiliary method to get division remainder exponent
	 */
	private static int getRemainderExponent(int dividendexp, String dividendraw) {
		int remainderstart = dividendraw.indexOf('1');
		
		if (remainderstart == 0xffffffff) return 0;
		else return dividendexp + ~dividendraw.indexOf('1') + 1;
	}
	
	/**
	 * Returns absolute value of giving number.
	 *
	 * <p>If number is positive, result will be the same,
	 * and if not result will be -number;
	 *
	 * @param number the number
	 * @return the absolute value of a giving number
	 */
	public static Double abs(double number) {
		return abs(new SlicedDouble(number)).getIEEE754();
	}
	
	/**
	 * Returns absolute value of giving number.
	 *
	 * <p>If number is positive, result will be the same,
	 * and if not result will be -number;
	 *
	 * <p>This method do not implement mutation, instead
	 * the new object of {@code SlicedDouble} type is created.
	 *
	 * @param number the number
	 * @return the absolute value of a giving number
	 */
	public static SlicedDouble abs(SlicedDouble number) {
		SlicedDouble numberclone = number.clone();
		numberclone.setSign("");
		
		return numberclone;
	}
	
	/**
	 * Returns the largest (closest to positive infinity) number that is 
	 * less than or equal to the argument and is equal to a mathematical integer.
	 *
	 * @param number the number
	 * @return the largest (closest to positive infinity) number that is 
	 * 			less than or equal to the argument and is equal to a mathematical integer.
	 */
	public static Double floor(double number) {	
		return floor(new SlicedDouble(number)).getIEEE754(); 
	}
	
	/**
	 * Returns the largest (closest to positive infinity) number that is 
	 * less than or equal to the argument and is equal to a mathematical integer.
	 *
	 * @param number the number
	 * @return the largest (closest to positive infinity) number that is 
	 * 			less than or equal to the argument and is equal to a mathematical integer.
	 */
	public static SlicedDouble floor(SlicedDouble number) {
		double dblnumber = number.getIntSD().getIEEE754();
		
		if (Double.isNaN(dblnumber) || dblnumber == Double.POSITIVE_INFINITY || 
			dblnumber == Double.NEGATIVE_INFINITY || number.getNegativeSign().isEmpty())
			return number.getIntSD();
		else {
			if (number.getFractRaw().length() > 0) return new SlicedDouble(dblnumber + 0xffffffff);
			else return number.getIntSD();
		}
	}
	
	/**
	 * Returns the smallest (closest to negative infinity) number that is greater
	 * than or equal to the argument and is equal to a mathematical integer
	 *
	 * @param number the number
	 * @return the smallest (closest to negative infinity) number that is greater 
	 *			than or equal to the argument and is equal to a mathematical integer
	 */
	public static Double ceil(double number) {
		return ceil(new SlicedDouble(number)).getIEEE754(); 
	}
	
	/**
	 * Returns the smallest (closest to negative infinity) number that is greater
	 * than or equal to the argument and is equal to a mathematical integer
	 *
	 * @param number the number
	 * @return the smallest (closest to negative infinity) number that is greater 
	 *			than or equal to the argument and is equal to a mathematical integer
	 */
	public static SlicedDouble ceil(SlicedDouble number) {
		double dblnumber = floor(number).getIEEE754();
		
		if (number.getFractRaw() != null && number.getFractRaw().length() > 0) {
			++dblnumber;
			if (dblnumber == 0.0) return new SlicedDouble("", 0, "-");
		} 
		
		return new SlicedDouble(dblnumber);
	}
	
	/**
	 * Returns value of fractional part of a giving number that is closest to zero
	 *
	 * @param number the number
	 * @return value of fractional part of a giving number that is closest to zero
	 */
	public static Double fraction(double number) {
		return fraction(new SlicedDouble(number)).getIEEE754(); 
	}
	
	/**
	 * Returns value of fractional part of a giving number that is closest to zero
	 *
	 * @param number the number
	 * @return value of fractional part of a giving number that is closest to zero
	 */
	public static SlicedDouble fraction(SlicedDouble number) {
		return number.getFractSD();
	}
	
	/**
	 * Return result of substracion
	 *
	 * @param minuend 		the minuend
	 * @param subtrahend 	the subtrahend
	 * @return the residual
	 */
	public static Double substraction(double minuend, double subtrahend) {
		return substraction(new SlicedDouble(minuend), new SlicedDouble(subtrahend)).getIEEE754();
	}
	
	/**
	 * Return result of substracion
	 *
	 * <p><b>Important.</b>
	 * <p>This method works directly with {@code SlicedDouble} type of inputs.
	 * If the arguments are not holding compilated {@code double} numbers
	 * them will be computed, so if you do not wish to lose advantages of
	 * using {@code SlicedDouble} type, you should not use this method.
	 * Instead you may use {@code innerSub()}:
	 *
	 * @param minuend 		the minuend
	 * @param subtrahend 	the subtrahend
	 * @return the residual
	 */
	public static SlicedDouble substraction(SlicedDouble minuend, SlicedDouble subtrahend) {
		double minuendnum = minuend.getIEEE754(), subtrahendnum = subtrahend.getIEEE754();
		
		if (Double.isNaN(minuendnum) || Double.isNaN(subtrahendnum))
			return new SlicedDouble(Double.NaN);
		else if (minuendnum == Double.POSITIVE_INFINITY) {
			if (subtrahendnum == Double.POSITIVE_INFINITY) return new SlicedDouble(Double.NaN);
			else return minuend;
		} else if (minuendnum == Double.NEGATIVE_INFINITY) {
			if (subtrahendnum == Double.NEGATIVE_INFINITY) return new SlicedDouble(Double.NaN);
			else return minuend;
		} else if (subtrahendnum == Double.POSITIVE_INFINITY)
			return new SlicedDouble(Double.NEGATIVE_INFINITY);
		else if (subtrahendnum == Double.NEGATIVE_INFINITY)
			return new SlicedDouble(Double.POSITIVE_INFINITY);
		else if (subtrahendnum == 0.0) return minuend;
		else if (minuendnum == 0.0)  {
			SlicedDouble clonesubtrahend = subtrahend.clone();
			clonesubtrahend.reverseSign();
			return clonesubtrahend;
		}
		
		SlicedDouble result = innerSub(minuend, subtrahend);
		result = checkExponentExtremum(result, 1024, -1075);
		
		return result;
	}
	
	/**
	 * Returns the residual of inputs
	 *
	 * <p>You may use it to get an intermidiate {@code SlicedDouble} type
	 * result that is not in limited to {@code double} range.
	 *
	 * <p>For example:<br>
	 *
	 * {@code SlicedDouble dividend = new SlicedDouble(345.34);}<br>
	 * {@code SlicedDouble divisor = new SlicedDouble(23.53);}<br>
	 * {@code SlicedDouble floordiv = MiniMath.innerDiv(dividend, divisor, "", 2).getIntSD();}<br>
	 * {@code SlicedDouble innermult = MiniMath.innerMult(floordiv, divisor, floordiv.getNegativeSign());}<br>
	 * {@code SlicedDouble innersub = MiniMath.innerSub(dividend, innermult);}
	 *
	 *
	 * <p>That would be striclty as if you were using 
	 * {@code MiniMath.floorMod(345.34, 23.53)} but in the true
	 * {@code SlicedDouble} format.
	 *
	 * @param minuend 		the minuend
	 * @param subtrahend 	the subtrahend
	 * @return the residual
	 */
	public static SlicedDouble innerSub(SlicedDouble minuend, SlicedDouble subtrahend) {
		if (minuend.getDouble() != null && Double.isNaN(minuend.getDouble()))
			return minuend;
		else if (subtrahend.getDouble() != null && Double.isNaN(subtrahend.getDouble()))
			return subtrahend;
		
		// because we want to hold SlicedDouble format with inner methods, 
		// we must to keep raw format as it is, and do not evaluate double value
		// cause it may not exist for this time (if we calling .getDouble(),
		// or if we call .getIEEE754() it may brings current object to unwanted mutation
		if (minuend.getBinaryRaw().length() == 0) {
			if (subtrahend.getBinaryRaw().length() > 0) {
				SlicedDouble subclone = subtrahend.clone();
				subclone.reverseSign();
				return subclone;
			} else return minuend;
		}
		if (subtrahend.getBinaryRaw().length() == 0) return minuend;
		
		if ((!minuend.isNegative() && subtrahend.isNegative())) {
			SlicedDouble number2 = subtrahend.clone();
			number2.setSign("");
			return innerSum(minuend, number2);
		}
		if (minuend.isNegative() && !subtrahend.isNegative()) {
			SlicedDouble number2 = subtrahend.clone();
			number2.setSign("-");
			return innerSum(minuend, number2);
		}
		
		String negativesign = "";
		String minraw, subraw;
		int minexp, subexp;
		
		if (subtrahend.getExp() > minuend.getExp()) {
			minraw = subtrahend.getBinaryRaw(); subraw = minuend.getBinaryRaw();
			minexp = subtrahend.getExp(); subexp = minuend.getExp();
			if (subtrahend.isNegative()) negativesign = "";
			else negativesign = "-";
		} else {
			minraw = minuend.getBinaryRaw(); subraw = subtrahend.getBinaryRaw();
			minexp = minuend.getExp(); subexp = subtrahend.getExp();
			if (minuend.isNegative()) negativesign = "-";
			else negativesign = "";
		}
		
		String prezeros = "";
		int expdiff = minexp + ~subexp + 1;
		for (int i = 0; i < expdiff && i < 64; i++) prezeros += '0';
		subraw = prezeros + subraw;
		for (int i = subraw.length(); i < minraw.length(); i++) subraw += '0';
		
		int len;
		if (subraw.length() > 64) { subraw = subraw.substring(0, 64); len = 64; }
		else len = subraw.length();
		for (int i = minraw.length(); i < len; i++) minraw += '0';
		
		long minlong = Long.parseUnsignedLong(minraw, 2);
		long sublong = Long.parseUnsignedLong(subraw, 2);
		
		long residual = minlong + ~sublong + 1;

		// we must to reverse long if it's negative and if subtrahend is less than 64 chars
		if (residual < 0) {
			if (subraw.indexOf('1') == 0) residual = ~residual + 1;
			// and also if (minexp == subexp) we need to change sign of result
			if (minexp == subexp) {
				if (negativesign.isEmpty()) negativesign = "-";
				else negativesign = "";
			}
		}
		
		String residualstr = Long.toBinaryString(residual);
		int resultexp = getSubstractionExponent(residualstr, minexp, subexp, minraw);
		
		return new SlicedDouble(residualstr, resultexp, negativesign);
	}
	
	/**
	 * Auxiliary method to get residual exponent
	 */
	private static int getSubstractionExponent(String residualstr, int minexp, int subexp, String minraw) {
		if (residualstr.indexOf('1') == 0xffffffff) return 0;
		if (minraw.indexOf('1') == 0xffffffff) return subexp;
		
		return minexp + ~(minraw.length() + ~residualstr.length() + 1) + 1;
	}
	
	/**
	 * Returns the sum
	 *
	 * @param number1 the addendum1
	 * @param number2 the addendum2
	 * @return sum
	 */
	public static Double sum(double number1, double number2) {
		return sum(new SlicedDouble(number1), new SlicedDouble(number2)).getIEEE754();
	}
	
	/**
	 * Returns the sum
	 *
	 * <p><b>Important.</b>
	 * <p>This method works directly with {@code SlicedDouble} type of inputs.
	 * If the arguments are not holding compilated {@code double} numbers
	 * them will be computed, so if you do not wish to lose advantages of
	 * using {@code SlicedDouble} type, you should not use this method.
	 * Instead you may use {@code innerSum()}:
	 *
	 * @param number1 the addendum1
	 * @param number2 the addendum2
	 * @return sum
	 */
	public static SlicedDouble sum(SlicedDouble number1, SlicedDouble number2) {
		double dblnumber1 = number1.getDouble(), dblnumber2 = number2.getDouble();
		
		if (Double.isNaN(dblnumber1) || Double.isNaN(dblnumber2))
			return new SlicedDouble(Double.NaN);
		if (dblnumber1 == Double.POSITIVE_INFINITY) {
			if (dblnumber2 == Double.NEGATIVE_INFINITY) return new SlicedDouble(Double.NaN);
			else return number1;
		} else if (dblnumber1 == Double.NEGATIVE_INFINITY) {
			if (dblnumber2 == Double.POSITIVE_INFINITY) return new SlicedDouble(Double.NaN);
			else return number1;
		} else if (dblnumber2 == Double.POSITIVE_INFINITY || 
			dblnumber2 == Double.NEGATIVE_INFINITY) 
			return number2;
			
		return innerSum(number1, number2);
	}
	
	/**
	 * Returns the sum without pre and post compilations to {@code double} type
	 *
	 * @param number1 the addendum1
	 * @param number2 the addendum2
	 * @return sum
	 */
	public static SlicedDouble innerSum(SlicedDouble number1, SlicedDouble number2) {
		if (number1.getDouble() != null && Double.isNaN(number1.getDouble()))
			return number1;
		else if (number2.getDouble() != null && Double.isNaN(number2.getDouble()))
			return number2;
		
		if (number1.getBinaryRaw().length() == 0) {
			if (number2.getBinaryRaw().length() == 0 && number2.isNegative()) {
				SlicedDouble num2clone = number2.clone();
				number2.setSign("");
				return number2;
			}	
			else return number2;
		} else if (number2.getBinaryRaw().length() == 0) return number1;
		
		if (!number1.isNegative() && number2.isNegative()) {
			SlicedDouble subtrahend = number2.clone();
			subtrahend.setSign("");
			return innerSub(number1, subtrahend);
		}
		if (number1.isNegative() && !number2.isNegative()) {
			SlicedDouble subtrahend = number1.clone();
			subtrahend.setSign("");
			return innerSub(number2, subtrahend);
		} 
		
		String negativesign;
		if (number1.isNegative()) negativesign = "-";
		else negativesign = "";
		
		String num1raw, num2raw;
		int num1exp, num2exp;
		
		if (number2.getExp() > number1.getExp()) {
			num1raw = number2.getBinaryRaw(); num2raw = number1.getBinaryRaw();
			num1exp = number2.getExp(); num2exp = number1.getExp();
		} else {
			num1raw = number1.getBinaryRaw(); num2raw = number2.getBinaryRaw();
			num1exp = number1.getExp(); num2exp = number2.getExp();
		}
		
		String prezeros = "";
		int expdiff = num1exp + ~num2exp + 1;
		for (int i = 0; i < expdiff && i < 64; i++) prezeros += '0';
		num2raw = prezeros + num2raw;
		for (int i = num2raw.length(); i < num1raw.length(); i++) num2raw += '0';
		
		int len;
		if (num2raw.length() > 64) { num2raw = num2raw.substring(0, 64); len = 64; }
		else len = num2raw.length();
		for (int i = num1raw.length(); i < len; i++) num1raw += '0';

		long num1long = Long.parseUnsignedLong(num1raw, 2);
		long num2long = Long.parseUnsignedLong(num2raw, 2);
		
		long sum = num1long + num2long;
		
		String sumstr = Long.toBinaryString(sum);
		
		if (sumstr.length() > num1raw.length()) num1exp++;
		// if there is overflow we need to handle it correctly
		else if (sumstr.length() < num1raw.length()) {
			prezeros = "";
			for (int i = sumstr.length(); i < num1raw.length(); i++) prezeros += '0';
			sumstr = '1' + prezeros + sumstr;
			if (sumstr.length() > 64 && sumstr.charAt(64) == '1') {
				sumstr = sumstr.substring(0, 64);
				sum = Long.parseUnsignedLong(sumstr, 2);
				sum++;
				sumstr = Long.toBinaryString(sum);
			}
			num1exp++;
		}

		return new SlicedDouble(sumstr, num1exp, negativesign);
	}
	
	public static void main(String[] args) {
		//-2.7973453643371784E-18!
//8.65966101108157E305
//-0.0
//-4.9E-324
		
		//out.println(division(-2.7973453643371784E-18, 8.65966101108157E305));
		//testSum();
		//testSubstraction();
		//testDivision();
		//testIntegerDivision();
		//testIntegerFloorDivision();
		//testRemainderOfDivision();
		//testFloorModulus();
		//testCeil();
		//testFloor();
	    //testPowInteger();
		//testPow();
		
	}
	
	/**
	 * Test of pow method
	 */
	public static void testPow() {
		double factor1 = 100000000000000000000000000000000000000000.0, factor2;
		int counter = 0;
		
		for (int i = 0; i < 80; i++) {
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
					out.println(Long.toBinaryString(Double.doubleToLongBits(result2)));
					counter++;
					}
				}
			}
		}
		
		out.println(counter + " results in real number powers test that have missed accuracy");
	}
	
	/**
	 * Test of pow method with integer powers
	 */
	public static void testPowInteger() {
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
					out.println(Long.toBinaryString(Double.doubleToLongBits(result2)));
					counter++;
					}
				}
			}
		}
		
		out.println(counter + " results in integer number powers test that have missed accuracy");
	}
	
	/**
	 * Test of summing
	 */
	public static void testSum() {
		double factor1 = 1.0E308, factor2;
		int counter = 0;
		
		for (int i = 0; i < 632; i++) {
			factor1 = factor1 / 10; out.println(factor1 + " sum factor");
			out.println("");
			factor2 = 1.0E308;
			for (int j = 0; j < 6500; j++) {
				if (j % 10 == 0) factor2 = factor2 / 10;
				double number1 = Math.random()*factor2, number2 = Math.random()*factor1;
				if (j % 2 == 0) number1 = -number1;
				if (j % 3 == 0) number2 = -number2;
				double result1 = sum(number1, number2), result2 = number1 + number2;
				if (result1 != result2 && (!Double.isNaN(result1) && !Double.isNaN(result1))) {
					if ((Double.toString(result1).length() < 15 || Double.toString(result2).length() < 15 || !Double.toString(result1).substring(0, 15).equals(Double.toString(result2).substring(0, 15)))) {
						out.println(number1 + "!");
						out.println(number2);
						out.println(result1);
						out.println(result2);
						out.println(Long.toBinaryString(Double.doubleToLongBits(result1)));
						out.println(Long.toBinaryString(Double.doubleToLongBits(result2)));
						counter++;
					}
				}
			}
		}

		out.println(counter + " results in summing test that have missed accuracy");
	}
	
	/**
	 * Test of substraction
	 */
	public static void testSubstraction() {
		double factor1 = 1.0E308, factor2;
		int counter = 0;
		
		for (int i = 0; i < 632; i++) {
			factor1 = factor1 / 10; out.println(factor1 + " minuend factor");
			out.println("");
			factor2 = 1.0E308;
			for (int j = 0; j < 6500; j++) {
				if (j % 10 == 0) factor2 = factor2 / 10;
				double minuend = Math.random()*factor2, subtrahend = Math.random()*factor1;
				if (j % 2 == 0) minuend = -minuend;
				if (j % 3 == 0) subtrahend = -subtrahend;
				double result1 = substraction(minuend, subtrahend), result2 = minuend - subtrahend;
				if (result1 != result2 && (!Double.isNaN(result1) && !Double.isNaN(result1))) {
					if ((Double.toString(result1).length() < 15 || Double.toString(result2).length() < 15 || !Double.toString(result1).substring(0, 15).equals(Double.toString(result2).substring(0, 15)))) {
						out.println(minuend + "!");
						out.println(subtrahend);
						out.println(result1);
						out.println(result2);
						out.println(Long.toBinaryString(Double.doubleToLongBits(result1)));
						out.println(Long.toBinaryString(Double.doubleToLongBits(result2)));
						counter++;
					}
				}
			}
		}
		
		out.println(counter + " results in substraction test that have missed accuracy");
	}
	
	/**
	 * Test of division
	 */
	public static void testDivision() {
		double factor1 = 1.0E308, factor2;
		int counter = 0;
		
		for (int i = 0; i < 632; i++) {
			factor1 = factor1 / 10; out.println(factor1 + " dividend factor");
			out.println("");
			factor2 = 1.0E308;
			for (int j = 0; j < 6500; j++) {
				if (j % 10 == 0) factor2 = factor2 / 10;
				double dividend = Math.random()*factor2, divisor = Math.random()*factor1;
				if (j % 2 == 0) dividend = -dividend;
				if (j % 3 == 0) divisor = -divisor;
				double result1 = division(dividend, divisor), result2 = dividend / divisor;
				if (result1 != result2 && (!Double.isNaN(result1) && !Double.isNaN(result1)) &&
					(abs(result2) > 1.0E-307 || abs(result2) < 1.0E-311)) {
					out.println(dividend + "!");
					out.println(divisor);
					out.println(result1);
					out.println(result2);
					out.println(Long.toBinaryString(Double.doubleToLongBits(result1)));
					out.println(Long.toBinaryString(Double.doubleToLongBits(result2)));
					counter++;
				}
			}
		}
		
		out.println(counter + " results in full division test have missed accuracy");
	}
	
	/**
	 * Test of integer division
	 */
	public static void testIntegerDivision() {
		double factor1 = 2100000000, factor2;
		int counter = 0;
		
		for (int i = 0; i < 10; i++) {
			factor1 = factor1 / 10; out.println(factor1 + " dividend factor");
			out.println("");
			factor2 = 2100000000;
			for (int j = 0; j < 1000; j++) {
				if (j % 100 == 0) factor2 = factor2 / 10;
				double dividend = Math.random()*factor2, divisor = Math.random()*factor1;
				if ((int) divisor == 0.0) continue;
				if (j % 2 == 0) dividend = -dividend;
				if (j % 3 == 0) divisor = -divisor;
				int idividend = (int) dividend, idivisor = (int) divisor;
				double result1 = div((double)idividend, (double)idivisor);
				int result2 = idividend / idivisor;
				if (result1 != result2 && (!Double.isNaN(result1) && !Double.isNaN(result1))) {
					out.println(dividend + "!");
					out.println(divisor);
					out.println(result1);
					out.println(result2);
					out.println(Long.toBinaryString(Double.doubleToLongBits(result1)));
					out.println(Long.toBinaryString(Double.doubleToLongBits(result2)));
					counter++;
				}
			}
		}
		
		out.println(counter + " results in integer division test have missed accuracy");
	}
	
	/**
	 * Test of floor integer division
	 */
	public static void testIntegerFloorDivision() {
		double factor1 = 2100000000, factor2;
		int counter = 0;
		
		for (int i = 0; i < 10; i++) {
			factor1 = factor1 / 10; out.println(factor1 + " dividend factor");
			out.println("");
			factor2 = 2100000000;
			for (int j = 0; j < 1000; j++) {
				if (j % 100 == 0) factor2 = factor2 / 10;
				double dividend = Math.random()*factor2, divisor = Math.random()*factor1;
				if ((int) divisor == 0.0) continue;
				if (j % 2 == 0) dividend = -dividend;
				if (j % 3 == 0) divisor = -divisor;
				int idividend = (int) dividend, idivisor = (int) divisor;
				double result1 = floorDiv((double)idividend, (double)idivisor);
				int result2 = Math.floorDiv(idividend, idivisor);
				if (result1 != result2 && (!Double.isNaN(result1) && !Double.isNaN(result1))) {
					out.println(dividend + "!");
					out.println(divisor);
					out.println(result1);
					out.println(result2);
					out.println(Long.toBinaryString(Double.doubleToLongBits(result1)));
					out.println(Long.toBinaryString(Double.doubleToLongBits(result2)));
					counter++;
				}
			}
		}
		
		out.println(counter + " results in integer floor division test have missed accuracy");
	}
	
	/**
	 * Test of division remainder extracting
	 */
	public static void testRemainderOfDivision() {
		double factor1 = 2100000000, factor2;
		int counter = 0;
		
		for (int i = 0; i < 10; i++) {
			factor1 = factor1 / 10; out.println(factor1 + " dividend factor");
			out.println("");
			factor2 = 2100000000;
			for (int j = 0; j < 1000; j++) {
				if (j % 100 == 0) factor2 = factor2 / 10;
				double dividend = Math.random()*factor2, divisor = Math.random()*factor1;
				if ((int) divisor == 0.0) continue;
				if (j % 2 == 0) dividend = -dividend;
				if (j % 3 == 0) divisor = -divisor;
				int idividend = (int) dividend, idivisor = (int) divisor;
				double result1 = divisionRemainder((double)idividend, (double)idivisor);
				int result2 = idividend % idivisor;
				if (result1 != result2 && (!Double.isNaN(result1) && !Double.isNaN(result1))) {
					out.println(dividend + "!");
					out.println(divisor);
					out.println(result1);
					out.println(result2);
					out.println(Long.toBinaryString(Double.doubleToLongBits(result1)));
					out.println(Long.toBinaryString(Double.doubleToLongBits(result2)));
					counter++;
				}
			}
		}
		
		out.println(counter + " results in getting remainder of integer division test have missed accuracy");
	}
	
	/**
	 * Test of floor modulus obtaining
	 */
	public static void testFloorModulus() {
		double factor1 = 2100000000, factor2;
		int counter = 0;
		
		for (int i = 0; i < 10; i++) {
			factor1 = factor1 / 10; out.println(factor1 + " dividend factor");
			out.println("");
			factor2 = 2100000000;
			for (int j = 0; j < 1000; j++) {
				if (j % 100 == 0) factor2 = factor2 / 10;
				double dividend = Math.random()*factor2, divisor = Math.random()*factor1;
				if ((int) divisor == 0.0) continue;
				if (j % 2 == 0) dividend = -dividend;
				if (j % 3 == 0) divisor = -divisor;
				int idividend = (int) dividend, idivisor = (int) divisor;
				double result1 = floorMod((double)idividend, (double)idivisor);
				int result2 = Math.floorMod(idividend, idivisor);
				if (result1 != result2 && (!Double.isNaN(result1) && !Double.isNaN(result1))) {
					out.println(dividend + "!");
					out.println(divisor);
					out.println(result1);
					out.println(result2);
					out.println(Long.toBinaryString(Double.doubleToLongBits(result1)));
					out.println(Long.toBinaryString(Double.doubleToLongBits(result2)));
					counter++;
				}
			}
		}
		
		out.println(counter + " results in getting floor modulus of integer division test have missed accuracy");
	}
	
	/**
	 * Test of intger floor obtaining
	 */
	public static void testFloor() {
		double factor1 = 1.0E308;
		int counter = 0;
		
		for (int i = 0; i < 63200; i++) {
			if (i % 100 == 0) { 
				factor1 = factor1 / 10; 
				out.println(factor1 + " number random factor"); 
			}
			double num = Math.random()*factor1;
			double result1 = floor(num), result2 = Math.floor(num);
			if (result1 != result2 && (!Double.isNaN(result1) && !Double.isNaN(result1))) {
				out.println(num + "!");
				out.println(result1);
				out.println(result2);
				out.println(Long.toBinaryString(Double.doubleToLongBits(result1)));
				out.println(Long.toBinaryString(Double.doubleToLongBits(result2)));
				counter++;
			}
		}
		
		out.println(counter + " results in floor test have missed accuracy");
	}
	
	/**
	 * Test of integer ceil obtaining
	 */
	public static void testCeil() {
		double factor1 = 1.0E308;
		int counter = 0;
		
		for (int i = 0; i < 63200; i++) {
			if (i % 100 == 0) { 
				factor1 = factor1 / 10; 
				out.println(factor1 + " number random factor"); 
			}
			double num = Math.random()*factor1;
			double result1 = ceil(num), result2 = Math.ceil(num);
			if (result1 != result2 && (!Double.isNaN(result1) && !Double.isNaN(result1))) {
				out.println(num + "!");
				out.println(result1);
				out.println(result2);
				out.println(Long.toBinaryString(Double.doubleToLongBits(result1)));
				out.println(Long.toBinaryString(Double.doubleToLongBits(result2)));
				counter++;
			}
		}
		
		out.println(counter + " results in ceil test have missed accuracy");
	}
		
		
	
	// In spite of that it do a rounding like it assumed in the standart
	// due to only 64 bits availible it returns a rounding that is less
	// accurate that a simple one that presents in SlicedDouble
	// service method getRoundedRawBin(). It is not deleted for now
	// because of possibility to adding additional accuracy in the
	// future (if there be extra number that would be holding some extra bits).
	/*
	private static double roundResult(SlicedDouble number) throws NullPointerException {
		String raw = number.getBinaryRaw();
		if (raw == null || raw.length() < 54) return number.getIEEE754();
		
		SlicedDouble a = new SlicedDouble(raw.substring(0, 53), number.getExp(), "");
		long longraw = Long.parseUnsignedLong(raw.substring(0, 53), 2);
		longraw++;
		String longb = Long.toBinaryString(longraw);
		int expb = number.getExp();
		if (longb.length() == 54) {
			longb = "1";
			expb++;
		}
		
		SlicedDouble b = new SlicedDouble(longb, expb, "");
		SlicedDouble square = innerMult(number, number, "");
		SlicedDouble product = innerMult(a, b, "");
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
	}
	*/
}