# Resource interface specification DSL

(Updated: Jan 26, 2016)

**Resource.hs**: This is a very early mockup of a language for specifying
resource interfaces, using the "Adapt to changes in server configuration"
challenge problem example from the wiki.

Currently the language is embedded in Haskell, which puts certain constraints
on the syntax. If DSL programs will primarily be generated, this is probably
fine long-term. However, if DSL programs will mostly be written by people, then
we will probably want a nicer concrete syntax as the language stabilizes.

So far, the language only provides mechanisms to declare the (parameterized)
resource requirements and provisions of individual components/DFUs. Missing is
the application model that glues the DFUs together. We already have a good idea
of what the type system for this looks like, but not for what constructs/syntax
is needed. The plan is figure this out by continuing to work through the CPs.
