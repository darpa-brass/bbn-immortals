{-# LANGUAGE OverloadedStrings #-}

module DSL.Serialize.Custom where

import Data.Aeson
import Data.Aeson.Types
import Data.Scientific (floatingOrInteger)

import DSL.Expr
import DSL.Predicate
import DSL.Primitive
import DSL.Row
import DSL.Type


-- * JSON Parsers

-- The Aeson library is designed under the assumption that each data type
-- has exactly one parser, which are linked via the FromJSON type class.
-- Here I instead define the parsers relative to the schema in order to
-- make later refactoring easier (e.g. the schema definition baseValue
-- does not currently directly correspond to a Haskell data type).
-- However, the decoupling is rather messy since I also use handy Aeson
-- functions that rely on this type class, such as (.:).

parseBaseType :: Value -> Parser Simple
parseBaseType (String "unit") = return TUnit
parseBaseType (String "bool") = return TBool
parseBaseType (String "int")  = return TInt
parseBaseType bad             = typeMismatch "baseType" bad

parseBaseValue :: Value -> Parser (Expr t)
parseBaseValue Null       = return Unit
parseBaseValue (Bool b)   = return (B b)
parseBaseValue (Number n) | Right i <- floatingOrInteger n = return (I i)
parseBaseValue bad        = typeMismatch "baseValue" bad

parseRefinedType :: Value -> Parser Refined
parseRefinedType (Object o) = do
    t <- o .: "baseType"
    c <- o .: "constraint"
    return (Refined t "value" c)
parseRefinedType bad = typeMismatch "refinedType" bad

parseResourceType :: Value -> Parser (Label, Type Refined)
parseResourceType (Object o) = do
    n <- o .: "resourceName"
    t <- o .: "resourceType"
    return (n,t)
parseResourceType bad = typeMismatch "resourceType" bad


-- * Instances

instance FromJSON Simple where
  parseJSON = parseBaseType

instance FromJSON Refined where
  parseJSON = parseRefinedType

instance FromJSON BPred where
  parseJSON _ = return (BLit True)  -- TODO

instance FromJSON t => FromJSON (Type t) where
  parseJSON = undefined           -- TODO
