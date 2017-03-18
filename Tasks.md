# Tasks

## Issues
Issue tracking [here](https://github.com/calincru/iptables-sefl/issues).

## High-level
- [ ] Look into Z3 and potential optimizations in how it's used in Symnet
- [ ] Simple implementation of filtering in iptables (using only DROP/ACCEPT
  jumps):
    - [x] Parse it and create an in-memory model (of iptables)
        - [x] Make sure that some rules can only appear in some tables/chains.
    - [ ] Generate SEFL code (in-memory too)
    - [ ] Symbolically execute it

## Ideas
- [ ] iptables capable device (ICD) modeling:
    - [ ] An ICD is a router enhanced with iptables filtering/mangling
      capabilities.
    - [ ] Given that we have a well modeled routing device, we can create an
      ICD on top of that (i.e. IS-A relationship) which modifies its behaviour
      by adding a couple of virtual ports/links connecting iptables filtering
      devices.
    - [ ] For instance, to model the PREROUTING chain, we could modify a
      routing device by overriding its input ports with new ones that go into
      a virtual device which enforces iptables PREROUTING rules and forwards
      packets to its sole output port which is directly connected to the real
      input port of the routing device.
- [ ] Jumps:
    - [ ] A jump to a user-defined chain can be easily modeled by forwarding
      the packet to another virtual device.
        - [ ] The question that arises is what happens if then a jump to the
          special 'RETURN' target is performed (or no rule matches)? A possible
          approach is to split the rules by the ones which contain a jump to a
          user-defined chain and dispatch each packet that enters the virtual
          device to its specific entry point (the first one for a new packet
          and the one right after a 'jump rule' if it is a returning packet).

- [x] Replace all `isValid' functions with something like `validate' (or
  semaValidate or the like) which returns a data structure aggregating useful
  data gathered during the semantic validation (such as the PlaceholderTarget
  to actual targets map).
    - [x] Implementation wise, this could be a `Maybe <T>', with `None' meaning
      failure to validate that particular structure.
    - [x] The nice part about this is that it is monadic (or applicative) in
      nature making the recursive validation easy.
        - [x] The problem with this is that the validation function which call
          down the AST has to somehow 'merge' those `Maybe's.
        - [x] A better solution is to add an implicit parameter which is that
          data structure (a semantic validation 'context') which is filled by
          recursively descending (and validating) the AST.
