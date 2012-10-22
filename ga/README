Author: Alex Ksikes

- The representation of each hypothesis is simply the coordinates
(in binary) of the 8 queens on the board.  Though I converted them
to integers to easily compute the fitness function.

for example:

h1 010 001 000 100 ....

first queen at (2,1), second queen at (0,4) ....

- I used a rank selection to probabilistically select the best hypotheses,
Pr(h)=(p-rank(h))/(Sumi p-rank(hi)).

- I used a uniform crossover.

- The mutation operator simply flips one bit of an hypothesis.

- Inconsistent hypotheses have more than one queen in a given coordinate,
I assigned to them a very low fitness (for my case a very high fitness).

- The fitness function simply counts the number of attacks from one queen 
to the others.

- The problem is solved when there is an hypothesis of fitness = 0.

It seems that the best set of parameters are p=33, r=0.6 and m=0.1

