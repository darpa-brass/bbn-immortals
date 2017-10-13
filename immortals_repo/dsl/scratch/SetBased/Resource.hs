module Resource where

import Control.Monad (liftM2)
import Data.Map (Map)
import qualified Data.Map as M


--
-- * Resource Types
--

-- | A resource name.
type ResourceName = String

-- | A set of named resources. Consumable resources are associated with a
--   number of units (Just k). Non-consumable resources are not (Nothing).
type Resources = Map ResourceName (Maybe Int)

-- | A resource type consists of a set of resource requirements and a set of
--   resource provisions.
data ResourceType = RT Resources Resources
  deriving (Eq,Show)

-- | The empty resource type.
emptyRT :: ResourceType
emptyRT = RT M.empty M.empty

-- | Consume provided resource units. The doubly nested Maybe is confusing
--   but works nicely with Map's xxxWith functions. Essentially:
--     * Nothing: consumable resource -> fully consumed
--     * Just (Just i): consumable resource -> partially consumed
--     * Just Nothing: non-consumable resource
--   Note that this function will throw a pattern-match failure if applied
--   to one consumable and one non-consumable resource units.
consume :: Maybe Int -> Maybe Int -> Maybe (Maybe Int)
consume Nothing  Nothing           = Just Nothing       -- not consumable
consume (Just p) (Just r) | p <= r = Nothing            -- all used up
                          | p >  r = Just (Just (p-r))  -- partially consumed
consume Nothing _ = error "Tried to consume a non-consumable provision."
consume _ Nothing = error "Treated consumable provision as non-consumable."

-- | Eliminate required resource units, if they're satisfied by a provision.
--   The doubly nested Maybe is for compatibility with Map's xxxWith functions.
--   but works nicely with Map's xxxWith functions. Essentially:
--     * Nothing: the consumable resource is fully consumed
--     * Just (Just i): the consumable resource is partially consumed
--     * Just Nothing: the non-consumable resource is not consumed
--   Note that this function will throw a pattern-match failure if applied
--   to one consumable and one non-consumable resource units.
satisfy :: Maybe Int -> Maybe Int -> Maybe (Maybe Int)
satisfy Nothing  Nothing           = Nothing            -- satisfied
satisfy (Just r) (Just p) | r <= p = Nothing            -- satisfied
                          | r >  p = Just (Just (r-p))  -- partially satisfied
satisfy _ Nothing = error "Requirement treats non-consumable resource as consumable."
satisfy Nothing _ = error "Requirement treats consumable resource as non-consumable."

-- | Combine resource units by adding them together, if possible.
combine :: Maybe Int -> Maybe Int -> Maybe Int
combine = liftM2 (+)

-- | Join two resources by merging their resource types.
join :: ResourceType -> ResourceType -> ResourceType
join (RT ars aps) (RT brs bps) = RT rs ps
  where aps' = M.differenceWith consume aps brs
        brs' = M.differenceWith satisfy brs aps
        rs = M.unionWith combine ars brs'
        ps = M.unionWith combine bps aps'


--
-- * Variable Environments
--

-- | A variable.
type Var = String

-- | A simple representation of an environment as a function from variables
--   to values.
type Env a = Var -> a

-- | For simplicity, the empty environment returns a default value rather than
--   failing.
emptyEnv :: a -> Env a
emptyEnv a = \_ -> a

-- | Add a new binding to the environment.
bind :: Var -> a -> Env a -> Env a
bind v a m = \w -> if v == w then a else m w


--
-- * Expressions
--

-- | A minimal expression language to support the scenario. This will be
--   extended in the obvious way as needed. For now, we simulate booleans
--   with integers by interpreting 0 as false, anything else as true.
data Expr = Lit Int
          | Ref Var
          | Add Expr Expr
          | Mul Expr Expr
  deriving (Eq,Show)

-- | Semantics of expressions.
evalExpr :: Env Int -> Expr -> Int
evalExpr m (Lit i)   = i
evalExpr m (Ref v)   = m v
evalExpr m (Add l r) = evalExpr m l + evalExpr m r
evalExpr m (Mul l r) = evalExpr m l * evalExpr m r


--
-- * Resource Description
--

-- ** Abstract syntax

-- | Describes the resource requirements and provisions of a component.
--   A resource description is parameterized by a list of named integer
--   (for now) arguments.
data Desc = Desc [Var] [DStmt]
  deriving (Eq,Show)

-- | The role of a resource in a component, either a requirement or provision.
data Role = Require | Provide
  deriving (Eq,Show)

-- | Statements that make up the body of a resource description:
--   resource requirements/provisions, and conditionals.
data DStmt = Res Role ResourceName (Maybe Expr)
           | If Expr [DStmt] [DStmt]
  deriving (Eq,Show)


-- ** Concrete syntax (i.e. smart constructors)

-- | A resource requirement.
require :: ResourceName -> DStmt
require n = Res Require n Nothing

-- | A resource provision.
provide :: ResourceName -> DStmt
provide n = Res Provide n Nothing

-- | Specify a quantity associated with a resource requirement/provision.
(@@) :: DStmt -> Expr -> DStmt
(@@) (Res r n _) e = Res r n (Just e)


-- ** Semantics

-- | Semantics of resource description statements.
dstmt :: Env Int -> DStmt -> ResourceType -> ResourceType
dstmt m (Res role name expr) (RT rs ps) =
    let addR = M.insert name (fmap (evalExpr m) expr) in
    case role of
      Require -> RT (addR rs) ps
      Provide -> RT rs (addR ps)
dstmt m (If c ts es) rt =
    foldr (dstmt m) rt (if evalExpr m c == 0 then es else ts)

-- | Semantics of a resource description. The integer arguments are bound
--   to the declared variable names.
desc :: [Int] -> Desc -> ResourceType
desc is (Desc vs ss) = foldr (dstmt m) emptyRT ss
  where m = foldr ($) (emptyEnv 0) (zipWith bind vs is)


--
-- * Application Model
--

-- | An application model describes how to stitch together the descriptions
--   of various components. A model is parameterized by a list of named
--   resource descriptions.
-- data Model = Model [Var] [MStmt]
--   deriving (Eq,Show)

-- | Statements that make up the body of an application model.
-- data MStmt = Use Var Var [Int]  -- use a component, give it a name, 
--            | Link Var ResourceName Var ResourceName  -- link a resource 


--
-- * Example
--

-- TODO:
--   * representation of application model:
--     * instantiate variables
--     * link requirements of one component to provisions of another
--       (e.g. client-memory in scenario to memory in client)
--   * include a component multiple times (client in scenario)
--   * ability to share consumable resources across components
--     (network between clients and server)
--   * ability to have several different instances of a consumable resource
--     (separate memory resource in clients and server)
