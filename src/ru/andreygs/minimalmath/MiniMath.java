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
				result = MiniMath.innermult(number, result);
			}
			result = MiniMath.innermult(result, result);
		}
		if ((ipwr & 1) == 1) {
			result = MiniMath.innermult(number, result);
		}
		
		System.out.println(ipwr);
		
		
		return result;
	}
	
	private static double innermult(double num1, double num2) {
		double result = 0.0;
		return result;
		
	}
	public static void main(String[] args) {
		
		
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
		
		System.out.println(Long.toBinaryString(Double.doubleToLongBits(0.5)));
		System.out.println(Long.toBinaryString(Double.doubleToLongBits(0.25)));
		System.out.println(Long.toBinaryString(Double.doubleToLongBits(0.75)));
		System.out.println(Long.toBinaryString(Double.doubleToLongBits(0.125)));
		System.out.println(Long.toBinaryString(Double.doubleToLongBits(0.625)));
		System.out.println(Long.toBinaryString(Double.doubleToLongBits(0.375)));
		System.out.println(Long.toBinaryString(Double.doubleToLongBits(0.875)));
		//System.out.println(Long.toBinaryString(Double.doubleToLongBits(0.0625)));
		
		
		
		//MiniMath.pow(5, 4.9);
	}
}