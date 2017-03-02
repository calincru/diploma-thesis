# TODO list
* [Mar 2] Look into Z3 and potential optimizations in how it's used in Symnet
* [Mar 2] Simple implementation of filtering in iptables (using only
  DROP/ACCEPT jumps):
    * Parse it and create an in-memory model (of iptables)
    * Generate SEFL code (in-memory too)
    * Execute it
