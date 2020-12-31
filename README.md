# TruthTable
/**
* This JavaFX application generates truth tables for the specified logical formulas.
* Formula operators are generally evaluated right to left without precedence.
*
* @author William Edison
* @version 1.05 August 2020
*
* Formulas may be typed into the text field, or a text file of formulas
* may be loaded using the Main Menu Load Data option. Formulas input
* using either the input field or from a text file are accumulated in the
* scrollable Combo Box area at the bottom of the screen and can be selected
* for processing.
*
* Results may be saved as an image file in various formats (bmp, jpg, png, gif)
* using the Main Menu Save Scene option.
*
* Results may be shown for:
* 	- all columns (View All option),
* 	- all operators (View Operators option),
* 	- or just the propositional variables and final evaluation (View Final option).
*
* The following character representations for logical operators are supported:
* 	- AND - Conjunction
*		p & q
*		p * q
*		(p)(q)
*
*	- OR - Disjunction
*		p | q
*		p + q
*	- Implication
*		p > q
*	- Backward Implication
*		p < q
*	- NAND
*		p ^ q
*	- NOR
*		p v q
*	- XNOR - Equivalence
*		p = q
*	- XOR
*		p : q
*	- NOT - Negation
*		!p
*		~p
*
*/
