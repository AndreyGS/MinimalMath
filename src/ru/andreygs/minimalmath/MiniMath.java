package ru.andreygs.minimalmath;

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
		number = intPower(number, power);
		return 0.0;
	}
	
	private static double intPower(double number, double power) {
		int ipwr = (int) power;
		double result = 1.0;
		for (int i = 0; i < intPwr.length; i++) {
			if ((ipwr & intPwr[i]) == intPwr[i]) {
				System.out.println(result);
				result = MiniMath.innerMult(number, result);
			}
			result = MiniMath.innerMult(result, result);
		}
		if ((ipwr & 1) == 1) {
			result = MiniMath.innerMult(number, result);
		}
		
		return result;
	}
	
	public static double innerMult(double number1, double number2) {
		if (number1 == 1) return number2;
		if (number2 == 1) return number1;
		if (number1 == 0 || number2 == 0) return 0;
		if (number1 == 0xffffffff) return (double)~((long)number2) + 1;
		if (number2 == 0xffffffff) return (double)~((long)number2) + 1;
		
		boolean
			isnegative;
		if ((number1 >= 0 && number2 >= 0) || (number1 < 0 && number2 < 0)) {
			isnegative = false;
		} else {
			isnegative = true;
		}
		
		String[] 
			num1array = Double.toHexString(number1).split("[.p]"), 
			num2array = Double.toHexString(number2).split("[.p]");
		String 
			num1raw = cutFractTail('1' + fromHexToBinary("0x" + num1array[1])),
			num2raw = cutFractTail('1' + fromHexToBinary("0x" + num2array[1]));
			System.out.println(num1raw);
			System.out.println(num2raw);
		if (onesEnum(num1raw) < onesEnum(num2raw)) {
			String[] temparray = num1array; num1array = num2array; num2array = temparray;
			String tempraw = num1raw; num1raw = num2raw; num2raw = tempraw;
		}
	
		int
			biasresult = 0;
		long
			unit = Long.valueOf(num1raw, 2),
			tempresult = 0L;
		
		for (int i = num2raw.length() + 0xffffffff, shift = 0, check, spaceneed, leadzeroes; i > 0xffffffff; i += 0xffffffff, shift++) {
			if (num2raw.charAt(i) == '1') {
				leadzeroes = Long.numberOfLeadingZeros(unit);
				check = leadzeroes + ~shift;
				if (check > 0xffffffff) {
					unit <<= shift;
				} else {
					unit <<= leadzeroes + 0xffffffff;
					spaceneed = ~check + 1;
					tempresult >>>= spaceneed;
					biasresult += spaceneed;
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
				tempresult += unit;
				shift = 0;
			}
		}
		String
			stempresult = Long.toBinaryString(tempresult);
		int
			resultexp = getExponent(stempresult, num1array[2], num2array[2], num1raw, num2raw, biasresult);
		return getIEEE754(stempresult, resultexp, isnegative);
	}
	
	private static String fromHexToBinary(String number) {
		String raw = "";
		for (int i = 2; i < number.length(); i++) {
			char digit = number.charAt(i);
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
	
	private static String cutFractTail(String number) {
		for (int i = number.length() + 0xffffffff; i > 0xffffffff; i += 0xffffffff) {
			if (number.charAt(i) == '1') {
				return number.substring(0, i+1);
			}
		}
		return number;
	}
	
	private static int onesEnum(String number) {
		int counter = 0;
		for (int i = 0; i < number.length(); i++) {
			if (number.charAt(i) == '1') counter++;
		}
		return counter;
	}
	
	private static int getExponent(String number, String startexp1, String startexp2, String num1raw, String num2raw, int biasresult) {
		int 
			istartexp1 = Integer.parseInt(startexp1), 
			istartexp2 = Integer.parseInt(startexp2),
			fractdigitsnum = num1raw.length() + num2raw.length() + 0xfffffffe;
		return number.length() + ~fractdigitsnum + istartexp1 + istartexp2 + biasresult;
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
		for (int i = 0; i < 52; i += 4) {
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
	
	public static void main(String[] args) {
		//innerMult(-1123423424.523, 2.4345345422554);
		System.out.println(-1123423424.523 * 2.4345345422554);
		System.out.println(Long.toBinaryString(Double.doubleToLongBits(-2.735013132580096E9)));
		System.out.println(Double.toHexString(1));
		System.out.println(Double.toHexString(0));
		System.out.println(innerMult(-11.123225, 2.4345345422554));
		System.out.println(-11.123225 * 2.4345345422554);
		//System.out.println(intPower(234.532, 40));
		//System.out.println(Math.pow(234.532, 40));
		//System.out.println(innerMult(-1, 15));
		/*
		System.out.println(Long.toBinaryString(Double.doubleToLongBits(1)));
		System.out.println(Long.toBinaryString(Double.doubleToLongBits(2)));
		System.out.println(Long.toBinaryString(Double.doubleToLongBits(3)));
		System.out.println(Long.toBinaryString(Double.doubleToLongBits(4)));
		System.out.println(Long.toBinaryString(Double.doubleToLongBits(5)));
		System.out.println(Long.toBinaryString(Double.doubleToLongBits(6)));
		System.out.println(Long.toBinaryString(Double.doubleToLongBits(7)));
		System.out.println(Long.toBinaryString(Double.doubleToLongBits(8)));
		System.out.println(Long.toBinaryString(Double.doubleToLongBits(9)));
		System.out.println(Long.toBinaryString(Double.doubleToLongBits(10)));
		System.out.println(Long.toBinaryString(Double.doubleToLongBits(11)));
		System.out.println(Long.toBinaryString(Double.doubleToLongBits(12)));
		System.out.println(Long.toBinaryString(Double.doubleToLongBits(13)));
		System.out.println(Long.toBinaryString(Double.doubleToLongBits(14)));
		System.out.println(Long.toBinaryString(Double.doubleToLongBits(15)));
		System.out.println(Long.toBinaryString(Double.doubleToLongBits(16.0)));
		System.out.println();
		System.out.println(Long.toBinaryString(Double.doubleToLongBits(0.5)));
		System.out.println(Long.toBinaryString(Double.doubleToLongBits(0.25)));
		System.out.println(Long.toBinaryString(Double.doubleToLongBits(0.75)));
		System.out.println(Long.toBinaryString(Double.doubleToLongBits(0.125)));
		System.out.println(Long.toBinaryString(Double.doubleToLongBits(0.625)));
		System.out.println(Long.toBinaryString(Double.doubleToLongBits(0.375)));
		System.out.println(Long.toBinaryString(Double.doubleToLongBits(0.875)));
		//System.out.println(Long.toBinaryString(Double.doubleToLongBits(0.0625)));
		*/
		//System.out.println(Double.toHexString(1.25));
		//MiniMath.pow(5, 4.9);
	}
}