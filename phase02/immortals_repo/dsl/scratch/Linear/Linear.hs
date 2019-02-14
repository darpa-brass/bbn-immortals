-- | A simple functional language with linear types.
--   Based on: Wadler, "A Taste of Linear Logic".
module Linear where

import Control.Monad.Reader
import Control.Monad.State

import Data.Map.Strict (Map)
import qualified Data.Map.Strict as M


--
-- * Syntax
--

-- | Variables.
type Var = String

-- | Abstract syntax of terms.
data Expr =
    -- simply typed lambda calculus
      Ref Var                        -- non-linear variable reference
    | Use Var                        -- linear variable reference
    | Abs Var Type Expr              -- function abstraction
    | App Expr Expr                  -- function application
    -- unrestricted use
    | Free Expr                      -- mark unrestricted term
    | Reuse Expr Var Expr            -- use unrestricted term
    -- have two, use both
    | Pair Expr Expr                 -- construct tensor-product type
    | Both Expr Var Var Expr         -- consume tensor-product type
    -- have two, use one
    | Choose Expr Expr               -- construct with-product type
    | Fst Expr                       -- first element from with-product
    | Snd Expr                       -- second element from with-product
    -- have either, use it
    | InL Expr Type                  -- left sum type constructor
    | InR Type Expr                  -- right sum type constructor
    | Either Expr Var Expr Var Expr  -- consume sum type
  deriving (Eq,Show)

-- | Is this term in normal form? 
normal :: Expr -> Bool
normal (Ref _)      = True
normal (Abs x _ _)  = True   -- don't normalize under abstraction
normal (Free e)     = normal e
normal (Pair l r)   = normal l && normal r
normal (Choose l r) = normal l && normal r
normal (InL e _)    = normal e
normal (InR _ e)    = normal e
normal _            = False


--
-- * Type System
--

-- | Abstract syntax of linear types.
data Type =
      Atom String     -- constant
    | Bang Type       -- of course
    | Type :-> Type   -- linear implication
    | Type :*: Type   -- tensor
    | Type :&: Type   -- with
    | Type :+: Type   -- choose
  deriving (Eq,Show)

-- | A typing environment maps variables to types.
type Env = Map Var Type

-- | A monad to support the typing relation. The environment in the
--   reader monad tracks non-linear assumptions, while the environment
--   in the state monad tracks linear assumptions.
type TypeM = ReaderT Env (StateT Env (Either String))

-- | Lookup a non-linear assumption.
lookRef :: Var -> TypeM Type
lookRef v = do
    m <- ask
    case M.lookup v m of
      Just t -> return t
      _      -> fail ("unbound non-linear variable: " ++ v)

-- | Lookup and consume a linear assumption.
lookUse :: Var -> TypeM Type
lookUse v = do
    m <- get
    case M.lookup v m of
      Just t -> put (M.delete v m) >> return t
      _      -> fail ("unavailable linear variable: " ++ v)

-- | Push a linear assumption onto the typing environment, then a run a typing
--   computation and check to see whether that assumption was consumed.
--   If so, return the result. Otherwise, fail.
typeWith :: Var -> Type -> TypeM a -> TypeM a
typeWith v t ma = do
    old <- gets (M.lookup v)
    modify (M.insert v t)
    result <- ma
    unused <- gets (M.member v)
    when unused (fail ("unused linear variable: " ++ v))
    case old of
      Just t -> modify (M.insert v t)
      _      -> return ()
    return result

-- | Check whether two types are compatible.
check :: String -> Type -> Type -> TypeM ()
check s t u | t == u    = return ()
            | otherwise = fail ("incompatible types in " ++ s ++": " ++ show t ++ " and " ++ show u)

-- | Print an error message for failed expectation.
expected :: String -> Type -> TypeM a
expected s t = fail ("expected " ++ s ++ ", got: " ++ show t)

-- | Typing relation. Returns the type of a term and the updated environment,
--   or else reports a type error (Nothing).
typeOf :: Expr -> TypeM Type
    -- environment lookup
typeOf (Ref x)      = lookRef x
typeOf (Use x)      = lookUse x
    -- introduction forms
typeOf (Abs x t b)  = typeWith x t (fmap (t :->) (typeOf b))
typeOf (Free e)     = liftM Bang (typeOf e)
typeOf (Pair l r)   = liftM2 (:*:) (typeOf l) (typeOf r)
typeOf (Choose l r) = liftM2 (:&:) (typeOf l) (typeOf r)
typeOf (InL e t)    = liftM (:+: t) (typeOf e)
typeOf (InR t e)    = liftM (t :+:) (typeOf e)
    -- elimination forms
typeOf (App l r) = do 
    tl <- typeOf l
    tr <- typeOf r
    case tl of
      t :-> t' -> check "application" t tr >> return t'
      t        -> expected "implication" t
typeOf (Reuse e x b) = do
    te <- typeOf e
    case te of
      Bang t -> local (M.insert x t) (typeOf b)
      t      -> expected "bang type" t
typeOf (Both e x y b) = do
    when (x == y) $ fail "vars in tensor elim must be different"
    te <- typeOf e
    case te of
      t :*: u -> typeWith x t (typeWith y u (typeOf b))
      t       -> expected "tensor type" t
typeOf (Fst e) = do
    te <- typeOf e
    case te of
      t :&: _ -> return t
      t       -> expected "with type" t
typeOf (Snd e) = do
    te <- typeOf e
    case te of
      _ :&: u -> return u
      t       -> expected "with type" t
typeOf (Either e x l y r) = do
    te <- typeOf e
    case te of
      t :+: u -> do
        top <- get
        tl <- typeOf l
        ml <- get
        put top
        tr <- typeOf r
        mr <- get
        check "either" tl tr
        when (ml /= mr) $ fail ("different linear assumptions on either branches")
        return tl 
      t       -> expected "choose type" t


--
-- * Semantics
--

-- | Fully reduce an expression. Does not type check or enforce linearity.
eval :: Expr -> Expr
eval t | normal t  = t
       | otherwise = eval (step t)

-- | Perform one step of reduction. Does not type check or enforce linearity.
step :: Expr -> Expr
    -- reduction rules
step (Reuse (Free e) x b)       = subst x e b
step (App (Abs x _ b) e)        = subst x e b
step (Both (Pair l r) x y b)    = subst y r (subst x l b)
step (Fst (Choose l _))         = l
step (Snd (Choose _ r))         = r
step (Either (InL e _) x b _ _) = subst x e b
step (Either (InR _ e) _ _ x b) = subst x e b
    -- congruence rules
step (App l r)      | normal l  = App l (step r)  -- call-by-value
                    | otherwise = App (step l) r
step (Free e)                   = Free (step e)
step (Reuse e x b)              = Reuse (step e) x b
step (Pair l r)     | normal l  = Pair l (step r)
                    | otherwise = Pair (step l) r
step (Both e x y b)             = Both (step e) x y b
step (Choose l r)   | normal l  = Choose l (step r)
                    | otherwise = Choose (step l) r
step (Fst e)                    = Fst (step e)
step (Snd e)                    = Snd (step e)
step (InL e t)                  = InL (step e) t
step (InR t e)                  = InR t (step e)
step (Either e x l y r)         = Either (step e) x l y r

-- | Capture avoiding substitution. Does not enforce linearity.
subst :: Var -> Expr -> Expr -> Expr
subst v s (Ref x)            = if v == x then s else Ref x
subst v s (Abs x t b)        = Abs x t (checkSub [x] v s b)
subst v s (App l r)          = App (subst v s l) (subst v s r)
subst v s (Free e)           = Free (subst v s e)
subst v s (Reuse e x b)      = Reuse (subst v s e) x (checkSub [x] v s b)
subst v s (Pair l r)         = Pair (subst v s l) (subst v s r)
subst v s (Both e x y b)     = Both (subst v s e) x y (checkSub [x,y] v s b)
subst v s (Choose l r)       = Choose (subst v s l) (subst v s r)
subst v s (Fst e)            = Fst (subst v s e)
subst v s (Snd e)            = Snd (subst v s e)
subst v s (InL e t)          = InL (subst v s e) t
subst v s (InR t e)          = InR t (subst v s e)
subst v s (Either e x l y r) = Either (subst v s e) x (checkSub [x] v s l)
                                                    y (checkSub [y] v s r)

-- | Perform substitution on an expression if the variable to be substituted
--   is different than the list of shadowed variables.
checkSub :: [Var] -> Var -> Expr -> Expr -> Expr
checkSub vs v s e | elem v vs = e
                  | otherwise = subst v s e
