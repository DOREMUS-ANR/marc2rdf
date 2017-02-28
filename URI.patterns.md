URI patterns for DOREMUS data
==============================

This file documents how the URI are generated for the DOREMUS data.
See also [#21](https://github.com/DOREMUS-ANR/marc2rdf/issues/21).


## Main entities

Pattern:

``` turtle
http://data.doremus.org/<group>/<uuid>
# i.e. http://data.doremus.org/expression/ad8ddf1f-f1d1-3284-91d7-34fe655f8258 
```

The `<group>` is taken from this table 

| Class | group |
| --- | --- |
| F22_SelfContainedExpression | expression |
| F28_ExpressionCreation | event |
| F14_IndividualWork | work |
| F15_ComplexWork | work |
| F42_RepresentativeExpressionAssignment | event |
| F24_PublicationExpression | publication |
| F30_PublicationEvent | event |
| F19_PublicationWork | work |
| F31_Performance | performance |
| F25_PerformancePlan | expression |
| F40_IdentifierAssignment | event |
| F50_ControlledAccessPoint |  |
| E21_Person | artist |
| E4_Period | period |

## Secondary entities

This group includes entities that cover specific information about the main entities.
The URI is realized appending a suffix to the parent main entity.

Pattern if only one instance per main entity is expected:

``` turtle
<uri of the main entity>/<suffix>
# i.e. http://data.doremus.org/expression/ad8ddf1f-f1d1-3284-91d7-34fe655f8258/dedication
```

Pattern if multiple instance per main entity are possible:
``` turtle
<uri of the main entity>/<suffix>/<progressive int>
# i.e. http://data.doremus.org/expression/6ad3a47e-61a2-3790-8fe1-8bb2a17a3c12/casting/1
```

The `<suffix>` is taken from this table:

| Class | suffix |
| --- | --- |
| M1_Catalogue_Statement | catalog |
| M2_Opus_Statement | opus |
| M4_Key | key |
| M6_Casting | casting |
| M15_Dedication | dedication |
| E4_Period | period |
| E7_Activity | activity |
| E52_Time-Span | time |
