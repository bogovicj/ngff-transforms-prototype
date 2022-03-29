## Transforms as a path through axes

suppose we have four transformations:
[0,1,2,3]
[x,y,z,t,c]
```
T1: [a] > [t]
T2: [0,1,3] > [x,y,z]
T3: [4] > [a]
T4: [2] > [c]
```

and a set of axes: `[0,1,2,3,4]` that we want to transform to another set of axes `[x,y,z,c,t]`.
what might the process look like, if we keep track of "source" and "destination" axes:

```
[0,1,2,3,4]
[x,y,z,c,a]
    T1: a > t
[0,1,2,3,4]
[x,y,z,c,a]
    T2: [0,1,2] > [x,y,z]
[2,4]
[c,a]
    T3: [4] > [a]
[2]
[c]
    T4: [2] > [c]
[]
[]
```

## Another example

suppose we have four transformations:
[0,1,2,3,4]
[z]

```
T1: [0] > [a,b]
T2: [1,2] > [c,d,e]
T3: [3,4] > [f]
T4: [a,c,f] > [z]
```

[0,1,2,3,4]
[z]
    T4 [a,c,f] > [z] 
[0,1,2,3,4]
[a,c,f]
    T3 [3,4] > [f] 
[0,1,2]
[a,c]
    T2 [1,2] > [c,d,e]
[0,d,e]
[a]
    T1 [0] > [a,b]
[b,d,e]
[]


## Coordinate transforms on subsets

suppose we have two transformations:
```
T1: [0] > [x]
T2: [2] > [y]
```

how should T1 be applied to a 2d point in a space with axes [0,1]?
p = (2,3)


