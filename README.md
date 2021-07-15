# MSc-Thesis
The codes related to my M.Sc. Thesis in 2010.

Data Aggregation and Routing Optimization in Wireless Sensor Network using <b>Learning Automata(Reinforcement Learning)<b> and <b>Swarm Intelligence(Ant Colony Optimization)<b>.
  
<b>Target<b>: Maximizing Network LifeTime

## Developement
- Java Development Kit Version: jdk6
- Simulator: Jsim-1.3
- IDE: Intelij-Idea 

Codes are in the following path:  /src/drcl/inet/sensorsim

Te network could be configured using the conf_files directory. 

The following methods are implemented:

- OneHop: One Hop Routing
- OneHopTDMA: One Hop Routing and TDMA schedule
- MultiHop: Multi Hop Routing
- LEACH: Routing based on Multi hop LEACH Protocol
- MHTrNoAgg: Multi Hop Tree Routing and No Aggregation
- MHTrAgg: Multi Hop Tree Routing and  Aggregation
- esna_LAg: The method which is proposed in "https://doi.org/10.1007/s11276-009-0162-5"
- LA_Agg: Proposed Learning Automata based Routing and Aggregation strategt
- AC_Agg: Proposed Ant Colong based Routing and Agrregation strategy

The paper in "https://doi.org/10.1007/s11276-015-0894-3" is based on LA_LAg method. 

AC_Agg has improved the result of LA_LAg in special condition (but the approach never has been published)
