# MinimalMath

The main goal of this project - to achive functionality likewise of some functions of *java.lang.Math* class, but with unusual condition - software calculating with using only some of the basic operators. Instead, we try to accomplish that without of using *substract*, *multiplying*, *division* and *remainder* operators. That's it - the only operator that we can use (from main mathematics operations from the box) is *summing*. To be more specific: *summing, bitwise with bit shifting operators, condition operators and unary minus (last one only in case where no calculations are actually needed - when it is not, and some kind of substraction is necessary, there uses a summing with complement)*. More of it, we are using only standart built-in classes and types. Most calculations are made with conjuction of *Long* and *String* classes that are the sliced parts of full *double (IEEE754)* in respective surrogate type.

## Classes

For now we have 2 classes, one is *ru.andreygs.minimalmath.MiniMath* - this is the main class where desired mathematic methods are realised, and the second is *ru.andreygs.minimalmath.SlashedDouble* - auxiliary class, members of which holds all intermediate values and parts of surrogate *double* values in accordance to use them in *MiniMath* methods.

## Current stage

As project is started not so along ago (at least it's true for time when I writing these lines ^)), not very much is made. But, there is a working MiniMath.pow() method, which can handle for now almost all numbers and positive degrees to produce fully-correct result according to IEEE754 standart (test will give more than 99.9999% among all incoming *double* values are strictly equal to those that are receiving from *java.lang.Math.pow()* (for 12 decimal digits after decimal point and about 99.995% for 13 digits)).

## So, for what?

I can't really answer this question precisely. For self-education and in the matter of science curiosity. Because of this I don't know when the project will be done or stopped maybe. It's obviously writing not for concurrency with built-in objects or some others on the basis of performance or accuracy. It's writting to be as minimalistic as it could be, but with saving major preciseness of operations. So far it's not clear will it be fully accomplished to IEEE754 standart or not, what methods will be added, and will I use in *java.math* package for adding BigInteger capabilities support... We'll see.

## How to use

There is a several working methods that may get an interest of some kind in MiniMath class:

- *static Double abs(double number)* - returns absolute value of a giving double number;
- *static SlashedDouble abs(SlashedDouble number)* - returns SlashedDouble that holds absolute value of number that argument holds;
- *static Double floor(double number)* - returns the largest (closest to positive infinity) double value that is less than or equal to the argument and is equal to a mathematical integer.
- *static SlashedDouble floor(SlashedDouble sdnum)* - returns the SlashedDouble that hold largest (closest to positive infinity) double value that is less than or equal to the number that argument holds, and is equal to a mathematical integer;
- *static Double ceil(double number)* - returns the smallest (closest to negative infinity) double value that is greater than or equal to the argument and is equal to a mathematical integer;
- *static SlashedDouble ceil(SlashedDouble sdnum)* - returns the SlashedDouble that holds the smallest (closest to negative infinity) double value that is greater than or equal to the number that argument holds, and is equal to a mathematical integer;
- *static Double fraction(double number)* - returns fractional part of double number that is closest to zero;
- *static SlashedDouble fraction(SlashedDouble sdnum)* - returns SlashedDouble that holds fractional part of number that argument holds, that is closest to zero;
- *static Double mult(double number1, double number2)* - makes a multiplication;
- *static SlashedDouble mult(SlashedDouble number1, SlashedDouble number2)* - makes a multiplication;
- *static Double division(double dividend, double divisor)* - makes a division;
- *static SlashedDouble division(SlashedDouble dividend, SlashedDouble divisor)* - makes a division;
- *static Double div(double dividend, double divisor)* - makes an integer division;
- *static SlashedDouble div(SlashedDouble dividend, SlashedDouble divisor)* - makes an integer division;
- *static Double floorDiv(double dividend, double divisor)* - makes an integer floor division;
- *static static SlashedDouble floorDiv(SlashedDouble dividend, SlashedDouble divisor)* - makes an integer floor division;
- *static Double divisionRemainder(double dividend, double divisor)* - returns remainder of division;
- *static SlashedDouble divisionRemainder(SlashedDouble dividend, SlashedDouble divisor)* - returns remainder of division;
- *static Double pow(double number, double power)* - returns the value of the number raised to the giving power;
- *static SlashedDouble pow(SlashedDouble sdnum, SlashedDouble sdpow)* - returns the SlashedDouble that hold value of the number that sdnum holds raised to the giving power that sdpow holds;

You are also may try to use some functionality of SlashedDouble objects - this have many public methods, but no documentation for them is included for now. All I can say, this class is made for slicing standart double value, than separately operating with its parts - an exponent, mantissa and sign - and finally put them all together in double again.

If you want to test MiniMath.pow() - there, in MiniMath.main(), is you can find 2 two level test cycles (yes, 2 cycles with two levels) of *MiniMath.pow()* function according to *java.lang.Math.pow()*, where random numbers and powers in staged manner applying to both of them (amplitudes of numbers and according powers are from 10^40 till 10^-39) and if result is not the same prints it out to the screen.


## License

Copyright (c) 2020 Andrey Grabov-Smetankin

Permission is hereby granted, free of charge, to any person
obtaining a copy of this software and associated documentation
files (the "Software"), to deal in the Software without
restriction, including without limitation the rights to use,
copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the
Software is furnished to do so, subject to the following
conditions:

The above copyright notice and this permission notice shall be
included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
OTHER DEALINGS IN THE SOFTWARE.