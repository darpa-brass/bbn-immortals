{-# LANGUAGE DeriveAnyClass #-}

module DSL.Example.CrossApp where

import Data.Data (Data,Typeable)
import GHC.Generics (Generic)
import Data.Aeson hiding (Value)

import Control.Monad (when)
import Options.Applicative
import Data.Foldable
import Data.List ((\\))
import Data.Monoid
import Data.SBV ((|||),(&&&),bnot)
import qualified Data.Text as T
import qualified Data.Set as S

import DSL.Types
import DSL.Name
import DSL.Environment
import DSL.Model
import DSL.Serialize
import DSL.Sugar
import DSL.Options


-- ** Initial environment

crossAppResEnv :: CrossAppInit -> ResEnv
crossAppResEnv r = envFromList
    [ ("/Server/AES-NI", ok $ serverAESNI r)
    , ("/Server/StrongEncryptionPolicy", ok $ serverSEP r)
    , ("/Client/AES-NI", ok $ clientAESNI r)
    , ("/Client/StrongEncryptionPolicy", ok $ clientSEP r) ]
  where
    ok (Chc d l r) = Chc d (ok l) (ok r)
    ok v@(One Nothing) = v
    ok v@(One (Just Unit)) = v
    ok _ = error "Only unit values are allowed in this example's initial resource environment."

crossAppResEnvAll :: ResEnv
crossAppResEnvAll = envFromList
    [ ("/Server/AES-NI", optional "ServerAESNI")
    , ("/Server/StrongEncryptionPolicy", optional "ServerSEP")
    , ("/Client/AES-NI", optional "ClientAESNI")
    , ("/Client/StrongEncryptionPolicy", optional "ClientSEP") ]
  where
    optional d = Chc d (One (Just Unit)) (One Nothing)
    


-- ** Application model

appModel = Model
  [
    Param "serverProvider" (One TSymbol),
    Param "clientProvider" (One TSymbol)
  ]
  (checkSEP ++ checkRules ++ [Elems [
    In "/Server/Cipher" [Elems [Load (ref "serverProvider") []]],
    In "/Client/Cipher" [Elems [Load (ref "clientProvider") []]],
    check "/Server/Cipher/Algorithm" tSymbol (val .==. res "/Client/Cipher/Algorithm"),
    check "/Server/Cipher/Mode"      tSymbol (val .==. res "/Client/Cipher/Mode"),
    check "/Server/Cipher/KeySize"   tSymbol (val .==. res "/Client/Cipher/KeySize"),
    check "/Server/Cipher/Padding"   tSymbol (val .==. res "/Client/Cipher/Padding")
  ]])
  where
    checkSEP :: Block
    checkSEP = [
        Split (foldOr (mkKeys [24,32,40,48,56,64]))
          [Elems [ checkUnit "/Server/StrongEncryptionPolicy"
                 , checkUnit "/Client/StrongEncryptionPolicy" ]]
          []
      ]

    algRules = [
        ("AES", [16, 24, 32]),
        ("ARIA", [16, 24, 32]),
        ("Blowfish", [8, 16, 24, 32, 40, 48, 56, 64]),
        ("Camellia", [16, 24, 32]),
        ("CAST5", [8, 16]),
        ("CAST6", [8, 16, 24, 32, 40, 48, 56, 64]),
        ("DES", [8]),
        ("DESede", [24, 16]),
        ("DSTU7624", [16, 32]),
        ("GCM", [16, 24, 32]),
        ("GOST28147", [32]),
        ("IDEA", [8, 16, 24, 32, 40, 48, 56, 64]),
        ("Noekeon", [16, 24, 32, 40, 48, 56, 64]),
        ("RC2", [8, 16, 24, 32, 40, 48, 56, 64]),
        ("RC5", [8, 16, 24, 32, 40, 48, 56, 64]),
        ("RC5_64", []),
        ("RC6", [8, 16, 24, 32, 40, 48, 56, 64]),
        ("Rijndael", [16, 24, 32]),
        ("SEED", [16, 24, 32, 40, 48, 56, 64]),
        ("SEEDWrap", []),
        ("Serpent_128", []),
        ("Skipjack", [16, 24, 32, 40, 48, 56, 64]),
        ("SM4", [16]),
        ("TEA", [16]),
        ("Threefish_256", [32]),
        ("Threefish_512", [64]),
        ("Threefish_1024", []),
        ("Twofish", [8, 16, 24, 32]),
        ("XTEA", [16])
      ]

    checkRules :: Block
    checkRules = foldMap (uncurry keyRule) algRules

    keyRule :: T.Text -> [Int] -> Block
    keyRule alg is = [
        Split (BRef alg &&& foldOr (allKeys \\ (mkKeys is)))
          [Elems [checkUnit "Fail"]]
          []
      ]

    foldOr [] = BLit False
    foldOr [x] = BRef x
    foldOr (x:xs) = BRef x ||| foldOr xs

-- ** DFUs

mkKeys :: [Int] -> [T.Text]
mkKeys = fmap (\i -> "KSZ" <> (T.pack . show) i)

allAlgs = ["AES", "ARIA", "Blowfish", "Camellia", "CAST5", "CAST6", "DES", "DESede", "DSTU7624", "GCM", "GOST28147", "IDEA", "Noekeon", "RC2", "RC5", "RC6", "Rijndael", "SEED", "Skipjack", "SM4", "TEA", "Threefish_256", "Threefish_512", "Twofish", "XTEA"]
allPads = ["ZeroBytePadding", "PKCS5Padding", "PKCS7Padding", "ISO10126_2Padding", "ISO7816_4Padding", "TBCPadding", "X923Padding", "NoPadding"]
allKeys = mkKeys [8,16,24,32,40,48,56,64]
allModes = ["ECB", "CBC", "CTR", "CFB", "CTS", "OFB", "OpenPGPCFB", "PGPCFBBlock", "SICBlock"]


mkCrossAppDFU :: T.Text -> [T.Text] -> [T.Text] -> [T.Text] -> Block
mkCrossAppDFU name algs pads modes =
    [ Elems [ create "DFU-Type" (sym "Cipher")
            , create "DFU-Name" (sym name) ]]
        ++ mk "Algorithm" algs allAlgs
        ++ mk "Mode" modes allModes
        ++ mk "Padding" pads allPads
        ++ mk "KeySize" allKeys allKeys
    where
      mk name good all = foldMap (\a -> [Split (foldOthers a all) [Elems [create name (mkVExpr (S . Symbol $ a)) ]] []]) good
      others x xs = S.difference (S.fromList xs) (S.singleton x)
      foldOthers x xs = foldl' (\b a -> b &&& (bnot (BRef a))) (BRef x) (S.toList (others x xs))

javaxDFU :: Model
javaxDFU = Model [] $ mkCrossAppDFU "Javax"
             ["AES", "Blowfish", "DES", "DESede", "RC2", "Rijndael"]
             ["PKCS5Padding", "NoPadding"]
             ["ECB", "CBC", "CTR", "CFB", "CTS", "OFB"]

bouncyDFU :: Model
bouncyDFU = Model [] $ mkCrossAppDFU "BouncyCastle" allAlgs allPads allModes
              ++ [Elems [If (res "Algorithm" .==. sym "AES") [Elems [checkUnit "../AES-NI"]] []]]

crossAppDFUs :: Dictionary
crossAppDFUs = modelDict [("Javax", javaxDFU), ("BouncyCastle", bouncyDFU)]


-- * Config

crossAppConfig :: Symbol -> Symbol -> [V PVal]
crossAppConfig s c = [One . S $ s, One . S $ c]

crossAppConfigAll :: [V PVal]
crossAppConfigAll = [ Chc "ServerJavax" (One "Javax") (One "BouncyCastle")
                    , Chc "ClientJavax" (One "Javax") (One "BouncyCastle") ]

--
-- * Requirements
--

crossAppReqs :: Profile
crossAppReqs = toProfile $ Model [] []

--
-- * Driver
--

data CrossAppConfig = CrossAppConfig
     { serverProv :: String
     , clientProv :: String }
  deriving (Show,Read,Eq,Data,Typeable,Generic,FromJSON)

data CrossAppInit = CrossAppInit
     { serverAESNI :: Value
     , serverSEP   :: Value
     , clientAESNI :: Value
     , clientSEP   :: Value }
  deriving (Eq,Read,Show,Data,Typeable,Generic,FromJSON)

data CrossAppOpts = CrossAppOpts
     { genDict      :: Bool
     , genModel     :: Bool
     , genInit      :: Maybe CrossAppInit
     , genInitAll   :: Bool
     , genConfig    :: Maybe CrossAppConfig
     , genConfigAll :: Bool
     , genReqs      :: Bool }
  deriving (Eq,Read,Show,Data,Typeable,Generic)

parseCrossAppOpts :: Parser CrossAppOpts
parseCrossAppOpts = CrossAppOpts
  <$> switch
       ( long "dict"
      <> help "Generate DFU dictionary" )

  <*> switch
       ( long "model"
      <> help "Generate application model" )

  <*> (optional . option readRecord)
       ( long "init"
      <> metavar "RECORD"
      <> help ("Generate the initial resource environment. The argument RECORD"
         <> " is a JSON object with the following fields: serverAESNI,"
         <> " serverSEP, clientAESNI, and clientSEP. Each field's value"
         <> " is a string representing a variational value. Only unit primitive values"
         <> " are permitted in the variational value.") )

  <*> switch
       ( long "init-all"
      <> help ("Generate an initial resource environment that represents all"
         <> " possible configurations of AES-NI and SEP on the client and server."
         <> " This overrides an argument to --init, if passed.") )

  <*> (optional . option readRecord)
       ( long "config"
      <> metavar "RECORD"
      <> help ("Generate configuration. The argument RECORD"
         <> " is a JSON object with the following fields: serverProv, and clientProv."
         <> " Each field's value is a string with the name of the DFU of the provider to load"
         <> " on the server and client, respectively."
        ) )

  <*> switch
       ( long "config-all"
      <> help ("Generate a configuration that explores all cipher DFUs."
         <> " This overrides an argument to --config, if passed.") )

  <*> switch
       ( long "reqs"
      <> help "Generate empty mission requirements" )

runCrossApp :: CrossAppOpts -> IO ()
runCrossApp opts = do
    when (genDict opts)   (writeJSON defaultDict crossAppDFUs)
    when (genModel opts)  (writeJSON defaultModel appModel)
    case (genInit opts) of
      Just r  -> writeJSON defaultInit (crossAppResEnv r)
      Nothing -> return ()
    when (genInitAll opts) (writeJSON defaultInit crossAppResEnvAll)
    case (genConfig opts) of
      Just (CrossAppConfig s c) -> writeJSON defaultConfig (crossAppConfig (str2sym s) (str2sym c))
      Nothing -> return ()
    when (genConfigAll opts) (writeJSON defaultConfig crossAppConfigAll)
    when (genReqs opts) (writeJSON defaultReqs crossAppReqs)
    where
      str2sym = Symbol . T.pack
