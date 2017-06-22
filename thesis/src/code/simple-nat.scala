test("rl lecture - unreachable example") {
  val postroutingChain = toChain(
    toRule("-s 192.168.1.0/24
              -j SNAT --to-source 141.85.200.2-141.85.200.6"),
    toRule("-s 192.168.1.100
              -j SNAT --to-source 141.85.200.1")
  )
  val (successPaths, _) =
    symExec(
      postroutingChain,
      postroutingChain.inputPort,
      Assign(IPSrc, ConstantValue(Ipv4(192, 168, 1, 100).host))
    )
  val rewriteConstrain =
    Constrain(
      IPSrc,
      :&:(:>=:(ConstantValue(Ipv4(141, 85, 200, 1).host)),
          :<=:(ConstantValue(Ipv4(141, 85, 200, 1).host)))
    )

  // FAILS
  successPaths should containConstrain rewriteConstrain
}
