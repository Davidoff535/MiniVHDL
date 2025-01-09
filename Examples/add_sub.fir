circuit add_sub :
  module add_sub :
    input a : UInt<8>
    input b : UInt<8>
    input sub : UInt<1>
    output result : UInt<8>

    wire bx : UInt<8>

    inst a0 of adder

    bx<=xor(b,cat(cat(cat(cat(cat(cat(cat(sub, sub), sub), sub), sub), sub), sub), sub))
    a0.a<=a
    a0.b<=bx
    a0.cin<=sub
    result<=a0.sum
  module adder :
    input a : UInt<8>
    input b : UInt<8>
    input cin : UInt<1>
    output cout : UInt<1>
    output sum : UInt<8>

    wire c1 : UInt<1>
    wire c2 : UInt<1>
    wire c3 : UInt<1>
    wire c4 : UInt<1>
    wire c5 : UInt<1>
    wire c6 : UInt<1>
    wire c7 : UInt<1>
    wire _sum0 : UInt<1>
    wire _sum1 : UInt<1>
    wire _sum2 : UInt<1>
    wire _sum3 : UInt<1>
    wire _sum4 : UInt<1>
    wire _sum5 : UInt<1>
    wire _sum6 : UInt<1>
    wire _sum7 : UInt<1>

    inst fa0 of fa
    inst fa1 of fa
    inst fa2 of fa
    inst fa3 of fa
    inst fa4 of fa
    inst fa5 of fa
    inst fa6 of fa
    inst fa7 of fa

    fa0.a<=bits(a,0,0)
    fa0.b<=bits(b,0,0)
    fa0.cin<=cin
    c1<=fa0.cout
    fa1.a<=bits(a,1,1)
    fa1.b<=bits(b,1,1)
    fa1.cin<=c1
    c2<=fa1.cout
    fa2.a<=bits(a,2,2)
    fa2.b<=bits(b,2,2)
    fa2.cin<=c2
    c3<=fa2.cout
    fa3.a<=bits(a,3,3)
    fa3.b<=bits(b,3,3)
    fa3.cin<=c3
    c4<=fa3.cout
    fa4.a<=bits(a,4,4)
    fa4.b<=bits(b,4,4)
    fa4.cin<=c4
    c5<=fa4.cout
    fa5.a<=bits(a,5,5)
    fa5.b<=bits(b,5,5)
    fa5.cin<=c5
    c6<=fa5.cout
    fa6.a<=bits(a,6,6)
    fa6.b<=bits(b,6,6)
    fa6.cin<=c6
    c7<=fa6.cout
    fa7.a<=bits(a,7,7)
    fa7.b<=bits(b,7,7)
    fa7.cin<=c7
    cout<=fa7.cout
    _sum0<=fa0.s
    _sum1<=fa1.s
    _sum2<=fa2.s
    _sum3<=fa3.s
    _sum4<=fa4.s
    _sum5<=fa5.s
    _sum6<=fa6.s
    _sum7<=fa7.s
    sum<=cat(_sum7,cat(_sum6,cat(_sum5,cat(_sum4,cat(_sum3,cat(_sum2,cat(_sum1,_sum0)))))))
  module fa :
    input a : UInt<1>
    input b : UInt<1>
    input cin : UInt<1>
    output cout : UInt<1>
    output s : UInt<1>

    wire x : UInt<1>
    wire y : UInt<1>
    wire z : UInt<1>

    inst ha0 of ha
    inst ha1 of ha

    ha0.a<=a
    ha0.b<=b
    x<=ha0.c
    y<=ha0.s
    ha1.a<=y
    ha1.b<=cin
    z<=ha1.c
    s<=ha1.s
    cout<=or(x,z)
  module ha :
    input a : UInt<1>
    input b : UInt<1>
    output c : UInt<1>
    output s : UInt<1>



    c<=and(a,b)
    s<=xor(a,b)

