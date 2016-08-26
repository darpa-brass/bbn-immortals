module Example.Network where

import DSL.Expr
import DSL.Type
import DSL.Resource
import DSL.Row


--
-- * Network Usage Challenge Problem
--


-- ** DFUs

genericEncrypt :: Expr Refined
genericEncrypt = Fun "r" genericEncryptT
    $ recUpdate "Image" (Ext "Encrpytion" (rec []))
    $ Use "r"

encryptWith :: Label -> Expr Refined
encryptWith alg = Fun "r" (encryptWithT alg)
    $ recUpdate "image" (recUpdate "Encryption" (provide alg))
    $ App genericEncrypt (Use "r")

aesEncrypt :: Expr Refined
aesEncrypt = encryptWith "AES"

futureEncrypt :: Expr Refined
futureEncrypt = encryptWith "FIPS-500-Future"


-- ** DFU types (eventually, these can be inferred)

-- | An "image" record entry with the given sub-entries and row variable "i".
image :: [(Label, Type Refined)] -> (Label, Type Refined)
image entries = ("Image", polyRec "i" entries)

genericEncryptT :: Schema Refined
genericEncryptT = Forall ["r","i"]
    $ polyRec "r" [image []]
  :-> polyRec "r" [image [("Encryption", monoRec [])]]

encryptWithT :: Label -> Schema Refined
encryptWithT alg = Forall ["r","i"]
    $ polyRec "r" [image []]
  :-> polyRec "r" [image [("Encryption", monoRec [has alg])]]
