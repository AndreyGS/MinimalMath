package ru.andreygs.minimalmath;

import static java.lang.System.*;

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
			out.println(result.getIEEE754());
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
		if (number1.getIEEE754() == 9344.147732546124) {
			out.println(num2raw);
		}
		for (int i = num2raw.length() + 0xffffffff, shift = 0, check, spaceneed, leadzeroes; i > 0xffffffff; i+= 0xffffffff, shift++) {
			if (num2raw.charAt(i) == '1') {
				if (number1.getIEEE754() == 9344.147732546124) {
					out.println(Long.toBinaryString(unit));
				}
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
						if (number1.getIEEE754() == 9344.147732546124) {
							out.println("?");
							out.println(spaceneed);
							out.println(Long.toBinaryString(result));
						}
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
				if (number1.getIEEE754() == 9344.147732546124) {
					out.println(shift);
					out.println(Long.toBinaryString(unit) + "!");
					out.println(biasresult);
				}
				result += unit;
				shift = 0;
			}
		}
		String mantissa = Long.toBinaryString(result);
		int resultexp = getExponent(mantissa, number1.getExp(), number2.getExp(), number1.getBinaryRaw(), number2.getBinaryRaw(), biasresult);
		
		return new SlashedDouble(mantissa, resultexp, negativesign, result);
	}
	
	private static int getExponent(String fraction, int startexp1, int startexp2, String num1raw, String num2raw, int biasresult) {
		int fractdigitsnum = num1raw.length() + num2raw.length() + 0xfffffffe;
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
		//System.out.println(pow(d1, d2));
		//System.out.println(Math.pow(d1, d2));
		System.out.println(pow(96.66513193776815, 21.0));
		System.out.println(Math.pow(96.66513193776815, 21.0));
	}
}