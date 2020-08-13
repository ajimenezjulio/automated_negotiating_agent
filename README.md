# Automated Negotiating Agent
Automated agent that wins the **University of Southampton** contest and **overcome** the best agents of **2016 ANAC** international competition. It was developed using **Genius** negotiation environment.

For a detailed explanation and analysis please refer at this document: [Agent Report](http://ajulio.com/assets/documents/Agent.pdf)

## Strategy
The Agent has a negotiation strategy in 4 steps. First it calculates the table of disutility, then compute the value for the threshold function. As a third step, performs a combined modelling of the opponent and finally proceed to generate the bid.
The disutility table is created considering a matrix of issues `i1 ... in` with their respective values `vi,1 ... vi, n`in the following way.

<p align="center"><img align="center" src="https://i.upmath.me/svg/%0A%5Cstackrel%7B%5Cmbox%7B%24%5Cboldsymbol%7Bi_1%7D%20%5Cquad%5Cquad%20%5Cboldsymbol%7Bi_2%7D%20%5Cquad%5C%20%20%5Cboldsymbol%7Bi_3%7D%20%5Cquad%5Cquad%5Cqquad%20%5Cboldsymbol%7Bi_n%7D%24%7D%7D%7B%0A%09%09%5Cbegin%7Bbmatrix%7D%0A%09%09%20%20%20%20v_%7B1%2C%5C%2C1%7D%20%20%20%26%20v_%7B2%2C%5C%2C1%7D%20%26%20v_%7B3%2C%5C%2C1%7D%20%26%20%5Cdots%20%26%20v_%7Bn%2C%5C%2C1%7D%20%5C%5C%0A%09%09%20%20%20%20v_%7B1%2C%5C%2C2%7D%20%20%20%26%20v_%7B2%2C%5C%2C2%7D%20%26%20v_%7B3%2C%5C%2C2%7D%20%26%20%5Cdots%20%26%20v_%7Bn%2C%5C%2C2%7D%20%5C%5C%0A%09%09%20%20%20%20%5Cvdots%20%09%09%26%20%5Cvdots%20%09%26%20%5Cvdots%20%09%26%20%5Cddots%20%26%20%5Cvdots%20%5C%5C%0A%09%09%20%20%20%20v_%7B1%2C%5C%2Cn%7D%20%20%20%26%20%5Cdots%20%09%09%26%20%5Cdots%20%09%09%26%20%5Cdots%20%26%20v_%7Bn%2C%5C%2Cn%7D%0A%09%09%5Cend%7Bbmatrix%7D%20%0A%09%7D%0A" alt="
\stackrel{\mbox{$\boldsymbol{i_1} \quad\quad \boldsymbol{i_2} \quad\  \boldsymbol{i_3} \quad\quad\qquad \boldsymbol{i_n}$}}{
		\begin{bmatrix}
		    v_{1,\,1}   &amp; v_{2,\,1} &amp; v_{3,\,1} &amp; \dots &amp; v_{n,\,1} \\
		    v_{1,\,2}   &amp; v_{2,\,2} &amp; v_{3,\,2} &amp; \dots &amp; v_{n,\,2} \\
		    \vdots 		&amp; \vdots 	&amp; \vdots 	&amp; \ddots &amp; \vdots \\
		    v_{1,\,n}   &amp; \dots 		&amp; \dots 		&amp; \dots &amp; v_{n,\,n}
		\end{bmatrix} 
	}
" /></p>

Next a disutility function is used to create our table.

<p align="center"><img align="center" src="https://i.upmath.me/svg/%0A%5Cboldsymbol%7Bu%7D_%7Bi%2C%5C%2Cj%7D%20%3D%20%5Cdfrac%7Bv_%7Bi%2C%5C%2Cj%7D%20-%20%5Cargmax%5C%7B%20v_i%20%5C%7D%7D%7B%5Cargmax%5C%7B%20v_i%20%5C%7D%7D%20%5Ctimes%20w_i%0A" alt="
\boldsymbol{u}_{i,\,j} = \dfrac{v_{i,\,j} - \argmax\{ v_i \}}{\argmax\{ v_i \}} \times w_i
" /></p>

## Threshold Function
The Threshold function `T(t)` plays a crucial role for the good performance of the agents during the negotiation, the agent uses a time-dependent strategy based on the following equation.


<p align="center"><img align="center" src="https://i.upmath.me/svg/%0AT(t)%20%3D%20%5Calpha%20%5Cpm%20t%20%5Ctimes%20%5Cbeta%0A" alt="
T(t) = \alpha \pm t \times \beta
" /></p>
