If(Constrain(IPDst, =="nat-new-src-ip"),
   // then..
   If(Constrain(TcpDst, =="snat-new-src-port"),
      // then..
      InstructionBlock(
        Assign(IPDst, "snat-orig-src-ip"),
        Assign(TcpDst, "snat-orig-src-port")),
      // else..
      NoOp),
   // else..
   NoOp)
