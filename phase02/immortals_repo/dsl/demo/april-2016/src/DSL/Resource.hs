module DSL.Resource where

import Data.List (intercalate,subsequences)

import DSL.Expr
import DSL.Type
import DSL.Row


-- ** Atomic resources

-- | A record entry for a reusable atomic resource.
has :: Label -> (Label, Type Refined)
has l = (l, Bang tUnit)

-- | Extend a record with a reusable atomic resource.
provide :: Label -> Expr t -> Expr t
provide l = Ext l (Free Unit)


-- ** Initial environments

-- | Generate an initial resource environment with the corresponding
--   capabilities as reusable atomic resources.
initEnv :: [Label] -> Expr Refined
initEnv = rec . map (\l -> (l, Free Unit))

-- | Generate all possible initial resource environments for a given list
--   of capabilities; pair each one with a corresponding name.
namedInitEnvs :: [Label] -> [(String, Expr Refined)]
namedInitEnvs = map (\ls -> (intercalate "+" ls, initEnv ls)) . tail . subsequences


-- ** Application model

-- | Trivial application model that applies a single (untyped, for now)
--   DFU to an environment.
untypedDFU :: Expr Refined
untypedDFU = Fun "dfu" untyped (Fun "env" untyped (App (Use "dfu") (Use "env")))

-- | Indicates that a function is untyped (temporary solution).
untyped :: Schema Refined
untyped = Forall [] tUnit
