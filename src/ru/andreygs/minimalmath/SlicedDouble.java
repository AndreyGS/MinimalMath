package ru.andreygs.minimalmath;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * The {@code SlicedDouble} class is an auxiliary type, that helps with
 * calculations in {@code MiniMath} class.
 *
 * <p>Its species are exists in two main states:
 *
 * <p>1. When the {@code double} value compilated - after evaluation 
 * of {@code getIEEE754()}method. As you can see that state is using to get 
 * an inputs and outputs when {@code double} value is expected.
 * 
 * <p>2. When there is no rounding of mantissa perfoming by evaluation of
 * {@code getIEEE754()} method and its submethods. That state is using to
 * manipulate the raw values without some of limitations of pure {@code double} format.
 * 
 * <p>It include a few different constructors that are using depend on
 * what input you may or can to proceed.
 *
 * <p>Beacuse of its surrogate nature the number in {@code SlicedDouble} type
 * is exists in sliced form. There is separate mantissa as {@code String}, 
 * an exponent as {@code Integer} and negativesign as {@code String}. These are
 * the base of number that holds in. All other fractions can be comupted with availible
 * instance methods.
 *
 * @author Andrey Grabov-Smetankin
 */
public class SlicedDouble implements Cloneable {
	
	/**
	 * That variable obtain a value by two different ways.
	 * 
	 * <p>First: as an input of respective constructor;
	 * <p>Second: after evaluation of {@code .getIEEE754()} method
	 */
	private Double number;
	
	/**
	 * Holds the negative sign: "-" if number is negative and "" if not
	 */
	private String negativesign;
	
	/**
	 * Mantissa of number without leading and trailing zeros.
	 * It can be empty in case if number is 0.0, or -0.0
	 * or Double.POSITIVE_INFINITY, or Double.NEGATIVE_INFINITY
	 * or Double.NaN;
	 */
	private String raw;
	
	/**
	 * The {@code long} value that obtains by {@code .parseUnsignedLong()}
	 * of raw {@code String}.
	 */
	private Long longraw;
	
	/**
	 * The raw of integer part of number
	 */
	private String intraw;
	
	/**
	 * The raw of fractional part of number
	 */
	private String fractraw;
	
	/**
	 * The exponent of number
	 */
	private Integer exp;
	
	/**
	 * That variable holds number of '1' in the raw
	 */
	private Integer onesnum;
	
	/**
	 * It's a rounded raw that apply when {@code double} format need to
	 * be obtained.
	 */
	private String roundedrawbin;
	
	/**
	 * The hexadecimal sview of rounded raw
	 */
	private String roundedrawhex;
	
	/**
	 * The binary form of view of full {@code double} number 
	 */
	private String ieee754bin;
	
	/**
	 * The hexadecimal form of view of full {@code double} number 
	 */
	private String ieee754hex;
	
	/**
	 * This constructor is using, when {@code double} number is supplied.
	 *
	 * <p>The giving number will be sliced to the mantissa, exponent and negativesign
	 *
	 * <p>The exceptions are {@code Double.POSITIVE_INFINITY} and
	 * {@code Double.NEGATIVE_INFINITY} - there will be no mantissa and exponent. And
	 * {@code Double.NaN} - there negativesign will also be null.
	 *
	 * @param number the {@code double} number
	 */
	public SlicedDouble(double number) {
		this.number = number;
		
		if (number != Double.POSITIVE_INFINITY &&  number != Double.NEGATIVE_INFINITY  &&
			!Double.isNaN(number)) {
			sliceIt();
		} else {
			if (number == Double.POSITIVE_INFINITY) negativesign = "";
			else if (number == Double.NEGATIVE_INFINITY) negativesign = "-";
		}
	}
	
	/**
	 * Constructs a {@code SlicedDouble} number from its inputs
	 *
	 * <p>If empty {@code String} was supplied to negativesign parameter
	 * negativesign will be empty, and equals "-" in any other case.
	 *
	 * @param longraw 		the long number that contains mantissa
	 * @param exp			the exponent of creating number
	 * @param negativesign	the negativesign
	 */
	public SlicedDouble(long longraw, int exp, String negativesign) {
		this.raw = cutFractTail(Long.toBinaryString(longraw));
		this.exp = exp;
		
		if (negativesign == null || negativesign.isEmpty()) this.negativesign = "";
		else this.negativesign = "-";
	}
	
	/**
	 * Constructs a {@code SlicedDouble} number from its inputs
	 *
	 * <p>If empty {@code String} was supplied to negativesign parameter
	 * negativesign will be empty, and equals "-" in any other case.
	 *
	 * <p>If the {@code String} that supplied as raw parameter contains
	 * a binary representation of mantissa maximum length of 64 digits
	 * apart from leading zeros it will be parsed and added to instance.
	 * If raw is null or if it not contains the valid binary, than
	 * empty {@code String} will be added, that is equivalent to '0.0' or '-0.0'
	 *
	 * @param raw 			the {@code String} that contains mantissa
	 * @param exp			the exponent of creating number
	 * @param negativesign	the negativesign
	 */
	public SlicedDouble(String raw, int exp, String negativesign) {
		this.raw = cutFractTail(parseRaw(raw));
		this.exp = exp;
		if (this.raw.isEmpty()) this.exp = 0;
		
		if (negativesign == null || negativesign.isEmpty()) this.negativesign = "";
		else this.negativesign = "-";
	}
	
	/**
	 * This constructor is using by {@code .clone()} method.
	 */
	private SlicedDouble(Double number, String negativesign, String raw, Long longraw,
		String intraw, String fractraw, Integer exp, Integer onesnum, String roundedrawbin,
		String roundedrawhex, String ieee754bin, String ieee754hex)	{
		if (number != null) this.number = Double.valueOf(number);
		this.negativesign = negativesign;
		this.raw = raw;
		if (longraw != null) this.longraw = Long.valueOf(longraw);
		this.intraw = intraw;
		this.fractraw = fractraw;
		if (exp != null) this.exp = Integer.valueOf(exp);
		if (onesnum != null) this.onesnum = Integer.valueOf(onesnum);
		this.roundedrawbin = roundedrawbin;
		this.roundedrawhex = roundedrawhex;
		this.ieee754bin = ieee754bin;
		this.ieee754hex = ieee754hex;
	}
	
	/**
	 * Auxiliary method to slice the input to the constructor {@code double} number.
	 */
	private void sliceIt() {
		String[] stripes = Double.toHexString(number).split("[.p]");
		
		if (stripes[0].charAt(0) == '-') negativesign = "-";
		else negativesign = "";
		
		roundedrawhex = stripes[1];
		raw = cutFractTail(fromHexToBinary(roundedrawhex));
		
		// check for zero and denormal numbers
		if (stripes[0].charAt(stripes[0].length() + 0xffffffff) == '0') {
			int denormaladdexp = raw.indexOf('1');
			if (denormaladdexp != 0xffffffff) {
				exp = 0xfffffc02 + ~denormaladdexp;
				raw = raw.substring(raw.indexOf('1'));
			} else {
				exp = 0;
				raw = "";
			}
		} else {
			exp = Integer.valueOf(stripes[2]);
			raw = "1" + raw;
		}
	}
	
	/**
 	 * Returns substring of a giving {@code Srting} that contians
	 * binary number maximum length of 64. Number is parsed
	 * in the {@code String} raw and leading zeros are omitting.
	 * If null is supplied as argument, or if in the argument there is no
	 * valid binary string, than it returns empty string.
	 *
	 * @param raw the {@code String} containg bianry raw
	 * @return binary number in the {@code String} form or empty {@code String}.
	 */
	public static String parseRaw(String raw) {
		if (raw.equals(null)) return "";
		
		Pattern p = Pattern.compile("(?<=0{0,}+)[01]{0,64}");
		Matcher m = p.matcher(raw);
		m.find();

		return m.group();
	}
	
	/**
 	 * Returns substring of a giving {@code Srting} that contians
	 * binary number maximum length of 64. Number is parsed
	 * in the {@code String} raw,  beginning at the specified beginIndex
	 * and extending to endIndex - 1. Leading zeros are omitting in the result.
	 * If null is supplied as argument, or if in the argument there is no
	 * valid binary string, than it returns empty string.
	 *
	 * @param raw 	the {@code String} containg bianry raw
	 * @param start the begining index, inclusive
	 * @param end	the ending index, exclusive
	 * @return binary number in the {@code String} form or empty {@code String}.
	 */
	public static String parseRaw(String raw, int start, int end) {
		return parseRaw(raw.substring(start, end));
	}
	
	/**
	 * Returns binary representation of the hexadecimal input {@code String}.
	 * If null or empty {@code String} is supplied that result is
	 * empty {@code String}.
	 *
	 * @param hexraw the hexadecimal {@code String}
	 * @return the binary representation of input.
	 */
	public static String fromHexToBinary(String hexraw) {
		if (hexraw == null) return "";
		
		String raw = "";
		
		for (int i = 0; i < hexraw.length(); i++) {
			char digit = hexraw.charAt(i);
			
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
	
	/**
	 * Returns hexadecimal representation of input binary {@code String}.
	 * It return empty string if argument is null, and "0" if argument
	 * is empty {@code String}.
	 *
	 * <p>If {@code raw.length() % 4 != 0} than additional zeros will be added.<br>
	 * For example:<br>
	 * {@code raw.equals("011011")} as input than it will be considered as 
	 * {@code raw.equals("01101100")} and the output will be "6c"
	 * 
	 * @param raw the binary {@code String}
	 * @return the hexadecimal representation of input
	 */
	public static String fromBinaryToHex(String raw) {
		if (raw == null) return "";
		if (raw.equals("")) return "0";
		
		String hexraw = "", digit;
		
		if (raw.length() < 52) {
			for (int i = raw.length(); (i & 3) != 0; i++) {
				raw += "0";
			}
		}
		
		for (int i = 0; i < 52 && i < raw.length(); i += 4) {
			digit = raw.substring(i, i+4);
			
			switch(digit) {
				case "0000": hexraw += "0"; break;
				case "0001": hexraw += "1"; break;
				case "0010": hexraw += "2"; break;
				case "0011": hexraw += "3"; break;
				case "0100": hexraw += "4"; break;
				case "0101": hexraw += "5"; break;
				case "0110": hexraw += "6"; break;
				case "0111": hexraw += "7"; break;
				case "1000": hexraw += "8"; break;
				case "1001": hexraw += "9"; break;
				case "1010": hexraw += "a"; break;
				case "1011": hexraw += "b"; break;
				case "1100": hexraw += "c"; break;
				case "1101": hexraw += "d"; break;
				case "1110": hexraw += "e"; break;
				default: hexraw += "f"; break;
			}
		}
		
		return hexraw;
	}
	
	/**
	 * Returns raw {@code String} without trailing zeros. But from the nature
	 * of how it works internally - if supplied raw {@code String} would not
	 * have any '1' - the result will be empty string, likewise if the input
	 * is null.
	 *
	 * <p>For example:<br>
	 * {@code raw.equals("0011110")} will return {@code raw.equals("001111")}.
	 *
	 * @param raw the binary {@code String}
	 * @return the raw without trailing zeros
	 */
	public static String cutFractTail(String raw) {
		if (raw == null) return "";
		
		for (int i = raw.length() + 0xffffffff; i > 0xffffffff; i += 0xffffffff) {
			if (raw.charAt(i) == '1') {
				return raw.substring(0, i+1);
			}
		}
		
		return "";
	}
	
	/**
	 * Returns the long representation of raw {@code String} that contain instance.
	 *
	 * @return the raw of instance in the long view
	 */
	public Long getLongRaw() {
		if (longraw == null) {
			if (raw.isEmpty()) longraw = 0l;
			else longraw = Long.parseUnsignedLong(raw, 2);
		}
		
		return longraw;
	}
	
	/**
	 * Returns current exponent of instance (null if it undefined)
	 * - for Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY)
	 *
	 * @return the exponent of instance
	 */
	public Integer getExp() {
		return exp;
	}
	
	/**
	 * Returns mantissa is {@code String} view
	 *
	 * @return the mantissa
	 */
	public String getBinaryRaw() {
		return raw;
	}
	
	/**
	 * Returns the {@code String} that contains hexadecimal mantissa 
	 * representation that was rounded according to {@code double} format.
	 *
	 * <p><b>Attention:</b> this method may imply some mutation on the
	 * internal raw {@code String}, in accordance with {@code double} format
	 * transcription. So you should use carefully.
	 *
	 * @return the rounded hexadecimal mantissa
	 */
	public String getRoundedRawHex() {
		if (roundedrawhex == null) {
			roundedrawhex = fromBinaryToHex(getRoundedRawBin());
		}
		
		return roundedrawhex;
	}
	
	/**
	 * Returns the {@code String} that contains binary mantissa 
	 * representation that was rounded according to {@code double} format.
	 *
	 * <p><b>Attention:</b> this method may imply some mutation on the
	 * internal raw {@code String}, in accordance with {@code double} format
	 * transcription. So you should use carefully.
	 *
	 * @return the rounded binary mantissa
	 */
	public String getRoundedRawBin() {
		if (roundedrawbin == null) {
			
			// These instructions supply additional accuracy
			// in spite of that we already have scaled rounding in MiniMath,
			// current final rounding highly increase precision of result,
			// total amount of missed values with accuracy of 13-14 digits after decimal point 
			// reach up to 10-20 times less, relatively of when these instructions are off
			//if (raw.length() > 53 && (raw.indexOf('0') > 52 || raw.indexOf('0') == 0xffffffff)) {
			if (raw.length() > 53 && raw.charAt(53) == '1') {
				if (raw.indexOf('0') > 53 || raw.indexOf('0') == 0xffffffff) {
					exp++;
					roundedrawbin = "";
					raw = "1";
				} else {
					long chunk = Long.valueOf(raw.substring(0,53), 2);
					chunk++;
					roundedrawbin = Long.toBinaryString(chunk).substring(1);
					if (roundedrawbin.length() == 53) exp++;
				}
			} else {
				if (raw.length() < 2) roundedrawbin = "";
				else roundedrawbin = raw.substring(1);
			}
		}
		
		return roundedrawbin;
	}
	
	/**
	 * Returns full hexadecimal representation in {@code String} format
	 * of valid {@code double} number.
	 *
	 * <p>There are three special cases:
	 * <ul>
	 *  <li>if {@code Double.isNaN(number) == true} returns "NaN"</li>
	 *  <li>if {@code number == Double.POSITIVE_INFINITY} returns "Infinity"</li>
	 *  <li>if {@code number == Double.NEGATIVE_INFINITY} returns "-Infinity"</li>
	 * </ul>
	 *
	 * <p><b>Attention:</b> this method may imply some mutation on the
	 * internal raw {@code String}, in accordance with {@code double} format
	 * transcription. So you should use carefully.
	 *
	 * @return the hexadecimal representation of internal number.
	 */
	public String getDoubleHexRaw() {
		if (number != null) {
			if (Double.isNaN(number)) return "NaN";
			else if (number == Double.POSITIVE_INFINITY) return "Infinity";
			else if (number == Double.NEGATIVE_INFINITY) return "-Infinity";
		} else if (exp > 1023) {
			if (negativesign.isEmpty()) return "Infinity";
			else return "-Infinity";
		} else if (exp < -1074) {
			if (negativesign.isEmpty()) return "0.0";
			else return "-0.0";
		}
		
		if (ieee754hex == null) {
			
			getRoundedRawBin();
			
			if ((raw.indexOf('1') == 0xffffffff) && (exp == 0 || exp  < 0xfffffbce)) {
				// if it's '0' or below minimum
				ieee754hex = negativesign + "0x0.0p0";
			} else {	
				ieee754hex = negativesign + "0x1." + getRoundedRawHex() + "p" + exp;
			}
		}
		
		return ieee754hex;
	}
	
	/**
	 * Computes and returns valid {@code double} number representation of
	 * the current instance.
	 *
	 * <p><b>Attention:</b> this method may imply some mutation on the
	 * internal raw {@code String}, in accordance with {@code double} format
	 * transcription. So you should use carefully.
	 *
	 * @return the {@code double} number representation of the current instance
	 */
	public Double getIEEE754() {
		if (number == null) {
			number = Double.valueOf(getDoubleHexRaw());
		}
		
		return number;	
	}
	
	/**
	 * Returns the sum of '1' in internal mantissa.
	 *
	 * @return the sum of '1' in internal mantissa
	 */
	public Integer onesEnum() {
		if (onesnum == null) {
			int counter = 0;
			
			for (int i = 0; i < getBinaryRaw().length(); i++) {
				if (raw.charAt(i) == '1') counter++;
			}
			
			onesnum = counter;
		}
		
		return onesnum;
	}
	
	/**
	 * Returns the integer part as a {@code String} of instance number.
	 *
	 * <p>Special cases:<br>
	 * if {@code Double.isNaN(number) == true} or
	 * if {@code number == Double.POSITIVE_INFINITY} or
	 * if {@code number == Double.NEGATIVE_INFINITY} it will return null.
	 *
	 * @return the integer part as a {@code String} of instance number
	 */
	public String getIntRaw() {
		if (intraw == null) {
			if (exp != null) {
				if (exp > 0) {
					if (exp < raw.length()) {
						intraw = raw.substring(0, exp+1);
					} else {
						intraw = raw;
					}
				} else if (exp == 0 && raw.indexOf('1') == 0) {
					intraw = "1";
				} else {
					intraw = "";
				}
			}
		}
		
		return intraw;
	}
	
	/**
	 * Returns the new SlicedDouble instance that is making from
	 * integer part of instance number.
	 *
	 * <p>Special cases:<br>
	 * if {@code Double.isNaN(number) == true} or
	 * if {@code number == Double.POSITIVE_INFINITY} or
	 * if {@code number == Double.NEGATIVE_INFINITY} it will return the same
	 * instance.
	 *
	 * @return the new SlicedDouble instance that is making from
	 * 			integer part of instance number
	 */
	public SlicedDouble getIntSD() {
		if (intraw == null) getIntRaw();
		
		if (exp == null) {
			return this;
		} else if (exp > 0xffffffff) {
			return new SlicedDouble(intraw, exp, negativesign);
		} else {
			return new SlicedDouble("", 0, negativesign);
		}
	}
	
	/**
	 * Returns the fractional part as a {@code String} of instance number.
	 *
	 * <p>Special cases:<br>
	 * <ul>
	 *  <li>if {@code Double.isNaN(number) == true} it will return null;</li>
	 *  <li>if {@code number == Double.POSITIVE_INFINITY} or
	 * if {@code number == Double.NEGATIVE_INFINITY} it will return empty {@code String}.</li>
	 * </ul>
	 *
	 * @return the fractional part as a {@code String} of instance number
	 */
	public String getFractRaw() {
		if (fractraw == null) {
			if (exp != null) {
				if (exp < 0) {
					fractraw = raw;
				} else if (exp < (raw.length() + 0xffffffff)) {
					fractraw = raw.substring(exp+1);
				} else {
					fractraw = "";
				}
			} else {
				if (!Double.isNaN(number)) return "";
			}
		}
		
		return fractraw;
	}
	
	/**
	 * Returns the new SlicedDouble instance that is making from
	 * integer part of instance number.
	 *
	 * <p>Special cases:<br>
	 * <ul>
	 *  <li>if {@code Double.isNaN(number) == true} it will return the same
	 * instance.</li>
	 *  <li>if {@code number == Double.POSITIVE_INFINITY} it will return instance
	 * that will be holding an equivalent of "0.0"</li>
	 *  <li>if {@code number == Double.NEGATIVE_INFINITY} it will return instance
	 * that will be holding an equivalent of "-0.0"</li>
	 * </ul>
	 *
	 * @return the new SlicedDouble instance that is making from
	 * 			fractional part of instance number
	 */
	public SlicedDouble getFractSD() {
		if (fractraw == null) getFractRaw();
		
		if (exp == null) {
			if (Double.isNaN(number)) return this;
			else if (number == Double.NEGATIVE_INFINITY) return new SlicedDouble("", 0, "-");
			else return new SlicedDouble("", 0, "");
		} else {
			if (fractraw.isEmpty()) return new SlicedDouble("", 0, negativesign);
			else {
				if (exp < 0) return new SlicedDouble(fractraw, exp, negativesign);
				else return new SlicedDouble(fractraw, ~fractraw.indexOf('1'), negativesign);
			}
		}
	}
	
	/**
	 * Returns true if number of instance is negative.
	 *
	 * @return true if number of instance is negative
	 */
	public boolean isNegative() {
		if (negativesign.equals("-")) return true;
		else return false;
	}
	
	/**
	 * Returns internal {@code double} number without precompilation.
	 * It is safe for use, as no mutation is applying. But if there is no
	 * compilated number for now, than result would be null.
	 *
	 * @return the {@code double} number
	 */
	public Double getDouble() {
		return number;
	}
	
	/**
	 * Returns the {@code String} representation number sign.
	 *
	 * @return the {@code String} representation number sign
	 */
	public String getNegativeSign() {
		return negativesign;
	}
	
	/**
	 * Sets sign to the current instance. If {@code String} argument is not
	 * empty, the sign would be negative, and positive in other case.
	 *
	 * @param sign the supplying sign to the number
	 */
	public void setSign(String sign) {
		if (number != null && Double.isNaN(number)) return;
		if (number != null && negativesign != sign) number = -number;
		if (sign.isEmpty()) negativesign = "";
		else negativesign = "-";
	}
	
	/**
	 * Reverses sign - is it was negative it become positive and vice versa.
	 */
	public void reverseSign() {
		if (Double.isNaN(number)) return;
		if (number != null) number = -number;
		if (negativesign.isEmpty()) negativesign = "-";
		else negativesign = "";
	}
	
	/**
	 * Returns true if the instance number is odd.
	 *
	 * @return true if the instance number is odd
	 */
	public boolean isOdd() {
		if ((exp < raw.length() && raw.charAt(exp) == 0) || exp < 0 || exp >= raw.length())
			return false;
		return true;
	}
	
	/**
	 * Returns true if the digits num of instance raw mantissa  is odd.
	 *
	 * @return true if the digits num of instance raw mantissa  is odd.
	 */
	public boolean isOddIntDigitsNum() {
		if ((exp & 1) == 0) return true;
		else return false;
	}
	
	/** 
	 * Returns full binary representation in {@code String} format
	 * of valid {@code double} number.
	 *
	 * <p><b>Attention:</b> this method may imply some mutation on the
	 * internal raw {@code String}, in accordance with {@code double} format
	 * transcription. So you should use carefully.
	 *
	 * @return the binary representation of internal number.
	 */
	public String getIEEE754Bin() {
		if (ieee754bin == null) {
			if (exp != null) {
				if (negativesign == "-") ieee754bin = "1";
				else ieee754bin = "0";
				
				int resultexp = exp + 1023;
				
				if (resultexp > 2046) {
					ieee754bin += "11111111111" + "0000000000000000000000000000000000000000000000000000"; // +-Infinity
				}
				
				getRoundedRawBin();
				
				String extroundedrawbin;
				
				if (roundedrawbin.length() == 0) 
					extroundedrawbin = "0000000000000000000000000000000000000000000000000000";
				else {
					extroundedrawbin = roundedrawbin.substring(0);
					for (int i = extroundedrawbin.length(); i < 52; i++) extroundedrawbin += '0';
				}
				
				if (resultexp == 1023 && raw == "") {
					ieee754bin += "00000000000" + extroundedrawbin;
				} else if (resultexp > 0) {
					String resultexpstr = Integer.toBinaryString(resultexp);
					String addzeros = "";
					
					for (int i = resultexpstr.length(); i < 11; i++) addzeros += '0';
					
					resultexpstr = addzeros + resultexpstr;
					
					ieee754bin += resultexpstr + extroundedrawbin; // normal numbers
				} else if (resultexp > 0xffffffcb) {
					ieee754bin += "00000000000" + extroundedrawbin; // denormal numbers
				} else {
					ieee754bin += "000000000000000000000000000000000000000000000000000000000000000"; // sub-denormal == +-0.0
				}
			} else {
				if (number == Double.POSITIVE_INFINITY)
					ieee754bin = "0111111111110000000000000000000000000000000000000000000000000000";
				else if (number == Double.NEGATIVE_INFINITY)
					ieee754bin = "1111111111110000000000000000000000000000000000000000000000000000";
				else 
					ieee754bin = "0111111111111000000000000000000000000000000000000000000000000000";
			}
		}
		
		return ieee754bin;	
	}
	
	/**
	 * Returns the deep cloned SlicedDouble instance.
	 *
	 * @return the deep cloned SlicedDouble instance
	 */
	public SlicedDouble clone() {
		SlicedDouble sdnum = new SlicedDouble(this.number, this.negativesign, this.raw, 
			this.longraw, this.intraw, this.fractraw, this.exp, this.onesnum, 
			this.roundedrawbin, this.roundedrawhex, this.ieee754bin, this.ieee754hex);
		
		return sdnum;
	}
}