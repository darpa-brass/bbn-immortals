module DSL.Serialize.Generic where

import Data.Aeson (ToJSON,FromJSON)

import DSL.Expr
import DSL.Predicate
import DSL.Primitive
import DSL.Type


-- * Generic instances

instance ToJSON BPred
instance ToJSON IPred
instance ToJSON Simple
instance ToJSON Refined
instance ToJSON B_B
instance ToJSON I_I
instance ToJSON BB_B
instance ToJSON II_B
instance ToJSON II_I
instance ToJSON Op1
instance ToJSON Op2
instance ToJSON a => ToJSON (Schema a)
instance ToJSON a => ToJSON (Type a)
instance ToJSON a => ToJSON (Expr a)

instance FromJSON BPred
instance FromJSON IPred
instance FromJSON Simple
instance FromJSON Refined
instance FromJSON B_B
instance FromJSON I_I
instance FromJSON BB_B
instance FromJSON II_B
instance FromJSON II_I
instance FromJSON Op1
instance FromJSON Op2
instance FromJSON a => FromJSON (Schema a)
instance FromJSON a => FromJSON (Type a)
instance FromJSON a => FromJSON (Expr a)
