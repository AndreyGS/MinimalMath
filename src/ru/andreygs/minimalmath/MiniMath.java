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
		number = intpower(number, power);
		return 0.0;
	}
	
	private static double intpower(double number, double power) {
		int ipwr = (int) power;
		double result = 1.0;
		for (int i = 0; i < intPwr.length; i++) {
			if ((ipwr & intPwr[i]) == 1) {
				result = MiniMath.innerMult(number, result);
			}
			result = MiniMath.innerMult(result, result);
		}
		if ((ipwr & 1) == 1) {
			result = MiniMath.innerMult(number, result);
		}
		
		System.out.println(ipwr);
		
		
		return result;
	}
	
	private static double innerMult(double number1, double number2) {
		double result = 0.0, base;
		String[] 
			num1array = Double.toHexString(number1).split("[.p]"), 
			num2array = Double.toHexString(number2).split("[.p]");
		String 
			num1raw = transformToOnes(num1array[1]),
			num2raw = transformToOnes(num2array[1]);
			
		if (onesEnum(num1raw) < onesEnum(num2raw)) {
			String[] temparray = num1array; num1array = num2array; num2array = temparray;
			String tempraw = num1raw; num1raw = num2raw; num2raw = tempraw;
		}
		
		int
			biasnum1 = Integer.parseInt(num1array[2]),
			biasnum2 = Integer.parseInt(num2array[2]),
			fractbiasnum1 = getFractDigitsNum(num1raw, biasnum1),
			fractbiasnum2 = getFractDigitsNum(num1raw, biasnum2);
		
		long
			tempresult = 0L,
			unit = 0L;
		
		for (int i = num1raw.length() + 0xffffffff; i > 0xffffffff; i += 0xffffffff) {
			if (num1raw.charAt(i) == '1') {
				tempresult = Long.valueOf(num1raw.substring(0, i+1), 2);
				unit = tempresult;
				break;
			}
		}
		
		for (int i = num2raw.length() + 0xffffffff; i > 0xffffffff; i += 0xffffffff) {
			if (num2raw.charAt(i) == '1') {
				num2raw = num2raw.substring(0, i);
				break;
			}
		}	
		
		int 
			biasresult = 0;
			
		tempresult = Long.MAX_VALUE-1;
		unit = tempresult;
		System.out.println(tempresult);
		num2raw = "1";
		for (int i = num2raw.length() + 0xffffffff, tempbias = 1, check, spaceneed, leadzeroes; i > 0xffffffff; i += 0xffffffff, tempbias++) {
			if (num2raw.charAt(i) == '1') {
				leadzeroes = Long.numberOfLeadingZeros(tempresult);
				check = ~tempbias + 1 + leadzeroes;
				System.out.println(leadzeroes);
				System.out.println(check);
				if (check > 0) {
					unit <<= tempbias;
					System.out.println(unit);
				} else {
					if (check < 0) {
						spaceneed = ~(leadzeroes + 0xffffffff) + 1 + check;
						unit <<= ~spaceneed + 1 + tempbias;
						tempresult >>>= spaceneed;
						biasresult += spaceneed;
						
						if ((tempresult + unit) > 0xffffffff) {
							System.out.println("!!");
							unit >>= 1;
							tempresult >>= 1;
							biasresult++;
						}
					} else if ((tempresult + (unit << tempbias)) <= 0xffffffff) {
						unit <<= tempbias;
					} else {
						tempresult >>= 1;
						biasresult++;
					}
					
				}
				tempresult += unit;
				tempbias = 0;
			}
			System.out.println(tempresult);
		}
		
		System.out.println((tempresult - unit)*2);
		System.out.println(Long.toBinaryString(Double.doubleToLongBits(number1)));
		
		
		return Double.valueOf(Long.valueOf(tempresult).toString());
		
	}
	
	private static String transformToOnes(String number) {
		String raw = "1";
		for (int i = 0; i < number.length(); i++) {
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
	
	private static int onesEnum(String number) {
		int counter = 0;
		for (int i = 0; i < number.length(); i++) {
			if (number.charAt(i) == '1') counter++;
		}
		return counter;
	}
	
	private static int getFractDigitsNum(String number, int bias) {
		if (bias < 0) {
			for (int i = number.length() + 0xffffffff; i > 0xffffffff; i += 0xffffffff) {
				if (number.charAt(i) == '1') {
					return ~bias + 1;
				}
			}
		} else {
			for (int i = number.length() + 0xffffffff; i > bias; i += 0xffffffff) {
				if (number.charAt(i) == '1') {
					return ~bias + 1 + i;
				}
			}
		}
		return 0;
	}
	
	public static void main(String[] args) {
		MiniMath.innerMult(2, 15);
		long test = 10000l;
		test += Long.MAX_VALUE << 1;
		
		System.out.println((test >>> 1) == ((Long.MAX_VALUE/2) + 5000l));
		System.out.println((test));
		System.out.println((Long.MAX_VALUE/2) + 5000l);
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
		System.out.println(Double.toHexString(1.25));
		//MiniMath.pow(5, 4.9);
	}
}