# MinimalMath

The main goal of this project - to achive functionality likiwise of some functions of *java.lang.Math* class, but with unusual condition - software calculating without using most of basic operators. Instead we try to accomplish that without of using *substract*, *multiplying*, *division* and *remainder* operators. That's it - the only operator that we can use (from main mathematics operations from the box) is *summing*. To be more specific: *summing, bitwise and bit shift operators and condition operators*. More of it, we are using only standart built-in classes and types. Most calculations are made with conjuction of *Long* and *String* classes in desire to get surrogate *double (IEEE754)* type.

## Classes

For now we have 2 classes, one is *ru.andreygs.minimalmath.MiniMath* - this is the main class where desired mathematic methods are realised, and the second is *ru.andreygs.minimalmath.SlashedDouble* - auxiliary class, members of which holds all intermediate values and parts of surrogate *double* values in accordance to use them in *MiniMath* methods.

## Current stage

As project is started not so along ago (at least it's true for time when I writing these lines ^)), not very much is made. But, there is a working MiniMath.pow() method, which can handle for now all most all numbers and positive degrees to produce fully-correct result according to IEEE754 standart (test will give more than 99.9999% among all incoming *double* values are strictly equal to those that are receiving from *java.lang.Math.pow()* (for 12 decimal digits after decimal point and about 99.995% for 13 digits)). Negative powers are not the case for now, because there is no *dividing* method released in. But it's a question of a couple of days (if I keep some gap, otherwise it will be done tomorrow, and no one actually read this lines). For this reason (missing *dividing*), in a couple of secondary, accessory kind, operations there is a *remainder* and *divide* operators exists yet. 

## So, for what?

I can't really answer this question precisely. For self-education and in the matter of science curiosity. Because of this I don't know when the project will be done or stopped maybe. It's obiously writing not for concurrency with built-in objects or some others on the basis of performance or accuracy. It's writting to be as minimalistic as it could be, but with saving major preciseness of operations. So far it's not clear will it be fully accomplished to IEEE754 standart or not, what methods will be added, and will I use in *java.math* package for adding BigInteger capabilities support... We'll see.

## How to use

There is a two working methods that may get an interest of some kind - *MiniMath.mult()*, and *MiniMath.pow()*. In the *main()* method of *MiniMath* class you can find two level test cycle of *MiniMath.pow()* function according to *java.lang.Math.pow()*, where random numbers and powers in staged manner applying to both of them and if result is not the same prints it out to the screen.


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