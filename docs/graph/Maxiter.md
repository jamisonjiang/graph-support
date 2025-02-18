# Maxiter

The **maxiter** attribute sets the **maximum number of iterations** for **force-directed (FDP) layout algorithms**. It controls the number of refinement steps used to optimize node placement.

------

## **Usage in DOT**

### **Increase Iterations for a More Refined Layout**

```dot
digraph G {
    layout=fdp;
    size=3
    maxiter=1

    // Define 50+ nodes
    A; B; C; D; E; F; G; H; I; J;
    K; L; M; N; O; P; Q; R; S; T;
    U; V; W; X; Y; Z; A1; B1; C1; D1;
    E1; F1; G1; H1; I1; J1; K1; L1; M1; N1;
    O1; P1; Q1; R1; S1; T1; U1; V1; W1; X1;

    // First cycle group
    A -> B; B -> C; C -> D; D -> E; E -> F; F -> A;
    A -> G; G -> H; H -> I; I -> J; J -> A;
    B -> H; C -> I; D -> J; E -> G; F -> H;

    // Second cycle group
    K -> L; L -> M; M -> N; N -> O; O -> P; P -> K;
    K -> Q; Q -> R; R -> S; S -> T; T -> K;
    L -> R; M -> S; N -> T; O -> Q; P -> R;

    // Third cycle group
    U -> V; V -> W; W -> X; X -> Y; Y -> Z; Z -> U;
    U -> A1; A1 -> B1; B1 -> C1; C1 -> D1; D1 -> U;
    V -> B1; W -> C1; X -> D1; Y -> A1; Z -> B1;

    // Fourth cycle group
    E1 -> F1; F1 -> G1; G1 -> H1; H1 -> I1; I1 -> J1; J1 -> E1;
    E1 -> K1; K1 -> L1; L1 -> M1; M1 -> N1; N1 -> E1;
    F1 -> L1; G1 -> M1; H1 -> N1; I1 -> K1; J1 -> L1;

    // Fifth cycle group
    O1 -> P1; P1 -> Q1; Q1 -> R1; R1 -> S1; S1 -> T1; T1 -> O1;
    O1 -> U1; U1 -> V1; V1 -> W1; W1 -> X1; X1 -> O1;
    P1 -> V1; Q1 -> W1; R1 -> X1; S1 -> U1; T1 -> V1;

    // Cross connections between groups
    A -> K; B -> L; C -> M; D -> N; E -> O; F -> P;
    G -> Q; H -> R; I -> S; J -> T;
    K -> U; L -> V; M -> W; N -> X; O -> Y; P -> Z;
    Q -> A1; R -> B1; S -> C1; T -> D1;
    U -> E1; V -> F1; W -> G1; X -> H1; Y -> I1; Z -> J1;
    A1 -> K1; B1 -> L1; C1 -> M1; D1 -> N1;
    E1 -> O1; F1 -> P1; G1 -> Q1; H1 -> R1; I1 -> S1; J1 -> T1;
    K1 -> U1; L1 -> V1; M1 -> W1; N1 -> X1;

    // Additional long-range connections
    B -> X1; F -> S1; J -> O1; T -> K1; Q -> P1; W -> N1;
}
```

------

## **Usage in Java**

### **Increase Iterations for a More Refined Layout**

```java
Node a = Node.builder().label("a").build();
Node b = Node.builder().label("b").build();

Graphviz graph = Graphviz.digraph()
    .layout(Layout.FDP)  // Use force-directed layout
    .maxiter(500)  // Increase iterations for better optimization
    .addLine(a, b)
    .build();
```