module DSL.Sugar where

import qualified Data.Text as T
import qualified Data.Set as S

import DSL.Name
import DSL.Types
import DSL.Primitive
import DSL.Parser


--
-- * Syntactic Sugar
--

-- ** Types

-- | Non-variational primitive types.
tUnit, tBool, tInt, tFloat, tSymbol :: V PType
tUnit   = One TUnit
tInt    = One TInt
tBool   = One TBool
tFloat  = One TFloat
tSymbol = One TSymbol


-- ** Expressions

-- | Non-variational variable reference.
ref :: Var -> V Expr
ref = One . Ref

-- | Non-variational resource reference.
res :: Path -> V Expr
res = One . Res

-- | Literal symbol name.
sym :: Name -> V Expr
sym = One . Lit . One . S . mkSymbol

-- | Literal component ID.
dfu :: Name -> V Expr
dfu = sym

-- | Primitive floor operation.
pFloor :: V Expr -> Expr
pFloor = P1 (F_I Floor)

-- | Primitive ceiling operation.
pCeil :: V Expr -> Expr
pCeil = P1 (F_I Ceil)

-- | Primitive round operation.
pRound :: V Expr -> Expr
pRound = P1 (F_I Round)


-- ** Statements

-- | Create a resource.
create :: Path -> V Expr -> Stmt
create p e = Do p (Create e)

-- | Check a resource.
check :: Path -> VType -> V Expr -> Stmt
check p t e = Do p (Check (Fun (Param "$val" t) e))

-- | Modify a resource.
modify' :: Path -> VType -> V Expr -> Stmt
modify' p t e = Do p (Modify (Fun (Param "$val" t) e))

modify :: Path -> PType -> V Expr -> Stmt
modify p t e = modify' p (One t) e

-- | Reference the current value of a resource.
--   For use with the 'check' and 'modify' smart constructors.
val :: V Expr
val = One (Ref "$val")

-- | Check whether a unit-valued resource is present.
checkUnit :: Path -> Stmt
checkUnit p = Do p (Check (Fun (Param "$val" (One TUnit)) true))

-- | Create a unit-valued resource.
createUnit :: Path -> Stmt
createUnit p = Do p (Create (One . Lit . One $ Unit))

mkVExpr :: PVal -> V Expr
mkVExpr = One . Lit . One

dim :: T.Text -> BExpr
dim t | Right b <- parseBExprText t = b
      | Left s <- parseBExprText t = error s
      | otherwise = error "impossible"

exclusive' :: S.Set T.Text -> S.Set T.Text -> Maybe BExpr
exclusive' all yes = combine (yesExpr yesList) (noExpr noList)
  where
    no = S.difference all yes
    noList = fmap (\t -> (bnot (BRef t))) (S.toList no)

    noExpr :: [BExpr] -> Maybe BExpr
    noExpr [] = Nothing
    noExpr (x:xs) | Just b <- noExpr xs = Just (x &&& b)
                  | otherwise           = Just x

    yesList = fmap BRef (S.toList yes)

    yesExpr :: [BExpr] -> Maybe BExpr
    yesExpr [] = Nothing
    yesExpr (x:xs) | Just b <- yesExpr xs = Just (x ||| b)
                   | otherwise            = Just x

    combine :: Maybe BExpr -> Maybe BExpr -> Maybe BExpr
    combine (Just y) (Just n) = Just (y &&& n)
    combine Nothing  (Just n) = Just n
    combine (Just y) Nothing  = Just y
    combine _        _        = Nothing

exclusive :: [T.Text] -> [T.Text] -> Maybe BExpr
exclusive all yes = exclusive' (S.fromList all) (S.fromList yes)

(.==.) :: V Expr -> V Expr -> V Expr
x .==. y = (One (P2 (SS_B SEqu) x y))

{- TODO TODO TODO
-- | Macro for an integer-case construct. Evaluates the expression, then
--   compares the resulting integer value against each case in turn, executing
--   the first matching block, otherwise executes the final block arugment.
caseOf :: V Expr -> [(Int,Block)] -> Block -> Stmt
caseOf expr cases other = Let x expr (foldr ifs other cases)
  where
    ifs (i,thn) els = [If (Ref x .== Lit (One (I i))) thn els]
    x = "$case"
-}
