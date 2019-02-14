-- | Implementations of the challenge problems.
module Challenge where

import Resource


--
-- * Adapt to Change in Server Configuration
--
--   https://dsl-external.bbn.com/tracsvr/immortals/wiki/AdaptToChangeInServerConfiguration


-- | A library that uses 128 units of memory.
gzip = Desc []
  [ require "memory" @@ Lit 128
  , provide "gzip-lib"
  ]

-- | A library that uses 64 units of memory.
tcp = Desc []
  [ require "memory" @@ Lit 64
  , provide "tcp-lib"
  ]

-- | A server that handles n requests per time unit
--   and optionally zips responses.
--    * uplink = bandwidth for transmissions from the server to clients
--    * downlink = bandwidth for transmissions from clients to server
server = Desc ["n","zip"]
  [ require "tcp-lib"
  , require "downlink" @@ Mul (Ref "n") (Lit 10)      -- requests are not zipped
  , If (Ref "zip")
      [ require "gzip-lib"
      , require "uplink" @@ Mul (Ref "n") (Lit 5) ]   -- zipped response
      [ require "uplink" @@ Mul (Ref "n") (Lit 25) ]  -- unzipped response
  , provide "requests" @@ Ref "n"
  ]

-- Library types.
gzipT = desc [] gzip
tcpT  = desc [] tcp

-- Example server types.
serverT0 = desc [100,0] server  -- zip disabled
serverT1 = desc [100,1] server  -- zip enabled
serverT2 = gzipT `join` tcpT `join` serverT1

-- | A client that submits n requests per time unit
--   and optionally unzips responses.
client = Desc ["n","zip"]
  [ require "tcp-lib"
  , require "requests" @@ Ref "n"
  , If (Ref "zip") [ require "gzip-lib" ] []
  , provide "client" @@ Lit 1
  ]

-- Example client types.
clientT0 = desc [4,0] client  -- zip disabled
clientT1 = desc [4,1] client  -- zip enabled

-- | A description of the environment/scenario. This is incomplete (see TODOs).
scenario = Desc ["c"]
  [ require "clients" @@ Ref "c"
  , provide "network" @@ Lit 1000
  , provide "client-memory" @@ Lit 256
  , provide "server-memory" @@ Lit 1024
  ]
